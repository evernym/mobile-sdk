package test.java.pageObjectsAndroid;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import test.java.appModules.AppiumUtils;

public class MyCredentialsPageAndroid implements test.java.pageObjects.MyCredentialsPage {

    public WebElement myCredentialsHeader(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"My Credentials\"]",
                "My Credentials Header"
        );
    }

    public WebElement burgerMenuButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//android.view.ViewGroup[@content-desc=\"burger-menu\"]",
                "Burger Menu Button"
        );
    }

    public WebElement testCredential(AppiumDriver driver, String name) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"" + name + "\"]",
                "Test Credential Item"
        );
    }

    public WebElement testCredentialTitle(AppiumDriver driver, String name) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//android.widget.TextView[@content-desc=\"" + name + "-title\"]",
                "Test Credential Title"
        );
    }

    public WebElement testCredentialAvatar(AppiumDriver driver, String name) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//android.view.ViewGroup[@content-desc=\"" + name + "-avatar\"]",
                "Test Credential Avatar"
        );
    }
}
