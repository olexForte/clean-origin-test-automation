package automation.keyword.mailtrap;

import automation.annotations.KeywordRegexp;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  Get value from Mailtrap email <br>
 *       <b>Example: </b>
 *       Get value from email 'saved.EXPECTED_PATTERN_FROM_EMAIL' by email url 'mailTrapEndpoints.MESSAGE_TEXT_IN_INBOX:mailTrap.inbox:saved.EMAIL_ID' to 'SAVED.messageContent';
 */
public class GetFieldFromEmailKeyword extends AbstractKeyword {

    @KeywordRegexp("Get value from email 'fieldDescription' by email url 'emailURL' to 'SAVED.value'")
    static String LABEL = "get value from email";
    String description = "";
    String email = "";
    String result = "";

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            GetFieldFromEmailKeyword result = (GetFieldFromEmailKeyword)super.generateFromLine(line);

            result.data = null;
            Pattern p =  Pattern.compile("('(.*?)')|(\"(.*?)\")");
            Matcher matcher = p.matcher(line);

            matcher.find();
            result.description = line.substring(matcher.start()+1, matcher.end()-1);

            matcher.find();
            result.email = line.substring(matcher.start()+1, matcher.end()-1);

            matcher.find();
            result.result = line.substring(matcher.start()+1, matcher.end()-1);

            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {
        description = (String)executor.testDataRepository.getData(description);
        email = executor.locatorsRepository.getTarget(email);
        String value = executor.mail.getEmailBodyValue(description, email);
        executor.testDataRepository.setData(result, value);
        return true;
    }
}
