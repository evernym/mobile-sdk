package test.java.pageObjectsAndroid;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import test.java.appModules.AppiumUtils;

public class BackupPageAndroid implements test.java.pageObjects.BackupPage {

    public WebElement recoveryHeader(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"Recovery Phrase generated\"]",
                "Recovery Header"
        );
    }

    public WebElement recoveryPhraseBox(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//android.widget.TextView[@index=\"4\"]",
                "Recovery Phrase Box"
        );
    }

    public WebElement continueButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text =\"Continue\"]",
                "Continue Button"
        );
    }

    public WebElement closeButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//android.widget.ImageView[@content-desc=\"recovery-header-close-image\"]",
                "Close Button"
        );
    }

    public WebElement verifyHeader(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"Verify your Recovery Phrase\"]",
                "Verify Header"
        );
    }

    public WebElement verifyPhraseBox(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//android.widget.EditText[@content-desc=\"verify-passphrase-container-text-input\"]",
                "Verify Phrase Box"
        );
    }

    public WebElement backupFileName(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[contains(@text, \"ConnectMe-2020-\")]",
                "Backup File Name"
        );
    }

    public WebElement zipDownloadButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"Downloaded .zip Backup\"]",
                "Zip Download Button"
        );
    }

    public WebElement cloudBackupButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"Cloud Backup\"]",
                "Cloud Backup Button"
        );
    }

    public WebElement exportEncryptedButton(AppiumDriver driver) throws Exception {

        return AppiumUtils.findElement(
                driver,
                "//android.view.ViewGroup[@content-desc=\"export-encrypted-backup\"]",
                "Export Encrypted Button"
        );
    }

    public WebElement saveToDriveButton(AppiumDriver driver) throws Exception {

        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"Save to Drive\"]",
                "Save to Drive Button"
        );
    }

    public WebElement saveButton(AppiumDriver driver) throws Exception {

        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"SAVE\"]",
                "Save Button"
        );
    }


    public WebElement doneButton(AppiumDriver driver) throws Exception {

        return AppiumUtils.findElement(
                driver,
                "//android.view.ViewGroup[@content-desc=\"backup-complete-submit-button\"]",
                "Done Button"
        );
    }

    public WebElement cloudHeader(AppiumDriver driver) throws Exception {

        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"Enable Automatic Backups?\"]",
                "Cloud Header"
        );
    }

    public WebElement oneCloudBackupButton(AppiumDriver driver) throws Exception {

        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"Just One Cloud Backup\"]",
                "One Cloud Backup Button"
        );
    }

    public WebElement enableCloudBackupsButton(AppiumDriver driver) throws Exception {

        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"Enable Automatic Backups\"]",
                "Enable Automatic Backups Button"
        );
    }

    public WebElement cloudBackupSuccessMessage(AppiumDriver driver) throws Exception {

        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"Successfully backed up\"]",
                "Cloud Backup Success Message"
        );
    }

    public WebElement cloudDoneButton(AppiumDriver driver) throws Exception {

        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"Done\"]",
                "Done Button"
        );
    }

}
