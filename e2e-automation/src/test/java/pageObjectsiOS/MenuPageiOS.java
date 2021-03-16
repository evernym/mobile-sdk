package test.java.pageObjectsiOS;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import test.java.appModules.AppiumUtils;

public class MenuPageiOS implements test.java.pageObjects.MenuPage {

    public WebElement menuContainer(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "(//XCUIElementTypeOther[@name=\"menu-container\"])[2]",
                "Menu Container"
        );
    }

    public WebElement connectMeBanner(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "(//XCUIElementTypeOther[@name=\"user-avatar\"])[1]/XCUIElementTypeOther[1]/XCUIElementTypeOther/XCUIElementTypeOther",
                "ConnectMe Banner"
        );
    }

    public WebElement userAvatar(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "(//XCUIElementTypeOther[@name=\"user-avatar\"])[2]",
                "User Avatar"
        );
    }

    public WebElement okButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//XCUIElementTypeButton[@name=\"OK\"]",
                "Ok Button"
        );
    }

    public WebElement menuAllowButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//XCUIElementTypeButton[@name=\"OK\"]",
                "Allow Button"
        );
    }

    public WebElement homeButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//XCUIElementTypeButton[@name=\"Home\"]",
                "Home Button"
        ); // it doesn't work [value or label]
    }

    public WebElement myConnectionsButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//XCUIElementTypeButton[@name=\"My Connections\"]",
                "My Connections Button"
        ); // it doesn't work [value or label]
    }

    public WebElement myCredentialsButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//XCUIElementTypeButton[@name=\"My Credentials\"]",
                "My Credentials Button"
        ); // it doesn't work [value or label]
    }

    public WebElement settingsButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//XCUIElementTypeButton[@name=\"Settings\"]",
                "Settings Button"
        ); // it doesn't work [value or label]
    }

    public WebElement connectMeLogo(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//XCUIElementTypeOther[@name=\"connect-me-logo\"]",
                "ConnectMe Logo"
        );
    }

    public WebElement builtByFooter(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[contains(@name, \"built by\")]",
                "Built By Footer"
        );
    }

    public WebElement versionFooter(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[contains(@name, \"Version\")]",
                "Version Footer"
        );
    }

}
