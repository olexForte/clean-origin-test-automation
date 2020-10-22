package automation.keyword.mailtrap;

import automation.annotations.KeywordRegexp;
import automation.configuration.ProjectConfiguration;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  Check for Email in Mailtrap inbox <br>
 *       <b>Example:</b>
 *       Check for Email with subject 'mailTrap.expectedSubject' for 'mailTrap.email' <br>
 */
public class CheckForEmailKeyword extends AbstractKeyword {

    @KeywordRegexp("Check for Email with subject 'subject' for 'email' [to 'saved.var'][and check timestamp]")
    static String LABEL = "check for email with subject";
    String target = "";
    String subject = "";
    String email = "";
    String result = "";

    String CHECK_TIMESTAMP_LABEL = "check timestamp";
    boolean checkTimestamp = false;

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            CheckForEmailKeyword result = (CheckForEmailKeyword)super.generateFromLine(line);

            result.data = null;
            Pattern p =  Pattern.compile("('(.*?)')|(\"(.*?)\")");
            Matcher matcher = p.matcher(line);

            matcher.find();
            result.subject = line.substring(matcher.start()+1, matcher.end()-1);

            matcher.find();
            result.email = line.substring(matcher.start()+1, matcher.end()-1);

            if(matcher.find())
                result.result = line.substring(matcher.start()+1, matcher.end()-1);

            if(line.contains(CHECK_TIMESTAMP_LABEL))
                result.checkTimestamp = true;

            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {

        String inboxURL = "mailTrapEndpoints.LIST_OF_MESSAGES_IN_INBOX:mailTrap.inbox"; //TODO move to parameters
        target = executor.locatorsRepository.getTarget(inboxURL);

        subject = (String)executor.testDataRepository.getData(subject);
        email = (String)executor.testDataRepository.getData(email);

        String emailId = executor.mail.checkForEmail(target, subject, email, checkTimestamp);

        if(!result.equals(""))
            executor.testDataRepository.setData(result, emailId);

        return true;
    }
}
