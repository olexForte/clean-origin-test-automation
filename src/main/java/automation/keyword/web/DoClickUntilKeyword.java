package automation.keyword.web;

import automation.annotations.KeywordRegexp;
import automation.keyword.AbstractKeyword;
import automation.execution.TestStepsExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Do click on elements described by locators until target element condition will be reached<br>
 *     <b>Example: </b>
 *         Do click on "button" until 'label' Is visible;
 */
public class DoClickUntilKeyword extends AbstractKeyword {

    private static final Logger logger = LoggerFactory.getLogger(DoClickUntilKeyword.class);
    private static final String SEPARATOR_BETWEEN_ITEMS_AND_TARGET = "until";
    private static final String SEPARATOR_BETWEEN_TARGET_AND_TIMEOUT = "visible";
    private static final String SEPARATOR_BETWEEN_TIMEOUTS = "with";

    @KeywordRegexp("Do click on 'locator'['locator'] until 'target' is [visible|invisible] [up to 'maxTimeoutInSeconds' seconds] [with timeout between clicks 'timeoutInSeconds'];")
    static String LABEL = "do click on";

    String INVISIBLE_MARK = "is invisible";
    boolean INVISIBLE = false;

    List<String> allItemsToInteractWith = new LinkedList<>();

    int maxTimeoutInSeconds = 300;
    int timeoutBetweenClicks = 1;

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            DoClickUntilKeyword result = (DoClickUntilKeyword)super.generateFromLine(line);

            Pattern p =  Pattern.compile("('(.*?)')|(\"(.*?)\")");
            String[] partsOfLine = line.split(SEPARATOR_BETWEEN_ITEMS_AND_TARGET);

            //get items to click on
            Matcher matcher = p.matcher(partsOfLine[0]);
            while(matcher.find()) {
                result.allItemsToInteractWith.add(line.substring(matcher.start()+1, matcher.end()-1));
            }

            //get target
            matcher = p.matcher(partsOfLine[1]);
            matcher.find();
            result.target = partsOfLine[1].substring(matcher.start()+1, matcher.end()-1);


            String lineWithTimeouts = partsOfLine[1].substring (
                    partsOfLine[1].lastIndexOf(SEPARATOR_BETWEEN_TARGET_AND_TIMEOUT) +  SEPARATOR_BETWEEN_TARGET_AND_TIMEOUT.length()).trim().replace(";","");

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
        for(int i = 0; i< allItemsToInteractWith.size(); i++) { // process dynamic values
            String item = allItemsToInteractWith.get(i);
            updatedListOfItemsToInteractWith.add(executor.locatorsRepository.getTarget(item));
        }
        //process dynamic values for target
        target = executor.locatorsRepository.getTarget(target);

        executor.page.clickOnElementsUntilConditionReached(target, updatedListOfItemsToInteractWith, INVISIBLE, timeoutBetweenClicks, maxTimeoutInSeconds);
        return true;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "{ " +
                ", target='" + target + '\'' +
                ", data='" + allItemsToInteractWith.stream().collect(Collectors.joining(",")) + '\'' +
                ", isOptional = '" + isOptional + '\'' +
                '}';
    }
}
