package automation.keyword.rest;

import automation.annotations.KeywordRegexp;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//UNDER CONSTRUCTION
/**
 * Send POST request with redirection to Login Admin app
 *  */
public class POSTRequestWithRedirectKeyword extends AbstractKeyword {

    @KeywordRegexp("Send redirected POST request 'initialURL' 'url' with data 'data' with headers 'headers' [ and save results to 'SAVED.data']")
    static String LABEL = "send redirected post request";

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            POSTRequestWithRedirectKeyword result = (POSTRequestWithRedirectKeyword)super.generateFromLine(line);;

            Pattern p =  Pattern.compile("('(.*?)')|(\"(.*?)\")");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.target = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.data = line.substring(matcher.start()+1, matcher.end()-1);
            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {
        String url = executor.locatorsRepository.getTarget(target); // process dynamic values
        if(url == null)
            url = (String)executor.testDataRepository.getData(target);

        Response response;
        try {
            response = null;//executor.api.loginToAdminAndGetCookies(url); //TODO FIX
        } catch (Exception e) {
            return isOptional;
        }

        executor.testDataRepository.setTestDataObject(data, response);
        return true;
    }
}
