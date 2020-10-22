package automation.keyword.general;

import automation.annotations.KeywordRegexp;
import automation.datasources.FileManager;
import automation.datasources.JSONConverter;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;
import automation.keyword.validation.ValidateEqualsKeyword;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Read file from location and save results to variable
 * <b>Examples of usage:</b>
 *
 * Check for Email with subject 'mailTrap.EMPLOYEE_IMPORT_ERRORS_CONTAINED_SUBJECT' for 'uploadEmployees.CLIENT_USER' to 'saved.EMAIL_ID' check timestamp;
 * Download attachment by path 'mailTrapEndpoints.MESSAGE_ATTACHMENT_IN_INBOX:mailTrap.inbox:saved.EMAIL_ID' to 'SAVED.attachments';
 * Read file 'SAVED.attachments' TEXT from output dir to 'SAVED.attachmentContent';
 *
 * Save hashmap from API response 'apiRequests.LIST_OF_FIELDS_WITH_EMPLOYEES' with keys 'apiRequests.LIST_OF_HEADERS_WITH_EMPLOYEES' from 'SAVED.apiResponse' to 'saved.EMPLOYEES_MAP_FROM_REQUEST';
 * Read file 'SAVED.EMPLOYEES_FILE_NAME' CSV from output dir  to 'saved.DATA_FROM_FILE';
 *
 *
 */
public class ReadFileKeyword extends AbstractKeyword {

    @KeywordRegexp("Read file 'fileLocation' [CSV|JSON|TEXT] from [output dir|data dir] to 'SAVED.data';")
    static String LABEL = "read file";

    String CSV_MARKER = " CSV ";
    String JSON_MARKER = " JSON ";
    String TEXT_MARKER = " TEXT ";

    String OUTPUT_DIR_MARKER = " output dir ";
    String DATA_DIR_MARKER = " data dir ";

    boolean isOutputDir = false;
    boolean isJSON = false;
    boolean isCSV = false;
    boolean isTEXT = false;

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if (prepareLine(line).toLowerCase().matches(LABEL.toLowerCase() + ".*")) {
            ReadFileKeyword result = (ReadFileKeyword) super.generateFromLine(line);

            Pattern p = Pattern.compile("(('(.*?)')|(\"(.*?)\"))");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.data = (line.substring(matcher.start() + 1, matcher.end() - 1));
            matcher.find();
            result.target = (line.substring(matcher.start() + 1, matcher.end() - 1));

            if(line.contains(OUTPUT_DIR_MARKER))
                result.isOutputDir = true;
            if(line.contains(JSON_MARKER))
                result.isJSON = true;
            if(line.contains(TEXT_MARKER))
                result.isTEXT = true;
            if(line.contains(CSV_MARKER))
                result.isCSV = true;

            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {

        File file;
        if (isOutputDir)
            file = executor.testDataRepository.getOutputFile(data);
        else
            file = executor.testDataRepository.getDataFile(data);

        if (isJSON) { //JSON
            List<HashMap<String, String>> result = JSONConverter.toHashMapListFromFile(file);
            executor.testDataRepository.setComplexData(target, result);

        } else if(isCSV) { //CSV
                List<String> lines = FileManager.getFileContentAsListOfLines(file);
                List<HashMap<String, String>> result = new ArrayList();
                String[] headers = lines.get(0).split(",");

                for (int lineIndex = 1; lineIndex < lines.size(); lineIndex++) {
                    String[] lineParts = lines.get(lineIndex).split(",");
                    HashMap<String, String> curMap = new HashMap<>();
                    for (int fieldIndex = 0; fieldIndex < lineParts.length; fieldIndex++) {
                        curMap.put(headers[fieldIndex], lineParts[fieldIndex]);
                    }
                    result.add(curMap);
                }

                executor.testDataRepository.setComplexData(target, result);
        } else { //TEXT
                String result = FileManager.getFileContent(file);
                executor.testDataRepository.setData(target, result);
        }

        return true;
    }
}
