package automation.keyword.web;

import automation.annotations.KeywordRegexp;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hover over Element by locator<br>
 *     <b>Examples: </b>
 *         Hover over item 'userPage.MENU_ICON'; <br>
 *         Hover over item "//a[text()='Log in']";
 */
public class HoverOverKeyword extends AbstractKeyword {

    @KeywordRegexp("Hover over item 'locator';")
    static String LABEL = "hover over item";

    @Override
    public AbstractKeyword generateFromLine(String line) {
        isOptional = isStepOptional(line);
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            HoverOverKeyword result = (HoverOverKeyword)super.generateFromLine(line);

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
        target = executor.locatorsRepository.getTarget(target); // process dynamic values
        executor.page.hoverElement(target, isOptional);
        return true;
    }
}
