package automation.execution;

import automation.api.BaseRestClient;
import automation.execution.repositories.LocatorsRepository;
import automation.execution.repositories.TestDataRepository;
import automation.keyword.AbstractKeyword;
import automation.mailtrap.MailTrapClient;
import automation.ssh.SSHClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import automation.reporting.ReporterManager;
import automation.web.BasePage;

import java.util.List;

public abstract class TestStepsExecutor {

    //reporter
    public ReporterManager reporter;

    //logger
    private static final Logger LOGGER = LoggerFactory.getLogger(TestStepsExecutor.class);

    public LocatorsRepository locatorsRepository;
    public TestDataRepository testDataRepository;
    public BasePage page;
    public BaseRestClient api;
    public SSHClient ssh;
    public MailTrapClient mail;

    //default value to save API response
    public static final String DEFAULT_LAST_API_RESULT = "SAVED.lastRequestResult";

    public TestStepsExecutor(){
        this.reporter = ReporterManager.Instance;
        this.testDataRepository = new TestDataRepository();
        this.locatorsRepository = new LocatorsRepository(testDataRepository);
        this.page = new BasePage();
        this.api = new BaseRestClient();
        this.ssh = new SSHClient();
        this.mail = new MailTrapClient();
    }

    /**
     * Execute all keywords
     * @param steps
     * @return
     * @throws Exception possible exception
     */
    public boolean executeAllKeywords(List<AbstractKeyword> steps) throws Exception {
        boolean result = true;
        result = runMainSteps(steps);
        runFinalSteps(steps);
        return result;
    }

    /**
     * Run all steps (except final)
     * @param steps
     * @return
     */
    protected boolean runMainSteps(List<AbstractKeyword> steps) {
        boolean result = true;

        //execute main steps
        for (AbstractKeyword step : steps) {
            if(step.isFinal) continue;

            //execute step
            try {
                if (!step.execute(this)) {
                    result = false;
                    if(!step.isOptional)
                        break; // main cycle finished
                }
            }catch (Exception e){
                result = false;
                e.printStackTrace();
                reporter.failWithScreenshot("Step execution failed", e);
                break; // main cycle finished
            }
        }

        reporter.info("Done with main steps. Starting clean up steps.");
        return result;
    }

    /**
     * Run final steps
     * @param steps
     */
    protected void runFinalSteps(List<AbstractKeyword> steps) {
        //execute finalization steps
        try {
            for (AbstractKeyword step : steps) {
                if (step.isFinal)
                    step.execute(this);
            }
        }catch (Exception e){
            /// don't care
        }

        reporter.info("Done");
    }

}
