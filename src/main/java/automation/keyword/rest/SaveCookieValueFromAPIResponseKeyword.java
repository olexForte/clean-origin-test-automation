package automation.keyword.rest;

import automation.annotations.KeywordRegexp;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;
import io.restassured.response.Response;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Save value from API response<br>
 *     <b>Example: </b>
 * Save API response cookie 'laravel_session' from 'saved.apiResponse' to 'saved.laravel_session';
 */
public class SaveCookieValueFromAPIResponseKeyword extends AbstractKeyword {
    @KeywordRegexp("Save API response cookie 'field' from 'saved.apiResponse' to 'saved.value';")
    static String LABEL = "save api response cookie";
    String source = "";
    String rule = "";

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            SaveCookieValueFromAPIResponseKeyword result = (SaveCookieValueFromAPIResponseKeyword)super.generateFromLine(line);

            Pattern p =  Pattern.compile("('(.*?)')|(\"(.*?)\")");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.data = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.source = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.target = line.substring(matcher.start()+1, matcher.end()-1);
            //if(matcher.find())
            //    result.rule = line.substring(matcher.start()+1, matcher.end()-1);
            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {
        String dataLine = (String)executor.testDataRepository.getData(data); // process dynamic values
        String savedValues = "";

        Response response =  (Response)executor.testDataRepository.getTestDataObject(source);

        Object val;
        val = response.getCookies().get( dataLine );
        savedValues = String.valueOf(val);

        executor.testDataRepository.setData(target, savedValues);
        return true;
    }
}
