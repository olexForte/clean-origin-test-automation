package automation.keyword.web;

import automation.annotations.KeywordRegexp;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Type value to locator<br>
 *     Examples:<br>
 *     Type value 'Name Surname' in 'mainPage.CHECKOUT_PAGE_NAME_ON_CARD';<br>
 *     Type value 'registration.TEST_USER_FIRST_NAME' to 'registrationPage.FIRST_NAME_INPUT';
 */
public class TypeKeyword extends AbstractKeyword {

    @KeywordRegexp("Type value 'data' to 'locator';")
    static String LABEL = "type value";

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            TypeKeyword result = (TypeKeyword)super.generateFromLine(line);

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
        executor.page.setText(target, data, isOptional);
        return true;
    }
}
