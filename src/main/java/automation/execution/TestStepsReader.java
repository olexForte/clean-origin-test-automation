package automation.execution;

import automation.datasources.FileManager;
import automation.execution.repositories.TestDataRepository;
import automation.keyword.AbstractKeyword;
import automation.keyword.KeywordEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import automation.reporting.ReporterManager;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  Process test files <br>
 *
 */

public class TestStepsReader {

    //logger
    private static final Logger LOGGER = LoggerFactory.getLogger(TestStepsReader.class);

    //list of special instructions
    public static final String DEFINE_STATEMENT = "#DEFINE";
    public static final String FOR_STATEMENT = "#FOR";
    public static final String FOR_TABLE_ROW = "#-";
    public static final String FOR_TABLE_FILE_MARKER = "#file:";
    public static final String END_STATEMENT = "#END";

    public static final String TAGS_DELIMITER = ",";
    public static final String TAG_LABEL = "!";
    public static final String COMMENT_START_LABEL = "#";

    static Map<String, String> ALL_TEST_ACTIONS = getAllActionFiles(); // map: Action label - File name
    /**
     * Get all available Actions
     */
    private static Map<String,String> getAllActionFiles() {
        LOGGER.info("Read all Actions");
        Collection<File> files = FileManager.getFilesFromActionsDir();
        TreeMap<String,String> names = new TreeMap<String,String>();
        String keyword = "";
        String filename = "";
        for(File file: files) {
            keyword = file.getName().replaceAll("\\..*", "").replace("_", " ").toLowerCase();
            filename = file.getName().replaceAll("\\..*", "");
            names.put(keyword, filename);
        }
        return names;
    }

    public static boolean isLineShouldBeSkiped(String line){
        return line.trim().equals("") || line.trim().startsWith(COMMENT_START_LABEL) || line.trim().startsWith(TAG_LABEL);
    }

    /**
     * Parse keywords from content
     * @param fileLocation
     * @return
     * @throws Exception possible exception
     */
    static public List<AbstractKeyword> parseKeywords(String fileLocation) throws Exception {
        LinkedList<AbstractKeyword> allSteps;

        //read content
        String content = FileManager.getFileContent(FileManager.getFileFromScriptsDir(fileLocation));

        // process DEFINE / FOR
        content = processSpecialInstructions(content);

        //get all steps
        allSteps = getStepsFromContent(content);

        return allSteps;
    }

    /**
     * Process special instructions before keywords processing
     * @param content
     * @return
     */
    static String processSpecialInstructions(String content) throws Exception {
        String result = content;

        //process DEFINE statement
        result = processDefineStatement(content);

        //process FOR statement
        result = processForStatement(result);

        return result;
    }

    /**
     * Process DEFINE statement
     * @param content
     * @return
     */
    static private String processDefineStatement(String content) {
        String result = content;

        String[] allRows = content.split("\n");
        String[] values;
        for(String row : allRows){
            if (row.trim().startsWith(DEFINE_STATEMENT)) {
                values = row.substring(DEFINE_STATEMENT.length()).trim().split(" ");
                result = result.replace(values[1], values[0]);
            }
        }
        return result;
    }

    /**
     * Process FOR statement
     * @param content
     * @return
     */
    static private String processForStatement(String content) throws Exception {
        StringBuilder result = new StringBuilder();

        String[] allRows = content.split("\n");
        String[] headers = null;

        List<Map<String,String>> tableOfValuesToReplace = new ArrayList<>();
        StringBuilder linesToDuplicate = new StringBuilder();

        for(String row : allRows){
            if (row.trim().startsWith(FOR_STATEMENT)) {
                headers = row.substring(FOR_STATEMENT.length()).trim().split(" "); // TODO delimiter
            } else if (row.trim().startsWith(FOR_TABLE_FILE_MARKER) && headers != null){
                // row with data FOR
                String fileName = row.replace(FOR_TABLE_FILE_MARKER,"").trim();
                String[] allLines = FileManager.getFileContent(FileManager.getFileFromDir(fileName, TestDataRepository.TEST_DATA_RESOURCES)).split("\n");

                for(String line : allLines) {
                    String[] values = line.split("\\|"); //TODO delimiter
                    HashMap<String,String> rowValues = new HashMap<>();
                    for (int i = 0; i < values.length; i++) {
                        rowValues.put(headers[i], values[i].trim());
                    }
                    tableOfValuesToReplace.add(rowValues);
                }
            } else if (row.trim().startsWith(FOR_TABLE_ROW) && headers != null) {
                  // row with data FOR
                    HashMap<String,String> rowValues = new HashMap<>();
                    String[] values = row.substring(FOR_TABLE_ROW.length()).trim().split("\\|");
                    for (int i = 0; i < values.length; i++)
                        rowValues.put(headers[i], values[i].trim());
                    tableOfValuesToReplace.add(rowValues);

            } else if (row.trim().startsWith(END_STATEMENT) && headers != null) {
                    result.append(duplicateLinesAndSubstituteValues(linesToDuplicate.toString(), tableOfValuesToReplace));

                    // clean up
                    headers = null;
                    tableOfValuesToReplace = new ArrayList<>();
                    linesToDuplicate = new StringBuilder();
            } else if(headers != null){ // add line to duplicate
                linesToDuplicate.append(row).append("\n");
            } else { // (headers == null
                result.append(row).append("\n");
            }
        }
        return result.toString();
    }

