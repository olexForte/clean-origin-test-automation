package automation.keyword.rest;

import automation.annotations.KeywordRegexp;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;
import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Save value from API response<br>
 *     <b>Example: </b>
 Save hashmap from API response 'apiRequests.LIST_OF_FIELDS_WITH_EMPLOYEES' with keys 'apiRequests.LIST_OF_HEADERS_WITH_EMPLOYEES' from 'SAVED.apiResponse' to 'saved.EMPLOYEES_MAP_FROM_REQUEST'
 */
public class SaveHashmapFromAPIResponseKeyword extends AbstractKeyword {
    @KeywordRegexp("Save hashmap from API response 'field' from 'saved.apiResponse' to 'saved.value';")
    static String LABEL = "save hashmap from API response";
    String source = "";
    String headers = "";
    String LIST_SEPARATOR = ",";

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            SaveHashmapFromAPIResponseKeyword result = (SaveHashmapFromAPIResponseKeyword)super.generateFromLine(line);

            Pattern p =  Pattern.compile("('(.*?)')|(\"(.*?)\")");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.data = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.headers = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.source = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.target = line.substring(matcher.start()+1, matcher.end()-1);

            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {
        String JSONPathsLine = (String)executor.testDataRepository.getData(data); // process dynamic values
        String headersLine = (String)executor.testDataRepository.getData(headers);

        Response response =  (Response)executor.testDataRepository.getTestDataObject(source);

        List<HashMap<String,String>> result = new ArrayList<>();
        String[] listOfHeaders = headersLine.split(LIST_SEPARATOR);
        String[] listOfJSONPaths = JSONPathsLine.split(LIST_SEPARATOR);

        String key;
        String stringVal;

        List<String> valList = response.jsonPath().getList(listOfJSONPaths[0], String.class);
        for(int i = 0; i < valList.size() ;i++){
            HashMap currentMap = new HashMap();
            currentMap.put(listOfHeaders[0],valList.get(i));
            result.add(currentMap);
        }

        for(int headerIndex = 1; headerIndex < listOfHeaders.length; headerIndex++){
            key = listOfHeaders[headerIndex];
            valList = response.jsonPath().getList(listOfJSONPaths[headerIndex], String.class);

            for(int i = 0; i < valList.size(); i++){
                result.get(i).put(key,valList.get(i));
            }
        }
        executor.testDataRepository.setComplexData(target, result);

        return true;
    }
}
