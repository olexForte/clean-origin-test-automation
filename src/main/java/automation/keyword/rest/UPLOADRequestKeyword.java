package automation.keyword.rest;

import automation.annotations.KeywordRegexp;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;
import io.restassured.response.Response;

import java.io.File;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Send  POST request to specified URL to upload file with data and headers and save results<br>
 *     <b>Example: </b>
 *         Send upload POST request 'apiEndpoints.CREDENTIALS_URL' with 'API.CREDENTIALS_REQUEST' and save results to 'saved.result'
 */
public class UPLOADRequestKeyword extends AbstractKeyword {

    @KeywordRegexp("Send upload POST request 'url' with [data file] 'data' with headers 'headers' and parameters 'parameters'")
    static String LABEL = "send upload post request";
    String parameters;
    String headers;

    String DATA_FILE_MARKER = " data file ";
    boolean fromDataDir = false;

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            UPLOADRequestKeyword result = (UPLOADRequestKeyword)super.generateFromLine(line);;

            Pattern p =  Pattern.compile("('(.*?)')|(\"(.*?)\")");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.target = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.data = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.headers = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.parameters = line.substring(matcher.start()+1, matcher.end()-1);

            if(line.toLowerCase().contains(DATA_FILE_MARKER))
                result.fromDataDir = true;

            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {
        String url = executor.locatorsRepository.getTarget(target); // process dynamic values
        if(url == null)
            url = (String)executor.testDataRepository.getData(target);
        File requestData;
        if(fromDataDir)
            requestData = executor.testDataRepository.getDataFile(data);
        else
            requestData = executor.testDataRepository.getOutputFile(data);

        HashMap<String, String> requestHeaders = executor.testDataRepository.getComplexData(headers);
        HashMap<String, String> requestParameters = executor.testDataRepository.getComplexData(parameters);
        Response response = executor.api.uploadPostRequest(url, requestData, requestHeaders, requestParameters);
        return true;
    }
}
