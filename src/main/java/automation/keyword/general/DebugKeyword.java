package automation.keyword.general;

import automation.annotations.KeywordRegexp;
import automation.keyword.AbstractKeyword;
import automation.execution.TestStepsExecutor;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Debug point (only for Development purpose)
 */
public class DebugKeyword extends AbstractKeyword {

    @KeywordRegexp("Debug")
    static String LABEL = "debug";
    @KeywordRegexp("Report message")
    static String LABEL_ALT = "report message ";

    ArrayList<String> messages = new ArrayList<>();

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase()) ||
                prepareLine(line).toLowerCase().startsWith(LABEL_ALT.toLowerCase())){
            DebugKeyword result = (DebugKeyword)super.generateFromLine(line);

            Pattern p =  Pattern.compile("('(.*?)')|(\"(.*?)\")");
            Matcher matcher = p.matcher(line);
            while(matcher.find())
                result.messages.add(line.substring(matcher.start()+1, matcher.end()-1));
            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {
        String finalMessage = "";
        for(String message : messages) {
            finalMessage = finalMessage + (String)executor.testDataRepository.getData(message) + "\n";
        }
        if(finalMessage != "")
            executor.reporter.pass(finalMessage);
        return true;
    }
}
