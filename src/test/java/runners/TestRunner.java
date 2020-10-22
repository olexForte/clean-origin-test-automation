package runners;

import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.devtools.DevTools;
import org.testng.annotations.Test;
import automation.web.BasePage;
//import static org.openqa.selenium.support.locators.RelativeLocator.withTagName;

public class TestRunner extends BaseUITest{

    @Test
    public void testexec(){
        WebDriver driver = BasePage.driver();
//        ((Selenium4Driver) driver).setBrowserLocation("Alabama");
//        ((Selenium4Driver)driver).get("http://www.google.com");
//        DevTools devtools = ((Selenium4Driver) driver).getDevTools();
//        System.out.println(devtools.toString());
        driver.quit();

    }
}
