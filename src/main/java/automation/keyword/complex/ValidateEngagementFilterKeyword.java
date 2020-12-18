package automation.keyword.complex;

import automation.annotations.KeywordRegexp;
import automation.entities.application.RingFilter;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;
import io.restassured.response.Response;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Set diamonds filters
 */
public class ValidateEngagementFilterKeyword extends AbstractKeyword {

    @KeywordRegexp("Validate engagement filter 'dataJsonField' [return 0];")
    static String LABEL = "validate engagement filter";

    Integer expectedNumberOfDiamonds = -1;
    String MARKER_OF_EXPECTED_NUMBER_REGEXP = ".*return (\\d+).*";

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if (prepareLine(line).toLowerCase().matches(LABEL.toLowerCase() + ".*")) {
            ValidateEngagementFilterKeyword result = (ValidateEngagementFilterKeyword) super.generateFromLine(line);

            Pattern p = Pattern.compile("(('(.*?)')|(\"(.*?)\"))");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.data = line.substring(matcher.start() + 1, matcher.end() - 1);
            if(matcher.find()) // number of pages
                result.target = line.substring(matcher.start() + 1, matcher.end() - 1);

            if(line.matches(MARKER_OF_EXPECTED_NUMBER_REGEXP))
                result.expectedNumberOfDiamonds = Integer.valueOf(line.replaceAll(MARKER_OF_EXPECTED_NUMBER_REGEXP, "$1"));
            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {
        HashMap<String, String> allFields = executor.testDataRepository.getComplexData(data);
        RingFilter filter = new RingFilter(allFields);

        Map<String,String> authorizedCookies = executor.api.loginToAdminAndGetCookies(executor.locatorsRepository.getTarget("apiEndpoints.ADMIN_URL"));

        int iCurrentPage = 0;
        int iMaxPage = 2; // todo
        if(target != null && !target.equals(""))
            iMaxPage = Integer.valueOf(target);

        boolean finalResult = true;
        while( iCurrentPage < iMaxPage){
            iCurrentPage++;
            executor.page.waitForPageToLoad();
            int numOfItemsOnPage = executor.page.findElementsIgnoreException(executor.locatorsRepository.getTarget("engagementPage.RING_ITEMS_LINKS"), 1).size();

            for( int i = 0 ; i < numOfItemsOnPage;i++){
                boolean result = true;
                LOGGER.info("Check Ring " + i);
                WebElement ring = executor.page.findElementsIgnoreException(executor.locatorsRepository.getTarget("engagementPage.RING_ITEMS_IDS"), 1).get(i);
                String ringID = ring.getAttribute("data-product-id");
                LOGGER.info("Ring ID: " + ringID);
                result = result && validateItemMatchesFilterUsingAPI(executor, authorizedCookies, ringID, filter);

                if(executor.page.isElementDisplayedRightNow(executor.locatorsRepository.getTarget("generalPage.CLOSE_MODAL_DIALOG"), 0))
                    executor.page.clickOnElement(executor.locatorsRepository.getTarget("generalPage.CLOSE_MODAL_DIALOG"), true);

                WebElement ringLink = executor.page.findElementsIgnoreException(executor.locatorsRepository.getTarget("engagementPage.RING_ITEMS_LINKS"), 1).get(i);
                String ringName = ringLink.getText();
                result = result && validateItemMatchesFilterUsingUI(executor, ringLink, filter);

                finalResult = finalResult && result;
                if(!result)
                    executor.reporter.failWithScreenshot("Ring does not match filter " + ringName + " " + ringID);
                else
                    executor.reporter.pass("Ring matches filter " + ringName + " " + ringID);
            }

            //if(!result)
            //    break;

            LOGGER.info("Opening next page " + iCurrentPage);
            if(executor.page.isElementDisplayedRightNow(executor.locatorsRepository.getTarget("engagementPage.NEXT_BUTTON"), 0)) {
                if(executor.page.isElementDisplayedRightNow(executor.locatorsRepository.getTarget("generalPage.CLOSE_MODAL_DIALOG"), 0))
                    executor.page.clickOnElement(executor.locatorsRepository.getTarget("generalPage.CLOSE_MODAL_DIALOG"), true);
                executor.page.clickOnElement(executor.locatorsRepository.getTarget("engagementPage.NEXT_BUTTON"));
            } else
                break;
        }

        if(finalResult) {
            executor.reporter.pass("Filter was applied successfully: " + filter.toString());
        } else {
            executor.reporter.failWithScreenshot("Filter was not applied successfully: " + filter.toString());
        }
        return finalResult;
    }

    private boolean validateItemMatchesFilterUsingAPI(TestStepsExecutor executor, Map<String,String> authorizedCookies, String id, RingFilter filter) throws Exception {

        String url = executor.locatorsRepository.getTarget("apiEndpoints.ADMIN_PRODUCT_SEARCH_BY_ID_URL:" + id);
        Response response = executor.api.getRequest( url, null, authorizedCookies);
        if(response == null || response.asString().equals("")) {
            LOGGER.error("Item was not found " + id);
            return false;
        }
        String allData = response.asString().replace("\n","").replaceAll(".*\"items\":\\[(.*?)\\]},\"update_url\".*", "$1");
        if(allData.equals("")) {
            LOGGER.error("Item was not found " + id);
            return false;
        }
        return checkDataCorrespondToDataFilter(allData, filter);

    }

    private boolean checkDataCorrespondToDataFilter(String allData, RingFilter filter) {
        RingFilter.RingCollection collection = RingFilter.RingCollection.valueFor(allData.replaceFirst(".*ring_collection\":\"(.*?)\".*","$1"));
        RingFilter.RingMetal metal = RingFilter.RingMetal.idFor(allData.replaceFirst(".*metal_type\":\"(.*?)\".*","$1"));
        String price = null;
        if (allData.contains("price"))
            price = allData.replaceFirst(".*price\":\"(.*?)\".*","$1");
//"type_id": "configurable",
        boolean result = true; //todo  ((metal != null) && filter.isMetalsInFilter(metal.name())) || allData.contains("\"configurable\"");
        result = result && (collection != null && filter.isCollectionsInFilter(collection.name()));
        if(price != null)
         result = result && filter.isPriceInFilterRange(price);

        if(!result)
            LOGGER.error("Ring does not match filter: " + collection + " " + metal + " " + price + "\n" + allData);

        return result;
    }

    boolean validateItemMatchesFilterUsingUI(TestStepsExecutor executor, WebElement ringLink, RingFilter filter) throws Exception {
        boolean result = true;

        if(executor.page.isElementDisplayedRightNow(executor.locatorsRepository.getTarget("generalPage.CLOSE_MODAL_DIALOG"), 0))
            executor.page.clickOnElement(executor.locatorsRepository.getTarget("generalPage.CLOSE_MODAL_DIALOG"), true);
        if(executor.page.isElementDisplayedRightNow(executor.locatorsRepository.getTarget("generalPage.CLOSE_CHAT_DIALOG"), 0))
            executor.page.clickOnElement(executor.locatorsRepository.getTarget("generalPage.CLOSE_CHAT_DIALOG"), true);

        executor.page.hoverElement(ringLink,true);
        ringLink.click();
        executor.page.waitForPageToLoad();

        String sku = executor.page.getText(executor.page.findDynamicElement(executor.locatorsRepository.getTarget("engagementPage.RING_SKU_LABEL")), true);
        String name = executor.page.getText(executor.page.findDynamicElement(executor.locatorsRepository.getTarget("engagementPage.RING_NAME_LABEL")), true);
        String shape = executor.page.getText(executor.page.findDynamicElement(executor.locatorsRepository.getTarget("engagementPage.RING_SHAPE_SELECT")), true);
        String metal = executor.page.getText(executor.page.findDynamicElement(executor.locatorsRepository.getTarget("engagementPage.RING_METAL_SELECT")), true);
        String prong = executor.page.getText(executor.page.findDynamicElement(executor.locatorsRepository.getTarget("engagementPage.RING_PRONG_SELECT")), true);
        String size = executor.page.getText(executor.page.findDynamicElement(executor.locatorsRepository.getTarget("engagementPage.RING_SIZE_SELECT")), true);
        String price = executor.page.getText(executor.page.findDynamicElement(executor.locatorsRepository.getTarget("engagementPage.RING_PRICE_LABEL")), true);

        if (!metal.equals("Select Option"))
            result = result && filter.isMetalsInFilter(metal);
        //result = result && filter.isCollectionsInFilter(shape);
        result = result && filter.isPriceInFilterRange(price);
        if(size == null || size.equals("") ||  prong == null || prong.equals("")) {
            result = false;
            LOGGER.error("Ring does not have size/prong " + sku);
        }

        if(!result) {
            LOGGER.error("Ring does not match filter: " + sku + " " + name + " " + metal + " " + shape + " " + prong + " " + price + "  does not match with filter " + filter );
        }

        executor.page.goBack();

        return result;
    }
}