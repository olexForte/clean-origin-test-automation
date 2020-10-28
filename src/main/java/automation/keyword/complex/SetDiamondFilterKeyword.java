package automation.keyword.complex;

import automation.annotations.KeywordRegexp;
import automation.entities.application.DiamondsFilter;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;
import automation.web.BasePage;
import org.openqa.selenium.By;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static automation.entities.application.DiamondsFilter.*;

/**
 * Set diamonds filters
 */
public class SetDiamondFilterKeyword extends AbstractKeyword {

    @KeywordRegexp("Set diamonds filter 'dataJsonField';")
    static String LABEL = "set diamonds filter";

    //fields
    String lowerScroller;
    String upperScroller;

    String resetFilterButton;

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if (prepareLine(line).toLowerCase().matches(LABEL.toLowerCase() + ".*")) {
            SetDiamondFilterKeyword result = (SetDiamondFilterKeyword) super.generateFromLine(line);

            Pattern p = Pattern.compile("(('(.*?)')|(\"(.*?)\"))");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.data = line.substring(matcher.start() + 1, matcher.end() - 1);
            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {
        HashMap<String, String> allFields = executor.testDataRepository.getComplexData(data);

        //set default UI elements
        lowerScroller = executor.locatorsRepository.getTarget("diamondsPage.SCROLLER_LOWER");
        upperScroller = executor.locatorsRepository.getTarget("diamondsPage.SCROLLER_UPPER");

        //for each filter item
        for (Map.Entry<String, String> field : allFields.entrySet()) {
            //executor.page.clickOnElement("generalPage.CLOSE_MODAL_DIALOG", true);
            String fieldLabel = field.getKey();
            String fieldValue = field.getValue();

            try {
                if (fieldValue != null && !fieldValue.equals("")) {
                    //executor.page.clickOnElement("generalPage.CLOSE_MODAL_DIALOG", true);
                    setFieldValue(executor, fieldLabel, fieldValue);
                    //executor.page.clickOnElement("generalPage.CLOSE_MODAL_DIALOG", true);
                    //setFieldValue(executor, fieldLabel, fieldValue);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    /**
     * Set field value in filter
     * @param executor executor object
     * @param fieldLabel label of field
     * @param fieldValue value of field
     * @throws Exception something wrong
     */
    private void setFieldValue(TestStepsExecutor executor, String fieldLabel, String fieldValue) throws Exception {
        String pointOnSlider;
        String parent;
        String field;
executor.page.sleepFor(2000);
        switch(fieldLabel.toLowerCase()){
            case SHAPES_LABEL:
                for(String shapeName : fieldValue.split(",")) {
                    String shapeFilter = executor.locatorsRepository.getTarget("diamondsPage.SHAPE_FILTER_TEMPLATE:" + shapeName);
                    if(!shapeWasSelected(executor, shapeFilter))
                        executor.page.clickOnElement(shapeFilter);
                }
                break;
            case CUT_FROM_LABEL:
                pointOnSlider = executor.locatorsRepository.getTarget("diamondsPage.CUT_FILTER_TEMPLATE:"+fieldValue);
                parent = executor.locatorsRepository.getTarget("diamondsPage.CUT_FILTER_PARENT");
                executor.page.dragAndDrop(parent + "/" + lowerScroller, pointOnSlider);
                break;
            case CUT_TO_LABEL:
                pointOnSlider = executor.locatorsRepository.getTarget("diamondsPage.CUT_FILTER_TEMPLATE:"+fieldValue);
                parent = executor.locatorsRepository.getTarget("diamondsPage.CUT_FILTER_PARENT");
                executor.page.dragAndDrop(parent + "/" + upperScroller, pointOnSlider);
                break;
            case CARAT_FROM_LABEL:
                field = executor.locatorsRepository.getTarget("diamondsPage.CARAT_FILTER_FROM");
                executor.page.setTextWithEnter(field, fieldValue, false);
                break;
            case CARAT_TO_LABEL:
                field = executor.locatorsRepository.getTarget("diamondsPage.CARAT_FILTER_TO");
                executor.page.setTextWithEnter(field, fieldValue, false);
                break;
            case COLOR_FROM_LABEL:
                pointOnSlider = executor.locatorsRepository.getTarget("diamondsPage.COLOR_FILTER_TEMPLATE:"+fieldValue);
                parent = executor.locatorsRepository.getTarget("diamondsPage.COLOR_FILTER_PARENT");
                executor.page.dragAndDrop(parent + "/" + lowerScroller, pointOnSlider);
                break;
            case COLOR_TO_LABEL:
                pointOnSlider = executor.locatorsRepository.getTarget("diamondsPage.COLOR_FILTER_TEMPLATE:"+fieldValue);
                parent = executor.locatorsRepository.getTarget("diamondsPage.COLOR_FILTER_PARENT");
                executor.page.dragAndDrop(parent + "/" + upperScroller, pointOnSlider);
                break;
            case PRICE_FROM_LABEL:
                field = executor.locatorsRepository.getTarget("diamondsPage.PRICE_FILTER_FROM");
                executor.page.setTextWithEnter(field, fieldValue, false);
                break;
            case PRICE_TO_LABEL:
                field = executor.locatorsRepository.getTarget("diamondsPage.PRICE_FILTER_TO");
                executor.page.setTextWithEnter(field, fieldValue, false);
                break;
            case CLARITY_FROM_LABEL:
                pointOnSlider = executor.locatorsRepository.getTarget("diamondsPage.CLARITY_FILTER_TEMPLATE:"+fieldValue);
                parent = executor.locatorsRepository.getTarget("diamondsPage.CLARITY_FILTER_PARENT");
                executor.page.dragAndDrop(parent + "/" + lowerScroller, pointOnSlider);
                break;
            case CLARITY_TO_LABEL:
                pointOnSlider = executor.locatorsRepository.getTarget("diamondsPage.CLARITY_FILTER_TEMPLATE:"+fieldValue);
                parent = executor.locatorsRepository.getTarget("diamondsPage.CLARITY_FILTER_PARENT");
                executor.page.dragAndDrop(parent + "/" + upperScroller, pointOnSlider);
                break;
            case HEARTS_ARROWS_LABEL:
                field = executor.locatorsRepository.getTarget("diamondsPage.HEARTS_AND_ARROWS_CHECKBOX");
                if(fieldValue.toLowerCase().equals(true)){ //check
                    if(executor.page.getAttribute(field, "background-image",false).equals("none"))
                        executor.page.clickOnElement(field);
                } else { //uncheck
                    if(!executor.page.getAttribute(field, "background-image",false).equals("none"))
                        executor.page.clickOnElement(field);
                }
                break;
            default:
                LOGGER.warn("No such field: " + fieldLabel);
        }
    }

    private boolean shapeWasSelected(TestStepsExecutor executor, String shapeFilter) {
        //if box-shadow == none - unselected
        return !executor.page.findDynamicElement(shapeFilter, 1).getCssValue("box-shadow").equals("none");
    }

}