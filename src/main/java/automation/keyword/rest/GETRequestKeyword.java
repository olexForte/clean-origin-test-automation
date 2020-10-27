package automation.keyword.rest;

import automation.annotations.KeywordRegexp;
import io.restassured.http.Cookies;
import io.restassured.http.Headers;
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

    @KeywordRegexp("Send GET request 'url' [with headers 'headers'][with cookies 'cookies'][ and save results to 'SAVED.data']")
    static String LABEL = "send get request";
    String result;
    String headers;
    String cookies;
    String regExpCookies = ".*with cookies ['\"](.*?)['\"].*";
    String regExpHeaders = ".*with headers ['\"](.*?)['\"].*";

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            GETRequestKeyword result = (GETRequestKeyword)super.generateFromLine(line);

            result.data = null;
            Pattern p =  Pattern.compile("('(.*?)')|(\"(.*?)\")");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.target = line.substring(matcher.start()+1, matcher.end()-1);

            if (line.matches(regExpCookies)) {
                result.cookies = line.replaceAll(regExpCookies, "$1");
            }
            if (line.matches(regExpHeaders)) {
                result.headers = line.replaceAll(regExpHeaders, "$1");
            }
            result.result = line.substring(matcher.start()+1, matcher.end()-1);

            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {
        String url = executor.locatorsRepository.getTarget(target); // process dynamic values
        Response response;
        if(url == null)
            url = (String)executor.testDataRepository.getData(target);

//        if (executor.testDataRepository.getTestDataObject(headers) instanceof Headers) {
//             response = executor.api.getRequest(url, (Headers)executor.testDataRepository.getTestDataObject(headers));
//        }else {
//            HashMap<String, String> requestHeaders = executor.testDataRepository.getComplexData(headers);
//             response = executor.api.getRequest(url, requestHeaders);
//        }
//
//        if (executor.testDataRepository.getTestDataObject(cookies) instanceof Cookies) {
//            response = executor.api.getRequest(url, (Headers)executor.testDataRepository.getTestDataObject(cookies));
//        }else {
//            HashMap<String, String> requestHeaders = executor.testDataRepository.getComplexData(cookies);
//            response = executor.api.getRequest(url, requestHeaders);
//        }

        response = executor.api.getRequest(url,
                executor.testDataRepository.getTestDataObject(headers),
                executor.testDataRepository.getTestDataObject(cookies));

        if(result == null || result.equals(""))
            result = executor.DEFAULT_LAST_API_RESULT;
        executor.testDataRepository.setTestDataObject(result, response);
        return true;
    }
}
