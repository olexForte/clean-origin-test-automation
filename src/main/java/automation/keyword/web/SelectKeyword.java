package automation.keyword.web;

import automation.annotations.KeywordRegexp;
import automation.keyword.AbstractKeyword;
import automation.execution.TestStepsExecutor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Select value in Select html element <br>
 *     Example:<br>
 *         Select dropdown value '(12) Dec' in 'mainPage.CHECKOUT_PAGE_CREDIT_CARD_MONTH';
 */
public class SelectKeyword extends AbstractKeyword {

    @KeywordRegexp("Select dropdown value 'data' in 'locator';")
    static String LABEL = "select dropdown value";

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            SelectKeyword result = (SelectKeyword)super.generateFromLine(line);

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
        data = (String)executor.testDataRepository.getData(data);
        executor.page.selectFromDropdown(target, data, isOptional);
        return true;
    }
}
