package automation.keyword.general;

import automation.annotations.KeywordRegexp;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pause execution for specified amount time<br>
 *     <b>Example: </b>
 *         Wait for '3' seconds; <br>
 */
public class WaitKeyword extends AbstractKeyword {

    @KeywordRegexp("Wait for 'amount' seconds;")
    static String LABEL = "wait for";

    @Override
    public AbstractKeyword generateFromLine(String line) {
        isOptional = isStepOptional(line);
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            WaitKeyword result = (WaitKeyword)super.generateFromLine(line);

            result.data = null;
            Pattern p =  Pattern.compile("('(.*?)')|(\"(.*?)\")");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.target = line.substring(matcher.start()+1, matcher.end()-1);
            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {
        int timeout = Integer.parseInt(target + "000");
        Thread.sleep(timeout);
        return true;
    }
}