package automation.keyword.complex;

import automation.annotations.KeywordRegexp;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;
import automation.web.BasePage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fill fields in common data form
 *
 * Fill all field on a screen using JSON file with structure:
 * {"label_next_to_field1":"value1", "label_next_to_field2":"value2", etc.}
 *
 * Example of data file:
 * SUBSCRIPTION_DATA_EXAMPLE = {"Address" : "5320 N Sheridan",\
 * "Address 2 (optional)":"1411",\
 * "City":"Chicago",\
 * "State (optional)":"Illinois",\
 * "ZIP/Postal Code":"60640",\
 * "Country":"#2",\
 * "Phone (optional)":"7734958072"}
 *
 * <b>Usage Examples</b>:
 * Fill data form 'subscriptionData.SUBSCRIPTION_DATA_EXAMPLE'
 *
 */
public class FillDataFormKeyword extends AbstractKeyword {
    private static final String COMPLEX_INPUT_MARKER = "->";

    @KeywordRegexp("Fill data form 'dataJsonField';")
    static String LABEL = "fill data form";

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().matches(LABEL.toLowerCase()+".*")){
            FillDataFormKeyword result = (FillDataFormKeyword)super.generateFromLine(line);

            Pattern p =  Pattern.compile("(('(.*?)')|(\"(.*?)\"))");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.data = line.substring(matcher.start()+1, matcher.end()-1);
            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {
        HashMap<String,String> allFields = executor.testDataRepository.getComplexData(data);

        for(Map.Entry<String,String> field : allFields.entrySet()){
            String fieldLabel = field.getKey();
            String fieldValue = field.getValue();

            try {
                executor.page.sleepFor(1500);
                if(fieldValue != null && !fieldValue.equals(""))
                    setFieldValue(executor.page, fieldLabel, fieldValue);
            }catch (Exception e){
                System.out.println();
            }
        }

        return true;
    }

    /**
     * Set value on form
     * TODO Add data picker and radio button processing
     * @param page
     * @param fieldLabel
     * @param fieldValue
     */
    private void setFieldValue(BasePage page, String fieldLabel, String fieldValue) throws Exception {

        String inputFieldXpathTemplate = "//span[text()='%s']/parent::label[1]//following-sibling::div[1]/input[1]";
        String selectFieldXpathTemplate = "//span[text()='%s']/parent::label[1]/following-sibling::div[1]/select[1]";
        String optionXpathTemplate = "//li/div[@role='option'][.='%s']";
        String optionByIndexXpathTemplate = "(//li/div[@role='option'])[%s]";
        String checkboxXpathTemplate = "//label[contains(.,'%s')]/input[@type='checkbox'] | //input[1][@type='checkbox']/following::label[1][contains(.,'%s')]";
        String dateInputTemplate = "//label[.='%s']/following::div[1]/input[1]";

        String complexInputXpathTemplate = "//span[.='%s']/parent::legend[1]/following-sibling::div//span[text()='%s']/parent::label[1]/following-sibling::div[1]/input[1]";

        if(fieldLabel.contains(COMPLEX_INPUT_MARKER)){
            String[] complexFieldParts = fieldLabel.split(COMPLEX_INPUT_MARKER);
            String locator = String.format(complexInputXpathTemplate, complexFieldParts[0], complexFieldParts[1]);
            page.scrollToElement(locator);
            page.setText(locator, fieldValue, false);
        } else
        if(page.isElementDisplayedRightNow(String.format(inputFieldXpathTemplate, fieldLabel), 0)){
            page.scrollToElement(String.format(inputFieldXpathTemplate, fieldLabel));
            page.setText(String.format(inputFieldXpathTemplate, fieldLabel), fieldValue, false);
        } else
        if(page.isElementDisplayedRightNow(String.format(selectFieldXpathTemplate, fieldLabel), 0)){
            page.scrollToElement(String.format(selectFieldXpathTemplate, fieldLabel));
            //page.selectFromDropdown(String.format(selectFieldXpathTemplate, fieldLabel), fieldValue, false);

            if(fieldValue.matches("#\\d+"))
                page.selectFromDropdownByIndex(String.format(selectFieldXpathTemplate, fieldLabel), Integer.valueOf(fieldValue.replace("#","")), false);
            else
                page.selectFromDropdown(String.format(selectFieldXpathTemplate, fieldLabel), fieldValue, false);;
        }
        //TODO update check box processing
        if(page.isElementDisplayedRightNow(String.format(checkboxXpathTemplate, fieldLabel, fieldLabel), 0)){
            page.scrollToElement(String.format(checkboxXpathTemplate, fieldLabel, fieldLabel));
            page.clickOnElement(String.format(checkboxXpathTemplate, fieldLabel, fieldLabel), false);
        }
//        if(page.isElementDisplayedRightNow(String.format(dateInputTemplate, fieldLabel), 0)){
//            page.clickOnElement(String.format(dateInputTemplate, fieldLabel), false);
//            page.runJSFor(String.format(dateInputTemplate, fieldLabel), "arguments[0].value = '" + fieldValue + "'");
//        }
    }
}
