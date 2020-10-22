package automation.keyword.rest;

import automation.annotations.KeywordRegexp;
import io.restassured.response.Response;
import automation.keyword.AbstractKeyword;
import automation.execution.TestStepsExecutor;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Send  GET request to specified URL with headers and save results<br>
 *       <b>Example: </b>
 * Send GET request 'apiEndpoints.GET_LIST_OF_COMPANIES' with headers 'apiRequests.COOKIES:saved.laravel_session' and save results to 'SAVED.apiResponse';
 */
public class GETRequestKeyword extends AbstractKeyword {

    @KeywordRegexp("Send GET request 'url' with headers 'headers' [ and save results to 'SAVED.data']")
    static String LABEL = "send get request";
    String result;
    String headers;


    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            GETRequestKeyword result = (GETRequestKeyword)super.generateFromLine(line);

            result.data = null;
            Pattern p =  Pattern.compile("('(.*?)')|(\"(.*?)\")");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.target = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.result = line.substring(matcher.start()+1, matcher.end()-1);
            if(matcher.find()){
                result.headers = result.result;
                result.result = line.substring(matcher.start()+1, matcher.end()-1);
            }

            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {
        String url = executor.locatorsRepository.getTarget(target); // process dynamic values
        if(url == null)
            url = (String)executor.testDataRepository.getData(target);
        HashMap<String, String> requestHeaders = executor.testDataRepository.getComplexData(headers);
        Response response = executor.api.getRequest(url, requestHeaders);
        if(result == null || result.equals(""))
            result = executor.DEFAULT_LAST_API_RESULT;
        executor.testDataRepository.setTestDataObject(result, response);
        return true;
    }
}
