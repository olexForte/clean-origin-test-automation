package automation.keyword.web;

import automation.annotations.KeywordRegexp;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Execute JS for Element by locator<br>
 */
public class ExecuteJSKeyword extends AbstractKeyword {

    @KeywordRegexp("Execute JS 'code' for item 'locator' [and save results to 'SAVED.value'];")
    static String LABEL = "execute js";

    String resultField = "";

    @Override
    public AbstractKeyword generateFromLine(String line) {
        isOptional = isStepOptional(line);
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            ExecuteJSKeyword result = (ExecuteJSKeyword)super.generateFromLine(line);

            result.data = null;
            Pattern p =  Pattern.compile("('(.*?)')|(\"(.*?)\")");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.data = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.target = line.substring(matcher.start()+1, matcher.end()-1);
            if(matcher.find())
                result.resultField = line.substring(matcher.start()+1, matcher.end()-1);
            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {
        target = executor.locatorsRepository.getTarget(target); // process dynamic values
        data = (String) executor.testDataRepository.getData(data);
        String result = executor.page.runJSFor(target, data);
        if(!resultField.equals(""))
            executor.testDataRepository.setData(resultField, result);
        return true;
    }
}