    /**
     * Duplicate lines with substitution of corresponding values (#FOR processing)
     * @param linesToDuplicate lines to duplicate
     * @param tableOfValues list of maps with values (label:replacement)
     * @return duplicated lines with new values from table
     */
    private static String duplicateLinesAndSubstituteValues(String linesToDuplicate, List<Map<String, String>> tableOfValues) {
        StringBuilder result = new StringBuilder();
        for(Map<String, String>  values : tableOfValues){
            String lines = linesToDuplicate;
            for(String key : values.keySet()){
                String substringToReplace = "$" + key;
                lines = lines.replace(substringToReplace, values.get(key));
            }
            result.append(lines).append("\n");
        }
        return result.toString();
    }

    /**
     * Get steps from content
     * @param content
     * @return
     * @throws Exception possible exception
     */
    private static LinkedList<AbstractKeyword> getStepsFromContent(String content) throws Exception {
        LinkedList<AbstractKeyword> allSteps = new LinkedList<AbstractKeyword>();
        String[] allRows = content.split("\n");

        for(String row : allRows) {
            if(!row.trim().equals("")) {
                LOGGER.info("Parse row: " + row);
                allSteps.addAll(getStepsFromRow(row));
            }
        }
        return allSteps;
    }

    /**
     * Get all steps from file row
     * @param row
     * @return
     * @throws Exception possible exception
     */
    private static LinkedList<AbstractKeyword> getStepsFromRow(String row) throws Exception {
        LinkedList<AbstractKeyword> stepsFromRow = new LinkedList<AbstractKeyword>();

        if(isLineShouldBeSkiped(row)) // comment or blank
            return stepsFromRow;

        String rowWithInstructions = row.trim();

        boolean keywordWasFoundInActions = false;
        for(String testAction : ALL_TEST_ACTIONS.keySet()){ // processing of Actions
            if(isRowDescribesTestAction(rowWithInstructions, testAction)) {
                keywordWasFoundInActions = true;
                String actionFileContent = getActionFileContent(rowWithInstructions, testAction);
                stepsFromRow.addAll(getStepsFromContent(actionFileContent));
                break;
            }
        }
        if (!keywordWasFoundInActions){ // simple keyword (not an action)
            stepsFromRow.add(parseRowWithSimpleKeyword(row));
        }

        return stepsFromRow;
    }

    /**
     * Get content of action file
     * @param rowWithInstructions
     * @param testAction
     * @return
     */
    private static String getActionFileContent(String rowWithInstructions, String testAction) {
        String actionFileContent = FileManager.getFileContent(FileManager.getFileFromActionsDir(ALL_TEST_ACTIONS.get(testAction)));
        if(actionFileContent == null){
            ReporterManager.Instance.fail("File for action was not found: " + rowWithInstructions);
            return null;
        }

        actionFileContent = processParametersInActionContent(actionFileContent, rowWithInstructions);
        // if rowWithInstructions is Final/Optional - make all steps Optional or Final
        if(AbstractKeyword.isStepOptional(rowWithInstructions))
            actionFileContent = makeAllStepsInContentSpecial(actionFileContent, AbstractKeyword.OPTIONAL_STEP_LABEL);
        if(AbstractKeyword.isKeywordFinal(rowWithInstructions))
            actionFileContent = makeAllStepsInContentSpecial(actionFileContent, AbstractKeyword.FINAL_STEP_LABEL);
        return actionFileContent;
    }

    /**
     * Make ALL steps in Action FINAL or OPTIONAL
     * @param actionFileContent
     * @param stepLabel
     * @return
     */
    private static String makeAllStepsInContentSpecial(String actionFileContent, String stepLabel) {
        String result = "";
        for(String line: actionFileContent.split("\n")){
            if(!TestStepsReader.isLineShouldBeSkiped(line))
                result = result + stepLabel + " " + line + "\n";
            else
                result = result +  line + "\n";
        }
        return result;
    }

    /**
     * Get and Replace parameters from action content
     * @param actionFileContent original content of Action
     * @param actionString parameters (ex: 'p1' 'p2')
     * @return updated content
     */
    private static String processParametersInActionContent(String actionFileContent, String actionString) {
        if(actionString.length() > 0){ // processing of action parameters
            Pattern p = Pattern.compile("(('(.*?)')|(\"(.*?)\"))");
            Matcher m  = p.matcher(actionString);
            int i = 0;
            while(m.find()){
                i++;
                actionFileContent = actionFileContent.replace("$" + i, actionString.substring(m.start()+1, m.end()-1)); //replace $i with matches
            }
        }
        return actionFileContent;
    }

    /**
     * Check that file row describes Action
     * @param rowWithInstructions
     * @param testAction
     * @return
     */
    private static boolean isRowDescribesTestAction(String rowWithInstructions, String testAction) {
        return AbstractKeyword.prepareLine(rowWithInstructions).toLowerCase().matches("^"+testAction+"( |('.*')|(\".*\"))*[;]*");
    }

    /**
     * Get keyword from row
     * @param row
     * @return
     * @throws Exception possible exception
     */
    private static AbstractKeyword parseRowWithSimpleKeyword(String row) throws Exception {
        AbstractKeyword result = null;
        for(KeywordEnum item : KeywordEnum.values()){
            if((result = item.getKeyword().generateFromLine(row)) != null)
                break;
        }
        if(result == null)
            throw new Exception("Failure parsing row. Keyword was not found: " + row);
        return result;
    }
}
