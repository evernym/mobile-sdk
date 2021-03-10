package test.java.pageObjectsiOS;

import java.util.List;

import org.openqa.selenium.WebElement;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import test.java.appModules.AppiumUtils;
import test.java.pageObjects.BackupRestoreWalletPage;


public class BackupRestoreWalletPageiOS implements BackupRestoreWalletPage {
	
	public WebElement copyRecoveryPhrase(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//XCUIElementTypeImage[@name='assets/app/images/textBubble@2x.png']/following-sibling::XCUIElementTypeOther", "Copy Recovery Phrase");
	}

	public WebElement continue_Button(AppiumDriver driver) throws Exception {
		
		return AppiumUtils.findElement(driver, "(//XCUIElementTypeOther[@name='submit-recovery-passphrase'])[2]", "Continue button");
	}
	
	public WebElement inputRecoveryPhrase(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//XCUIElementTypeImage[@name='assets/app/images/transparentBands2@2x.png']", "Input Recovery Phrase");
	}
	
	public WebElement restoreFromAbackUp_Button(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public WebElement startFresh_Button(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public WebElement exportEncrypted_Button(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public WebElement saveToDrive_Icon(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public WebElement save_Button(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public WebElement done_Button(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public WebElement backupbanner_Header(AppiumDriver driver) throws Exception {
		
		return AppiumUtils.findElement(driver, "(//XCUIElementTypeOther[@name='wallet-backup-banner-label-touchable'])[7]", "Backup Banner");
	}
	
	public WebElement genPassPhraseClose_Button(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//XCUIElementTypeImage[@name='recovery-header-close-image']", "Backup Screen Close Button");
	}
	
	public WebElement copyRecoveryPhraseClose_Button(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//XCUIElementTypeImage[@name='verify-passphrase-close-image']", "Recovery Phrase screen Close Button");
	}
		
	public WebElement copyRecoveryPhraseBack_Button(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//XCUIElementTypeImage[@name='verify-passphrase-back-image']", "Recovery Phrase screen Back Button");
	}
	
	public WebElement exportBackupClose_Button(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//XCUIElementTypeImage[@name='export-backup-close-image']", "Export Backup screen Close Button");
	}
	
	public WebElement exportBackupBack_Button(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//XCUIElementTypeImage[@name='export-backup-back-image']", "Export Backup screen Back Button");
	}
	
	public WebElement passphraseError_Banner(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public WebElement showRootDir_Button(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public WebElement drive_Button(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public WebElement myDrive_Button(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public WebElement backupfile_icon(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public WebElement restorePhrase_TextBox(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public WebElement myDriveSorting_Button(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public WebElement backUpFileName(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public WebElement zipDownload(AppiumDriver driver) throws Exception {
		return null;
	}
}
