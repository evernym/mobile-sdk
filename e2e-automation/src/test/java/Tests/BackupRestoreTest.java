package test.java.Tests;

import io.appium.java_client.AppiumDriver;

import org.testng.annotations.*;

import com.google.inject.Guice;
import com.google.inject.Injector;

import appModules.AppInjector;
import test.java.appModules.AppUtils;
import test.java.appModules.RestApi;
import test.java.funcModules.BackupRestoreModules;
import test.java.funcModules.ConnectionModules;
import test.java.funcModules.LockModules;
import test.java.funcModules.TokenModules;
import test.java.utility.Config;
import test.java.utility.IntSetup;

/**
 * The BackupRestoreTest class is a Test class which holds test
 * method related to backup and restore flow
 */

public class BackupRestoreTest extends IntSetup {

	public AppiumDriver driverBrowser;
	RestApi objRestApi = new RestApi();
	Injector injector = Guice.createInjector(new AppInjector());
	BackupRestoreModules objBackupModules = injector.getInstance(BackupRestoreModules.class);
	AppUtils objAppUtlis = injector.getInstance(AppUtils.class);
	LockModules objLockModules = injector.getInstance(LockModules.class);
	TokenModules objTokenModules = injector.getInstance(TokenModules.class);
	ConnectionTest objConnectionTest = injector.getInstance(ConnectionTest.class);
	ConnectionModules objConnectionModules = injector.getInstance(ConnectionModules.class);

//	/**
//	 * Test to click on Backup my wallet button and extract the recovery phrase
//	 * @return  void
//	 */
//	@Test(groups = { "Smoke", "Regression" }, priority = 16)
//	public void getRecoveryPhraseBackupTest() throws Exception {
//		driverApp.launchApp();
//		Thread.sleep(3000);
//		objAppUtlis.enterPincode(driverApp);
//		objBackupModules.navigateBackupWalletScreen(driverApp);
//		AppUtils.Success = true;
//
//	}
//
//	/**
//	 * Test to send the invalid recovery phrase
//	 * @return  void
//	 */
//	@Test(groups = { "Smoke", "Regression" }, priority = 17)
//	public void sendinvalidRecoveryPhrase() throws Exception {
//		objAppUtlis.checkSkip();
//		objBackupModules.invalidRecoveryPhrase(driverApp);
//		AppUtils.Success = true;
//
//	}
//
//	/**
//	 * Test to send the valid recovery phrase
//	 * @return  void
//	 */
//	@Test(groups = { "Smoke", "Regression" }, priority = 18)
//	public void sendvalidRecoveryPhrase() throws Exception {
//		objAppUtlis.checkSkip();
//		objBackupModules.validRecoveryPhrase(driverApp);
//		AppUtils.Success = true;
//
//	}
//
//	/**
//	 * Test to save the back up file
//	 * @return  void
//	 */
//	@Test(groups = { "Smoke", "Regression" }, priority = 19)
//	public void saveBackup() throws Exception {
//		objAppUtlis.checkSkip();
//		objBackupModules.saveBackupWallet(driverApp);
//		AppUtils.Success = true;
//
//	}
//
//	/**
//	 * Test to check the backup flow again through backup banner
//	 * @return  void
//	 */
//	@Test(groups = { "Smoke", "Regression" }, priority = 20, enabled = false)
//	public void backupViaBannerFlow() throws Exception {
//		objTokenModules.navigateTokenScreen(driverApp);
//		objTokenModules.copyToClipboard(driverApp);
//		objBackupModules.invokeBackupbanner(driverApp);
//		objBackupModules.validRecoveryPhrase(driverApp);
//		objBackupModules.saveBackupWallet(driverApp);
//
//	}
//
//	/**
//	 * Test to check the redirection in backup flow and ensure recovery phrase is not persisted
//	 * @return  void
//	 */
//	@Test(groups = { "Smoke", "Regression" }, priority = 21, enabled = false)
//	public void checkPhrasePersistence() throws Exception {
//		objBackupModules.navigateBackupWalletScreen(driverApp);
//		objBackupModules.validRecoveryPhrase(driverApp);
//		objBackupModules.backupnavigation(driverApp);
//
//	}
//
//	/**
//	 * Test to install the app and restore from recent Backup
//	 * @return  void
//	 */
//	@Test(groups = { "Smoke", "Regression" }, priority = 22, enabled = false)
//	public void restoreFromBackup() throws Exception {
//		objAppUtlis.checkSkip();
//		driverApp.removeApp("com.connectme");
//		driverApp.quit();
//		if(Config.Device_Type.equals("iOS"))
//		{driverBrowser = IntSetup.configureBrowserDriver();}
//		else
//		{
//	    driverBrowser = IntSetup.configureBrowserDriver();}
//		objConnectionModules.installApp(driverBrowser, "https://connectme.app.link");
//		Thread.sleep(40000);
//		objLockModules.navigateRestore(driverApp,"android");
//		objBackupModules.restoreFromBackup(driverApp);
//		AppUtils.Success = true;
//
//	}

	@AfterClass
	public void AfterClass() {
		driverApp.close();
	}

}
