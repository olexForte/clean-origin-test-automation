package runners;

import automation.entities.TestFile;
import automation.execution.ITestExecutor;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import automation.execution.TestsExecutor;

import java.util.Iterator;

public class Test5 extends BaseUITest {

    int INDEX_OF_TESTS = 5;

    @DataProvider(name="Scripts to run")
    Iterator<Object[]> getScriptsToRun(){
        return MainDataProvider.getTestObjects(INDEX_OF_TESTS);
    }

    @Test(dataProvider = "Scripts to run")
    public void runner5(TestFile file){
        ITestExecutor te = new TestsExecutor();
        if (!te.executeAllStepsFromFile(file))
            Assert.fail("Test failed");
    }

}
