package test.java.utility;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.Reporter;

import java.net.URL;
import java.util.concurrent.TimeUnit;

public class AppDriver {

  private static AppiumDriver driver;
  public static final int EXTRA_LARGE_TIMEOUT = 120;
  public static final int LARGE_TIMEOUT = 60;
  public static final int SMALL_TIMEOUT = 15;
  public static final int SUPER_SMALL_TIMEOUT = 10;

  public static AppiumDriver getDriver() {
    if (driver == null) {
      String deviceType = Config.Device_Type;

      try {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        if (deviceType.equals("iOS") || deviceType.equals("iOSSimulator")) {
          capabilities.setCapability("platformName", "iOS");
          capabilities.setCapability("platformVersion", "13.0");
          capabilities.setCapability("deviceName", Config.Device_Name);// devie
          capabilities.setCapability("udid", Config.Device_UDID);// udid
          capabilities.setCapability("bundleId", "com.evernym.connectme.callcenter");
          capabilities.setCapability("xcodeOrgId", "ES8QU3D2A4");
          capabilities.setCapability("xcodeSigningId", "iPhone Developer");
          capabilities.setCapability("noReset", true);
          capabilities.setCapability("newCommandTimeout", "60000");
          driver = new IOSDriver(new URL(Config.Appium_Server), capabilities);
          driver.manage().timeouts().implicitlyWait(LARGE_TIMEOUT, TimeUnit.SECONDS);
          System.out.println("connectMe application launched successfully in iOS");
        } else if (deviceType.equals("awsiOS")) {
          capabilities.setCapability("platformVersion", "13.0");
          capabilities.setCapability("bundleId", "com.evernym.connectme.callcenter");
          capabilities.setCapability("xcodeOrgId", "ES8QU3D2A4");
          capabilities.setCapability("xcodeSigningId", "iPhone Developer");
          capabilities.setCapability("waitForQuiescence", "false");
          capabilities.setCapability("shouldUseSingletonTestManager", "true");
          capabilities.setCapability("wdaStartupRetryInterval", "1000");
          capabilities.setCapability("noReset", true);
          capabilities.setCapability("newCommandTimeout", "60000");
          driver = new IOSDriver(new URL(Config.Appium_Server), capabilities);
          driver.manage().timeouts().implicitlyWait(LARGE_TIMEOUT, TimeUnit.SECONDS);
          System.out.println("connectMe application launched successfully in iOS");
        } else if (deviceType.equals("android")) {
          capabilities.setCapability("automationName", "UiAutomator2");
          capabilities.setCapability("platformName", "Android");
          capabilities.setCapability("noReset", "true");// newCommandTimeout
          capabilities.setCapability("newCommandTimeout", "60000");
          capabilities.setCapability("appPackage", "me.connect");
          capabilities.setCapability("appActivity", ".MainActivity");// appActivity
          capabilities.setCapability("deviceName", Config.Device_Name);// device
          driver = new AndroidDriver(new URL(test.java.utility.Config.Appium_Server), capabilities);
          driver.manage().timeouts().implicitlyWait(LARGE_TIMEOUT, TimeUnit.SECONDS);
          System.out.println("connectMe application launched successfully in android");
        } else if (deviceType.equals("awsAndroid")) {
          capabilities.setCapability("automationName", "UiAutomator2");
          capabilities.setCapability("platformName", "Android");
          capabilities.setCapability("noReset", "true");// newCommandTimeout
          capabilities.setCapability("newCommandTimeout", "60000");
          capabilities.setCapability("appPackage", "me.connect");
          capabilities.setCapability("appActivity", ".MainActivity");// appActivity
          capabilities.setCapability("deviceName", test.java.utility.Config.Device_Name);// device
          capabilities.setCapability("printPageSourceOnFindFailure", "true");
          driver = new AndroidDriver(new URL(test.java.utility.Config.Appium_Server), capabilities);
          driver.manage().timeouts().implicitlyWait(LARGE_TIMEOUT, TimeUnit.SECONDS);
          System.out.println("connectMe application launched successfully in android");
        }

      } catch (Exception e) {
        Reporter.log("Class Setup | Method OpenBrowser | Exception desc : " + e.getMessage());
        System.out.println("Class Setup | Method OpenBrowser | Exception desc : " + e.getMessage());
        Assert.fail();
      }
    }
    return driver;
  }

  public static void quit() {
    if (driver != null) {
      driver.quit();
    }
  }
}
