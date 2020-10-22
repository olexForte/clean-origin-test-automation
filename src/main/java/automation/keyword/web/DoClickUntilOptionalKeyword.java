package automation.keyword.web;

import automation.annotations.KeywordRegexp;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TODO UNDER CONSTRUCTION
/**
UNDER CONSTRUCTION
 */
public class DoClickUntilOptionalKeyword extends AbstractKeyword {

    private static final Logger logger = LoggerFactory.getLogger(DoClickUntilOptionalKeyword.class);
    private static final String SEPARATOR_BETWEEN_ITEMS_AND_TARGET = "until";
    private static final String SEPARATOR_BETWEEN_TARGET_AND_TIMEOUT = "visible";
    private static final String SEPARATOR_BETWEEN_TIMEOUTS = "with";

    @KeywordRegexp("Do optional 'locator' exists click on 'button' and 'nextButton' from file 'file' until 'target' is [visible|invisible] [up to 'maxTimeoutInSeconds' seconds] [with timeout between clicks 'timeoutInSeconds'];")
    static String LABEL = "do optional";

    String INVISIBLE_MARK = "is invisible";
    boolean INVISIBLE = false;

    List<String> allItemsToInteractWith = new LinkedList<>();

    int maxTimeoutInSeconds = 300;
    int timeoutBetweenClicks = 1;

    String fileName = "";
    String locator = "";
    String button = "";
    String itemToActivate = "";

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            DoClickUntilOptionalKeyword result = (DoClickUntilOptionalKeyword)super.generateFromLine(line);

            Pattern p =  Pattern.compile("('(.*?)')|(\"(.*?)\")");

            //get items to click on
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.locator = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.button = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.itemToActivate = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.fileName = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.target = line.substring(matcher.start()+1, matcher.end()-1);

            String lineWithTimeouts = line.substring(
                    line.lastIndexOf(SEPARATOR_BETWEEN_TARGET_AND_TIMEOUT) + SEPARATOR_BETWEEN_TARGET_AND_TIMEOUT.length()).trim().replace(";", "");

            if(lineWithTimeouts.length()>0) {
                matcher = p.matcher(lineWithTimeouts);

                int indexOfTimeoutSeparator = lineWithTimeouts.indexOf(SEPARATOR_BETWEEN_TIMEOUTS);
                if(indexOfTimeoutSeparator == -1){ // no timeout between clicks specified
                    matcher.find();
                    result.maxTimeoutInSeconds = Integer.valueOf(lineWithTimeouts.substring(matcher.start()+1, matcher.end()-1).trim());
                } else if(indexOfTimeoutSeparator == 0){ // no max timeout specified
                    matcher.find();
                    result.timeoutBetweenClicks = Integer.valueOf(lineWithTimeouts.substring(matcher.start()+1, matcher.end()-1).trim());
                } else { // both timeouts specified
                    matcher.find();
                    result.maxTimeoutInSeconds = Integer.valueOf(lineWithTimeouts.substring(matcher.start()+1, matcher.end()-1).trim());
                    matcher.find();
                    result.timeoutBetweenClicks = Integer.valueOf(lineWithTimeouts.substring(matcher.start()+1, matcher.end()-1).trim());
                }
            }

            if(line.contains(INVISIBLE_MARK))
                result.INVISIBLE = true;
            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {
        List<String > updatedListOfItemsToInteractWith = new LinkedList<>();

        LinkedList<String> locators = new LinkedList<String> ();
        LinkedList<String> buttons = new LinkedList<String> ();

        List<List<String>> fields = executor.testDataRepository.getTableDataFromFile(fileName);

        //process dynamic values for target
        target = executor.locatorsRepository.getTarget(target);

        itemToActivate = executor.locatorsRepository.getTarget(itemToActivate);

        String STRING_TO_REPLACE = "VALUE_FROM_FILE";

        for(List item : fields) {
            if(item.size()>2){
                for(int i = 1; i<item.size();i++) {
                    locators.add((String) item.get(0));
                    buttons.add(executor.locatorsRepository.getTarget(button.replace(STRING_TO_REPLACE, (String) item.get(i))));
                }
            } else {
                locators.add((String) item.get(0));
                buttons.add(executor.locatorsRepository.getTarget(button.replace(STRING_TO_REPLACE, (String) item.get(1))));
            }
        }

        executor.page.clickOnElementsUntilConditionReached(target, locators, itemToActivate, buttons, INVISIBLE, timeoutBetweenClicks, maxTimeoutInSeconds);
        return true;
    }

}
