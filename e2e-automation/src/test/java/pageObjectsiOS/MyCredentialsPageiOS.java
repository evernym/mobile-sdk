package test.java.pageObjectsiOS;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import test.java.appModules.AppiumUtils;

public class MyCredentialsPageiOS implements test.java.pageObjects.MyCredentialsPage {

    public WebElement myCredentialsHeader(AppiumDriver driver) throws Exception {
      return AppiumUtils.findElement(
        driver,
        "//XCUIElementTypeStaticText[@name=\"My Credentials\"]",
        "My Credentials Header"
      );
    }

    public WebElement burgerMenuButton(AppiumDriver driver) throws Exception {
      return AppiumUtils.findElement(
        driver,
        "//XCUIElementTypeOther[@name=\"burger-menu\"]",
        "Burger Menu Button"
      );
    }

    public WebElement testCredential(AppiumDriver driver, String name) throws Exception {
      return AppiumUtils.findElementsByAccessibilityId(
        driver,
        name + "-title",
        "Test Credential Item"
      );
    }

    public WebElement testCredentialTitle(AppiumDriver driver, String name) throws Exception {
        return AppiumUtils.findElementsByAccessibilityId(
                driver,
                name + "-title",
                "Test Credential Item"
        );
    }

    public WebElement testCredentialAvatar(AppiumDriver driver, String name) throws Exception {
        return AppiumUtils.findElementsByAccessibilityId(
                driver,
                name + "-avatar",
                "Test Credential Item"
        );
    }
}
