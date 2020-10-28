package automation.keyword.general;

import automation.annotations.KeywordRegexp;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Calculate data values (can be used to Calculate values/discounts)<br>
 *     Supports % calculation <br>
 *     Example: Calculate value '9$' plus 'general.data' minus '10%' plus '10' to 'saved.result';
 */
public class CalculateKeyword extends AbstractKeyword {

    @KeywordRegexp("Calculate value 'saved.val1' [plus|minus|div|mod] 'saved.value2' [[plus|minus|div|mod] 'saved.value2'] to 'saved.result';")
    static String LABEL =     "calculate value";

    private ArrayList<String> operands = new ArrayList<String>();
    private ArrayList<String> operators = new ArrayList<String>();

    static String PLUS_OPERATOR = "plus";
    static String MINUS_OPERATOR = "minus";
    static String DIV_OPERATOR = "div";
    static String MOD_OPERATOR = "mod";
    static String TO_OPERATOR = "to";

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().matches(LABEL.toLowerCase()+".*")){
            CalculateKeyword result = (CalculateKeyword)super.generateFromLine(line);

            Pattern p =  Pattern.compile(" (plus|minus|div|mod|to) ");
            Matcher matcher = p.matcher(line);
            while(matcher.find()) {
                result.operators.add(matcher.group(0).trim());
            }
            p =  Pattern.compile("(('(.*?)')|(\"(.*?)\"))");
            matcher = p.matcher(line);
            while(matcher.find()) {
                result.operands.add(line.substring(matcher.start()+1, matcher.end()-1));
            }
            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {

        float result = toValue( (String)executor.testDataRepository.getData(operands.get(0)), 0); // save initial value as result

        for(int operationNumber = 0; operationNumber <operators.size();operationNumber++){
            String operator = operators.get(operationNumber);
            float curOperand = 0f;
            if(operator.equals(PLUS_OPERATOR)){
                curOperand = toValue((String)executor.testDataRepository.getData(operands.get(operationNumber+1)), result);
                result = result + curOperand;
            }
            if(operator.equals(MINUS_OPERATOR)){
                curOperand = toValue((String)executor.testDataRepository.getData(operands.get(operationNumber+1)), result);
                result = result - curOperand;
            }
            if(operator.equals(DIV_OPERATOR)){
                curOperand = toValue((String)executor.testDataRepository.getData(operands.get(operationNumber+1)), result);
                result = Math.round(result / curOperand);
            }
            if(operator.equals(MOD_OPERATOR)){
                curOperand = toValue((String)executor.testDataRepository.getData(operands.get(operationNumber+1)), result);
                result = Math.floorMod(Math.round(result) , Math.round(curOperand));
            }
            if(operator.equals(TO_OPERATOR)){
                executor.testDataRepository.setData(operands.get(operationNumber+1), String.format("%.2f", result));
            }
        }

        return true;
    }

    /**
     * Transform string into value<br>
     * Process percentage if required<br>
     *     Removes $ and , chars
     * @param string
     * @param result
     * @return
     */
    float toValue(String string, float result){
        if(string.contains("%")){
            float percents = Float.valueOf(string.replace("%", "").replace(",","").trim());
            return (result/100)*percents;
        } else{
            return Float.valueOf(string.replace("$", "").replace(",","").trim());
        }
    }
}
