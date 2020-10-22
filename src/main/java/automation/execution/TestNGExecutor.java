package automation.execution;

import com.google.common.collect.Lists;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import java.util.List;

/**
 * Main class to run automation from command line<br>
 *<br>
 * Example of command line execution:<br>
 * java -Dconfig=CONFIG  -Dtests=TEST_SUITE -classpath keywordBasedFramework-1.0-SNAPSHOT.jar:keywordBasedFramework-1.0-SNAPSHOT-tests.jar TestNGExecutor<br>
 *<br>
 * To create executable jar with dependencies use : mvn package <br>
 * it will create in target directory: <br>
 * keywordBasedFramework-1.0-SNAPSHOT-tests.jar<br>
 * keywordBasedFramework-1.0-SNAPSHOTjar<br>
 */

public class TestNGExecutor {
    public static void main(String[] args) {

        TestListenerAdapter tla = new TestListenerAdapter();
        TestNG testng = new TestNG();
        List<String> suites = Lists.newArrayList();
        suites.add("tests.xml");//path to xml
        testng.setTestSuites(suites);
        testng.addListener(tla);
        testng.run();

    }
}
