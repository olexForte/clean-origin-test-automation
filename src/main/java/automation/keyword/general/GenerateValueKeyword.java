package automation.keyword.general;

import automation.annotations.KeywordRegexp;
import automation.keyword.AbstractKeyword;
import automation.execution.TestStepsExecutor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generate value and save to variable <br>
 *     Example: Generate value 'RANDOM.licenseExpirationDate:CURRENT_DATE:ISO_OFFSET_DATE_TIME' to 'SAVED.field';<br>
 *
 */
public class GenerateValueKeyword  extends AbstractKeyword {
    @KeywordRegexp("Generate value 'RANDOM.field' to 'SAVED.target';")
    static String LABEL = "generate value";
    @KeywordRegexp("Save value '12' to 'SAVED.target';")
    static String LABEL_ALT = "save value";

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().matches(LABEL.toLowerCase()+".*") ||
                prepareLine(line).toLowerCase().matches(LABEL_ALT.toLowerCase()+".*")){
            GenerateValueKeyword result = (GenerateValueKeyword)super.generateFromLine(line);

            Pattern p = Pattern.compile("(('(.*?)')|(\"(.*?)\"))");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.data = (line.substring(matcher.start()+1, matcher.end()-1));
            if(matcher.find())
                result.target = (line.substring(matcher.start()+1, matcher.end()-1));

            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {

        String value = (String)executor.testDataRepository.getData(data);
        if(target != null && !target.equals(""))
            executor.testDataRepository.setData(target, value);
        return true;
    }
}
