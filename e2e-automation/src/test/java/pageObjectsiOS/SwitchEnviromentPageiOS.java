package test.java.pageObjectsiOS;

import io.appium.java_client.AppiumDriver;
import test.java.appModules.AppiumUtils;
import test.java.pageObjects.SwitchEnviromentPage;

import org.openqa.selenium.WebElement;

/**
* The SwitchEnviromentPageiOS class is to hold webelement for SwitchEnviroment Page for iOS  
* 
*/
public class SwitchEnviromentPageiOS implements SwitchEnviromentPage  {

	public  WebElement agencyUrl_TextBox(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "//XCUIElementTypeTextField[@name='text-input-agencyUrl']",
				"agencyUrl Textbox");

	}

	public  WebElement agencyDID_TextBox(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "//XCUIElementTypeTextField[@name='text-input-agencyDID']",
				"agencyDID Textbox");

	}

	public  WebElement agencyVerificationKey_TextBox(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "//XCUIElementTypeTextField[@name='text-input-agencyVerificationKey']",
				"agencyVerificationKey Textbox");

	}

	public  WebElement poolConfig_TextBox(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "//XCUIElementTypeTextField[@name='text-input-poolConfig']",
				"poolConfig Textbox");

	}

	public  WebElement save_Button(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "//*[@name='switch-environment-footer-accept']", "Save_Button");

	}
	
	public  WebElement env_Button(AppiumDriver driver,String envType) throws Exception {

		return AppiumUtils.findElement(driver, "//XCUIElementTypeOther[@name='switch-environment-"+envType+"']", "Env_Button");

	}

}
