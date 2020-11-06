package automation.keyword.validation;

import automation.annotations.KeywordRegexp;
import automation.keyword.AbstractKeyword;
import automation.execution.TestStepsExecutor;
import automation.tools.ComparatorTool;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validate that value in element defined by 'locator' equals/contains to value stored in 'data' field
 * <b>Example:</b><br>
 *    validate value 'mycartpage.grand_total_field' equals 'saved.result';
 */
public class ValidateEqualsKeyword extends AbstractKeyword {

    public enum ActionType {
        EQUALS(" equals "),
        CONTAINS(" contains "),
        STRICT_CONTAINS(" strict contains "),
        SIMILAR(" similar "),
        NOT_CONTAINS(" does not contain "),
        MATCHES(" match ");

        String marker;
        ActionType(String marker){
            this.marker = marker;
        }

    }

    ActionType action = ActionType.EQUALS;

    @KeywordRegexp("Validate value 'locatorORdata' [equals|contains|does not contain|similar|match] 'locatorORdataORregexp';")
    static String LABEL = "validate value";

    @Override
    public AbstractKeyword generateFromLine(String line) {

        isOptional = isStepOptional(line);
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            ValidateEqualsKeyword result = (ValidateEqualsKeyword)super.generateFromLine(line);

            Pattern p =  Pattern.compile("('(.*?)')|(\"(.*?)\")");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.data = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.target = line.substring(matcher.start()+1, matcher.end()-1);

            for(ActionType act : ActionType.values())
                if(line.contains(act.marker))
                    result.action = act;

            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {

        boolean result = false;

        String firstValue = "";
        String secondValue = "";

        // Getting actual value
        String value = data;
        try {
            data = executor.locatorsRepository.getTarget(data); // process dynamic values
        } catch (Exception e) {
            data = null;
        }

        //if data was found in locations
        if (data != null)
            firstValue = executor.page.getText(data, isOptional);
        else //actual value from Data
            firstValue = (String) executor.testDataRepository.getData(value);

        // Getting expected value
        value = target;
        try {
            target = executor.locatorsRepository.getTarget(target); // process dynamic values
        } catch (Exception e) {
            target = null;
        }

        //if target was found in locations
        if (target != null)
            secondValue = executor.page.getText(target, isOptional);
        else //actual value from Data
            secondValue = (String) executor.testDataRepository.getData(value);

        switch (action){
            case EQUALS:
                //if comparing prices
                if (ComparatorTool.looksLikePrice(secondValue) &&
                        ComparatorTool.looksLikePrice(firstValue)) {
                    LOGGER.info("Process values as Numbers");
                    firstValue = String.valueOf(ComparatorTool.getFloatValue(firstValue));
                    secondValue = String.valueOf(ComparatorTool.getFloatValue(secondValue));
                }
                result = secondValue.equals(firstValue);

                if (result){
                    executor.reporter.passWithScreenshot("Elements are equal: \n First: " + firstValue + "\nSecond: " + secondValue);
                }else {
                    executor.reporter.failWithScreenshot("Elements are not equal: \n First: " + firstValue + "\nSecond: " + secondValue);
                    return false;
                }
                break;
            case CONTAINS:
                result = ComparatorTool.cleanValue(firstValue).contains(ComparatorTool.cleanValue(secondValue));
                if (result){
                    executor.reporter.passWithScreenshot("Element  \n First: " + firstValue + " contains: \nSecond: " +  secondValue);
                }else {
                    executor.reporter.failWithScreenshot("Elements \n First: " + firstValue + " does not contain: \n Second: " + secondValue);
                    return false;
                }
                break;
            case STRICT_CONTAINS:
                result = firstValue.contains(secondValue);
                if (result){
                    executor.reporter.passWithScreenshot("Element  \n First: " + firstValue + " contains: \nSecond: " +  secondValue);
                }else {
                    executor.reporter.failWithScreenshot("Elements \n First: " + firstValue + " does not contain: \n Second: " + secondValue);
                    return false;
                }
                break;
            case SIMILAR:
                result = ComparatorTool.cleanValue(firstValue).equals(ComparatorTool.cleanValue(secondValue));
                if (result){
                    executor.reporter.pass("Elements are similar:  \n First: " + firstValue + "\nSecond: " +  secondValue);
                }else {
                    executor.reporter.fail("Elements are not similar:  \n First: " + firstValue + "\nSecond: " +  secondValue);

//                    char[] lineOne = cleanValue(firstValue).toCharArray();
//                    char[] lineTwo = cleanValue(secondValue).toCharArray();
//                    for (int i = 0; i < lineOne.length ;i++) {
//                        if(lineOne[i] != lineTwo[i])
//                            executor.reporter.fail("Elements are not similar:  \n First: ..." + getCharsFrom(cleanValue(firstValue), i) + "\n Second: ..." + getCharsFrom(cleanValue(secondValue), i));
//                    }

                    return false;
                }
                break;
            case NOT_CONTAINS:
                result = firstValue.contains(secondValue);
                if (result){
                    executor.reporter.failWithScreenshot("Element  \n First: " + firstValue + " contains: \nSecond: " +  secondValue);
                    return false;
                }else {
                    executor.reporter.passWithScreenshot("Elements \n First: " + firstValue + " does not contain: \n Second: " + secondValue);
                }
                break;
            case MATCHES:
                result = firstValue.replace("\n"," ").matches(secondValue);
                if (result){
                    executor.reporter.passWithScreenshot("Element  \n Value: " + firstValue + " matches: \nExpression: " +  secondValue);
                }else {
                    executor.reporter.failWithScreenshot("Elements \n Value: " + firstValue + " does not match: \n Expression: " + secondValue);
                    return false;
                }
                break;
        }
        return true;
    }

    private String getCharsFrom(String line, int startChar) {
        String val = "";
        val = line.substring(startChar, (startChar + 100) > line.length()? line.length(): (startChar + 100) );
        return val;
    }

}