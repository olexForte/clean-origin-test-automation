package automation.keyword.validation;

import automation.annotations.KeywordRegexp;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validate that checkbox defined by 'locator' is checked/unchecked
 * <b>Example:</b><br>
 *    validate that 'mycartpage.grand_total_field' is checked;
 */
public class ValidateCheckedKeyword extends AbstractKeyword {

    @KeywordRegexp("Validate element 'locator' [is checked|is unchecked];")
    static String LABEL = "Validate element";
    static String UNCHECKED_LABEL = "is unchecked";
    boolean IS_CHECKED = true;


    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            ValidateCheckedKeyword result = (ValidateCheckedKeyword)super.generateFromLine(line);

            result.data = null;
            Pattern p =  Pattern.compile("('(.*?)')|(\"(.*?)\")");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.target = line.substring(matcher.start()+1, matcher.end()-1);
            if(line.contains(UNCHECKED_LABEL))
                result.IS_CHECKED = false;
            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {
        String targetLocator = executor.locatorsRepository.getTarget(target); // process dynamic values

        if(IS_CHECKED) {
            if (!executor.page.isElementChecked(targetLocator, IS_CHECKED)) {
                executor.reporter.failWithScreenshot("Element is checked: " + target);
                return false;
            } else {
                executor.reporter.passWithScreenshot("Element is unchecked: " + target);
            }
            return true;
        } else {

            if (executor.page.isElementChecked(targetLocator, IS_CHECKED)){
                executor.reporter.failWithScreenshot("Element is checked: " + target);
                return false;
            } else {
                executor.reporter.passWithScreenshot("Element is unchecked: " + target);
            }
            return true;
        }
    }

}