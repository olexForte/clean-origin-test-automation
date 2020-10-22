package automation.keyword.validation;

import automation.annotations.KeywordRegexp;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//UNDER CONSTRUCTION

public class ValidateTableKeyword extends AbstractKeyword {

    private ValidateTableKeyword.ActionType action = ValidateTableKeyword.ActionType.EQUALS;

    public enum ActionType {
        EQUALS(" equals "),
        CONTAINS(" contains "),
        SIMILAR(" similar "),
        NOT_CONTAINS(" does not contain "),
        MATCHES(" match ");

        String marker;
        ActionType(String marker){
            this.marker = marker;
        }

    }

    String ROWCOUNT_MARKER = " rowcount ";
    boolean GET_ROWCOUNT = false;

    @KeywordRegexp("Validate table 'locatorORdata' [rowcount] [equals] 'value';")
    static String LABEL = "Validate table";

    @Override
    public AbstractKeyword generateFromLine(String line) {

        isOptional = isStepOptional(line);
        if (prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())) {
            ValidateTableKeyword result = (ValidateTableKeyword) super.generateFromLine(line);

            Pattern p = Pattern.compile("('(.*?)')|(\"(.*?)\")");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.data = line.substring(matcher.start() + 1, matcher.end() - 1);
            matcher.find();
            result.target = line.substring(matcher.start() + 1, matcher.end() - 1);

            if(line.contains(ROWCOUNT_MARKER)){
                result.GET_ROWCOUNT = true;
            }

            for (ValidateTableKeyword.ActionType act : ValidateTableKeyword.ActionType.values())
                if (line.contains(act.marker))
                    result.action = act;

            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {

        boolean result = false;

        // Getting actual value
        List<HashMap<String, String>> firstValue = null;
        firstValue = executor.testDataRepository.getComplexDataList(data); // process dynamic values

        String secondValue = "";

        if(GET_ROWCOUNT){
            secondValue = (String)executor.testDataRepository.getData(target);
        }

        switch (action) {

            case EQUALS:
                String rowcount = String.valueOf(firstValue.size());
                result = rowcount.equals(secondValue);
                if (result) {
                    executor.reporter.pass("Rowcount actual: " + rowcount + " equals to expected " + secondValue);
                } else {
                    executor.reporter.fail("Rowcount actual: " + rowcount + " does not equal to expected " + secondValue);
                    return false;
                }
                break;
//            case CONTAINS:
//
//                break;
//            case SIMILAR:

//                break;
//            case NOT_CONTAINS:

//                break;
//            case MATCHES:

//                break;
        }
        return true;
    }

}