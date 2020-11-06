package automation.keyword.rest;

import automation.annotations.KeywordRegexp;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;
import io.restassured.response.Response;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Save cookies/headers from API response<br>

 */
public class SaveHeadersCookiesFromAPIResponseKeyword extends AbstractKeyword {
    @KeywordRegexp("Save all cookies from API response 'saved.apiResponse' to 'saved.value';")
    static String LABEL = "save all cookies from api response";
    @KeywordRegexp("Save all headers from API response 'saved.apiResponse' to 'saved.value';")
    static String ALT_LABEL = "save all headers from api response";
    String source = "";
    boolean saveHeaders = false;

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            SaveHeadersCookiesFromAPIResponseKeyword result = (SaveHeadersCookiesFromAPIResponseKeyword)super.generateFromLine(line);

            Pattern p =  Pattern.compile("('(.*?)')|(\"(.*?)\")");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.source = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.target = line.substring(matcher.start()+1, matcher.end()-1);

            result.saveHeaders = false;
            return result;
        }
        if(prepareLine(line).toLowerCase().startsWith(ALT_LABEL.toLowerCase())){
            SaveHeadersCookiesFromAPIResponseKeyword result = (SaveHeadersCookiesFromAPIResponseKeyword)super.generateFromLine(line);

            Pattern p =  Pattern.compile("('(.*?)')|(\"(.*?)\")");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.source = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.target = line.substring(matcher.start()+1, matcher.end()-1);

            result.saveHeaders = true;
            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {

        Response response = (Response)executor.testDataRepository.getTestDataObject(source);
        if(saveHeaders)
            executor.testDataRepository.setTestDataObject(target, response.getHeaders());
        else
            executor.testDataRepository.setTestDataObject(target, response.getDetailedCookies());
        return true;
    }
}
