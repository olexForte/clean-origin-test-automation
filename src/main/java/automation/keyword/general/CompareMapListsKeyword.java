package automation.keyword.general;

import automation.annotations.KeywordRegexp;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DEPRECATED
 * Compare list of hashmaps
 *
 * <b>Example:</b>: Compare hashmaps 'saved.DATA_FROM_FILE' and 'saved.EMPLOYEES_MAP_FROM_REQUEST'
 *
 * <b>Example of usage</b>
 * Save hashmap from API response 'apiRequests.LIST_OF_FIELDS_WITH_EMPLOYEES' with keys 'apiRequests.LIST_OF_HEADERS_WITH_EMPLOYEES' from 'SAVED.apiResponse' to 'saved.EMPLOYEES_MAP_FROM_REQUEST';
 * Read file 'SAVED.EMPLOYEES_FILE_NAME' CSV from output dir  to 'saved.DATA_FROM_FILE';
 * Compare hashmaps 'saved.DATA_FROM_FILE' and 'saved.EMPLOYEES_MAP_FROM_REQUEST';
 */

public class CompareMapListsKeyword extends AbstractKeyword {
    @KeywordRegexp("Compare hashmaps 'hashMap1' to expected 'hashMap1';")
    static String LABEL = "compare hashmaps";

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().matches(LABEL.toLowerCase()+".*")){
            CompareMapListsKeyword result = (CompareMapListsKeyword)super.generateFromLine(line);

            Pattern p = Pattern.compile("(('(.*?)')|(\"(.*?)\"))");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.target = (line.substring(matcher.start()+1, matcher.end()-1));
            matcher.find();
            result.data = (line.substring(matcher.start()+1, matcher.end()-1));

            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {

        List<HashMap<String, String>> actualData = executor.testDataRepository.getComplexDataList(target); //get output file name
        List<HashMap<String, String>> expectedData = executor.testDataRepository.getComplexDataList(data);

        for(HashMap<String,String> actIt : actualData) {
            boolean wasFound = false;
            for (HashMap<String, String> expIt : expectedData) {
                boolean keyWasFound = true;
                for (String key : actIt.keySet()) {
                    try {
                        if (!expIt.get(key).equals(actIt.get(key))) {
                            keyWasFound = false;
                            break;
                        } else {
                            keyWasFound = true;
                        }
                    }catch (Exception e){
                        // no key
                    }
                }
                if(keyWasFound) {
                    wasFound = true;
                    break;
                }
            }
            if(wasFound) {
                LOGGER.trace("Item was found: " + actIt);
            }else {
                LOGGER.trace("Item was not found: " + actIt);
            }
        }
        return true;
    }
}
