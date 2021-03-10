package test.java.pageObjectsAndroid;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import test.java.appModules.AppiumUtils;

public class SettingsPageAndroid implements test.java.pageObjects.SettingsPage {

    public WebElement settingsContainer(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//android.view.ViewGroup[@content-desc=\"settings-container\"]",
                "Settings Container"
        );
    }

    public WebElement settingsHeader(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"Settings\"]",
                "Settings Header"
        );
    }

    public WebElement burgerMenuButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//android.view.ViewGroup[@content-desc=\"burger-menu\"]",
                "Burger Menu Button"
        );
    }

    public WebElement createBackupButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"Create a Backup\"]",
                "Create Backup Button"
        );
    }

    public WebElement manualBackupButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"Manual Backup\"]",
                "Manual Backup Button"
        );
    }

    public WebElement automaticCloudBackupsButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"Automatic Cloud Backups\"]",
                "Automatic Cloud Backups Button"
        );
    }

    public WebElement biometricsButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"Biometrics\"]",
                "Biometrics Button"
        );
    }

    public WebElement passcodeButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"Passcode\"]",
                "Passcode Button"
        );
    }

    public WebElement chatButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"Give app feedback\"]",
                "Chat Button"
        );
    }

    public WebElement aboutButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"About\"]",
                "About Button"
        );
    }

    public WebElement onfidoButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"Get your ID verified by Onfido\"]",
                "Onfido Button"
        );
    }

}
