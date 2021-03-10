package test.java.Tests;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidKeyCode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import appModules.AppInjector;
import test.java.appModules.AppUtils;
import test.java.appModules.AppiumUtils;
import test.java.utility.IntSetup;

public class BackupTest extends IntSetup {

    Injector injector = Guice.createInjector(new AppInjector());
    AppUtils objAppUtils = injector.getInstance(AppUtils.class);
    test.java.pageObjects.HomePage objHomePage = injector.getInstance(test.java.pageObjects.HomePage.class);
    test.java.pageObjects.MenuPage objMenuPage = injector.getInstance(test.java.pageObjects.MenuPage.class);
    test.java.pageObjects.SettingsPage objSettingsPage = injector.getInstance(test.java.pageObjects.SettingsPage.class);
    test.java.pageObjects.BackupPage objBackupPage = injector.getInstance(test.java.pageObjects.BackupPage.class);

    @BeforeClass
    public void BeforeClassSetup() throws Exception {
        driverApp.launchApp();
        Thread.sleep(3000);
        objAppUtils.enterPincode(driverApp);
        objHomePage.burgerMenuButton(driverApp).click(); // go to Menu
        objMenuPage.settingsButton(driverApp).click(); // go to Settings
    }

    @Test
    public void checkLocalBackup() throws Exception {
        try { // first backup
            objSettingsPage.createBackupButton(driverApp).click();
            objBackupPage.recoveryHeader(driverApp).isDisplayed();
            ctx.recoveryPhrase = objBackupPage.recoveryPhraseBox(driverApp).getText();
            System.out.println(ctx.recoveryPhrase);
            objBackupPage.continueButton(driverApp).click();
            objBackupPage.verifyPhraseBox(driverApp).sendKeys(ctx.recoveryPhrase);
            AndroidDriver androidDriver = (AndroidDriver) driverApp;
            androidDriver.pressKeyCode(AndroidKeyCode.KEYCODE_ENTER);
//            objBackupPage.zipDownloadButton(driverApp).click(); // this button disappeared in the latest build
            ctx.backupFileName = objBackupPage.backupFileName(driverApp).getText();
            System.out.println(ctx.backupFileName);
            ctx.dumpContext();
        }
        catch (Exception ex) { // not first backup
            objSettingsPage.manualBackupButton(driverApp).click();
        }
        finally{
            objBackupPage.exportEncryptedButton(driverApp).isEnabled();
            objBackupPage.exportEncryptedButton(driverApp).click();
            objBackupPage.saveToDriveButton(driverApp).isEnabled();
            objBackupPage.saveToDriveButton(driverApp).click();
            objBackupPage.saveButton(driverApp).isEnabled();
            objBackupPage.saveButton(driverApp).click();
            objBackupPage.doneButton(driverApp).isEnabled();
            objBackupPage.doneButton(driverApp).click();
        }
    }

    @Test(dependsOnMethods = "checkLocalBackup", enabled = false) // this feature is switched off
    public void checkOneCloudBackup() throws Exception {
        objSettingsPage.automaticCloudBackupsButton(driverApp).click();
        try {
            objBackupPage.oneCloudBackupButton(driverApp).isEnabled();
            objBackupPage.oneCloudBackupButton(driverApp).click();
        }
        catch (Exception ex) {} // automatic cloud backup is already enabled
        finally {
            objBackupPage.cloudBackupSuccessMessage(driverApp).isDisplayed();
            objBackupPage.cloudDoneButton(driverApp).isEnabled();
            objBackupPage.cloudDoneButton(driverApp).click();
        }
    }

    @Test(dependsOnMethods = "checkOneCloudBackup", enabled = false) // this feature is switched off
    public void enableAutomaticCloudBackup() throws Exception {
        objSettingsPage.automaticCloudBackupsButton(driverApp).click();
        try {
            objBackupPage.enableCloudBackupsButton(driverApp).isEnabled();
            objBackupPage.enableCloudBackupsButton(driverApp).click();
        }
        catch (Exception ex) {} // automatic cloud backup is already enabled
        finally {
            objBackupPage.cloudBackupSuccessMessage(driverApp).isDisplayed();
            objBackupPage.cloudDoneButton(driverApp).isEnabled();
            objBackupPage.cloudDoneButton(driverApp).click();
        }
    }

    @Test(dependsOnMethods = "enableAutomaticCloudBackup", enabled = false) // this feature is switched off
    public void disableAutomaticCloudBackup() throws Exception {
        new TouchAction(driverApp)
                // FIXME
                .press(AppiumUtils.findElement(driverApp, "//android.view.ViewGroup[@content-desc=\"settings-container\"]/android.widget.ScrollView/android.view.ViewGroup/android.view.ViewGroup/android.view.ViewGroup[2]/android.view.ViewGroup[2]/android.view.ViewGroup", "Slider"))
                .moveTo(AppiumUtils.findElement(driverApp, "//android.view.ViewGroup[@content-desc=\"settings-container\"]/android.widget.ScrollView/android.view.ViewGroup/android.view.ViewGroup/android.view.ViewGroup[2]/android.view.ViewGroup[1]", "Cloud Icon"))
                .release();
    }

    @AfterClass
    public void AfterClass() {
        driverApp.close();
    }

}
