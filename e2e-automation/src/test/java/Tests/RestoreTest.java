package test.java.Tests;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidKeyCode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import appModules.AppInjector;
import test.java.appModules.AppUtils;
import test.java.funcModules.ConnectionModules;
import test.java.funcModules.LockModules;
import test.java.utility.IntSetup;

public class RestoreTest extends IntSetup {
    Injector injector = Guice.createInjector(new AppInjector());
    ConnectionModules objConnectionModules = injector.getInstance(ConnectionModules.class);
    LockModules objLockModules = injector.getInstance(LockModules.class);
    test.java.pageObjects.RestorePage objRestorePage = injector.getInstance(test.java.pageObjects.RestorePage.class);
    AppUtils objAppUtlis = injector.getInstance(AppUtils.class);
    test.java.pageObjects.HomePage objHomePage = injector.getInstance(test.java.pageObjects.HomePage.class);

    @BeforeMethod
    public void BeforeMethodSetup() throws Exception {
        driverApp.launchApp();
        Thread.sleep(3000);
        driverApp.removeApp("me.connect");
    }

    @Test
    public void checkLocalRestore() throws Exception {
        ctx.getContext();
        objConnectionModules.installApp(driverApp, "");
        objLockModules.navigateRestore(driverApp,"android");
        objRestorePage.restoreFromBackupButton(driverApp).click();
        objRestorePage.restoreHeader(driverApp).isDisplayed();
        objRestorePage.restoreFromDeviceButton(driverApp).click();
        objRestorePage.zipFileSelector(driverApp, ctx.backupFileName).click();
        objRestorePage.recoveryPhraseBoxLocal(driverApp).sendKeys(ctx.recoveryPhrase);
        AndroidDriver androidDriver = (AndroidDriver) driverApp;
        androidDriver.pressKeyCode(AndroidKeyCode.KEYCODE_ENTER);
        objRestorePage.enterPasscodeMessage(driverApp).isDisplayed();
        objAppUtlis.enterPincode(driverApp);
        objHomePage.homeHeader(driverApp).isDisplayed();
    }

    @Test(dependsOnMethods = "checkLocalRestore", enabled = false) // this feature is switched off
    public void checkCloudRestore() throws Exception {
        ctx.getContext();
        objConnectionModules.installApp(driverApp, "");
        objLockModules.navigateRestore(driverApp,"android");
        objRestorePage.restoreFromBackupButton(driverApp).click();
        objRestorePage.restoreHeader(driverApp).isDisplayed();
        objRestorePage.restoreFromCloudButton(driverApp).click();
        objRestorePage.recoveryPhraseBoxCloud(driverApp).sendKeys(ctx.recoveryPhrase);
        AndroidDriver androidDriver = (AndroidDriver) driverApp;
        androidDriver.pressKeyCode(AndroidKeyCode.KEYCODE_ENTER);
        // FIXME I got `Recovery phrase doesn't match here so it looks like env should be switched in the beginning`
    }

    @AfterClass
    public void AfterClass() {
        driverApp.quit();
    }

}
