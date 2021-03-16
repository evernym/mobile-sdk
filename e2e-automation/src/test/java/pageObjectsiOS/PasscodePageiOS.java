package test.java.pageObjectsiOS;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import test.java.appModules.AppiumUtils;

public class PasscodePageiOS implements test.java.pageObjects.PasscodePage {

    public WebElement passcodeContainer(AppiumDriver driver) throws Exception {
        return null;
    }

    public WebElement passcodeHeader(AppiumDriver driver) throws Exception {
        return null;
    }

    public WebElement backArrow(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "(//XCUIElementTypeOther[@name=\"left-icon\"])[2]",
                "Back Arrow"
        );
    }

    public WebElement passcodeTitle(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElementsByAccessibilityId(driver, 	"Enter your passcode", "Enter Passcode Title");
    }

}
