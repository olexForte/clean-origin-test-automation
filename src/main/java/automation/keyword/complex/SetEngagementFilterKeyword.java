package automation.keyword.complex;

import automation.annotations.KeywordRegexp;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static automation.entities.application.RingFilter.*;

/**
 * Set diamonds filters
 */
public class SetEngagementFilterKeyword extends AbstractKeyword {

    @KeywordRegexp("Set engagement filter 'dataJsonField';")
    static String LABEL = "set engagement filter";

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if (prepareLine(line).toLowerCase().matches(LABEL.toLowerCase() + ".*")) {
            SetEngagementFilterKeyword result = (SetEngagementFilterKeyword) super.generateFromLine(line);

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

        //for each filter item
        for (Map.Entry<String, String> field : allFields.entrySet()) {
            //executor.page.clickOnElement("generalPage.CLOSE_MODAL_DIALOG", true);
            String fieldLabel = field.getKey();
            String fieldValue = field.getValue();

            try {
                if (fieldValue != null && !fieldValue.equals("")) {
                    setFieldValue(executor, fieldLabel, fieldValue);
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

        if (executor.page.isElementDisplayedRightNow(executor.locatorsRepository.getTarget("generalPage.CLOSE_MODAL_DIALOG"), 0))
            executor.page.clickOnElement(executor.locatorsRepository.getTarget("generalPage.CLOSE_MODAL_DIALOG"), true);
        if(executor.page.isElementDisplayedRightNow(executor.locatorsRepository.getTarget("generalPage.CLOSE_CHAT_DIALOG"), 0)) {
            executor.page.clickOnElement(executor.locatorsRepository.getTarget("generalPage.CLOSE_CHAT_DIALOG"),true);
        }

        switch(fieldLabel.toLowerCase()){
            case METAL_LABEL:
                for(String name : fieldValue.split(",")) {
                    executor.page.clickOnElement(executor.locatorsRepository.getTarget("engagementPage.RING_METAL_FILTER_BUTTON"));
                    String filter = executor.locatorsRepository.getTarget("engagementPage.RING_METAL_FILTER_ITEM:" + name);
                    String filterItem = executor.locatorsRepository.getTarget("engagementPage.CURRENT_RING_FILTER_ITEM:" + name);
                    executor.page.sleepFor(1);
                    if(!metalWasSelected(executor, filterItem)) {
                        executor.page.clickOnElement(filter);
                        executor.page.waitForPageToLoad();
                    }
                }
                break;
            case COLLECTION_LABEL:
                for(String name : fieldValue.split(",")) {
                    executor.page.clickOnElement(executor.locatorsRepository.getTarget("engagementPage.RING_COLLECTION_FILTER_BUTTON"));
                    String filter = executor.locatorsRepository.getTarget("engagementPage.RING_COLLECTION_FILTER_ITEM:" + name);
                    String filterItem = executor.locatorsRepository.getTarget("engagementPage.CURRENT_RING_FILTER_ITEM:" + name);
                    executor.page.sleepFor(1);
                    if(!collectionWasSelected(executor, filterItem)) {
                        executor.page.clickOnElement(filter);
                        executor.page.waitForPageToLoad();
                    }
                }
                break;
            case PRICE_FROM_LABEL:
                executor.page.clickOnElement(executor.locatorsRepository.getTarget("engagementPage.RING_PRICE_FILTER_BUTTON"));
                field = executor.locatorsRepository.getTarget("engagementPage.RING_PRICE_FILTER_FROM");
                executor.page.setTextWithEnter(field, fieldValue, false);
                break;
            case PRICE_TO_LABEL:
                if(!executor.page.isElementDisplayedRightNow(executor.locatorsRepository.getTarget("engagementPage.RING_PRICE_FILTER_TO"), 1))
                    executor.page.clickOnElement(executor.locatorsRepository.getTarget("engagementPage.RING_PRICE_FILTER_BUTTON"));
                field = executor.locatorsRepository.getTarget("engagementPage.RING_PRICE_FILTER_TO");
                executor.page.setTextWithEnter(field, fieldValue, false);
                break;
            default:
                LOGGER.warn("No such field: " + fieldLabel);
        }
    }

    private boolean metalWasSelected(TestStepsExecutor executor, String filter) {
        //if box-shadow == none - unselected
        return executor.page.isElementDisplayedRightNow(filter, 1);
    }

    private boolean collectionWasSelected(TestStepsExecutor executor, String filter) {
        //if box-shadow == none - unselected
        return executor.page.isElementDisplayedRightNow(filter, 1);
    }
}