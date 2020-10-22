package automation.keyword.validation.ui;

import automation.annotations.KeywordRegexp;
import automation.keyword.AbstractKeyword;
import automation.execution.TestStepsExecutor;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validate that entity described by 'locators' is [present|not present] and contains expected 'data'
 *     <b>Examples: </b>
 *  Validate entity on page 'userPage.TABLE_STRUCTURE' 'userLicense.CLIENT_ITEM_IN_TABLE:RANDOM.str1:RANDOM.str2' not present
 *
 *      Each entity locator should be expressed in JSON format. <br>
 *      Locator should contain 'parent' element and child elements<br>
 *     <b> Entity Locator example: </b>
 *          TABLE_STRUCTURE = { "parent" : "//tr[@role='row'][@class='odd' or @class='even']", \
 *  "Full Name" : "(.//td)[1]"  ,  \
 *  "Email Address" : "(.//td)[2]", \
 *  "Permissions" : "(.//td)[3]", \
 *  "Status" : "(.//td)[4]" }
 *
 *      Data element it is a JSON string that should have SAME field names as Entity locator (Except 'parent')
 *      <b>Entity Data example: </b>
 *      CLIENT_ITEM_IN_TABLE = { "Full Name" : "TestName", \
 *                 "Email Address" : "TestName@test.com", \
 *                 "Permissions" : "$PARAMETER1",\
 *                 "Status" : "$PARAMETER2" }
 *
 */
public class ValidateEntityOnPageKeyword extends AbstractKeyword {

    @KeywordRegexp("Validate entity on page 'locators' 'data' [present|not present];")
    static String LABEL = "validate entity on page";

    static String NOT_LABEL = "not present";

    public boolean positive = true;

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            ValidateEntityOnPageKeyword result =(ValidateEntityOnPageKeyword)super.generateFromLine(line);

            Pattern p =  Pattern.compile("('(.*?)')|(\"(.*?)\")");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.target = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.data = line.substring(matcher.start()+1, matcher.end()-1);
            if(line.contains(NOT_LABEL))
                result.positive = false;
            else
                result.positive = true;
            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {
        Map<String, String> targets = executor.locatorsRepository.getComplexTarget(target); // process dynamic values
        Map<String, String> expectedValues = executor.testDataRepository.getComplexData(data);

        if(positive){
            if (executor.page.wasComplexElementDisplayed(targets, expectedValues, positive)) {
                executor.reporter.passWithScreenshot("Object was found: " + data);
                return true;
            } else {
                executor.reporter.failWithScreenshot("Object was not found: " + data);
                return false;
            }
        } else {
            if (executor.page.wasComplexElementDisplayed(targets, expectedValues, positive)) {
                executor.reporter.failWithScreenshot("Object was found: " + data);
                return false;
            } else {
                executor.reporter.passWithScreenshot("Object was not found: " + data);
                return true;
            }
        }
    }
}
