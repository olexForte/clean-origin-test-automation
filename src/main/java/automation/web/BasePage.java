package automation.web;

import automation.configuration.ProjectConfiguration;
import automation.configuration.SessionManager;
import automation.datasources.FileManager;
import automation.execution.TestsExecutor;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import automation.reporting.ReporterManager;
import automation.web.dynamic.EtalonObjectProcessor;
import automation.datasources.RandomDataGenerator;
import automation.web.visual.GoogleDriveImageProvider;
import automation.web.visual.ImageComparator;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * Base class for Page objects
 */
public class BasePage {
    // main logger
    private static final Logger LOGGER = LoggerFactory.getLogger(BasePage.class);
    // performance logger
    private static final Logger analytics = LoggerFactory.getLogger("analytics");
    //reporter
    static public ReporterManager reporter = ReporterManager.Instance;
    
    private static int DEFAULT_TIMEOUT          = 15;
    private static int DEFAULT_SHORT_TIMEOUT    = 3;
    private static int DEFAULT_STATIC_TIMEOUT   = 500;

    public String pageURL = "";
    public String pageTitle = "";

    public static ThreadLocal<WebDriver> driver = new ThreadLocal<WebDriver>();

    public static final int MAIN_TIMEOUT = getTimeout();
    public static final int SHORT_TIMEOUT = getShortTimeout();
    public static final int STATIC_TIMEOUT =  getStaticTimeout();

    public String DROPDOWN_SEPARATOR_VALUE = ";";

    private final static String FRAME_PROCESSING_MARKER = "//frame";
    private final static String SPECIFIED_FRAME_PROCESSING_MARKER = "//iframe[";
    private final static String DIALOG_PROCESSING_MARKER = "//dialog";
    private static final String ACCEPT_DIALOG_MARKER = "//OK";
    private static final String DISMISS_DIALOG_MARKER = "//CANCEL";

    private static int getTimeout() {
        String timeout = ProjectConfiguration.getConfigProperty("DefaultTimeoutInSeconds");
        if (timeout == null ) {
            reporter.fail("DefaultTimeoutInSeconds parameter was not found");
            return DEFAULT_TIMEOUT;
        }

        return Integer.parseInt(timeout);
    }

    private static int getShortTimeout() {
        String timeout = ProjectConfiguration.getConfigProperty("ShortTimeoutInSeconds");
        if (timeout == null ) {
            return DEFAULT_SHORT_TIMEOUT;
        }

        return Integer.parseInt(timeout);
    }

    private static int getStaticTimeout() {
        String timeout = ProjectConfiguration.getConfigProperty("StaticTimeoutMilliseconds");
        if (timeout == null ) {
            return DEFAULT_STATIC_TIMEOUT;
        }
        return Integer.parseInt(timeout);
    }

    //object that helps with processing of dynamic locators
    EtalonObjectProcessor etalonObjectProcessor;

    public BasePage() {
        // waitForPageToLoad();
        //etalonObjectProcessor = new EtalonObjectProcessor(driver());
    }

    public static WebDriver driver(){
        return driver.get();
    }

    /**
     * Stop Driver
     */
    public static void stopDriver(){
        driver().quit();
        driver.set(null);
    }

    /**
     * Setup driver before opening page
     */
    public void setupDriver(){
        if(driver() == null) {
            try {
                reporter.info("Driver creation");
                driver.set(DriverProvider.getDriver(reporter.TEST_NAME.get())); // for BS / SL drivers

                reporter.info("Driver created " + BasePage.driver.get().hashCode());
            } catch (Exception e) {
                reporter.failWithScreenshot("Before test failure during Driver creation", e);
                reporter.stopReporting();
                reporter.closeReporter();
                Assert.fail();
            }
        } else { // try to "clean" session
            driver().manage().deleteAllCookies();
            //driver().close();
            //driver().get("about://blank");
        }

        //setup Etalon if required
        if(ProjectConfiguration.isUsingEtalon)
            etalonObjectProcessor = new EtalonObjectProcessor(driver());
    }

    /**
     * Activate frame
     * @param xpath
     */
    public void switchToFrame(String xpath) throws Exception {
        reporter.info("Switch to frame: " + xpath);
        driver().switchTo().frame(findDynamicElement(xpath));
    }

    /**
     * Activate default frame
     */
    public void switchToDefaultContent(){
        reporter.info("Switch to default content");
        driver().switchTo().defaultContent();
    }

    /**
     * Go on previous page
     */
    public void goBack(){
        driver().navigate().back();
    }

    /**
     * Open next tab
     */
    public void openNextTab() {
        boolean idWasFoundOnPreviousIteration = false;
        String currentWindowID = driver().getWindowHandle();
        Object[] allWindowIDS = driver().getWindowHandles().toArray();
        for(Object id : allWindowIDS){
            if(idWasFoundOnPreviousIteration){
                driver().switchTo().window((String)id);
                return;
            }
            if ((id).equals(currentWindowID)){
                idWasFoundOnPreviousIteration = true;
            }
        }
        LOGGER.error("Next tab was not open");
    }

