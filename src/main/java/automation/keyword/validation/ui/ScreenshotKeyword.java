package automation.keyword.validation.ui;

import automation.annotations.KeywordRegexp;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;

/**
 * Take additional screenshot
 */
public class ScreenshotKeyword extends AbstractKeyword {

    @KeywordRegexp("Screenshot")
    static String LABEL = "screenshot";

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            ScreenshotKeyword result = (ScreenshotKeyword)super.generateFromLine(line);

            result.data = null;
            result.target = line.replaceFirst(".*['\"](.*)[\"'].*","$1");
            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) {
        executor.reporter.passWithScreenshot("Review: " + target);
        return true;
    }
}
