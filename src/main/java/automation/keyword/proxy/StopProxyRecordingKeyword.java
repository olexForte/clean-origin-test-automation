package automation.keyword.proxy;

import automation.annotations.KeywordRegexp;
import automation.keyword.AbstractKeyword;
import automation.execution.TestStepsExecutor;

/**
 * Dump all recorded by proxy requests/responses to HAR file
 */
public class StopProxyRecordingKeyword extends AbstractKeyword {

    @KeywordRegexp("Stop proxy recordings")
    static String LABEL = "stop proxy recordings";

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            StopProxyRecordingKeyword result = (StopProxyRecordingKeyword)super.generateFromLine(line);

            result.data = null;
            result.target = null;
            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {
        executor.page.dumpHARRecordingResults();
        return true;
    }
}
