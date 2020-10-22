package automation.keyword;

import automation.execution.TestStepsExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractKeyword {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractKeyword.class);

    static public String OPTIONAL_STEP_LABEL = "Optional";
    static public String FINAL_STEP_LABEL = "Final";

    @Override
    public String toString() {
        return this.getClass().getName() + "{ " +
                ", target='" + target + '\'' +
                ", data='" + data + '\'' +
                ", isOptional = '" + isOptional + '\'' +
                ", isFinal = '" + isFinal + '\'' +
                '}';
    }

    public String target; // where : url, locators
    public String data; // what : parameters from data directory or random
    public boolean isOptional = false; // should we report error in case of step failure
    public boolean isFinal = false; // should be executed even in case of failure
    public String originalLine;

    public AbstractKeyword generateFromLine(String line) {
        this.originalLine = line;
        this.isFinal = isKeywordFinal(line);
        this.isOptional = isStepOptional(line);
        return this;
    }

    public boolean execute(TestStepsExecutor executor) throws Exception {
        return true;
    }

    public String toShortString() {
        return this.getClass().getName() +  (target == null ? "" : " \'" + target + "\'") + (data == null ? "" : " \'" + data + "\'");
    }

    public static String prepareLine(String line){
        return line.replace(OPTIONAL_STEP_LABEL, "").replace(FINAL_STEP_LABEL, "").trim();
    }

    public static boolean isStepOptional(String line){
        return line.toLowerCase().startsWith(OPTIONAL_STEP_LABEL.toLowerCase());
    }

    public static boolean isKeywordFinal(String line){
        return line.toLowerCase().startsWith(FINAL_STEP_LABEL.toLowerCase());
    }
}
