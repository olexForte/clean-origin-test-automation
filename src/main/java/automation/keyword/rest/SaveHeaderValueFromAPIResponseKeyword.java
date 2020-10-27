package automation.keyword.rest;

import automation.annotations.KeywordRegexp;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;
import io.restassured.response.Response;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Save header from API response<br>
 */
public class SaveHeaderValueFromAPIResponseKeyword extends AbstractKeyword {
    @KeywordRegexp("Save API response header 'field' from 'saved.apiResponse' to 'saved.value';")
    static String LABEL = "save api response header";
    String source = "";
    String rule = "";

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            SaveHeaderValueFromAPIResponseKeyword result = (SaveHeaderValueFromAPIResponseKeyword)super.generateFromLine(line);

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
        val = response.getHeaders().getValue( dataLine );
        savedValues = String.valueOf(val);

        executor.testDataRepository.setData(target, savedValues);
        return true;
    }
}
