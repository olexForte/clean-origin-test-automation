package automation.execution;

import automation.entities.TestFile;

public interface ITestExecutor {
    public boolean executeAllStepsFromFile(TestFile testFile);
}
