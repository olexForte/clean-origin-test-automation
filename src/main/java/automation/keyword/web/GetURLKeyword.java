package automation.keyword.web;

import automation.annotations.KeywordRegexp;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Get url<br>
 *     <b>Example</b>
 *             Get URL 'http://em-test.cleanOriginmedia.com/en-us/auth/login/';
 */
public class GetURLKeyword extends AbstractKeyword {

    @KeywordRegexp("Get value from URL 'description' to save 'SAVED.value';")
    static String LABEL = "get value from url";

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            GetURLKeyword result = (GetURLKeyword) super.generateFromLine(line);

            result.data = null;
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
        target = executor.locatorsRepository.getTarget(target); // process dynamic values
        executor.testDataRepository.setData(data, executor.page.getURLPartByDescription(target));
        return true;
    }
}
