package test.java.pageObjectsAndroid;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import test.java.appModules.AppiumUtils;

public class RestorePageAndroid implements test.java.pageObjects.RestorePage {

    public WebElement restoreFromBackupButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"Restore From A Backup\"]",
                "Restore From Backup Button"
        );
    }

    public WebElement restoreHeader(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"Where is your backup?\"]",
                "Restore Header"
        );
    }

    public WebElement restoreFromCloudButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"In the Evernym Cloud\"]",
                "Restore From Cloud Button"
        );
    }

    public WebElement restoreFromDeviceButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"On this device\"]",
                "Restore From Device Button"
        );
    }

    public WebElement zipFileSelector(AppiumDriver driver, String fileName) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[contains(@text, \""+fileName+"\")]",
                "Zip File Selector"
        );
    }

    public WebElement recoveryPhraseBoxLocal(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//android.widget.EditText[@content-desc=\"restore-encrypt-phrase-text-input\"]",
                "Recovery Phrase Box Local"
        );
    }

    public WebElement recoveryPhraseBoxCloud(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//android.widget.EditText[@content-desc=\"verify-passphrase-container-text-input\"]",
                "Recovery Phrase Box Cloud"
        );
    }

    public WebElement recoveryWaitingMessage(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"Please wait while your data is restored\"]",
                "Recovery Waiting Message"
        );
    }

    public WebElement enterPasscodeMessage(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"Please enter your current Connect.Me passcode!\"]",
                "Enter Passcode Message"
        );
    }

}
