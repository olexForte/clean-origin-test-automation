package automation.keyword.general;

import automation.annotations.KeywordRegexp;
import automation.datasources.RandomDataGenerator;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;
import automation.keyword.web.UploadFileKeyword;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Apply Randomization to specified file
 *
 */
public class RandomizeFileKeyword extends AbstractKeyword {

        @KeywordRegexp("Randomize file 'data'")
        static String LABEL = "randomize file ";

        @Override
        public AbstractKeyword generateFromLine(String line) {
            if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
                RandomizeFileKeyword result = (RandomizeFileKeyword)super.generateFromLine(line);

                Pattern p =  Pattern.compile("('(.*?)')|(\"(.*?)\")");
                Matcher matcher = p.matcher(line);
                matcher.find();
                result.data = line.substring(matcher.start()+1, matcher.end()-1);

                return result;
            }
            return null;
        }

        @Override
        public boolean execute(TestStepsExecutor executor) throws Exception {
            RandomDataGenerator.randomizeFile(executor.testDataRepository.getDataFile(data));
            //TODO in case of BS - upload file to BS
            return true;
        }

    }
