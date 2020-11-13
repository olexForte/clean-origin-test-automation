package automation.keyword.web;

import automation.annotations.KeywordRegexp;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Save value from data to target variable
 */
public class GetValueFromSavedKeyword extends AbstractKeyword {

    @KeywordRegexp("Get value from saved 'data' [TEXT|JSON|MAP] by 'regexp' and save to 'saved.VALUE';")
    static String LABEL = "get value from saved";

    static String TEXT_MARKER = " TEXT ";
    static String JSON_MARKER = " JSON ";
    static String MAP_MARKER = " MAP ";
    boolean isJSON = false;
    boolean isMap = false;

    String expression = "";

    @Override
    public AbstractKeyword generateFromLine(String line) {
        isOptional = isStepOptional(line);
        if(prepareLine(line).toLowerCase().matches(LABEL.toLowerCase()+".*")){
            GetValueFromSavedKeyword result = (GetValueFromSavedKeyword)super.generateFromLine(line);

            Pattern p =  Pattern.compile("('(.*?)')|(\"(.*?)\")");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.data = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.expression = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.target = line.substring(matcher.start()+1, matcher.end()-1);

            if(line.contains(JSON_MARKER))
                result.isJSON = true;
            else if(line.contains(MAP_MARKER))
                result.isMap = true;

            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {
        //TODO processing of maps

        data = (String)executor.testDataRepository.getData(data); // process dynamic values
        expression = (String)executor.testDataRepository.getData(expression); // process dynamic values

        //by text - use regexp
        String result = data.replace("\n","").replaceAll(expression, "$1");

        executor.testDataRepository.setData(target, result);

        return true;
    }
}
