package test.java.pageObjectsAndroid;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import test.java.appModules.AppiumUtils;

public class PasscodePageAndroid implements test.java.pageObjects.PasscodePage {

    public WebElement backArrow(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//android.view.ViewGroup[@content-desc=\"left-icon\"]",
                "Back Arrow"
        );
    }


    public WebElement passcodeTitle(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(driver, "//android.widget.TextView[@content-desc=\"Enter your passcode\"]", "Passcode Code Title");
    }

}
