package test.java.funcModules;

import org.openqa.selenium.Keys;
import com.google.inject.Guice;
import com.google.inject.Injector;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidKeyCode;
import appModules.AppInjector;
import test.java.appModules.AppPageInjector;
import test.java.appModules.AppUtils;
import test.java.utility.Config;

/**
 * The BackupRestoreModules class is to implement method related to Backup and Restore flow
 *
 */
public class BackupRestoreModules extends AppPageInjector {

	public static String recoveryPhrase;
	public static String backUpFileName;
	Injector injector = Guice.createInjector(new AppInjector());
	AppUtils objAppUtlis = injector.getInstance(AppUtils.class);


	/**
	 * click on start fresh button on restore back up page
	 * @param driver - appium driver available for session
	 * @return void
	 */
	public void clickStartFreshButton(AppiumDriver driver) throws Exception {
		backuprestoreWalletPage.startFresh_Button(driver).click();
	}

	/**
	 * click Backup my wallet button and get recovery phrase
	 * @param  driver - appium driver available for session
	 * @return void
	 */
	public void navigateBackupWalletScreen(AppiumDriver driver) throws Exception {
		homePage.burgerMenuButton(driver).click();
		menuPage.settingsButton(driver).click();
//		settingPage.backupWallet_Button(driver).click();
		recoveryPhrase = backuprestoreWalletPage.copyRecoveryPhrase(driver).getText(); //To get the recovery phrase that was generated
		backuprestoreWalletPage.continue_Button(driver).click();
	}

	/**
	 * Enter the recovery phrase that was extracted
	 * @param  driver - appium driver available for session
	 * @return void
	 */
	public void validRecoveryPhrase(AppiumDriver driver) throws Exception {
		backuprestoreWalletPage.inputRecoveryPhrase(driver).sendKeys(recoveryPhrase);
		if (Config.iOS_Devices.contains(Config.Device_Type))
		{
			backuprestoreWalletPage.inputRecoveryPhrase(driver).sendKeys(Keys.RETURN);
		}
		else {
			AndroidDriver androidDriver = (AndroidDriver) driver;
			androidDriver.pressKeyCode(AndroidKeyCode.KEYCODE_ENTER);
		}
	}

	/**
	 * Enter the invalid recovery phrase that was extracted
	 * @param  driver - appium driver available for session
	 * @return void
	 */
	public void invalidRecoveryPhrase(AppiumDriver driver) throws Exception {
		String incorrectPhrase = recoveryPhrase.substring(0,recoveryPhrase.length()-1); //Remove the last character from the recovery phrase string
		backuprestoreWalletPage.inputRecoveryPhrase(driver).sendKeys(incorrectPhrase);
		if (Config.iOS_Devices.contains(Config.Device_Type))
		{
			backuprestoreWalletPage.inputRecoveryPhrase(driver).sendKeys(Keys.RETURN);
		}
		else {
			AndroidDriver androidDriver = (AndroidDriver) driver;
			androidDriver.pressKeyCode(AndroidKeyCode.KEYCODE_ENTER);
		}
		backuprestoreWalletPage.passphraseError_Banner(driver).isDisplayed();
		backuprestoreWalletPage.inputRecoveryPhrase(driver).click();
		backuprestoreWalletPage.inputRecoveryPhrase(driver).clear();
	}

	/**
	 * Save the backup wallet to external/internal storage
	 * @param  driver - appium driver available for session
	 * @return void
	 */
	public void saveBackupWallet(AppiumDriver driver) throws Exception {
		backUpFileName=backuprestoreWalletPage.backUpFileName(driver).getText();
		System.out.println("backUpFileName"+backUpFileName);
		backuprestoreWalletPage.zipDownload(driver).click();
		backuprestoreWalletPage.exportEncrypted_Button(driver).click();
		backuprestoreWalletPage.saveToDrive_Icon(driver).click();
		backuprestoreWalletPage.save_Button(driver).click();
		backuprestoreWalletPage.done_Button(driver).click();
//		homePage.user_Avatar(driver).click();
	}

	/**
	 * click Backup banner that gets displayed on copying payment address and get recovery phrase
	 * @param  driver - appium driver available for session
	 * @return void
	 */
	public void invokeBackupbanner(AppiumDriver driver) throws Exception {
		if (Config.iOS_Devices.contains(Config.Device_Type))
		{
			sendTokenPage.tokenScreenClose_Button(driver).click();
		}
		else {
			AndroidDriver androidDriver = (AndroidDriver) driver;
			androidDriver.pressKeyCode(AndroidKeyCode.KEYCODE_BACK);
		}
		backuprestoreWalletPage.backupbanner_Header(driver).click();
		recoveryPhrase = backuprestoreWalletPage.copyRecoveryPhrase(driver).getText(); //To get the recovery phrase that was generated
		backuprestoreWalletPage.continue_Button(driver).click();
	}

	/**
	 * check the navigation of the backup screens and persistence of phrase
	 * @param  driver - appium driver available for session
	 * @return void
	 */
	public void backupnavigation(AppiumDriver driver) throws Exception {
		backuprestoreWalletPage.exportBackupBack_Button(driver).click();
		backuprestoreWalletPage.copyRecoveryPhraseBack_Button(driver).click();
		backuprestoreWalletPage.continue_Button(driver).click();
		backuprestoreWalletPage.copyRecoveryPhraseClose_Button(driver).click();
	}

	/**
	 * to restore from the recent backup
	 * @param  driver - appium driver available for session
	 * @return void
	 */
	public void restoreFromBackup(AppiumDriver driver) throws Exception {
		backuprestoreWalletPage.restoreFromAbackUp_Button(driver).click();
		backuprestoreWalletPage.showRootDir_Button(driver).click();
		backuprestoreWalletPage.drive_Button(driver).click();
		backuprestoreWalletPage.myDrive_Button(driver).click();
		Thread.sleep(3000);
		backuprestoreWalletPage.myDriveSorting_Button(driver).click();
	    Thread.sleep(3000);
		backuprestoreWalletPage.backupfile_icon(driver).click();
		System.out.println("recoveryPhrase "+recoveryPhrase);
		Thread.sleep(6000);
		backuprestoreWalletPage.restorePhrase_TextBox(driver).sendKeys(recoveryPhrase);
		if (Config.iOS_Devices.contains(Config.Device_Type))
		{
			backuprestoreWalletPage.restorePhrase_TextBox(driver).sendKeys(Keys.RETURN);
		}
		else {
			AndroidDriver androidDriver = (AndroidDriver) driver;
			androidDriver.pressKeyCode(AndroidKeyCode.KEYCODE_ENTER);
		}
		Thread.sleep(60000);
		objAppUtlis.enterPincode(driver);
		Thread.sleep(6000);

	}

}
