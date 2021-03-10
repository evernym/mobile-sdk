package test.java.pageObjectsAndroid;

import java.util.List;

import org.openqa.selenium.WebElement;

import io.appium.java_client.AppiumDriver;
import test.java.appModules.AppiumUtils;
import test.java.funcModules.BackupRestoreModules;
import test.java.pageObjects.BackupRestoreWalletPage;


public class BackupRestoreWalletPageAndroid implements BackupRestoreWalletPage {
	
	
	
	
	public WebElement copyRecoveryPhrase(AppiumDriver driver) throws Exception {
		
		return AppiumUtils.findElement(driver, "//android.widget.TextView[@index='4']", "Copy Recovery Phrase");	 // index is used as '4' to extract the recovery phrase
		
	}

	
	public WebElement continue_Button(AppiumDriver driver) throws Exception {
		
		return AppiumUtils.findElement(driver, "//*[@text =\"Continue\"]", "Continue button");
	}
		
	
	public WebElement inputRecoveryPhrase(AppiumDriver driver) throws Exception {
		
		return AppiumUtils.findElement(driver,"//android.widget.EditText[@content-desc='verify-passphrase-container-text-input']" , "Input Recovery Phrase");
	}
	
	
	public WebElement restoreFromAbackUp_Button(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "//android.view.ViewGroup[@content-desc='restore-from-backup']", "Restore From BackUp Button");
	}

	
	public WebElement startFresh_Button(AppiumDriver driver) throws Exception {
		
		return AppiumUtils.findElement(driver, "//android.view.ViewGroup[@content-desc='start-fresh']", "Start Fresh Button");
	}
	
	
	public WebElement exportEncrypted_Button(AppiumDriver driver) throws Exception {
		
		return AppiumUtils.findElement(driver, "//android.view.ViewGroup[@content-desc='export-encrypted-backup']", "exportEncrypted Button");
	}
	
	
	public WebElement saveToDrive_Icon(AppiumDriver driver) throws Exception {
		
		return AppiumUtils.findElement(driver, "//*[@text=\"Save to Drive\"]", "SaveToDrive Icon");
	}
	
	
	public WebElement save_Button(AppiumDriver driver) throws Exception {
		
		return AppiumUtils.findElement(driver, "//*[@text=\"SAVE\"]", "Save Button");
	}
	
	
	public WebElement done_Button(AppiumDriver driver) throws Exception {
		
		return AppiumUtils.findElement(driver, "//android.view.ViewGroup[@content-desc='backup-complete-submit-button']", "Done Button");
	}
	
	
	public WebElement backupbanner_Header(AppiumDriver driver) throws Exception {
		
		return AppiumUtils.findElement(driver, "//android.view.ViewGroup[@content-desc='wallet-backup-banner-label-touchable']", "Backup Banner");
	}
	
	
	public WebElement genPassPhraseClose_Button(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//android.widget.ImageView[@content-desc='recovery-header-close-image']", "Backup Screen Close Button");
	}
	
	
	public WebElement copyRecoveryPhraseClose_Button(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//android.widget.ImageView[@content-desc='verify-passphrase-close-image']", "Recovery Phrase screen Close Button");
	}
		
	
	public WebElement copyRecoveryPhraseBack_Button(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//android.widget.ImageView[@content-desc='verify-passphrase-back-image']", "Recovery Phrase screen Back Button");
	}
	
	
	public WebElement exportBackupClose_Button(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//android.widget.ImageView[@content-desc='export-backup-close-image']", "Export Backup screen Close Button");
	}
	
	
	public WebElement exportBackupBack_Button(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//android.widget.ImageView[@content-desc='export-backup-back-image']", "Export Backup screen Back Button");
	}
	
	
	public WebElement passphraseError_Banner(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//android.view.ViewGroup[@content-desc='verify-passphrase-error-banner']", "Passphrase Error Banner");
	}

	
	public WebElement showRootDir_Button(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//android.widget.ImageButton[@content-desc='Show roots']", "showRootDir Button");
	}

	
	public WebElement drive_Button(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text ='Drive']", "Drive Button");
	}

	
	public WebElement myDrive_Button(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text =\"My Drive\"]", "My Drive Button");

	}

	
	public WebElement backupfile_icon(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text ='"+BackupRestoreModules.backUpFileName+"']", "backup File");

	}

	
	public WebElement restorePhrase_TextBox(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//android.widget.EditText[@content-desc='restore-encrypt-phrase-text-input']" , "restorePhrase TextBox");
 	}

	
	public WebElement myDriveSorting_Button(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver,"//android.widget.ImageView[@content-desc='Ascending']" , "myDriveSorting Button");
	}

	
	public WebElement backUpFileName(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//android.widget.TextView[@index='5']", "backUpFileName TextBox");
	}

	public WebElement zipDownload(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text =\"Downloaded .zip Backup\"]", "Zip Download Button");
	}
}
