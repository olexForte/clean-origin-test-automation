package automation.keyword.web;

import automation.annotations.KeywordRegexp;
import automation.configuration.SessionManager;
import automation.datasources.FileManager;
import automation.datasources.RandomDataGenerator;
import automation.execution.TestStepsExecutor;
import automation.keyword.AbstractKeyword;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Set file location to Upload input<br>
 *     Examples:<br>
 *         Upload file 'companyData.FILE_WITH_EMPLOYEES' to 'employeePage.UPLOAD_SPREADSHEET_INPUT';<br>
 */
public class UploadFileKeyword extends AbstractKeyword {

    @KeywordRegexp("Upload file 'data' to 'locator' [with randomization];")
    static String LABEL = "upload file ";

    String RANDOMIZATION_LABEL = "randomization";
    boolean RANDOMIZATION_REQUIRED = false;

    @Override
    public AbstractKeyword generateFromLine(String line) {
        if(prepareLine(line).toLowerCase().startsWith(LABEL.toLowerCase())){
            UploadFileKeyword result = (UploadFileKeyword)super.generateFromLine(line);

            Pattern p =  Pattern.compile("('(.*?)')|(\"(.*?)\")");
            Matcher matcher = p.matcher(line);
            matcher.find();
            result.data = line.substring(matcher.start()+1, matcher.end()-1);
            matcher.find();
            result.target = line.substring(matcher.start()+1, matcher.end()-1);

            if(line.toLowerCase().contains(RANDOMIZATION_LABEL))
                result.RANDOMIZATION_REQUIRED = true;
            return result;
        }
        return null;
    }

    @Override
    public boolean execute(TestStepsExecutor executor) throws Exception {
        target = executor.locatorsRepository.getTarget(target); // process dynamic values
        if(RANDOMIZATION_REQUIRED)
            data = RandomDataGenerator.randomizeFile(executor.testDataRepository.getDataFile(data));
        else
            data = (String)executor.testDataRepository.getDataFile(data).getAbsoluteFile().getPath();
        //TODO in case of BS - upload file to BS
        executor.page.setUploadFileField(target, data, isOptional);
        return true;
    }

}
