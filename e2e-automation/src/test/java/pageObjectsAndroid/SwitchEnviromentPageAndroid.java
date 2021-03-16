package test.java.pageObjectsAndroid;

import io.appium.java_client.AppiumDriver;
import test.java.appModules.AppiumUtils;
import test.java.pageObjects.SwitchEnviromentPage;

import org.openqa.selenium.WebElement;

/**
* The SwitchEnviromentPageAndroid class is to hold webelement for SwitchEnviroment Page for Android 
* 
*/
public class SwitchEnviromentPageAndroid implements SwitchEnviromentPage  {

	public WebElement agencyUrl_TextBox(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public WebElement agencyDID_TextBox(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public WebElement agencyVerificationKey_TextBox(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public WebElement poolConfig_TextBox(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public WebElement save_Button(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text='Save']", "Save Button");
	}
	


	public WebElement env_Button(AppiumDriver driver, String envType) throws Exception {
		Thread.sleep(3000);
		return AppiumUtils.findElement(driver, "//android.widget.Button[@content-desc=\"switch-environment-" + envType + "\"]/android.widget.TextView", "Env Button");


	}

}
