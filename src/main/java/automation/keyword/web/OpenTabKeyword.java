package automation.keyword.web;

import automation.annotations.KeywordRegexp;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;

/**
 *     Open next tab if available <br>
 *     Close current tab and open previous tab<br>
 */

public class OpenTabKeyword extends AbstractKeyword {

    @KeywordRegexp("Close current tab")
    static String LABEL_PREV = "close current tab";
    @KeywordRegexp("Open next tab")
    static String LABEL_LAST = "open next tab";

    boolean CLOSE_CURRENT = false;

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL_PREV.toLowerCase())){
            OpenTabKeyword result = (OpenTabKeyword) super.generateFromLine(line);
            result.CLOSE_CURRENT = true;
            return result;
        }
        if(prepareLine(line).toLowerCase().startsWith(LABEL_LAST.toLowerCase())){
            OpenTabKeyword result = (OpenTabKeyword) super.generateFromLine(line);
            result.CLOSE_CURRENT = false;
            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {
        if(this.CLOSE_CURRENT) {
            executor.page.close();
            executor.page.driver().switchTo().window((String)executor.page.driver().getWindowHandles().toArray()[0]);
        } else
            executor.page.openNextTab();
        return true;
    }
}