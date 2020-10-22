package automation.keyword.validation.rest;

import automation.annotations.KeywordRegexp;
import io.restassured.response.Response;
import automation.keyword.AbstractKeyword;
import automation.execution.TestStepsExecutor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
// UNDER CONSTRUCTION
/**
 * Validate response from API using rule
 */
public class ValidateResponseKeyword extends AbstractKeyword {

    @KeywordRegexp("Validate response 'SAVED.data' with rule 'rule'")
    static String LABEL = "validate response";

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            ValidateResponseKeyword result = (ValidateResponseKeyword)super.generateFromLine(line);

            Pattern p =  Pattern.compile("('(.*?)')|(\"(.*?)\")");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.data = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.target = line.substring(matcher.start()+1, matcher.end()-1);
            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {
        target = executor.locatorsRepository.getTarget(target); // process dynamic values

        Response response =  (Response)executor.testDataRepository.getTestDataObject(data);

        //TODO add response processing


//        String actualValue = executor.page.getText(target);
//        String expectedValue = executor.testDataProcessor.getData(data);
//        if (actualValue.equals(expectedValue)){
//            executor.reporter.passWithScreenshot("Elements are equal: \n Actual:" + actualValue + "\nExpected: " + expectedValue);
//        }else {
//            executor.reporter.failWithScreenshot("Elements are not equal: \n Actual:" + actualValue + "\nExpected: " + expectedValue);
//            return false;
//        }
        return true;
    }
}
