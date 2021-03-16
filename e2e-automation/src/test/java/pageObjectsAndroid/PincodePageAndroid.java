package test.java.pageObjectsAndroid;

import io.appium.java_client.AppiumDriver;
import test.java.appModules.AppiumUtils;
import test.java.pageObjects.PincodePage;

import org.openqa.selenium.WebElement;

/**
 * The PincodePageAndroid class is to hold webelement for Pincode Page for Android
 * 
 */
public class PincodePageAndroid implements PincodePage {

	
	public WebElement pinCodeLock_TextBox(AppiumDriver driver, int i) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	
	public WebElement close_Button(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "//android.view.ViewGroup[@content-desc='close-button']", "Close Button");
	}

	
	public WebElement pinCodeSe_TextBox(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "//android.view.ViewGroup[@content-desc='pin-code-digit-0-touchable']", "Pin Code TextBox");
	}

	
	public WebElement pinCodeSeNew_TextBox(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	
	public WebElement pinCodeRe_TextBox(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	
	public WebElement pinCodeReNew_TextBox(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	
	public WebElement pinCode_TextBox(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	
	public WebElement pinCodeVerify_TextBox(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "//android.view.ViewGroup[@content-desc='pin-code-digit-0-touchable']", "Pin Code TextBox");
	}

	public WebElement pinCodeTitle(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//android.widget.TextView[@content-desc=\"Enter passcode\"]", "Pin Code Title");
	}

}
