package automation.keyword.rest;

import automation.annotations.KeywordRegexp;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import automation.keyword.AbstractKeyword;
import automation.execution.TestStepsExecutor;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Send  POST request to specified URL with data and headers and save results<br>
 *     <b>Example: </b>
 *         Send POST request 'apiEndpoints.CREDENTIALS_URL' with 'API.CREDENTIALS_REQUEST' and save results to 'saved.result'
 */
public class POSTRequestKeyword extends AbstractKeyword {

    @KeywordRegexp("Send POST request 'url' with data 'data' with headers 'headers' [ and save results to 'SAVED.data']")
    static String LABEL = "send post request";
    String result;
    String headers;

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            POSTRequestKeyword result = (POSTRequestKeyword)super.generateFromLine(line);;

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
        String url = executor.locatorsRepository.getTarget(target); // process dynamic values
        if(url == null)
            url = (String)executor.testDataRepository.getData(target);
        String requestData = (String)executor.testDataRepository.getData(data);
        HashMap<String, String> requestHeaders = executor.testDataRepository.getComplexData(headers);
        Response response;
//        if (requestData.startsWith("URLENC"))
//            response = executor.api.postRequest(target, requestData, requestHeaders, ContentType.URLENC);
//        else
        response = executor.api.postRequest(url, requestData, requestHeaders);

        if(result == null || result.equals(""))// TODO results or headers?
            result = executor.DEFAULT_LAST_API_RESULT;
        executor.testDataRepository.setTestDataObject(result, response);
        return true;
    }
}
