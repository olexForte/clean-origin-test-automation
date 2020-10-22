package runners;

import automation.entities.TestFile;
import automation.execution.ITestExecutor;
import automation.execution.TestsExecutor;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;

public class Test1 extends BaseUITest {

    int INDEX_OF_TESTS = 1;

    @DataProvider(name="Scripts to run")
    Iterator<Object[]> getScriptsToRun(){
        return MainDataProvider.getTestObjects(INDEX_OF_TESTS);
    }

    @Test(dataProvider = "Scripts to run")
    public void runner1(TestFile file){

        ITestExecutor te = new TestsExecutor();
        if (!te.executeAllStepsFromFile(file))
            Assert.fail("Test failed");
    }

}
