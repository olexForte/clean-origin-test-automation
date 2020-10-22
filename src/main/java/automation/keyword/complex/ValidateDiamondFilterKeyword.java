package automation.keyword.complex;

import automation.annotations.KeywordRegexp;
import automation.entities.application.DiamondsFilter;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static automation.entities.application.DiamondsFilter.*;

/**
 * Set diamonds filters
 */
public class ValidateDiamondFilterKeyword extends AbstractKeyword {

    @KeywordRegexp("Validate diamond filter 'dataJsonField' [return 0];")
    static String LABEL = "validate diamond filter";

    Integer expectedNumberOfDiamonds = -1;
    String MARKER_OF_EXPECTED_NUMBER_REGEXP = ".*return (\\d+).*";

    //fields
    String lowerScroller;
    String upperScroller;

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if (prepareLine(line).toLowerCase().matches(LABEL.toLowerCase() + ".*")) {
            ValidateDiamondFilterKeyword result = (ValidateDiamondFilterKeyword) super.generateFromLine(line);

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
        DiamondsFilter filter = new DiamondsFilter(allFields);

        List<HashMap<String, String>> allDiamondsOnPage;

        int iCurrentPage = 0;
        int iMaxPage = 3;
        if(target != null && !target.equals(""))
            iMaxPage = Integer.valueOf(target);

        executor.page.scrollToElement(executor.locatorsRepository.getTarget("diamondsPage.FOOTER"));

        boolean result = true;
        while( iCurrentPage < iMaxPage){
            iCurrentPage++;
            allDiamondsOnPage = executor.page.getComplexObject(executor.locatorsRepository.getComplexTarget("diamondsPage.DIAMONDS_TABLE_STRUCTURE"));
            LOGGER.info("Number of diamonds on page: " + iCurrentPage + " " +  allDiamondsOnPage.size());
            if(expectedNumberOfDiamonds != -1) {
                if (expectedNumberOfDiamonds == allDiamondsOnPage.size()) {
                    executor.reporter.pass("Number of diamonds on page " + allDiamondsOnPage.size() + " equals to expected " + expectedNumberOfDiamonds);
                }else {
                    executor.reporter.fail("Number of diamonds on page " + allDiamondsOnPage.size() + " does not equal to expected " + expectedNumberOfDiamonds);
                    return false;
                }
            } else {
                if (allDiamondsOnPage.size() == 0) {
                    executor.reporter.failWithScreenshot("No diamonds on page");
                    return false;
                }
            }

            result = result && validateFilterWasAppliedToEntities(allDiamondsOnPage, filter);
            if(executor.page.isElementDisplayedRightNow(executor.locatorsRepository.getTarget("diamondsPage.NEXT_BUTTON"), 0))
                executor.page.clickOnElement(executor.locatorsRepository.getTarget("diamondsPage.NEXT_BUTTON"));
            else
                break;
        }

        if(result) {
            executor.reporter.pass("Filter was applied successfully: " + filter.toString());
        } else {
            executor.reporter.failWithScreenshot("Filter was not applied successfully: " + filter.toString());
        }

        return result;
    }

    private boolean validateFilterWasAppliedToEntities(List<HashMap<String, String>> allDiamondsOnPage, DiamondsFilter filter) {
        boolean result = true;
        for (HashMap<String, String> currentDiamond : allDiamondsOnPage) {

            result = result && filter.isShapesInFilter(currentDiamond.get("shape").toUpperCase());

            if(!currentDiamond.get("weight").equals(""))
                result = result && filter.isCaratInFilterRange(currentDiamond.get("weight"));

            if(!currentDiamond.get("color").equals(""))
                result = result && filter.isColorInFilterRange(currentDiamond.get("color"));

            if(!currentDiamond.get("clarity").equals(""))
                result = result && filter.isClarityInFilterRange(currentDiamond.get("clarity"));

            if(!currentDiamond.get("grade").equals(""))
                result = result && filter.isCutInFilterRange(currentDiamond.get("grade").toUpperCase().replace(" ","_"));

            if(!currentDiamond.get("price").equals(""))
                result = result && filter.isPriceInFilterRange(currentDiamond.get("price").replace("$", "").replace(",", ""));

            if (!result) {
                LOGGER.error("Diamond does not match filter: " + currentDiamond.toString());
                return result;
            }
        }

        return result;
    }

}