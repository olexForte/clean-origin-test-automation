package automation.keyword.general;

import automation.annotations.KeywordRegexp;
import automation.datasources.FileManager;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compare Actual file (from downloads dir) with expected (from data dir)<br>
 *     Example: Compare file 'files.APIResults1' to 'saved.fileName';<br>
 *
 */
public class CompareFilesKeyword extends AbstractKeyword {
    @KeywordRegexp("Compare actual file 'filename1' to expected 'filename2';")
    static String LABEL = "compare actual file";

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().matches(LABEL.toLowerCase()+".*")){
            CompareFilesKeyword result = (CompareFilesKeyword)super.generateFromLine(line);

            Pattern p = Pattern.compile("(('(.*?)')|(\"(.*?)\"))");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.target = (line.substring(matcher.start()+1, matcher.end()-1));
            matcher.find();
            result.data = (line.substring(matcher.start()+1, matcher.end()-1));

            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {

        File actualFile = new File(FileManager.getFileFromDownloadDir((String)executor.testDataRepository.getData(target))); //get output file name
        File expectedFile = executor.testDataRepository.getDataFile(data); // get expected file location
        if(FileManager.compareFiles(actualFile, expectedFile)) {
            executor.reporter.info("Files are equal");
            return true;
        } else {
            executor.reporter.fail("Files are not equal");
            return false;
        }

    }
}
