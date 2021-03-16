package test.java.pageObjects;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;

public interface BackupPage {
    public WebElement recoveryHeader(AppiumDriver driver) throws Exception;
    public WebElement recoveryPhraseBox(AppiumDriver driver) throws Exception;
    public WebElement continueButton(AppiumDriver driver) throws Exception;
    public WebElement closeButton(AppiumDriver driver) throws Exception;
    public WebElement verifyHeader(AppiumDriver driver) throws Exception;
    public WebElement verifyPhraseBox(AppiumDriver driver) throws Exception;
    public WebElement backupFileName(AppiumDriver driver) throws Exception;
    public WebElement zipDownloadButton(AppiumDriver driver) throws Exception;
    public WebElement cloudBackupButton(AppiumDriver driver) throws Exception;
    public WebElement exportEncryptedButton(AppiumDriver driver) throws Exception;
    public WebElement saveToDriveButton(AppiumDriver driver) throws Exception;
    public WebElement saveButton(AppiumDriver driver) throws Exception;
    public WebElement doneButton(AppiumDriver driver) throws Exception;
    public WebElement cloudHeader(AppiumDriver driver) throws Exception;
    public WebElement oneCloudBackupButton(AppiumDriver driver) throws Exception;
    public WebElement enableCloudBackupsButton(AppiumDriver driver) throws Exception;
    public WebElement cloudBackupSuccessMessage(AppiumDriver driver) throws Exception;
    public WebElement cloudDoneButton(AppiumDriver driver) throws Exception;
}
