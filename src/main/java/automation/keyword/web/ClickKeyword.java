package automation.keyword.web;

import automation.annotations.KeywordRegexp;
import automation.keyword.AbstractKeyword;
import automation.execution.TestStepsExecutor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Click on Element by locator<br>
 *     <b>Examples: </b>
 *         Click on item 'userPage.MENU_ICON'; <br>
 *         Click on item "//a[text()='Log in']";
 */
public class ClickKeyword extends AbstractKeyword {

    @KeywordRegexp("Click on item 'locator';")
    static String LABEL = "click on item";

    @Override
    public AbstractKeyword generateFromLine(String line) {
        isOptional = isStepOptional(line);
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            ClickKeyword result = (ClickKeyword)super.generateFromLine(line);

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
        executor.page.clickOnElement(target, isOptional);
        return true;
    }
}
