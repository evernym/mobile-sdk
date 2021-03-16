package test.java.pageObjects;

import org.openqa.selenium.WebElement;

import io.appium.java_client.AppiumDriver;
import test.java.appModules.AppiumUtils;

/**
 * The BackupandRestore Interface is to hold webelement for backup and restore Pages
 * 
 */
public interface BackupRestoreWalletPage {
	
	public  WebElement copyRecoveryPhrase(AppiumDriver driver) throws Exception ;
	public  WebElement continue_Button(AppiumDriver driver) throws Exception ;
	public  WebElement inputRecoveryPhrase(AppiumDriver driver) throws Exception ;
	public  WebElement restoreFromAbackUp_Button(AppiumDriver driver) throws Exception;
	public  WebElement startFresh_Button(AppiumDriver driver) throws Exception;
	public  WebElement exportEncrypted_Button(AppiumDriver driver) throws Exception;
	public  WebElement saveToDrive_Icon(AppiumDriver driver) throws Exception;
	public  WebElement save_Button(AppiumDriver driver) throws Exception;
	public  WebElement done_Button(AppiumDriver driver) throws Exception;
	public  WebElement backupbanner_Header(AppiumDriver driver) throws Exception;
	public  WebElement genPassPhraseClose_Button(AppiumDriver driver) throws Exception;
	public  WebElement copyRecoveryPhraseClose_Button(AppiumDriver driver) throws Exception;
	public  WebElement copyRecoveryPhraseBack_Button(AppiumDriver driver) throws Exception;
	public  WebElement exportBackupClose_Button(AppiumDriver driver) throws Exception;
	public  WebElement exportBackupBack_Button(AppiumDriver driver) throws Exception;
	public  WebElement passphraseError_Banner(AppiumDriver driver) throws Exception;
	public  WebElement showRootDir_Button(AppiumDriver driver) throws Exception;
	public  WebElement drive_Button(AppiumDriver driver) throws Exception;
	public  WebElement myDrive_Button(AppiumDriver driver) throws Exception;
	public  WebElement backupfile_icon(AppiumDriver driver) throws Exception;
	public  WebElement myDriveSorting_Button(AppiumDriver driver) throws Exception;
	public  WebElement restorePhrase_TextBox(AppiumDriver driver) throws Exception;
	public  WebElement backUpFileName(AppiumDriver driver) throws Exception;
	public WebElement zipDownload(AppiumDriver driver) throws Exception;
	
}
