package runners;

import com.relevantcodes.extentreports.LogStatus;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.Logs;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import java.util.Properties;
import java.util.logging.Level;

import static automation.web.BasePage.driver;

import automation.datasources.FileManager;
import automation.configuration.ProjectConfiguration;
import automation.datasources.RandomDataGenerator;
import automation.reporting.ReporterManager;
import automation.web.BasePage;
import automation.web.DriverProvider;

//
import java.net.URI;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.client.entity.UrlEncodedFormEntity;

public class BaseUITest {

    //report
    public ReporterManager reporter;
    // main logger
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseUITest.class);

    @BeforeMethod
    public void beforeWithData(Object[] data, Method method) throws IOException {

        ProjectConfiguration.threadProperties.set(new Properties());

        //init reporter
        reporter = ReporterManager.Instance;
        reporter.startReporting(method, data);

//        //init threadlocal driver
//        try {
//            reporter.info("Driver creation");
//            driver.set(DriverProvider.getDriver(reporter.TEST_NAME.get())); // for BS / SL drivers
//            //driver.set(DriverProvider.getDriver());
//
//            reporter.info("Driver created " + BasePage.driver.get().hashCode());
//        }catch (Exception e){
//            reporter.failWithScreenshot("Before test failure during Driver creation", e);
//            reporter.stopReporting();
//            reporter.closeReporter();
//            Assert.fail();
//        }

        //BasePage.driver().manage().window().maximize();

        // create Etalon file variables
        if(ProjectConfiguration.isUsingEtalon) {

            String etalonDirectory = ProjectConfiguration.getConfigProperty("EtalonLocatorsDir");
            String etalonFileName = reporter.TEST_NAME.get() + ProjectConfiguration.getConfigProperty("EnvType") + ProjectConfiguration.getConfigProperty("LocatorsDir");
            String etalonFileLocation = etalonDirectory + File.separator + etalonFileName;
            String currentEtalonFileLocation = etalonDirectory + File.separator + etalonFileName + RandomDataGenerator.getCurDateTime();

            if(FileManager.doesExist(etalonFileLocation))
                FileManager.copyFile(etalonFileLocation, currentEtalonFileLocation);

            ProjectConfiguration.setLocalThreadConfigProperty("CurrentEtalonFile", currentEtalonFileLocation);
            ProjectConfiguration.setLocalThreadConfigProperty("EtalonFile", etalonFileLocation);
        }


    }

    @AfterMethod
    public void endTest(ITestResult testResult, Method method) throws Exception {

        try {
            logJavaScriptConsoleError();
        }catch (Exception e){
            //know issue with browsers except Chrome
        }

        // close reporter
        reporter.stopReporting(testResult);


        //dynamic locators processing
        if(testResult.isSuccess()){ // replace Etalon file if PASSED
            if(ProjectConfiguration.isUsingEtalon) {
                LOGGER.info("Copy Current Etalon file as Main Etalon file");
                if(FileManager.doesExist(ProjectConfiguration.getConfigProperty("CurrentEtalonFile")))
                    FileManager.copyFile(ProjectConfiguration.getConfigProperty("CurrentEtalonFile"), ProjectConfiguration.getConfigProperty("EtalonFile"));
            }
        };

        if(ProjectConfiguration.isUsingEtalon){
            LOGGER.info("Delete Current Etalon file");
            if(FileManager.doesExist(ProjectConfiguration.getConfigProperty("CurrentEtalonFile")))
                FileManager.deleteFile(ProjectConfiguration.getConfigProperty("CurrentEtalonFile"));
        }

//        else { //TODO test and add
//            String lastFailure = driver().getPageSource();
//            FileIO.appendToFile(reporter.TEST_NAME.get() + Tools.getCurDateTime()+ ".html", lastFailure);
//        }

        //set Remote Test execution status (for BS)
        if(ProjectConfiguration.getConfigProperty("Driver").toLowerCase().equals("bs")) {
            String sessionID = ((RemoteWebDriver) DriverProvider.getCurrentDriver()).getSessionId().toString();
            String status = "";
            if(!testResult.isSuccess())
                status = ReporterManager.report().getTest().getLogList().stream().filter(l -> l.getLogStatus().equals(LogStatus.FAIL)).findFirst().get().getDetails().replaceAll("<.+?>", "");
            markBSTest(testResult.isSuccess(), sessionID, status);
        }

        //close driver / stop remote test
        if(driver() != null) {
            BasePage.stopDriver();
            DriverProvider.closeDriver();
        }
    }

    @AfterSuite(alwaysRun = true)
    public void flushReporter()  {
        //reporter.addFinalStepsToReport();
        reporter.closeReporter();
        reporter.addCustomScriptsAndStyles();
    }


    /**
     * Change status of BS test
     * @param isPassed
     * @throws Exception possible exception
     */
    public static void markBSTest(boolean isPassed, String sessionID, String status) throws Exception {

        URI apiUri = new URI("https://"
                + ProjectConfiguration.getConfigProperty("BSUsername") + ":"
                + ProjectConfiguration.getConfigProperty("BSAutomateKey")
                +"@api.browserstack.com/automate/sessions/" + sessionID + ".json");
        HttpPut putRequest = new HttpPut(apiUri);

        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        if(!isPassed) {
            nameValuePairs.add((new BasicNameValuePair("status", "failed")));
            nameValuePairs.add((new BasicNameValuePair("reason", status)));
        } else {
            nameValuePairs.add((new BasicNameValuePair("status", "passed")));
            nameValuePairs.add((new BasicNameValuePair("reason", "success")));
        }
        putRequest.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        HttpClientBuilder.create().build().execute(putRequest);
    }

    /**
     * add all messages in JS Console in report
     */
    void logJavaScriptConsoleError() {
        Logs logs = driver().manage().logs();
        LogEntries logEntries = logs.get(LogType.BROWSER);
        String errorsInConsole = "";
        for(LogEntry logEntry :logEntries)
        {
            if (logEntry.getLevel() == Level.SEVERE) {
                errorsInConsole = errorsInConsole + "\n" + logEntry.toString();
            }
        }
        if(errorsInConsole.length() > 0)
            reporter.warn("Error Messages in Console:\n " + errorsInConsole);
    }
}
