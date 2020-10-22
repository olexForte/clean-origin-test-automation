package automation.keyword.validation.ui;

import automation.annotations.KeywordRegexp;
import automation.keyword.AbstractKeyword;
import automation.execution.TestStepsExecutor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validate that element defined by 'locator' is [visible|invisible]<br>
 *     <b>Example: </b>
 *         validate element 'mainpage.about_us_link' is visible; <br>
 *         validate element 'mainpage.about_us_link' is invisible;
 *
 */
public class ValidateVisibleKeyword extends AbstractKeyword {

    @KeywordRegexp("Validate element 'locator' [is visible|is invisible];")
    static String LABEL = "Validate element";
    static String INVISIBLE_LABEL = "is invisible";
    boolean IS_VISIBLE = true;


    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            ValidateVisibleKeyword result = (ValidateVisibleKeyword)super.generateFromLine(line);

            result.data = null;
            Pattern p =  Pattern.compile("('(.*?)')|(\"(.*?)\")");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.target = line.substring(matcher.start()+1, matcher.end()-1);
            if(line.contains(INVISIBLE_LABEL))
                result.IS_VISIBLE = false;
            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {
        String targetLocator = executor.locatorsRepository.getTarget(target); // process dynamic values

        if(IS_VISIBLE) {
            if (!executor.page.isElementVisible(targetLocator, IS_VISIBLE)) {
                executor.reporter.failWithScreenshot("Element is invisible: " + target);
                return false;
            } else {
                executor.reporter.passWithScreenshot("Element is visible: " + target);
            }
            return true;
        } else {

                if (executor.page.isElementVisible(targetLocator, IS_VISIBLE)){
                    executor.reporter.failWithScreenshot("Element is visible: " + target);
                    return false;
                } else {
                    executor.reporter.passWithScreenshot("Element is invisible: " + target);
                }
                return true;
            }
    }
}
