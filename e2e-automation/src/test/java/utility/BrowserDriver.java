package test.java.utility;

import com.google.common.collect.ImmutableMap;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.Reporter;
import test.java.utility.AppDriver;
import test.java.utility.Config;

import java.net.URL;
import java.util.concurrent.TimeUnit;

public class BrowserDriver {

	private static AppiumDriver driver;

	public static AppiumDriver getDriver() {
		if (driver == null) {
			String deviceType = Config.Device_Type;

			try {
				DesiredCapabilities capabilities = new DesiredCapabilities();
				if (deviceType.equals("iOS") || deviceType.equals("iOSSimulator")) {
					driver = AppDriver.getDriver();
					driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
					System.out.println("safari browser launched successfully in iOS");

//					DeviceInfo deviceInfo = DeviceInfo.getInstance();
//					capabilities.setCapability("platformName", "iOS");
//					capabilities.setCapability("platformVersion", "13.0");
//					capabilities.setCapability("deviceName", deviceInfo.getName());// devie
//					capabilities.setCapability("udid", deviceInfo.getUDID());// udid
//					capabilities.setCapability("browserName", "Safari");
//					capabilities.setCapability("newCommandTimeout", "5000");
//					capabilities.setCapability("wdaStartupRetryInterval", "1000");
//					driverBrowser = new IOSDriver(new URL(Config.Appium_Server), capabilities);
//					driverBrowser.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
//					System.out.println("safari browser launched successfully in iOS");
				} else if (deviceType.equals("awsiOS")) {
					driver = AppDriver.getDriver();
					driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
					System.out.println("safari browser launched successfully in iOS");
				} else if (deviceType.equals("android")) {
					capabilities.setCapability("browserName", "Chrome");
					capabilities.setCapability("automationName", "UiAutomator2");
					capabilities.setCapability("platformName", "Android");
					capabilities.setCapability("newCommandTimeout", "60000");
					capabilities.setCapability("deviceName", Config.Device_Name);// device
					capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.ACCEPT);
					capabilities.setCapability("autoAcceptAlerts", true);
					capabilities.setCapability("appium:chromeOptions", ImmutableMap.of("w3c", false));
//					capabilities.setCapability("chromedriverExecutable", "/usr/local/bin/chromedriver");
					driver = new AndroidDriver(new URL(test.java.utility.Config.Appium_Server), capabilities);
					driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
					System.out.println("chrome browser launched successfully in Android");
				} else if (deviceType.equals("awsAndroid")) {
					capabilities.setCapability("browserName", "Chrome");
					capabilities.setCapability("automationName", "UiAutomator2");
					capabilities.setCapability("platformName", "Android");
					capabilities.setCapability("newCommandTimeout", "60000");
					capabilities.setCapability("deviceName", Config.Device_Name);// device
					capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.ACCEPT);
					capabilities.setCapability("autoAcceptAlerts", true);
					driver = new AndroidDriver(new URL(test.java.utility.Config.Appium_Server), capabilities);
					driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
					System.out.println("chrome browser launched successfully in Android");
				}
			} catch (Exception e) {
				Reporter.log("Class Setup | Method OpenBrowser | Exception desc : " + e.getMessage());
				System.out.println("Class Setup | Method OpenBrowser | Exception desc : " + e.getMessage());
				Assert.fail();
			}
		}
		return driver;
	}

	public static void closeApp() {
		if (driver != null) {
			if ((Config.Device_Type.equals("android") || Config.Device_Type.equals("awsAndroid"))) {
				driver.closeApp();
			}
		}
	}
}
