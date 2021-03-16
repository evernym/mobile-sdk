package test.java.funcModules;

import org.openqa.selenium.Keys;

import io.appium.java_client.AppiumDriver;
import test.java.appModules.AppPageInjector;
import test.java.utility.Config;
import test.java.utility.EnvType;

/**
 * The SwitchEnvModules class is to implement method related to switch enviroment
 */
public class SwitchEnvModules extends AppPageInjector {

  /**
   * switch the environment like dev ,sandbox
   *
   * @param driver - appium driver available for session
   * @return void
   */
  public void switchEnv(AppiumDriver driver, EnvType envType) throws Exception {
    switchEnviromentPage.env_Button(driver, envType.getValue()).click();
    if (Config.iOS_Devices.contains(Config.Device_Type)) {
      switchEnviromentPage.poolConfig_TextBox(driver).sendKeys(Keys.RETURN);
    }
    Thread.sleep(3000);
    switchEnviromentPage.save_Button(driver).click();
  }

}
