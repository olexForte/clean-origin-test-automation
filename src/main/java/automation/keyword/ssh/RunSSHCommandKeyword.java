package automation.keyword.ssh;

import automation.annotations.KeywordRegexp;
import automation.keyword.AbstractKeyword;
import automation.execution.TestStepsExecutor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Run SSH command and wait until it will be finished
 * <b>Example:</b>
 * Run SSH command 'ssh.SSH_START_COMMAND';
 */
public class RunSSHCommandKeyword extends AbstractKeyword {

    @KeywordRegexp("Run SSH command 'command'")
    static String LABEL = "run ssh command";

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().matches(LABEL.toLowerCase()+".*")){
            RunSSHCommandKeyword result = (RunSSHCommandKeyword)super.generateFromLine(line);

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

        target = (String)executor.testDataRepository.getData(target);
        executor.ssh.runCommand(target);

        return true;
    }
}
