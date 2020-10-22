package automation.web.dynamic;

import automation.configuration.ProjectConfiguration;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import automation.datasources.FileManager;
import automation.datasources.JSONConverter;

import java.io.IOException;
import java.util.*;

import static automation.web.BasePage.MAIN_TIMEOUT;

/**
 * Basic class to support Dynamic locators <br>
 *     TODO Still on POC stage
 */
public class EtalonObjectProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(EtalonObjectProcessor.class);

    WebDriver driver;

    //etalon object variable
    static final String LABEL_SEARCH_MARK = "LABEL";
    static final String FULL_XPATH_MARK = "FULL";
    static final String LAST_XPATH_MARK = "LAST";
    static final String TAG_MARK = "TAG";
    static final String LOCATION_MARK = "LOCATION";
    static final String ALTERNATIVE_XPATH_MARK = "ALTERNATIVE";

    public EtalonObjectProcessor(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Record Found element as Etalon object (with additional info that may help with searching in future)
     *
     * @param xpath
     * @param foundElement
     */
    public void recordFoundElementAsNewEtalon(String xpath, WebElement foundElement, String successXpath) {

        if (!ProjectConfiguration.isUsingEtalon)
            return;
        TreeMap<String, String> etalonObjectContent = generateEtalonFor(foundElement, successXpath);

        String etalonFileContent = null;
        if (FileManager.doesExist(ProjectConfiguration.getConfigProperty("CurrentEtalonFile")))
            etalonFileContent = FileManager.getFileContent(ProjectConfiguration.getConfigProperty("CurrentEtalonFile"));

        if (etalonFileContent == null) {
            try {
                FileManager.appendToFile(ProjectConfiguration.getConfigProperty("CurrentEtalonFile"), xpath.hashCode() + "=" + JSONConverter.objectToJson(etalonObjectContent));
            } catch (IOException e) {
                LOGGER.error("Etalon recording failed : " + ProjectConfiguration.getConfigProperty("CurrentEtalonFile"));
                e.printStackTrace();
            }
        } else {
            if (etalonFileContent.contains(String.valueOf(xpath.hashCode()))) {
                etalonFileContent = FileManager.replaceStringInFileContent(etalonFileContent, String.valueOf(xpath.hashCode()), xpath.hashCode() + "=" + JSONConverter.objectToJson(etalonObjectContent));
            } else {
                etalonFileContent = etalonFileContent + "\n" + xpath.hashCode() + "=" + JSONConverter.objectToJson(etalonObjectContent);
            }
            try {
                FileManager.writeToFile(ProjectConfiguration.getConfigProperty("CurrentEtalonFile"), etalonFileContent);
            } catch (IOException e) {
                LOGGER.error("Etalon update failed : " + ProjectConfiguration.getConfigProperty("CurrentEtalonFile"));
                e.printStackTrace();
            }
        }

    }

    /**
     * Try to use Etalon object to find new locator for element
     *
     * @param xpath
     * @param condition expected condition
     * @return
     */
    public WebElement tryToFindElementUsingEtalon(String xpath, ExpectedCondition condition) {
        WebElement result = null;

        if (!ProjectConfiguration.isUsingEtalon)
            return null;

        //check if etalon exists
        String etalonFileContent = "";
        if(FileManager.doesExist(ProjectConfiguration.getConfigProperty("CurrentEtalonFile"))) {
            //read current etalon file
            etalonFileContent = FileManager.getFileContent(ProjectConfiguration.getConfigProperty("CurrentEtalonFile"));
        }

        if (etalonFileContent.equals("")) {
            LOGGER.warn("No Etalon file found : " + ProjectConfiguration.getConfigProperty("CurrentEtalonFile"));
        }

        result = searchElementUsingEtalonFileContent(etalonFileContent, xpath, condition);

        return result;
    }

    /**
     * Find element using Etalon file content
     * @param etalonFileContent
     * @param xpath
     * @param condition
     * @return
     */
    private WebElement searchElementUsingEtalonFileContent(String etalonFileContent, String xpath, ExpectedCondition condition) {
        WebElement result = null;

        //find etalon object for current xpath
        String value = "";
        String hashcode = String.valueOf(xpath.hashCode());
        for (String line : etalonFileContent.split("\n")) {
            if (line.startsWith(hashcode)) {
                value = line.replace(hashcode + "=", "");
                break;
            }
        }

        // no items found in Etalon file for current item
        LOGGER.info(xpath + " = " + value);
        if (value.equals("")) {
            LOGGER.warn("No Etalon object found: use original xpath: " + xpath);
            result = findElementForXpath(xpath, condition);
            if(result != null) {
                LOGGER.info("Element was found by original xpath: " + xpath);
                recordFoundElementAsNewEtalon(xpath, result, xpath); // record element to file
            }
            return result;
        }

        //read etalon object for current xpath
        HashMap<String, String> etalonObjectContent = JSONConverter.toHashMapFromJsonString(value);

        // process HARD
        result = getElementFromEtalonObjectContent(etalonObjectContent, xpath);
        return result;
    }

    /**
     * Get element from Etalon object content
     * @param etalonObjectContent
     * @param xpath
     * @return
     */
    private WebElement getElementFromEtalonObjectContent(HashMap<String,String> etalonObjectContent, String xpath) {
        WebElement result = null;

        //try to get item by "previously good" xpath (LAST xpath)
        result = findElementForXpath(etalonObjectContent.get(LAST_XPATH_MARK), null);
        if(result != null) {
            LOGGER.info("Element was found by LAST xpath: " + xpath);
            return result;
        }

        // try alternative XPATH
        result = findElementForXpath(etalonObjectContent.get(ALTERNATIVE_XPATH_MARK), null);
        if(result != null) {
            recordFoundElementAsNewEtalon(xpath, result, etalonObjectContent.get(ALTERNATIVE_XPATH_MARK)); // record element to file
            LOGGER.info("Element was found by ALTERNATIVE xpath: " + xpath);
            return result;
        }

        String successfullXpath = ""; // will be used as LAST xpath in case of success

        HashMap<String, WebElement> candidateElements = new HashMap<>();
        List<WebElement> elements;

        //try different ways of getting element

        //by label
        String label = etalonObjectContent.get(LAST_XPATH_MARK);
        if(!label.equals("")){
            LOGGER.info("Search by Label: " + label);
            String xpathLabel = "//*[text()='" + label + "']";
            elements = driver.findElements(By.xpath(xpathLabel));
            if(elements.size() == 1){ // only element with expected Label was found - TAKE IT
                LOGGER.info("Item with Label was found: " + xpathLabel);
                successfullXpath = xpathLabel;
                result = elements.get(0);
                recordFoundElementAsNewEtalon(xpath, result, successfullXpath) ;
                return result;
            }
            if(elements.size() > 1){// more than one element with expected Label was found - LOOK AT location/tag
                LOGGER.info("Multiple Items with Label was found: " + elements.size());
                for(int i = 0; i<elements.size();i++) {
                    WebElement element = elements.get(i);
                    LOGGER.info("Item with Label,Tag and Location was found: " + successfullXpath);
                    String location = element.getLocation().getX() + "/" + element.getLocation().getY() +"/"+element.getSize().getWidth() +"/"+element.getSize().getHeight();
                    String tagName = element.getTagName();
                    if(location.equals(etalonObjectContent.get(LOCATION_MARK)) && tagName.equals(tagName)){
                        successfullXpath = xpathLabel+"["+(i+1)+"]";
                        result = element;
                        recordFoundElementAsNewEtalon(xpath, result, successfullXpath) ;
                        LOGGER.info("Item with Label,Tag and Location was found: " + tagName + "\n" + location + "\n" + successfullXpath );
                        return result;
                    } else {
                        candidateElements.put( xpathLabel+"["+(i+1)+"]",element);
                    }
                }
            }
        }

        // by parent
        String fullXpath = etalonObjectContent.get(FULL_XPATH_MARK);
        String[] allParents = fullXpath.split("/");

        //for each parent
        for(int curParentIndex = allParents.length-1; curParentIndex == 0 ;curParentIndex--){
            StringBuilder xpathLabel = new StringBuilder();
            for(int i = 0; i > curParentIndex-1; i++){
                xpathLabel.append("/").append(allParents[i]);
            }
            xpathLabel.append("/*"); // get all children
            LOGGER.info("Search by parent: " + xpathLabel.toString());
            elements = driver.findElements(By.xpath(xpathLabel.toString()));

            if(elements.size() == 1){ // only one child found - take it
                LOGGER.info("Element was found by parent: " + xpathLabel.toString());
                successfullXpath = xpathLabel.toString();
                result = elements.get(0);
                recordFoundElementAsNewEtalon(xpath, result, successfullXpath) ;
                return result;
            }
            if(elements.size() > 1){// more than one element with parent was found - LOOK AT location/tag
                LOGGER.info("Multiple Elements were found by parent: " + elements.size());
                for(int i = 0; i<elements.size();i++) {
                    WebElement element = elements.get(i);
                    String location = element.getLocation().getX() + "/" + element.getLocation().getY() +"/"+element.getSize().getWidth() +"/"+element.getSize().getHeight();
                    String tagName = element.getTagName();
                    if(location.equals(etalonObjectContent.get(LOCATION_MARK)) && tagName.equals(tagName)) {
                        successfullXpath = xpathLabel+"["+(i+1)+"]";
                        result = element;
                        recordFoundElementAsNewEtalon(xpath, result, successfullXpath) ;
                        LOGGER.info("Element with Parent ,Tag and Location was found: " + tagName + "\n" + location + "\n" + successfullXpath );
                        return result;
                    } else {
                        candidateElements.put( xpathLabel+"["+(i+1)+"]",element);
                    }
                }
                break; // candidates were found
            }
            //if(elements.size() == 0) // take next parent
        }

        //candidates analysis
        result = compareCandidateElements(etalonObjectContent, candidateElements, xpath);
        return result;
    }

    private WebElement compareCandidateElements(HashMap<String, String> etalonObjectContent, HashMap<String, WebElement> candidateElements, String xpath) {
        WebElement result = null;

        HashMap<String, Integer> score = new HashMap<>();
        LOGGER.info("Candidates to process: " + candidateElements.size());

        for(String key : candidateElements.keySet()){
            WebElement element = candidateElements.get(key);
            score.put(key, 0);
            //check for similar items (different xpath - same element)
            for(String anotherKey : candidateElements.keySet()){
                if(element.equals(candidateElements.get(anotherKey))){
                    LOGGER.info("Elements are equal: " + key);
                    score.put(key, score.get(key) + 2); // increment score  if same element was found
                }
            }

            if(element.getTagName().equals(etalonObjectContent.get(TAG_MARK))){
                score.put(key, score.get(key) + 1);
                LOGGER.info("Tag is same for: " + key + "\n" + element.getTagName());
            }

            String location = element.getLocation().getX() + "/" + element.getLocation().getY() +"/"+element.getSize().getWidth() +"/"+element.getSize().getHeight();
            if(location.equals(etalonObjectContent.get(LOCATION_MARK))){
                score.put(key, score.get(key) + 1);
                LOGGER.info("Location is same for: " + key + "\n" + location);
            }
        }

        String resultKey = "";
        Integer resultScore = 0;
        for(String key : score.keySet()){
            if(resultKey.equals("")){
                resultKey = key;
                resultScore = score.get(key);
            } else{
                Integer newScore = score.get(key);
                if(newScore > resultScore){
                    resultKey = key;
                    resultScore = newScore;
                }
            }
        }

        LOGGER.info("Found Candidate: " + resultKey);
        recordFoundElementAsNewEtalon(xpath, candidateElements.get(resultKey), resultKey) ;

        return result;
    }

    /**
     * Get element for Xpath
     * @param xpath
     * @param condition
     * @return
     */
    private WebElement findElementForXpath(String xpath, ExpectedCondition condition) {
        WebElement result = null;
        try { // happy path
            if (condition != null)
                (new WebDriverWait(driver, MAIN_TIMEOUT)).until(condition);
            result = driver.findElement(By.xpath(xpath));
            return result;
        }catch (Exception e){
            LOGGER.trace("Element was not found: " + xpath + "(" + e.getMessage() + "\n" + e.toString() +")");
            return null;
        }
    }

    /**
     * Generate Etalon object
     * @param foundElement
     * @param successXpath
     * @return
     */
    private TreeMap<String, String> generateEtalonFor(WebElement foundElement, String successXpath) {
        TreeMap<String, String> etalonObject = new TreeMap<String, String>();

        etalonObject.put(LAST_XPATH_MARK, successXpath);

        etalonObject.put(TAG_MARK, foundElement.getTagName());

        etalonObject.put(LABEL_SEARCH_MARK, foundElement.getText());

        etalonObject.put(LOCATION_MARK, foundElement.getLocation().getX() + "/" + foundElement.getLocation().getY() +"/"+foundElement.getSize().getWidth() +"/"+foundElement.getSize().getHeight());

        String alternativexpath = (String) ((JavascriptExecutor) driver).executeScript(SCRIPT_TO_FIND_XPATH, foundElement);
        etalonObject.put(ALTERNATIVE_XPATH_MARK, alternativexpath);

        String fullxpath = (String) ((JavascriptExecutor) driver).executeScript(SCRIPT_TO_FIND_FULL_XPATH, foundElement);
        etalonObject.put(FULL_XPATH_MARK, fullxpath);

        return etalonObject;
    }

    String SCRIPT_TO_FIND_XPATH = " " +
"function createXPathFromElement(originalElement) {\n" +
            "        var allNodes = document.getElementsByTagName('*');\n" +
            "        var curElement = originalElement;\n" +
            "        for (var pathParts = []; curElement && curElement.nodeType == 1; curElement = curElement.parentNode)\n" +
            "        {\n" +
            "            if (curElement.hasAttribute('id')) {\n" +
            "                pathParts.unshift(curElement.localName.toLowerCase() + '[@id=\"' + curElement.getAttribute('id') + '\"]');\n" +
            "                break;\n" +
            "            }\n" +
            "            if (curElement.hasAttribute('name')) {\n" +
            "                pathParts.unshift(curElement.localName.toLowerCase() + '[@name=\"' + curElement.getAttribute('name') + '\"]');\n" +
            "                break;\n" +
            "            }\n" +
            "            if (curElement.innerText != \"\" && curElement.textContent.trim().length < 50) {\n" +
            "                console.log(\"Text\")\n" +
            "                pathParts.unshift(curElement.localName.toLowerCase() + '[contains(text(),\"' + curElement.textContent.trim() + '\")]');\n" +
            "                if(lookupElementByXPath('//' + pathParts.join('/')) == 0){\n" +
            "                    pathParts.shift(); // remove first item - it was bad\n" +
            "                    console.log(\"Bad Text\")\n" +
            "\n" +
            "                    if (curElement.hasAttribute('class')) {\n" +
            "                            console.log(\"Class after Text\")\n" +
            "                          pathParts.unshift(curElement.localName.toLowerCase() + '[@class=\"' + curElement.getAttribute('class') + '\"]');\n" +
            "                    } else {\n" +
            "                        console.log(\"Index after Text\")\n" +
            "                          for (i = 1, sib = curElement.previousSibling; sib; sib = sib.previousSibling) {\n" +
            "                               if (sib.localName == curElement.localName)  i++;\n" +
            "                          };\n" +
            "                          pathParts.unshift(curElement.localName.toLowerCase() + '[' + i + ']');\n" +
            "                    };\n" +
            "                }\n" +
            "            } else\n" +
            "            if (curElement.hasAttribute('class')) {\n" +
            "                console.log(\"Class\")\n" +
            "                pathParts.unshift(curElement.localName.toLowerCase() + '[@class=\"' + curElement.getAttribute('class') + '\"]');\n" +
            "            } else {\n" +
            "                console.log(\"Index\")\n" +
            "                for (i = 1, sib = curElement.previousSibling; sib; sib = sib.previousSibling) {\n" +
            "                    if (sib.localName == curElement.localName)  i++;\n" +
            "                };\n" +
            "                pathParts.unshift(curElement.localName.toLowerCase() + '[' + i + ']');\n" +
            "            };\n" +
            "            if(lookupElementByXPath('//' + pathParts.join('/')) == 1){\n" +
            "                return '//' + pathParts.join('/')\n" +
            "            }\n" +
            "        };\n" +
            "        // final processing (adding index if more than 1 was found)\n" +
            "        if(lookupElementByXPath('//' + pathParts.join('/')) == 1)\n" +
            "            return pathParts.length ? '//' + pathParts.join('/') : null;\n" +
            "        else\n" +
            "            for(var i = 1; i < lookupElementByXPath('//' + pathParts.join('/')) + 1 ; i++){\n" +
            "                var xpathWithIndex = '(//' + pathParts.join('/') + ')[' + i + ']'\n" +
            "                if(getElementByXPath(xpathWithIndex) == originalElement)\n" +
            "                    return xpathWithIndex;\n" +
            "            }\n" +
            "    };" +
            "\n" +
            "// get number of elements described by XPATH\n" +
            "function lookupElementByXPath(path) {\n" +
            "        var evaluator = new XPathEvaluator();\n" +
            "        var result = evaluator.evaluate(path, document.documentElement, null, XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null);\n" +
            "        console.log(path)\n" +
            "        console.log(result.snapshotLength)\n" +
            "        return  result.snapshotLength;\n" +
            "    }\n" +
            "\n" +
            "function getElementByXPath(path) {\n" +
            "        var evaluator = new XPathEvaluator();\n" +
            "        var result = evaluator.evaluate(path, document.documentElement, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null);\n" +
            "        console.log(path)\n" +
            "        console.log(result.singleNodeValue)\n" +
            "        return  result.singleNodeValue;\n" +
            "    }" +
            "return createXPathFromElement(arguments[0])     ;";


    String SCRIPT_TO_FIND_FULL_XPATH = "elm =  arguments[0];\n" +
            "        var finalresultxpath = \"\";\n" +
            "        var allNodes = document.getElementsByTagName('*');\n" +
            "        for (var segs = []; elm.localName.toLowerCase() != \"body\"; elm = elm.parentNode)\n" +
            "        {\n" +
            "            resultxpath = \"/\" + elm.localName.toLowerCase();\n" +
            "            if (elm.hasAttribute('id')) {\n" +
            "                 resultxpath = resultxpath + '[@id=\"' + elm.getAttribute('id') + '\"]'\n" +
            "            } if (elm.hasAttribute('name')) {\n" +
            "                 resultxpath = resultxpath + '[@name=\"' + elm.getAttribute('name') + '\"]'\n" +
            "            } if (elm.hasAttribute('class')) {\n" +
            "                resultxpath = resultxpath + '[@class=\"' + elm.getAttribute('class') + '\"]'\n" +
            "            }\n" +
            "                for (i = 1, sib = elm.previousSibling; sib; sib = sib.previousSibling) {\n" +
            "                    if (sib.localName == elm.localName)  i++;\n" +
            "                };\n" +
            "                resultxpath = resultxpath + '[' + i + ']';\n" +
            "\n" +
            "                finalresultxpath = resultxpath + finalresultxpath\n" +
            "        };\n" +
            "        return \"/html/body\" + finalresultxpath;\n";

}
