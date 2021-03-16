package test.java.pageObjectsAndroid;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import test.java.appModules.AppiumUtils;

public class AboutPageAndroid implements test.java.pageObjects.AboutPage {

    public WebElement aboutHeader(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"About this App\"]",
                "About Header"
        );
    }

    public WebElement backArrow(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//android.view.ViewGroup[@content-desc=\"left-icon\"]",
                "Back Arrow"
        );
    }

    public WebElement termsAndConditionsButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"Terms and Conditions\"]",
                "Terms And Conditions Button"
        );
    }

    public WebElement privacyPolicyButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"Privacy Policy\"]",
                "Privacy Policy Button"
        );
    }

}
