package automation.keyword.proxy;

import automation.annotations.KeywordRegexp;
import automation.keyword.AbstractKeyword;
import automation.execution.TestStepsExecutor;

/**
 * Start recording of all requests/responses using Proxy <br>
 *     StopProxyRecordingKeyword - dumps recorded data
 */
public class StartProxyRecordingKeyword extends AbstractKeyword {

    @KeywordRegexp("Start proxy recordings")
    static String LABEL = "start proxy recordings";

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            StartProxyRecordingKeyword result = (StartProxyRecordingKeyword)super.generateFromLine(line);

            result.data = null;
            result.target = null;
            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {
        executor.page.startHARRecording();
        return true;
    }
}





