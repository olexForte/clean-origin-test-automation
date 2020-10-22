package automation.entities;

/**
 * Class represents file with Test <br> test name and meta information (tags)
 */
public class TestFile {

    String testName;
    String[] tags;

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }
}
