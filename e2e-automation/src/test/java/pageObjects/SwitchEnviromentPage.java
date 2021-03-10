package test.java.pageObjects;

import io.appium.java_client.AppiumDriver;
import test.java.appModules.AppiumUtils;

import org.openqa.selenium.WebElement;

/**
* The SwitchEnviromentPage Interface is to hold webelement for SwitchEnviroment Page  
* 
*/
public interface SwitchEnviromentPage {

	public  WebElement agencyUrl_TextBox(AppiumDriver driver) throws Exception;
	public  WebElement agencyDID_TextBox(AppiumDriver driver) throws Exception;
	public  WebElement agencyVerificationKey_TextBox(AppiumDriver driver) throws Exception;
	public  WebElement poolConfig_TextBox(AppiumDriver driver) throws Exception;
	public  WebElement save_Button(AppiumDriver driver) throws Exception;	
	public  WebElement env_Button(AppiumDriver driver, String envType) throws Exception ;

}
