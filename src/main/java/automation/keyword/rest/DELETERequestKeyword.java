package automation.keyword.rest;

import automation.annotations.KeywordRegexp;
import io.restassured.response.Response;
import automation.keyword.AbstractKeyword;
import automation.execution.TestStepsExecutor;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Send  DELETE request to specified URL with data and headers and save results<br>
 *       <b>Example: </b>
 *           Send DELETE request 'apiEndpoints.ADD:saved.USER_ID' with data '' with headers 'userLicense.USER_LICENSE_TO_ADD_HEADERS:saved.laravel_session' and save results to 'saved.RESULTS'
 */
public class DELETERequestKeyword extends AbstractKeyword {

    @KeywordRegexp("Send DELETE request 'url' with data 'data' with headers 'headers'[ and save results to 'SAVED.data']")
    static String LABEL = "send delete request";
    String result;
    String headers;

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            DELETERequestKeyword result = (DELETERequestKeyword)super.generateFromLine(line);;

            Pattern p =  Pattern.compile("('(.*?)')|(\"(.*?)\")");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.target = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.data = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.result = line.substring(matcher.start()+1, matcher.end()-1);
            if(matcher.find()){ // if headers were specified
                result.headers = result.result;
                result.result = line.substring(matcher.start()+1, matcher.end()-1);
            }
            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {
        target = executor.locatorsRepository.getTarget(target);
        String requestData = (String)executor.testDataRepository.getData(data);
        HashMap<String, String> requestHeaders = executor.testDataRepository.getComplexData(headers);
        Response response = executor.api.deleteRequest(target, requestData, requestHeaders);
        if(result == null || result.equals(""))
            result = executor.DEFAULT_LAST_API_RESULT;
        executor.testDataRepository.setTestDataObject(result, response);
        return true;
    }
}
