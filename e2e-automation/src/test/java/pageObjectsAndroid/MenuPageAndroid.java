package test.java.pageObjectsAndroid;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import test.java.appModules.AppiumUtils;

public class MenuPageAndroid implements test.java.pageObjects.MenuPage {

    public WebElement menuContainer(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//android.view.ViewGroup[@content-desc=\"menu-container\"]",
                "Menu Container"
        );
    }

    public WebElement connectMeBanner(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//android.view.ViewGroup[@content-desc=\"menu-container\"]/android.view.ViewGroup[1]",
                "ConnectMe Banner"
        );
    }

    public WebElement userAvatar(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//android.view.ViewGroup[@content-desc=\"user-avatar\"]",
                "User Avatar"
        );
    }

    public WebElement okButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"OK\"]",
                "OK Button"
        );
    }

    public WebElement menuAllowButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findTextElementInDifferentCases(driver, "Allow", "Allow Button");
    }

    public WebElement homeButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"Home\"]",
                "Home Button"
        );
    }

    public WebElement myConnectionsButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"My Connections\"]",
                "My Connections Button"
        );
    }

    public WebElement myCredentialsButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"My Credentials\"]",
                "My Credentials Button"
        );
    }

    public WebElement settingsButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"Settings\"]",
                "Settings Button"
        );
    }

    public WebElement connectMeLogo(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//android.widget.ImageView[@content-desc=\"connect-me-logo\"]",
                "ConnectMe Logo"
        );
    }

    public WebElement builtByFooter(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[contains(@text, \"built by\")]",
                "Built By Footer"
        );
    }

    public WebElement versionFooter(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[contains(@text, \"Version\")]",
                "Version Footer"
        );
    }

}
