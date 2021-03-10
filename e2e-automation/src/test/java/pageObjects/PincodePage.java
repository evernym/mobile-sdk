package test.java.pageObjects;

import io.appium.java_client.AppiumDriver;
import test.java.appModules.AppiumUtils;

import org.openqa.selenium.WebElement;

/**
 * The PincodePage Interface is to hold webelement for Pincode Page  
 * 
 */
public interface PincodePage {

	/**
	 * to get all the password text box at pincode page
	 * @param  driver - appium driver available for session
	 * @param  i - to get the i the text box at pincode page
	 * @return void
	 */
	public  WebElement pinCodeLock_TextBox(AppiumDriver driver, int i) throws Exception;
	public  WebElement close_Button(AppiumDriver driver) throws Exception;	
	public  WebElement pinCodeSe_TextBox(AppiumDriver driver) throws Exception;
	public  WebElement pinCodeSeNew_TextBox(AppiumDriver driver) throws Exception;
	public  WebElement pinCodeRe_TextBox(AppiumDriver driver) throws Exception;
	public  WebElement pinCodeReNew_TextBox(AppiumDriver driver) throws Exception;
	public  WebElement pinCode_TextBox(AppiumDriver driver) throws Exception;
	public  WebElement pinCodeVerify_TextBox(AppiumDriver driver) throws Exception;
	public  WebElement pinCodeTitle(AppiumDriver driver) throws Exception;

}
