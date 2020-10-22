package automation.keyword.web;

import automation.annotations.KeywordRegexp;
import automation.keyword.AbstractKeyword;
import automation.execution.TestStepsExecutor;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Save entity Object from specified Entity locator<br>
 *     <b>Examples: </b>
 *         save entity 'mainPage.TABLE_STRUCTURE' to 'saved.user' by position '1';<br>
 *         save entity 'userPage.TABLE_STRUCTURE' to 'saved.user' by value 'UserName1';<br>
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
public class SaveEntityFromPageKeyword extends AbstractKeyword {

    @KeywordRegexp("Save entity 'locator' to 'data' [by position [last| 'position' ]|by value 'value'];")
    static String LABEL = "save entity";
    String BY_POSITION = "0"; // Default position - first one
    String BY_VALUE = "";

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            SaveEntityFromPageKeyword result = (SaveEntityFromPageKeyword)super.generateFromLine(line);


            Pattern p =  Pattern.compile("('(.*?)')|(\"(.*?)\")");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.target = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.data = line.substring(matcher.start()+1, matcher.end()-1);

            if (line.toLowerCase().contains("by position last")) {
                result.BY_POSITION = "-1";

            } else {
                if(matcher.find())
                    if(line.toLowerCase().contains("by value"))
                        result.BY_VALUE = line.substring(matcher.start()+1, matcher.end()-1);
                    else
                        result.BY_POSITION = line.substring(matcher.start()+1, matcher.end()-1);
            }

            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {
        if(!BY_VALUE.equals(""))
            BY_VALUE = (String)executor.testDataRepository.getData(BY_VALUE);
        BY_POSITION = (String)executor.testDataRepository.getData(BY_POSITION);
        Map<String, String> targets = executor.locatorsRepository.getComplexTarget(target); // process dynamic values
        Map<String, String> actualValues = executor.page.getComplexObject(targets, Integer.valueOf(BY_POSITION), BY_VALUE);

        executor.testDataRepository.setComplexData(data, actualValues);
        return true;

        }
}
