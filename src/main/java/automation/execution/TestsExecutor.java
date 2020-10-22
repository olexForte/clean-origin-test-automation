package automation.execution;

import automation.entities.TestFile;
import automation.keyword.AbstractKeyword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import automation.reporting.ReporterManager;

import java.util.*;

/**
 * Main processor of tests<br>
 *     Process keywords and executes them
  */

public class TestsExecutor implements ITestExecutor{

    private TestStepsExecutor testStepExecutor;

    //reporter
    public ReporterManager reporter;

    //logger
    private static final Logger LOGGER = LoggerFactory.getLogger(TestsExecutor.class);

    public TestsExecutor() {
        this.reporter = ReporterManager.Instance;
        this.testStepExecutor = new TestStepsExecutorWithPerfRecording();
    }

    /**
     * Execute list of Steps from specified file
     * @param testFile test file object
     */
    public boolean executeAllStepsFromFile(TestFile testFile) {
        String fileName  = testFile.getTestName();
        boolean result = false;
        reporter.info("Processing file: " + fileName);

        List<AbstractKeyword> keywords;
        try{
            reporter.info("Parse keywords");
            keywords = TestStepsReader.parseKeywords(fileName);
            reporter.info("Execute keywords");
            result = testStepExecutor.executeAllKeywords(keywords);
        } catch (Exception e){
            e.printStackTrace();
            reporter.fail(ReporterManager.getStackTrace(e));
            return false;
        }
        return result;
    }
}
