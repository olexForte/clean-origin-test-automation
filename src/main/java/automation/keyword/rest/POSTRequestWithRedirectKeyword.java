package automation.keyword.rest;

import automation.annotations.KeywordRegexp;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Send POST request with redirection to specified URL with data and headers and save results<br>
 *     <b>Example: </b>
 * Send redirected POST request 'APIENDPOINTS.GET_REGISTER_COMPANY' 'APIENDPOINTS.POST_REGISTER_COMPANY' with data '$2' with headers '' and save results to 'saved.apiResponse';
 */
public class POSTRequestWithRedirectKeyword extends AbstractKeyword {

    @KeywordRegexp("Send redirected POST request 'initialURL' 'url' with data 'data' with headers 'headers' [ and save results to 'SAVED.data']")
    static String LABEL = "send redirected post request";
    String result;
    String headers;
    String initialURL;

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            POSTRequestWithRedirectKeyword result = (POSTRequestWithRedirectKeyword)super.generateFromLine(line);;

            Pattern p =  Pattern.compile("('(.*?)')|(\"(.*?)\")");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.initialURL = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.target = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.data = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.headers = line.substring(matcher.start()+1, matcher.end()-1);
            if(matcher.find()){ // if headers were specified
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
        initialURL = executor.locatorsRepository.getTarget(initialURL);
        String requestData = (String)executor.testDataRepository.getData(data);
        HashMap<String, String> requestHeaders = executor.testDataRepository.getComplexData(headers);
        Response response;
        try {
            response = executor.api.postWithRedirect(initialURL, url, requestData, requestHeaders);
        } catch (Exception e) {
            return isOptional;
        }
        if(result == null || result.equals(""))// TODO results or headers?
            result = executor.DEFAULT_LAST_API_RESULT;
        executor.testDataRepository.setTestDataObject(result, response);
        return true;
    }
}
