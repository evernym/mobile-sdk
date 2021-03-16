package test.java.funcModules;

import com.google.inject.Guice;
import com.google.inject.Injector;

import io.appium.java_client.AppiumDriver;
import appModules.AppInjector;
import test.java.appModules.AppPageInjector;
import test.java.appModules.AppUtils;
import test.java.appModules.AppiumUtils;
import test.java.utility.Config;
import test.java.utility.IntSetup;

/**
 * The LockModules class is to implement method related to lock
 */
public class LockModules extends AppPageInjector {

  Injector injector = Guice.createInjector(new AppInjector());
  AppUtils objAppUtlis = injector.getInstance(AppUtils.class);
  SwitchEnvModules objSwitchEnvModules = injector.getInstance(SwitchEnvModules.class);

  /**
   * set up the pincode for installed connectme app
   *
   * @param driver- appium driver available for session
   * @return void
   */
  public void passStartUpWizard(AppiumDriver driver) throws Exception {
    startUpPage.set_up_button(driver).click();

    for (int i = 0; i < 2; i++) {
      Thread.sleep(1000);
      objAppUtlis.enterPincode(driver);
    }
    Thread.sleep(1000);

    switchEnv(driver);
    chooseLockPage.pinCodeLock_Button(driver).click();
    chooseLockPage.eula_Accept(driver).click();
  }

  public void switchEnv(AppiumDriver driver) throws Exception {
    navigateSwitchEnv(driver);
    if ((Config.Device_Type.equals("android") || Config.Device_Type.equals("awsAndroid"))) {
      objSwitchEnvModules.switchEnv(driver, Config.Env_Type);
    }
  }

  /**
   * set up invalid pincode for installed connectme app
   *
   * @param driver - appium driver available for session
   * @return void
   */
  public void invalidPinCodeSetup(AppiumDriver driver) throws Exception {
    chooseLockPage.pinCodeLock_Button(driver).click();
    objAppUtlis.enterPincode(driver);
    objAppUtlis.enterPincodeReverse(driver);

  }

  /**
   * change user avatar for
   *
   * @param driver - appium driver available for session
   * @return void
   */
//  public void changeUserAvatar(AppiumDriver driver) throws Exception {
//    // HomePage.Setting_Button(driver).click();
//    settingPage.user_Avatar(driver).click();
//    if (Config.Device_Type.equals("android") || (Config.Device_Type.equals("awsAndroid"))) {
//      settingPage.list_Storage(driver).click();
//      settingPage.photos(driver).click();
//    }
//    try {
//      driver.switchTo().alert().accept();// handling alerts
//    } catch (Exception e) {
//    }
//    settingPage.all_Photos(driver).click();
//    settingPage.image(driver).click();
//  }

  /**
   * navigate switch environment screen
   *
   * @param driver - appium driver available for session
   * @return void
   */
  public void navigateSwitchEnv(AppiumDriver driver) throws Exception {
    if (Config.iOS_Devices.contains(Config.Device_Type)) {
      // ios - use staging net
      chooseLockPage.stagingNetSwitcher(driver).click();
    } else {
      // android - enter env switching screen
      AppiumUtils.longPress(driver, chooseLockPage.or_Text(driver));
      AppiumUtils.nClick(10, chooseLockPage.or_Text(driver));
      chooseLockPage.ok_button(driver).click();
    }

//		if (Config.iOS_Devices.contains(Config.Device_Type)) {
//			driver.switchTo().alert().accept();
//		} else {
//			chooseLockPage.ok_button(driver).click();
//		}
  }

  /**
   * navigate to "start fresh or restore from back up" page
   *
   * @param driver          - appium driver available for session
   * @param deviceType-type of device
   * @return void
   */
  public void navigateRestore(AppiumDriver driver, String deviceType) throws Exception {
    driver.launchApp();
//		if(Config.Device_Type.equals("android")||(Config.Device_Type.equals("awsAndroid"))) {
//			chooseLockPage.allow_button(driver).click();
//		}
    chooseLockPage.eula_Accept(driver).click();
  }
}
