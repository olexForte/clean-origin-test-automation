package automation.keyword.rest;

import automation.annotations.KeywordRegexp;
import io.restassured.response.Response;
import automation.keyword.AbstractKeyword;
import automation.execution.TestStepsExecutor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Save value from API response<br>
 *     <b>Example: </b>
 *     Save API response value 'companyData.DASHBOARD_PAGE_UPLOAD_COMPANY_ID_DESCRIPTION' from HTML 'SAVED.apiResponse' to 'saved.COMPANY_UPLOAD_ID';
 *     Save API response value 'data' from 'saved.apiResponse' to 'saved.totalClientRecords' as maps
 */
public class SaveValueFromAPIResponseKeyword extends AbstractKeyword {
    @KeywordRegexp("Save API response value 'field' from [HTML] 'saved.apiResponse' to 'saved.value' [as list|as maps];")
    static String LABEL = "save api response value";
    String source = "";
    String rule = "";

    String AS_LIST_MARKER = " as list";
    boolean asList = false;

    String AS_MAPS_MARKER = " as maps";
    boolean asMaps = false;

    String AS_HTML_MARKER = " from html";
    boolean fromHTML = false;

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            SaveValueFromAPIResponseKeyword result = (SaveValueFromAPIResponseKeyword)super.generateFromLine(line);

            Pattern p =  Pattern.compile("('(.*?)')|(\"(.*?)\")");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.data = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.source = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.target = line.substring(matcher.start()+1, matcher.end()-1);
            //if(matcher.find())
            //    result.rule = line.substring(matcher.start()+1, matcher.end()-1);
            if(line.toLowerCase().contains(AS_LIST_MARKER))
                result.asList = true;
            if(line.toLowerCase().contains(AS_MAPS_MARKER))
                result.asMaps = true;
            if(line.toLowerCase().contains(AS_HTML_MARKER))
                result.fromHTML = true;
            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {

        String dataLine = "";
        try {
            dataLine = executor.locatorsRepository.getTarget(data); // process dynamic values
        } catch (Exception e) {
            dataLine = null; // process dynamic values
        }

        if(dataLine == null)
            dataLine = (String) executor.testDataRepository.getData(data);

        Response response =  (Response)executor.testDataRepository.getTestDataObject(source);

        Object val;
        if(asList){
            if(fromHTML) {
                val = response.htmlPath().getList(dataLine, String.class);
                executor.testDataRepository.setTestDataObject(target, val);
            }else {
                val = response.jsonPath().getList(dataLine, String.class);
                executor.testDataRepository.setTestDataObject(target, val);
            }
        } else if(asMaps) {
            if(fromHTML) {
                val = response.htmlPath().getList(dataLine);
                executor.testDataRepository.setTestDataObject(target, val);
            }else {
                val = response.jsonPath().getList(dataLine);
                executor.testDataRepository.setTestDataObject(target, val);
            }
        } else {
            if(fromHTML) {
                val = response.htmlPath().get(dataLine);
                String savedValues = String.valueOf(val);
                executor.testDataRepository.setData(target, savedValues);
            } else {
                val = response.jsonPath().get(dataLine);
                String savedValues = String.valueOf(val);
                executor.testDataRepository.setData(target, savedValues);
            }
        }

        return true;
    }
}
