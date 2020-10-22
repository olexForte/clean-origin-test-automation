package automation.keyword.web;

import automation.annotations.KeywordRegexp;
import automation.keyword.AbstractKeyword;
import automation.execution.TestStepsExecutor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Start browser and Open url<br>
 *     <b>Example</b>
 *         Open page 'mainPage.MAIN_URL';<br>
 */
public class OpenKeyword extends AbstractKeyword {

    @KeywordRegexp("Open page 'url' [no cleanup];")
    static String LABEL = "open page";

    String NO_CLEANUP_MARKER = "no cleanup";
    boolean NO_CLEANUP = false;

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            OpenKeyword result = (OpenKeyword) super.generateFromLine(line);

            result.data = null;
            Pattern p =  Pattern.compile("('(.*?)')|(\"(.*?)\")");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.target = line.substring(matcher.start()+1, matcher.end()-1);
            if(line.toLowerCase().contains(NO_CLEANUP_MARKER))
                result.NO_CLEANUP = true;
            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {
        if(!NO_CLEANUP)
            executor.page.setupDriver();
        String url = executor.locatorsRepository.getTarget(target); // process dynamic values
        if(url == null)
            url = (String)executor.testDataRepository.getData(target);
        executor.page.open(url);
        return true;
    }
}
