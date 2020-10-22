package automation.keyword.validation.ui;

import automation.annotations.KeywordRegexp;
import automation.keyword.AbstractKeyword;
import automation.execution.TestStepsExecutor;

/**
 * Compare screenshot with expected image
 */
public class ScreenshotValidationKeyword extends AbstractKeyword {

    @KeywordRegexp("Compare screenshot with expected image 'image description'")
    static String LABEL = "compare screenshot with expected image";

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            ScreenshotValidationKeyword result = (ScreenshotValidationKeyword)super.generateFromLine(line);

            result.data = null;
            result.target = line.replaceFirst(".*['\"](.*)[\"'].*","$1");
            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) {
        return executor.page.compareScreenshots(normalizeTarget(target));
    }

    String normalizeTarget(String target){
        return target.replace(" ","_")+ ".png";
    }
}
