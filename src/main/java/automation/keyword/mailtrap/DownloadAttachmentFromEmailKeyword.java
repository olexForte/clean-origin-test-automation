package automation.keyword.mailtrap;

import automation.annotations.KeywordRegexp;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Download attachment from Mailtrap email <br>
 *       <b>Usage Example: </b>
 *       Download attachment by path 'mailTrapEndpoints.MESSAGE_ATTACHMENT_IN_INBOX:mailTrap.inbox:saved.EMAIL_ID' to 'SAVED.attachments';
 * Read file 'SAVED.attachments' TEXT from output dir to 'SAVED.attachmentContent';
 */
public class DownloadAttachmentFromEmailKeyword extends AbstractKeyword {

    @KeywordRegexp("Download attachment by path 'emailID' to 'SAVED.value'")
    static String LABEL = "download attachment by path";
    String email = "";
    String result = "";

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            DownloadAttachmentFromEmailKeyword result = (DownloadAttachmentFromEmailKeyword)super.generateFromLine(line);

            result.data = null;
            Pattern p =  Pattern.compile("('(.*?)')|(\"(.*?)\")");
            Matcher matcher = p.matcher(line);

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
        email = executor.locatorsRepository.getTarget(email);
        String filename = executor.mail.downloadAttachments(email);
        executor.testDataRepository.setData(result, filename);
        return true;
    }
}
