package runners;

import automation.entities.TestFile;
import automation.execution.TestStepsReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import automation.datasources.FileManager;
import automation.configuration.ProjectConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class MainDataProvider {

    private static final Logger logger = LoggerFactory.getLogger(MainDataProvider.class);

    static int NUMBER_OF_THREADS = ProjectConfiguration.getConfigProperty("NumberOfThreads") != null ? Integer.valueOf(ProjectConfiguration.getConfigProperty("NumberOfThreads")) : 3;

//    public static Iterator<Object[]> getTestNames(int indexOfTest) {
//        List<Object[]> testFiles;
//        String[] scriptFileNames = null;
//
//        String tags = ProjectConfiguration.getConfigProperty("tags");
//        String fileWithListOfTests = ProjectConfiguration.getConfigProperty("tests");
//
//        logger.info("Tags: " + tags);
//        logger.info("Test suite: " + fileWithListOfTests);
//
//        if(fileWithListOfTests == null) { // no test suite specified
//            logger.info("No Test Suite Specified");
//            if(tags == null){
//                logger.error("No test Tags Specified");
//            } else { // some tags were found
//                scriptFileNames = FileManager.getFilesFromScriptsDir().stream().filter(file -> fileContainsOneOfTags(file, tags)).map(file -> (Object)file.getName()).toArray(size -> new String[size]);
//            }
//        } else {
//            String suiteFileContent = FileManager.getFileContent(FileManager.getFileFromSuitesDir(fileWithListOfTests));
//            scriptFileNames = Arrays.stream(suiteFileContent.split("\n")).filter(line -> !isLineCommentOrBlank(line)).toArray(size -> new String[size]);
//        }
//
//        if(scriptFileNames.length == 0){
//            logger.error("No files to run were found");
//        }
//
//        //select only tests for specified index
//        testFiles = selectTestWithIndex(scriptFileNames, indexOfTest);
//
//        return testFiles.iterator();
//    }

    public static Iterator<Object[]> getTestObjects(int indexOfTest) {
        List<Object[]> testFiles;
        String[] scriptFileNames = null;

        String tags = ProjectConfiguration.getConfigProperty("tags");
        String fileWithListOfTests = ProjectConfiguration.getConfigProperty("tests");

        logger.info("Tags: " + tags);
        logger.info("Test suite: " + fileWithListOfTests);

        if(fileWithListOfTests == null) { // no test suite specified
            logger.info("No Test Suite Specified");
            if(tags == null){
                logger.error("No test Tags Specified");
            } else { // some tags were found
                scriptFileNames = FileManager.getFilesFromScriptsDir().stream()
                        .filter(file -> fileContainsOneOfTags(file, tags))
                        .map(file -> file.getName().substring(0, file.getName().lastIndexOf(".")))
                        .toArray(size -> new String[size]);
            }
        } else {
            String suiteFileContent = FileManager.getFileContent(FileManager.getFileFromSuitesDir(fileWithListOfTests));
            scriptFileNames = Arrays.stream(suiteFileContent.split("\n"))
                    .filter(line -> !isLineCommentOrBlank(line))
                    .toArray(size -> new String[size]);
        }

        if(scriptFileNames == null || scriptFileNames.length == 0){
            logger.error("No files to run were found");
            return null;
        }

        //select only tests for specified index
        testFiles = getTestObjectsWithIndex(scriptFileNames, indexOfTest);

        return testFiles.iterator();
    }

    /**
     * Select items with corresponding index from list
     * @param items array of items
     * @param index index of items
     * @return list of items
     */
    private static List<Object[]> getTestObjectsWithIndex(String[] items, int index) {
        List<Object[]> result = new ArrayList<>();
        int i = 0;
        for (String currentScriptFile : items) {
            TestFile test = new TestFile();
            test.setTestName(currentScriptFile);
            test.setTags(getTagsFromTestFile(currentScriptFile));
            i++;
            if (index == i)
                result.add(new Object[]{test});
            if (i == NUMBER_OF_THREADS)
                i = 0;
        }
        return result;
    }

    private static String[] getTagsFromTestFile(String scriptFile) {
        String lineWithTags = null;
        try {
            lineWithTags = FileManager.getLineThatStartsWithSubstring(
                    FileManager.getFileFromScriptsDir(scriptFile), TestStepsReader.TAG_LABEL);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(lineWithTags == null)
            return null;

        return getTagsFromString(lineWithTags);
    }

    /**
     * Check that line in suite file is commented or blank
     * @param line line from suite file
     * @return is blank or commented
     */
    private static boolean isLineCommentOrBlank(String line) {
        return TestStepsReader.isLineShouldBeSkiped(line);
    }

    /**
     * Check that file contains required Tags
     * @param file file object
     * @param expectedTags list of tags separated by comma
     * @return is tag present
     */
    private static boolean fileContainsOneOfTags(File file, String expectedTags) {
        String line = null;
        try {
            line = FileManager.getLineThatStartsWithSubstring(file, TestStepsReader.TAG_LABEL);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] actualTags = getTagsFromString(line);
        if(actualTags == null)
            return false;

        for(String expectedTag : expectedTags.toLowerCase().split(TestStepsReader.TAGS_DELIMITER)){
            for(String actualTag : actualTags)
                if(actualTag.equals(expectedTag)) // TAGS SHOULD BE UNIQUE
                    return true;
        }
        return false;
    }

    private static String[] getTagsFromString(String lineWithTags) {
        if(lineWithTags == null || lineWithTags.equals(""))
            return null;
        return lineWithTags       // TODO make it good
                .replace(TestStepsReader.COMMENT_START_LABEL,"")
                .replace(" ","")
                .replace("\r","")
                .replace("\n","")
                .replace(TestStepsReader.TAG_LABEL, "")
                .toLowerCase()
                .split(TestStepsReader.TAGS_DELIMITER);
    }

    //DEPRECATED
    @DataProvider(name="Scripts to run", parallel = true)
    Iterator<Object[]> getScriptsToRun(Method method){
        int INDEX_OF_TESTS = Integer.valueOf(method.getName().replace("runner",""));
        int i = 0;
        ArrayList<Object[]> inputFiles = new ArrayList<>();
        String fileWithListOfTests = ProjectConfiguration.getConfigProperty("tests");
        String testFiles = FileManager.getFileContent(FileManager.getFileFromSuitesDir(fileWithListOfTests));
        String[] files = testFiles.split("\n");

        for(String scriptFile : files){
            if(TestStepsReader.isLineShouldBeSkiped(scriptFile))
                continue;
            i++;
            if (INDEX_OF_TESTS == i)
                inputFiles.add(new Object[]{scriptFile});
            if(i == NUMBER_OF_THREADS)
                i = 0;
        }
        return inputFiles.iterator();
    }
}
