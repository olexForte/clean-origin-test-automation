package automation.keyword.general;

import automation.annotations.KeywordRegexp;
import automation.keyword.AbstractKeyword;
import automation.execution.TestStepsExecutor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generate value and save to variable <br>
 *     Example: Generate value 'RANDOM.licenseExpirationDate:CURRENT_DATE:ISO_OFFSET_DATE_TIME' to 'SAVED.field';<br>
 *
 */
public class GenerateValueKeyword  extends AbstractKeyword {

    private static final String REPLACE_ACTION_LABEL = "REPLACE:";
    private static final String TRIM_ACTION_LABEL = "TRIM";
    private static final String UP_ACTION_LABEL = "UP";
    private static final String LOW_ACTION_LABEL = "LOW";
    private static final String REMOVE_ACTION_LABEL = "REMOVE";
    private static final String REGEXP_REMOVE_ACTION_LABEL = "REGEXP_REMOVE";
    private static final String PARAMETER_SPLITER = ":";

    @KeywordRegexp("Generate value 'RANDOM.field' to 'SAVED.target' [if empty][and 'TRIM|UP|LOW|REMOVE:what|REPLACE:what:with|REGEXP_REMOVE:what|'];")
    static String LABEL = "generate value";
    @KeywordRegexp("Save value '12' to 'SAVED.target';")
    static String LABEL_ALT = "save value";

    static String IF_EMPTY_MARKER = " if empty";
    boolean ifEmpty = false;
    private String action = "";

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().matches(LABEL.toLowerCase()+".*") ||
                prepareLine(line).toLowerCase().matches(LABEL_ALT.toLowerCase()+".*")){
            GenerateValueKeyword result = (GenerateValueKeyword)super.generateFromLine(line);

            Pattern p = Pattern.compile("(('(.*?)')|(\"(.*?)\"))");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.data = (line.substring(matcher.start()+1, matcher.end()-1));
            matcher.find();
            result.target = (line.substring(matcher.start() + 1, matcher.end() - 1));

            if(matcher.find())
                result.action = line.substring(matcher.start()+1, matcher.end()-1);

            if(line.contains(IF_EMPTY_MARKER))
                result.ifEmpty = true;

            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {

        String value = (String)executor.testDataRepository.getData(data);
        if(target != null && !target.equals("")) {
            String currentValue = (String) executor.testDataRepository.getData(target);
            if ( !ifEmpty || ( currentValue == null || currentValue.equals("") )) {
                if(!action.equals("")){
                    value = performActions(value);
                }
                executor.testDataRepository.setData(target, value);
            }
        }
        return true;
    }


    /**
     * Perform action over value string (trim, replace etc.)
     * @param value
     * @return
     */
    String performActions(String value){
        String result = value;
        //replace
        //TODO processing of ':'
        if(action.toUpperCase().startsWith(REPLACE_ACTION_LABEL)){
            String[] parts = action.split(PARAMETER_SPLITER);
            return result.replace(parts[1], parts[2]);
        }
        //trim
        if(action.toUpperCase().startsWith(TRIM_ACTION_LABEL)){
            return result.replaceAll("[\n\r\t ]"," ").trim();
        }
        //uppercase
        if(action.toUpperCase().startsWith(UP_ACTION_LABEL)){
            return result.toUpperCase();
        }
        //lowercase
        if(action.toUpperCase().startsWith(LOW_ACTION_LABEL)){
            return result.toLowerCase();
        }
        //remove
        if(action.toUpperCase().startsWith(REMOVE_ACTION_LABEL)){
            String[] parts = action.split(PARAMETER_SPLITER);
            return result.replace(parts[1], "");
        }
        //remove regexp
        if(action.toUpperCase().startsWith(REGEXP_REMOVE_ACTION_LABEL)){
            String[] parts = action.split(PARAMETER_SPLITER);
            return result.replaceAll(parts[1], "");
        }

        return result;
    }
}
