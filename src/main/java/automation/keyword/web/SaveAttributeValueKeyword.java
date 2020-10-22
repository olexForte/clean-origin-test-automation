package automation.keyword.web;

import automation.annotations.KeywordRegexp;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Save attribute value of element to variable <br>
 * <b>Example: </b>
 *     save element attribute 'position' 'mycartpage.grand_total_field' to 'saved.val1';
 */
public class SaveAttributeValueKeyword extends AbstractKeyword {

    private static final String REPLACE_ACTION_LABEL = "REPLACE:";
    private static final String TRIM_ACTION_LABEL = "TRIM";
    private static final String UP_ACTION_LABEL = "UP";
    private static final String LOW_ACTION_LABEL = "LOW";
    private static final String REMOVE_ACTION_LABEL = "REMOVE";
    private static final String PARAMETER_SPLITER = ":";

    @KeywordRegexp("Save element attribute 'attribute' from 'locator' to 'SAVED.data' [and 'TRIM|UP|LOW|REMOVE:what|REPLACE:what:with'];")
    static String LABEL = "save element attribute";

    String action = "";

    String attribute = "value";

    @Override
    public AbstractKeyword generateFromLine(String line) {
        isOptional = isStepOptional(line);
        if(prepareLine(line).toLowerCase().matches(LABEL.toLowerCase()+".*")){
            SaveAttributeValueKeyword result = (SaveAttributeValueKeyword)super.generateFromLine(line);

            Pattern p =  Pattern.compile("('(.*?)')|(\"(.*?)\")");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.attribute = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.target = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.data = line.substring(matcher.start()+1, matcher.end()-1);
            if(matcher.find())
                result.action = line.substring(matcher.start()+1, matcher.end()-1);
            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {
        String processedValue = "";
        String fieldValue = target;
        target = executor.locatorsRepository.getTarget(target); // process dynamic values

        //if target was found in locations
        processedValue =  executor.page.getAttribute(target, attribute , isOptional);

        if(!action.equals("")){
            processedValue = performActions(processedValue);
        }

        executor.testDataRepository.setData(data, processedValue);

        return true;
    }

    /**
     * Perform action over value string (trim, replace etc.)
     * @param value
     * @return
     */
    String performActions(String value){
        String result = value;
        //replace
        //TODO processing of ':'
        if(action.toUpperCase().startsWith(REPLACE_ACTION_LABEL)){
            String[] parts = action.split(PARAMETER_SPLITER);
            return result.replace(parts[1], parts[2]);
        }
        //trim
        if(action.toUpperCase().startsWith(TRIM_ACTION_LABEL)){
            return result.trim();
        }
        //uppercase
        if(action.toUpperCase().startsWith(UP_ACTION_LABEL)){
            return result.toUpperCase();
        }
        //lowercase
        if(action.toUpperCase().startsWith(LOW_ACTION_LABEL)){
            return result.toLowerCase();
        }
        //remove
        if(action.toUpperCase().startsWith(REMOVE_ACTION_LABEL)){
            String[] parts = action.split(PARAMETER_SPLITER);
            return result.replace(parts[1], "");
        }

        return result;
    }
}
