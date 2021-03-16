package test.java.pageObjectsiOS;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import test.java.appModules.AppiumUtils;

public class AboutPageiOS implements test.java.pageObjects.AboutPage {

    public WebElement aboutHeader(AppiumDriver driver) throws Exception {
        return null;
    }

    public WebElement backArrow(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//XCUIElementTypeOther[@name=\"left-icon\"]",
                "Back Arrow"
        );
    }

    public WebElement termsAndConditionsButton(AppiumDriver driver) throws Exception {
        return null;
    }

    public WebElement privacyPolicyButton(AppiumDriver driver) throws Exception {
        return null;
    }

}
