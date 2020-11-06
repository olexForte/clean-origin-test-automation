package automation.keyword.web;

import automation.annotations.KeywordRegexp;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Save LIST OF entity Objects from specified Entity locator<br>
 *     <b>Examples: </b>
 * Save table 'employeePage.ENGAGEMENT_SCORES_TABLE_STRUCTURE' to 'saved.table';
 * Validate value 'saved.table' similar 'employeesData.EXPECTED_DATA_SCORES';
 *
 *      Each entity locator should be expressed in JSON format. <br>
 *      Locator should contain 'parent' element and child elements <br>
 *     Entity locator <b>Example: </b>
 *          TABLE_STRUCTURE = { "parent" : "//tr[@role='row'][@class='odd' or @class='even']", \
 *  "Full Name" : "(.//td)[1]"  ,  \
 *  "Email Address" : "(.//td)[2]", \
 *  "Permissions" : "(.//td)[3]", \
 *  "Status" : "(.//td)[4]" }
 *
 *     Saved Entity object - JSON object with SAME list of fields that was described in Entity Locator (all fields except PARENT)
 *
 */
public class SaveTableFromPageKeyword extends AbstractKeyword {

    @KeywordRegexp("Save table 'locatorSchema' to 'data';")
    static String LABEL = "save table";

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            SaveTableFromPageKeyword result = (SaveTableFromPageKeyword)super.generateFromLine(line);

            Pattern p =  Pattern.compile("('(.*?)')|(\"(.*?)\")");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.target = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.data = line.substring(matcher.start()+1, matcher.end()-1);
            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {

        Map<String, String> targets = executor.locatorsRepository.getComplexTarget(target); // process dynamic values
        List<HashMap<String, String>> actualValues = executor.page.getComplexObject(targets);

        executor.testDataRepository.setComplexData(data, actualValues);
        return true;

    }
}
