package automation.execution;

import automation.configuration.ProjectConfiguration;
import automation.keyword.AbstractKeyword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class TestStepsExecutorWithPerfRecording extends TestStepsExecutor {

    //performance logger
    private static final Logger analytics = LoggerFactory.getLogger("analytics");

    /**
     * Run all steps (except final)
     * @param steps
     * @return
     */
    @Override
    protected boolean runMainSteps(List<AbstractKeyword> steps) {
        boolean result = true;
        LocalDateTime executionPointTime = LocalDateTime.now();

        //execute main steps
        for (AbstractKeyword step : steps) {
            if(step.isFinal) continue;

            //debug
            if(step.originalLine.toUpperCase().equals("DEBUG"))
                System.out.println("Debug");
            else
                reporter.info(step.originalLine);

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

            noteCurrentExecutionTime(executionPointTime, step);
        }

        reporter.info("Done with main steps. Starting clean up steps.");
        return result;
    }

    /**
     * Note execution time of a step
     * @param executionStartTime
     * @param step
     * @return
     */
    private LocalDateTime noteCurrentExecutionTime(LocalDateTime executionStartTime, AbstractKeyword step) {
        if(!ProjectConfiguration.isPerformanceProfilingRequired)
            return null;

        LocalDateTime currentTime = LocalDateTime.now();
        Duration d = Duration.between(executionStartTime, currentTime);
        analytics.info(reporter.TEST_NAME.get() + " - Step: " + step.toShortString() + " took " + d.toMillis() + " ms");
        return currentTime;
    }

}