    /**
     * Sleep from timeout
     * @param timeout
     */
    public void sleepFor(int timeout){
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
        }
    }

    /**
     * Wait for Browser alert
     * @param driver
     * @param timeout
     */
    void waitForAlert(WebDriver driver, int timeout) {
        int i = 0;
        while (i++ < timeout) {
            try {
                Alert alert = driver.switchTo().alert();
                break;
            } catch (NoAlertPresentException e)  // wait for second
            {
                sleepFor(1);
                continue;
            }
        }
    }

    /**
     * Wait until page is completely downloaded
     */
    public void waitForPageToLoad() {
        sleepFor(STATIC_TIMEOUT);
        ExpectedCondition<Boolean> expectationReadyState = new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver driver) {
                String result = (String)((JavascriptExecutor) driver()).executeScript("return document.readyState");
                LOGGER.info("Page loading status - " + result );
                return result.equals("complete");
            }
        };

        Wait<WebDriver> waitReadyState = new WebDriverWait(driver(), MAIN_TIMEOUT);

        try {
            if(!waitReadyState.until(webDriver -> expectationReadyState.apply(webDriver)))
            //if(!waitReadyState.until(expectationReadyState))
                reporter.info("JavaScript readyState query timeout - The page has not finished loading");
        } catch (Exception error) {
            //reporter.failWithScreenshot("JavaScript readyState query timeout - The page has not finished loading");
            reporter.info("The page has not finished loading: " + error.getMessage());
        }

    }

    /**
     * Check if page is loaded
     * @return
     */
    public boolean isPageLoaded() {
        boolean result = false;
        reporter.info("Page title is: " + driver().getTitle());
        reporter.info("Page URL is: " + driver().getCurrentUrl());
        if (driver().getTitle().contains(pageTitle))
            result = true;
        else {
            reporter.info("Expected title: " + pageTitle);
            result = false;
        }

        if (driver().getCurrentUrl().contains(pageURL))
            result = true;
        else {
            reporter.info("Expected URL: " + pageURL);
            result = false;
        }

        return result;
    }

    /**
     * Refresh page
     */
    public void reloadPage() {
        driver().navigate().refresh();
    }

    /**
     * Open URL
     * @param url
     */
    public void open(String url) {

        reporter.info("Opening the page: " + "\"" + url + "\"");
        driver().get(url);

        if(ProjectConfiguration.isPerformanceProfilingRequired)
            getPerformanceInfo();

        String urlCurrent = driver().getCurrentUrl();
        if(!urlCurrent.equals(url))
            driver().get(url);
    }

    //TODO performance - should be  refactored

    /**
     * Get basic performance info after page loading
     */
    private void getPerformanceInfo() {
        long navigationStart = Long.valueOf((Long) ((JavascriptExecutor) driver()).executeScript("return window.performance.timing.navigationStart"));
        long responseStart = Long.valueOf((Long) ((JavascriptExecutor) driver()).executeScript("return window.performance.timing.responseStart"));
        long domComplete = Long.valueOf((Long) ((JavascriptExecutor) driver()).executeScript("return window.performance.timing.domComplete"));

        long backendPerformance_calc = responseStart - navigationStart;
        long frontendPerformance_calc = domComplete - responseStart;

        analytics.info( ReporterManager.Instance.TEST_NAME.get() + " - Back End: " + backendPerformance_calc + " ms");
        analytics.info( ReporterManager.Instance.TEST_NAME.get() + " - Front End: " + frontendPerformance_calc + " ms");
    }


    /**
     * Close tab/browser
     */
    public void close() {
        reporter.info("Closing current tab/browser");
        driver().close();
    }


    /**
     * Find element by xpath
     * @param xpath
     * @param condition
     * @param timeout
     * @return
     */
    public WebElement findDynamicElement(String xpath, ExpectedCondition condition, int... timeout) {
        int timeoutForFindElement = timeout.length < 1 ? MAIN_TIMEOUT : timeout[0];
        if(timeoutForFindElement != 0)
            waitForPageToLoad();
        try {
            return dynamicFind(xpath, condition, timeoutForFindElement);
        } catch (Exception e) {
            LOGGER.warn("Find element failed:" + xpath + "\n" + e.getMessage() + "\n" + e.toString());
            return null;
        }
    }

    /**
     * Find element by xpath
     * @param xpath
     * @param timeout
     * @return
     */
    public WebElement findDynamicElement(String xpath,  int... timeout) {
        return findDynamicElement(xpath, ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)), timeout);
    }


    /**
     * Find element using By (DEPRECATED)
     * @param element
     * @param timeout
     * @return
     */
    @Deprecated
    public WebElement findElement(By element, int... timeout) throws Exception {
        int timeoutForFindElement = timeout.length < 1 ? MAIN_TIMEOUT : timeout[0];
        waitForPageToLoad();
        try {
            //synchronize();
            (new WebDriverWait(driver(), timeoutForFindElement))
                    //.until(ExpectedConditions.visibilityOfElementLocated(element));
                    //.until(ExpectedConditions.presenceOfElementLocated(element));
                    .until(webDriver ->ExpectedConditions.presenceOfElementLocated(element).apply(webDriver));
            return driver().findElement(element);
        } catch (Exception e) {
            throw new Exception("Failure finding element." , e);
        }
    }

    /**
     * Find element using By and ignore exception
     * @param element
     * @param timeout
     * @return
     */
    @Deprecated
    public WebElement findElementIgnoreException(By element, int... timeout) {
        int timeoutForFindElement = timeout.length < 1 ? MAIN_TIMEOUT : timeout[0];
        waitForPageToLoad();
        try {
            //synchronize();
            (new WebDriverWait(driver(), timeoutForFindElement))
                    //.until(ExpectedConditions.visibilityOfElementLocated(element));
                    .until(webDriver ->ExpectedConditions.visibilityOfElementLocated(element).apply(webDriver));
            return driver().findElement(element);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Find elements (does not support dynamic locators)
     * @param element
     * @param timeout
     * @return
     */
    public List<WebElement> findElementsIgnoreException(String element, int... timeout) {
        //waitForPageToLoad();
        int timeoutForFindElement = timeout.length < 1 ? MAIN_TIMEOUT : timeout[0];
        waitForPageToLoad();
        try {
            //synchronize();
            (new WebDriverWait(driver(), timeoutForFindElement))
                    .until(ExpectedConditions.presenceOfElementLocated(By.xpath(element)));
                    //.until(webDriver ->ExpectedConditions.presenceOfElementLocated(By.xpath(element)).apply(webDriver));

            return driver().findElements(By.xpath(element));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Simple click
     * @param xpath
     */
    public void clickOnElement(String xpath){
        LOGGER.info("Click on " + xpath);
        driver().findElement(By.xpath(xpath)).click();
    }

    /**
     * Click on element
     * @param xpath
     * @param ignoreException
     * @return
     * @throws Exception possible exception
     */
    public boolean clickOnElement(String xpath, boolean ignoreException) throws Exception {
        LOGGER.info("Click on " + xpath);
        WebElement element;
        try {
            element = findDynamicElement(xpath, ignoreException?getShortTimeout():getTimeout());
            scrollToElement(element);
            if (element.isDisplayed()) {
//                if(ProjectConfiguration.isIE())
//                    clickOnElementUsingJS(element);
//                else
                element.click();
                //clickOnElementUsingJS(element);
            } else
            if(ignoreException)
                return false;
            else
                throw new Exception("Failure clicking on element - element not visible: " + xpath);
            waitForPageToLoad();
            return true;
        }
        catch (ElementClickInterceptedException e1) {
            element = findDynamicElement(xpath, ignoreException?getShortTimeout():getTimeout());
            LOGGER.info("Click on element using JS");
            if(clickOnElementUsingJS(element)) {
                return true;
            } else {
                if(ignoreException)
                    return false;
                else
                    throw new Exception("Failure clicking on element: " + xpath, e1);
            }
        }
        catch (WebDriverException e2) {
            if(e2.getMessage().contains("element click intercepted")) {
                element = findDynamicElement(xpath, ignoreException ? getShortTimeout() : getTimeout());
                LOGGER.info("Click on element using JS");
                if (clickOnElementUsingJS(element)) {
                    return true;
                } else {
                    if (ignoreException)
                        return false;
                    else
                        throw new Exception("Failure clicking on element: " + xpath, e2);
                }
            } else {
                if(ignoreException)
                    return false;
                else
                    throw new Exception("Failure clicking on element: " + xpath, e2);
            }
        }
        catch (Exception e) {
            if(ignoreException)
                return false;
            else
                throw new Exception("Failure clicking on element: " + xpath, e);
        }
    }

    /**
     * Set text to element
     * @param xpath
     * @param value
     * @param ignoreException
     * @throws Exception possible exception
     */
    public void setText(String xpath, String value, boolean ignoreException) throws Exception {

//        //replace last \n \t chars for ENTER / TAB  TODO test with \r\n
//            valueToType = valueToType.replace("\n", Keys.chord(Keys.SHIFT, Keys.ENTER));
//        }

        try {
            if (value != null) {
                WebElement item = findDynamicElement((xpath), ignoreException ? getShortTimeout() : getTimeout());
                item.clear();
                item.sendKeys(value);
            }
        } catch (Exception e){
            if(!ignoreException)
                throw new Exception("Failure typing in element: " + xpath, e);
        }
    }

    /**
     * Set text to element
     * @param xpath
     * @param value
     * @param ignoreException
     * @throws Exception possible exception
     */
    public void setTextWithEnter(String xpath, String value, boolean ignoreException) throws Exception {

//        //replace last \n \t chars for ENTER / TAB  TODO test with \r\n
//            valueToType = valueToType.replace("\n", Keys.chord(Keys.SHIFT, Keys.ENTER));
//        }

        try {
            if (value != null) {
                WebElement item = findDynamicElement((xpath), ignoreException ? getShortTimeout() : getTimeout());
                item.clear();
                //item.sendKeys(value);
                Actions act = new Actions(driver());
                item.clear();
                act.click(item).sendKeys(value).sendKeys(Keys.ENTER).build().perform();
            }
        } catch (Exception e){
            if(!ignoreException)
                throw new Exception("Failure typing in element: " + xpath, e);
        }
    }

    /**
     * Set text to invisible element
     * @param xpath
     * @param value
     * @param ignoreException
     * @throws Exception possible exception
     */
    public void setUploadFileField(String xpath, String value, boolean ignoreException) throws Exception {

        try {
            if (value != null) {
                WebElement item = findDynamicElement(xpath, ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)),ignoreException ? getShortTimeout() : getTimeout());
                item.sendKeys(value);
            }
        } catch (Exception e){
            if(!ignoreException)
                throw new Exception("Failure typing in element: " + xpath, e);
        }
    }

    /**
     * Select from dropdown
     * @param xpath
     * @param value
     * @param ignoreException
     * @throws Exception possible exception
     */
    public void selectFromDropdown(String xpath, String value, boolean ignoreException) throws Exception {
        try {
            Select dropdown = new Select(findDynamicElement(xpath, ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)), ignoreException ? getShortTimeout() : getTimeout()));
            dropdown.selectByVisibleText(value);
        }catch (Exception e){
            if(!ignoreException)
                throw new Exception("Failure selecting value: " + xpath, e);
        }
    }

    /**
     * Select from dropdown
     * @param xpath
     * @param value
     * @param ignoreException
     * @throws Exception possible exception
     */
    public void selectFromDropdownByIndex(String xpath, int value, boolean ignoreException) throws Exception {
        try {
            Select dropdown = new Select(findDynamicElement(xpath, ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)), ignoreException ? getShortTimeout() : getTimeout()));
            dropdown.selectByIndex(value);
        }catch (Exception e){
            if(!ignoreException)
                throw new Exception("Failure selecting value: " + xpath, e);
        }
    }

    /**
     * Get all values from dropdown as string
     * @param element
     * @return
     */
    public String getDropdownValuesAsString(String element) throws Exception {
        Select dropdown = new Select(findDynamicElement(element));
        return dropdown.getOptions().stream().map(e -> e.getText()).sorted().collect(Collectors.joining(DROPDOWN_SEPARATOR_VALUE));
    }


    /**
     * Is element visible
     * @param xpath
     * @return
     */
    public boolean isElementVisible(String xpath, boolean isVisible) {
        try {
            WebElement element = findDynamicElement(xpath, isVisible?ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)):ExpectedConditions.invisibilityOfElementLocated(By.xpath(xpath)));
            return element != null && isVisible;
        } catch (Exception e) {
            return !isVisible; // return false - for isVisible - return true for visible
        }
    }

    /**
     * Is text present on page
     * @param text
     * @return
     */
    public boolean isTextPresentOnPage(String text) {
        return driver().getPageSource().contains(text);
    }

    /**
     * Is element displayed right now
     * @param xpath
     * @return
     */
    public boolean isElementDisplayedRightNow(String xpath, int... timeout) {
        try {
            return findDynamicElement(xpath, timeout).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get text from element
     * @param xpath
     * @return
     */
/*    public String getText(String xpath) throws Exception {
        WebElement elem = findDynamicElement(xpath);
        if(elem == null)
            throw new Exception("Element was not found: " + xpath);
        if(elem.getAttribute("value") != null && !elem.getAttribute("value").equals(""))
            return elem.getAttribute("value");
        return elem.getText();
    }
  */
    public String getText(String xpath, boolean ignoreException) throws Exception {
        try {
            WebElement elem = findDynamicElement(xpath, ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)), ignoreException?getShortTimeout():getTimeout());
            if (elem == null)
                if(ignoreException)
                    return "";
                else
                    throw new Exception("Element was not found: " + xpath);

            if(elem.getTagName().toLowerCase().equals("select")) // select text
                return (new Select(elem)).getFirstSelectedOption().getText();
            if (elem.getAttribute("value") != null && !elem.getAttribute("value").equals(""))
                return elem.getAttribute("value"); //TODO add type validation
            return elem.getText();
        } catch (Exception e) {
            if(ignoreException)
                return "";
            else
                throw new Exception("Cannot get text from element: " + xpath);
        }
    }

    /**
     * Get text from element
     * @param element
     * @return
     */
    private String getText(WebElement element) {
        String value = element.getText();
        if(element.getAttribute("value") != null && !element.getAttribute("value").equals(""))
            return element.getAttribute("value");
        return value;
    }

    /**
     * Get complex elements from page and compare values with expected structure<br>
     *     expectedValues - should have same list of keys as targets (all keys except 'parent')<br>
     *         targets - should have 'parent' key
     * @param targets
     * @param expectedValues
     * @param isVisible
     * @return
     */
    public boolean wasComplexElementDisplayed(Map<String, String> targets, Map<String, String> expectedValues, boolean isVisible) {
        boolean wasFound = false;

        LocalDateTime timeout = LocalDateTime.now().plusSeconds(getShortTimeout());

        while(timeout.isAfter(LocalDateTime.now())) {
            List<WebElement> allItems = findElementsIgnoreException(targets.get("parent"));
            if (allItems == null) {// no parent elements found
                wasFound = false;
                if(!isVisible)
                    return false;
                sleepFor(500);
                continue;
            }

            String actualValue = "";
            String expectedValue = "";
            for (WebElement parent : allItems) {
                wasFound = true;
                for (Map.Entry<String, String> value : expectedValues.entrySet()) {
                    if(!targets.containsKey(value.getKey()))
                        continue;
                    actualValue = getText(parent.findElement(By.xpath(targets.get(value.getKey()))));
                    expectedValue = value.getValue();
                    if (!expectedValue.equals(actualValue)) {
                        wasFound = false;
                        break;
                    }
                }
                if (wasFound && isVisible)
                    return true;
            }
        }
        return wasFound;
    }


    /**
     * Generate complex object represented by HashMap
     * @param targets map of xpath for search and Parent element xpath
     * @param by_position index of position: -1 last, 0 first
     * @param by_value part of value to search for
     * @return
     */
    public Map<String, String> getComplexObject(Map<String, String> targets, int by_position, String by_value) {
        HashMap<String, String> result = new HashMap<>();
        //sleepFor(3000); // TODO Sync Add wait for attribute
        //FluentWait f = new FluentWait();
        //f.until(findElementsIgnoreException(targets.get("parent")).get(0).isDisplayed());
        List<WebElement> allItems = findElementsIgnoreException(targets.get("parent"));
        boolean valueWasFound = false;
        int objectPosition = (by_position == -1? allItems.size()-1 : by_position);
        if(allItems.size() == 0) {
            return result;
        }
        if(!by_value.equals("")) {
            for (int elementNumber = 0; elementNumber < allItems.size(); elementNumber++ ) {
                WebElement currentElement = allItems.get(elementNumber);
                for(String key: targets.keySet()) {
                    if(key.equals("parent"))
                        continue;
                    WebElement currentTarget = currentElement.findElement(By.xpath(targets.get(key)));
                    if(getText(currentTarget).contains(by_value)) {
                        valueWasFound = true;
                    }
                    result.put(key, getText(currentTarget));
                }
                if(valueWasFound) {
                    return result;
                }
            }
            return new HashMap<>();
        }
        // BY_POSITION
        WebElement currentElement = allItems.get(objectPosition);
        for(String key: targets.keySet()) {
            if(key.equals("parent"))
                continue;
            WebElement currentTarget = currentElement.findElement(By.xpath(targets.get(key)));

            result.put(key, getText(currentTarget));
        }

        return result;
    }

    /**
     * Generate complex object represented by HashMap
     * @param targets map of xpath for search and Parent element xpath
     * @return
     */
    public List<HashMap<String, String>> getComplexObject(Map<String, String> targets) {
        List<HashMap<String, String>> result = new LinkedList();
        List<WebElement> allItems = findElementsIgnoreException(targets.get("parent"));

        waitForPageToLoad();

        if(allItems == null || allItems.size() == 0) {
            return result;
        }

            for (int elementNumber = 0; elementNumber < allItems.size(); elementNumber++ ) {
                WebElement currentElement = allItems.get(elementNumber);
                HashMap<String, String> item = new HashMap<>();
                for(String key: targets.keySet()) {
                    if(key.equals("parent"))
                        continue;
                    WebElement currentTarget;
                    try {
                        currentTarget = currentElement.findElement(By.xpath(targets.get(key)));
                        item.put(key, getText(currentTarget));
                    }catch (Exception e){
                        item.put(key, "");
                    }
                };
                result.add(item);
            }

        return result;
    }

    /**
     * Find element and update Etalon file if required
     * @param xpath
     * @param condition ExpectedCondition
     * @param timeoutForFindElement
     * @return
     */
    private WebElement dynamicFind(String xpath, ExpectedCondition condition, int timeoutForFindElement) {
        WebElement foundElement = null;

        boolean isFrame = xpath.startsWith(FRAME_PROCESSING_MARKER);
        boolean isDialog = xpath.startsWith(DIALOG_PROCESSING_MARKER);
        boolean isSpecifiedFrame = xpath.startsWith(SPECIFIED_FRAME_PROCESSING_MARKER);;

        By by;
        if(isDialog)
            return getDialogElement(xpath.replace(DIALOG_PROCESSING_MARKER, ""));
        else if(isFrame)
            by = By.xpath(xpath.replaceFirst(FRAME_PROCESSING_MARKER, ""));
        else if(isSpecifiedFrame) {

            driver().switchTo().defaultContent();

            Pattern p =  Pattern.compile("//iframe\\[(.*?)\\]");
            Matcher matcher = p.matcher(xpath);
            ArrayList<By> frames = new ArrayList<>();
            while(matcher.find()){
                frames.add(By.xpath(String.format("//iframe[%s]" ,matcher.group(1))));
            };

            for(By frameLocator : frames){
                driver().switchTo().frame(driver().findElement(frameLocator));
            }

            by = By.xpath(xpath.replaceAll("//iframe\\[(.*?)\\]", ""));
        }else
            by = By.xpath(xpath);

        if(ProjectConfiguration.isUsingEtalon) { // using dynamic locators //TODO update
            foundElement = etalonObjectProcessor.tryToFindElementUsingEtalon(xpath, condition);  //take locator from Etalon if it exists
        } else {
            if(!isSpecifiedFrame)
                driver().switchTo().defaultContent();

            List<WebElement> elems;

            //process frame
            if(isFrame) {
                List<WebElement> frames = driver().findElements(By.tagName("iframe"));
                if (frames.size() > 0){
                    for (WebElement frame: frames) {
                        driver().switchTo().frame(frame);
                        elems = driver().findElements(by);
                        if(elems.size() > 0) {
                            foundElement = elems.get(0);
                            break;
                        }
//                        try {
//                            List<WebElement> subFrames = frame.findElements(By.tagName("iframe"));
//                            for (WebElement subframe : subFrames) {
//                                driver().switchTo().frame(subframe);
//                                elems = driver().findElements(by);
//                                if (elems.size() > 0) {
//                                    foundElement = elems.get(0);
//                                    break;
//                                }
//                                driver().switchTo().defaultContent();
//                                driver().switchTo().frame(frame);
//                            }
//                        } catch (Exception e){
//                            System.out.println();
//                        }
                        driver().switchTo().defaultContent();
                    }
                }
            } else { //default behaviour

                //wait statement
                Wait<WebDriver> wait = new FluentWait<WebDriver>(driver())
                        .withTimeout(Duration.of(timeoutForFindElement, SECONDS))
                        .ignoring(NoSuchElementException.class);
                // does not work .ignoring(TimeoutException.class);

                if(!isSpecifiedFrame)
                    wait.until(condition);

                elems = driver().findElements(by);
                if(elems.size() > 0)
                    foundElement = elems.get(0);
            }
        }
        return foundElement;
    }

    /**
     * Get dialog WebElement
     * @param dialogButtonLocator //OK or //CANCEL
     * @return webelement that represents Dialog
     */
    private WebElement getDialogElement(String dialogButtonLocator) {
        LOGGER.info("Dialog processing");
        return new WebElement() {
            @Override
            public void click() {
                if(dialogButtonLocator.equals(ACCEPT_DIALOG_MARKER))
                    driver().switchTo().alert().accept();
                if(dialogButtonLocator.equals(DISMISS_DIALOG_MARKER))
                    driver().switchTo().alert().dismiss();
            }

            @Override
            public void submit() {

            }

            @Override
            public void sendKeys(CharSequence... charSequences) {
                driver().switchTo().alert().sendKeys(String.valueOf(charSequences));
            }

            @Override
            public void clear() {
                driver().switchTo().alert().sendKeys("");
            }

            @Override
            public String getTagName() {
                return null;
            }

            @Override
            public String getAttribute(String s) {
                return null;
            }

            @Override
            public boolean isSelected() {
                return false;
            }

            @Override
            public boolean isEnabled() {
                return false;
            }

            @Override
            public String getText() {
                return null;
            }

            @Override
            public List<WebElement> findElements(By by) {
                return null;
            }

            @Override
            public WebElement findElement(By by) {
                return null;
            }

            @Override
            public boolean isDisplayed() {
                return false;
            }

            @Override
            public Point getLocation() {
                return null;
            }

            @Override
            public Dimension getSize() {
                return null;
            }

            @Override
            public Rectangle getRect() {
                return null;
            }

            @Override
            public String getCssValue(String s) {
                return null;
            }

            @Override
            public <X> X getScreenshotAs(OutputType<X> outputType) throws WebDriverException {
                return null;
            }
        };
    }

    /**
     * Scroll to element
     * @param element
     */
    public void scrollToElement(WebElement element) {
        if(element == null)
            return;
        waitForPageToLoad();
        LOGGER.info("Scroll to element");
        ((JavascriptExecutor) driver()).executeScript("if (arguments[0].getBoundingClientRect().y > window.innerHeight) {arguments[0].scrollIntoView(); window.scrollBy(0,-450);}", element);
        sleepFor(1000);
    }

    /**
     * scroll page to element by xPath
     * @param xPath    xPath of element
     */
    public void scrollToElement(String xPath) {
        try {
            ((JavascriptExecutor) driver()).executeScript(
                    "var element = document.evaluate(\"" + xPath + "\", document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;" +
                            "element.scrollIntoView();window.scrollBy(0,-450)"
            );
        }catch (Exception e){
            LOGGER.info(e.getMessage() + "\n" + e.toString());
        }
    }

    /**
     * Scroll with offset
     * @param element
     * @param horizontalOffset
     * @param verticalOffset
     */
    public void scrollToElement(WebElement element, int horizontalOffset, int verticalOffset) {
        waitForPageToLoad();
        ((JavascriptExecutor) driver()).executeScript("arguments[0].scrollIntoView();arguments[0].focus(); window.scrollBy("+ horizontalOffset  +", "+ verticalOffset +")", element);
    }

    @Deprecated
    /**
     * Scroll inside of table
     * @param column
     * @param bottomTableScroller
     */
    public void scrollToColumn(WebElement column, WebElement bottomTableScroller){
        String script = "elem = arguments[0]; scrollElem = arguments[1];" +
                "pixToMove=100;" +
                "maxNumberOfAttempts = window.innerWidth/pixToMove ;\n" +
                "numberOfAttempts = 0;\n" +

                "function scrollFunc() {\n" +
                "console.log(elem.getBoundingClientRect().x);\n" +
                "if(!(elem.getBoundingClientRect().x + elem.getBoundingClientRect().width <   window.innerWidth  && elem.getBoundingClientRect().x > window.innerWidth / 4)) {\n" +
                "scrollElem.scrollBy(pixToMove,0); return false; } else {return true;}}\n" +

                "function runWithTimeout() {\n" +
                "    if(scrollFunc()) return;\n" +
                "    numberOfAttempts++;\n" +
                "    if( numberOfAttempts < maxNumberOfAttempts ){\n" +
                "        setTimeout( runWithTimeout, 500 );\n" +
                "    } else {return;}\n" +
                "}\n" +
                "runWithTimeout();";

        //scrollElem = $$("div.grid-bottom-scroll")[0]; elem = $x("//portal-report-grid-page-columns-column")[5];
        //rect = elem.getBoundingClientRect(); while(!(window.innerWidth/2 + 300 > rect.x && window.innerWidth/2 -300 < rect.x)){scrollElem.scrollBy(100,0);rect = elem.getBoundingClientRect();}

        ((JavascriptExecutor) driver()).executeScript(script ,column, bottomTableScroller);

    }


    /**
     * Put mouse pointer over element
     * @param element
     * @throws Exception possible exception
     */
    // NOTE: Did not work because of geckodriver bug - https://stackoverflow.com/questions/40360223/webdriverexception-moveto-did-not-match-a-known-command
    @Deprecated
    public void hoverItem(String element) throws Exception {
        reporter.info("Put mouse pointer over element: " + element.toString());
        WebElement target = findDynamicElement(element);
        scrollToElement(target);
        Actions action = new Actions(driver());
        sleepFor(1000);
        action.moveToElement(target).perform();

        // String javaScript = "var evObj = document.createEvent('MouseEvents');" +
        //        "evObj.initMouseEvent(\"mouseover\",true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);" +
        //        "arguments[0].dispatchEvent(evObj);";
        //
        //
        //((JavascriptExecutor)reportPage.driver()).executeScript(javaScript, e);
    }


    /**
     * click on Horizontal scroller element to move scroller
     * @param locator
     * @param scrollRight
     */
    @Deprecated
    public void moveHorizontalScroller(By locator, boolean scrollRight){
        Actions act = new Actions(driver());
        WebElement element = driver().findElement(locator);
        act.click(element).
                sendKeys(Keys.ARROW_RIGHT).
                sendKeys(Keys.ARROW_RIGHT).
                sendKeys(Keys.ARROW_RIGHT).
                sendKeys(Keys.ARROW_RIGHT).
                sendKeys(Keys.ARROW_RIGHT).
                sendKeys(Keys.ARROW_RIGHT).
                sendKeys(Keys.ARROW_RIGHT).
                sendKeys(Keys.ARROW_RIGHT).
                sendKeys(Keys.ARROW_RIGHT).
                sendKeys(Keys.ARROW_RIGHT)
        .build().perform();
    }

    /**
    * Check if new tab was opened
    * @return   boolean value is new tab is opened
    */
    @Deprecated
    public boolean isNewTabOpened() {
        String currentWindowID = driver().getWindowHandle();
        Object[] allWindowIDS = driver().getWindowHandles().toArray();

        int currentWindowIndex = Arrays.asList(allWindowIDS).indexOf(currentWindowID);
        try {
            Object nextTabtWindowID = allWindowIDS[currentWindowIndex + 1];
            return true;
        } catch (Exception e) {
            LOGGER.error("Next tab was not opened");
            return false;
        }
    }

    /**
     * check is WebElement has specific class
     * @param element       WebElement which has to be verified
     * @param className     class that is being sought
     * @return              boolean value
     */
    @Deprecated
    public boolean hasCertainClass(WebElement element, String className) {
        String[] classes = element.getAttribute("class").split( " ");
        for (String elementClass : classes) {
            if (elementClass.equals(className)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Click on element using JS
     * @param element
     */
    public boolean clickOnElementUsingJS(WebElement element) {
        try {
            ((JavascriptExecutor) driver()).executeScript("arguments[0].click()", element);
        } catch (JavascriptException e) {
            LOGGER.warn(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Click using right button (using Actions)
     * @param element
     */
    public void clickOnElementRightButton(By element) {
        waitForPageToLoad();
        try {
            (new WebDriverWait(driver(), MAIN_TIMEOUT))
                    //.until(ExpectedConditions.visibilityOfElementLocated(element));
            .until(webDriver ->ExpectedConditions.visibilityOfElementLocated(element).apply(webDriver));


            Actions action= new Actions(driver());
            action.contextClick(driver().findElement(element)).build().perform();
        } catch (Exception e) { // try again in case of exception
            Actions action= new Actions(driver());
            action.contextClick(driver().findElement(element)).build().perform();
        }
        waitForPageToLoad();
    }

    /**
     * Zoom in/Out  (NOTE: NOT FOR PARALLEL EXECUTION)
     *
     * @param zoomLevel<br>
     *   zoomLevel  -1 -2 -3 -4 -5 .. - corresponds to Zoom OUT 90, 80, 75, 67, 50 <br>
     *   zoom level  1 2 3 4 5      .. - corresponds to Zoom IN 110, 125, 150, 175, 200 <br>
     *   zoom level &gt; 5 || &lt; -5 - Zoom using JS
     * @throws AWTException possible exception
     */
    @Deprecated
    public void zoom(int zoomLevel) throws AWTException {
        sleepFor(2000);
        if(zoomLevel > 5 || zoomLevel < -5) {
            ((JavascriptExecutor) driver()).executeScript("document.body.style.zoom = '" + zoomLevel + "%'");

        } else {
        Robot r = new Robot();
        r.mouseMove(2000,200);
        r.mousePress(InputEvent.BUTTON1_MASK);
        r.mouseRelease(InputEvent.BUTTON1_MASK);
        if(zoomLevel > 0) {
            for (int i = 0; i < zoomLevel; i++) {
                if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                    r.keyPress(KeyEvent.VK_META);
                    sleepFor(1000);
                    r.keyPress(KeyEvent.VK_ADD);
                    r.keyRelease(KeyEvent.VK_ADD);
                    sleepFor(1000);
                    r.keyRelease(KeyEvent.VK_META);
                } else {
                    r.keyPress(KeyEvent.VK_CONTROL);
                    sleepFor(1000);
                    r.keyPress(KeyEvent.VK_ADD);
                    r.keyRelease(KeyEvent.VK_ADD);
                    sleepFor(1000);
                    r.keyRelease(KeyEvent.VK_CONTROL);
                }
            }
        }else {
            for (int i = 0; i > zoomLevel; i--) {
                if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                    r.keyPress(KeyEvent.VK_META);
                    sleepFor(1000);
                    r.keyPress(KeyEvent.VK_SUBTRACT);
                    r.keyRelease(KeyEvent.VK_SUBTRACT);
                    sleepFor(1000);
                    r.keyRelease(KeyEvent.VK_META);
                } else {
                    r.keyPress(KeyEvent.VK_CONTROL);
                    sleepFor(1000);
                    r.keyPress(KeyEvent.VK_SUBTRACT);
                    r.keyRelease(KeyEvent.VK_SUBTRACT);
                    sleepFor(1000);
                    r.keyRelease(KeyEvent.VK_CONTROL);
                }
            }
        }
        }
    }


    /**
     * Zoom using JS
     * @param value
     */
    public void zoomInZoomOut(String value) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver();
            js.executeScript("document.body.style.zoom='" + value + "'");
        }catch (Exception e){e.printStackTrace();}
    }

    /**
     * Click on each element in list until target is visible/invisible
     * @param target
     * @param lisOfItemsToInteractWith
     * @param invisible
     * @param timeoutInSeconds timeout between elements search
     * @param maxTimeoutInSeconds
     * @return
     */
    public boolean clickOnElementsUntilConditionReached(String target, List<String> lisOfItemsToInteractWith, boolean invisible,  int timeoutInSeconds, int maxTimeoutInSeconds) throws Exception {
        LocalDateTime expectedTime = LocalDateTime.now().plusSeconds(maxTimeoutInSeconds);
        boolean previousClickFailed = false;
        while ( isElementDisplayedRightNow(target, timeoutInSeconds) == invisible && expectedTime.isAfter(LocalDateTime.now()) && !previousClickFailed){
            for(String xpath : lisOfItemsToInteractWith) {
                previousClickFailed = !clickOnElement(xpath, true);
                if(previousClickFailed) break;
            }
            waitForPageToLoad();
        }
        if(!expectedTime.isAfter(LocalDateTime.now()))
            LOGGER.warn("Timeout reached");
        if(previousClickFailed)
            LOGGER.warn("Last click failed");
        return isElementDisplayedRightNow(target, timeoutInSeconds) == !invisible;
    }

    /**
     * in progress
     * @param target
     * @param lisOfItemsToCheck
     * @param itemToActivate
     * @param lisOfItemsToInteractWith
     * @param invisible
     * @param timeoutInSeconds
     * @param maxTimeoutInSeconds
     * @return
     * @throws Exception possible exception
     */
    public boolean clickOnElementsUntilConditionReached(String target, List<String> lisOfItemsToCheck, String itemToActivate, List<String> lisOfItemsToInteractWith, boolean invisible,  int timeoutInSeconds, int maxTimeoutInSeconds) throws Exception {
        LocalDateTime expectedTime = LocalDateTime.now().plusSeconds(maxTimeoutInSeconds);
        boolean noElementsFound = true;
        while ( isElementDisplayedRightNow(target, timeoutInSeconds) == invisible && expectedTime.isAfter(LocalDateTime.now())){
            noElementsFound = true;
            String source = driver().getPageSource();
            for( int index = 0; index < lisOfItemsToCheck.size(); index++) {
                if(source.contains(lisOfItemsToCheck.get(index))) {
                    noElementsFound = false;
                    if(clickOnElement(lisOfItemsToInteractWith.get(index), true))
                        break;
                }
            }
            if(noElementsFound)
                break;
            clickOnElement(itemToActivate,false);
            waitForPageToLoad();
        }
        if(!expectedTime.isAfter(LocalDateTime.now()))
            LOGGER.warn("Timeout reached");
        if(noElementsFound)
            LOGGER.warn("No elements found");
        return isElementDisplayedRightNow(target, timeoutInSeconds) == !invisible;
    }

    //TODO move methods to another class

    /**
     * start recording of HAR using Proxy
     */
    public void startHARRecording() {
        LOGGER.info("HAR_NAME" + reporter.TEST_NAME.get() + "_" + RandomDataGenerator.getCurDateTime());
        //Add Proxy hash code : BrowserProxy.getInstance().hashCode() + "_" +
        BrowserProxy.getInstance().createHARFileFromProxy( ReporterManager.Instance.TEST_NAME.get() + "_" + RandomDataGenerator.getCurDateTime());
    }

    /**
     * Stop recording of HAR file using proxy and save it
     */
    public void dumpHARRecordingResults() {
        BrowserProxy.getInstance().dumpHARFileFromProxy();
    }

        /**
         * Compare screenshot with specified description with baselined version (using Google Drive)
         * @param target
         * @return
         */
        public boolean compareScreenshots(String target) {
            boolean result = false;
            //take current screenshot
            String currentImage = FileManager.OUTPUT_DIR + File.separator + reporter.takeScreenshot(driver(), target);
            String baselinedImage = GoogleDriveImageProvider.getBaselinedImage(target);//FileImageProvider.getBaselinedImage(target);
            String resultImage = FileManager.OUTPUT_DIR + File.separator + "Result_" + target;

            if (baselinedImage != null){

                reporter.addImage("Actual: " + currentImage, currentImage);
                reporter.addImage("Expected: " + baselinedImage, baselinedImage);
                reporter.addImage("Result: " + resultImage, resultImage);

                try {
                    result = ImageComparator.compareImages(baselinedImage, currentImage, resultImage);
                } catch (Exception e) {
                    e.printStackTrace();
                    LOGGER.warn(e.getMessage());
                    result = false;
                }
            } else {
                reporter.warn("No Baselined image was found for " +  target);
            }
            if(!result) {
                String driveImageFolderURL = "";
                synchronized (TestsExecutor.class) {
                    driveImageFolderURL = GoogleDriveImageProvider.addImageToReview(currentImage);
                }
                // FileImageProvider.addImageToReview(currentImage);
                String messageWithURL = reporter.infoAsURL("Please review new screenshot:  " + target, driveImageFolderURL);

                SessionManager.addFinalStep(reporter.TEST_NAME.get() + " " + messageWithURL);
            }
            return result;
        }

    public boolean hoverElement(String target, boolean isOptional) throws Exception {
        LOGGER.info("Hovering over " + target);
        try {
            WebElement element = findDynamicElement(target, isOptional?getShortTimeout():getTimeout());

            scrollToElement(element);
            if (element.isDisplayed()) {
                Actions action = new Actions(driver());
                sleepFor(1000);
                action.moveToElement(element).perform();
                //clickOnElementUsingJS(element);
            } else
            if(isOptional)
                return false;
            else
                throw new Exception("Failure clicking on element - element not visible: " + target);
            waitForPageToLoad();
            return true;
        } catch (Exception e) {
            if(isOptional)
                return false;
            else
                throw new Exception("Failure clicking on element: " + target, e);
        }

    }

    /**
     * Run JS script and return results
     * @param target locator
     * @param jsCode js code
     * @return result of execution
     */
    public String runJSFor(String target, String jsCode) {
        return String.valueOf(((JavascriptExecutor) driver()).executeScript(jsCode , driver().findElement(By.xpath(target))));
    }

    /**
     * Get attribute value
     * @param xpath xpath
     * @param attribute attribute name
     * @param ignoreException true - if exception ignored
     * @return attribute value / css value
     * @throws Exception exception
     */
    public String getAttribute(String xpath, String attribute, boolean ignoreException) throws Exception {
        try {
            WebElement elem = findDynamicElement(xpath);
            if (elem == null)
                if(ignoreException)
                    return "";
                else
                    throw new Exception("Element was not found: " + xpath);
            if (elem.getAttribute(attribute) != null && !elem.getAttribute(attribute).equals("")) {
                return elem.getAttribute(attribute);
            }
            return elem.getCssValue(attribute);
        } catch (Exception e) {
            if(ignoreException)
                return "";
            else
                throw new Exception("Cannot get attribute " + attribute + " from element: " + xpath);
        }
    }

    /**
     * Get p[art of URL described by Description
     * @param description regexp
     * @return URL part
     */
    public String getURLPartByDescription(String description) {
        waitForPageToLoad();
        if(description == null || description.equals(""))
            return driver().getCurrentUrl();
        else
            return driver().getCurrentUrl().replaceAll(description, "$1");
    }

    /**
     * Drag Element and drop element to Target
     * @param itemToHold
     * @param targetElement
     */
    public void dragAndDrop(String itemToHold, String targetElement) {

        reporter.info("Drag: " + itemToHold + " to " + targetElement);
        WebElement item = findDynamicElement(itemToHold);
        WebElement target = findDynamicElement(targetElement);
        scrollToElement(target);
        Actions action = new Actions(driver());
        sleepFor(1000);
        action.dragAndDrop(item, target).perform();

    }
}
