package test.java.pageObjectsiOS;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import test.java.appModules.AppiumUtils;

public class SettingsPageiOS implements test.java.pageObjects.SettingsPage {

    public WebElement settingsContainer(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//XCUIElementTypeOther[@name=\"settings-container\"]",
                "Settings Container"
        );
    }

    public WebElement settingsHeader(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//XCUIElementTypeStaticText[@name=\"Settings\"]",
                "Settings Header"
        );
    }

    public WebElement burgerMenuButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "(//XCUIElementTypeOther[@name=\"burger-menu\"])[2]",
                "Burger Menu Button"
        );
    }

    public WebElement createBackupButton(AppiumDriver driver) throws Exception {
        return null;
    }

    public WebElement manualBackupButton(AppiumDriver driver) throws Exception {
        return null;
    }

    public WebElement automaticCloudBackupsButton(AppiumDriver driver) throws Exception {
        return null;
    }

    public WebElement biometricsButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//XCUIElementTypeOther[@name=\"Biometrics Use your finger or face to secure app\"]",
                "Biometrics Button"
        );
    }

    public WebElement passcodeButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//XCUIElementTypeOther[@name=\"Passcode View/Change your Connect.Me passcode\"]",
                "Passcode Button"
        );
    }

    public WebElement chatButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//XCUIElementTypeOther[@name=\"Give app feedback Tell us what you think of Connect.Me\"]",
                "Chat Button"
        );
    }

    public WebElement aboutButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//XCUIElementTypeOther[@name=\"About Legal, Version, and Network Information\"]",
                "About Button"
        );
    }

    public WebElement onfidoButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//XCUIElementTypeOther[@name=\"Get your ID verified by Onfido ONFIDO\"]",
                "Onfido Button"
        );
    }

}
