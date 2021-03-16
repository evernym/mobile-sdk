package test.java.pageObjectsAndroid;

import io.appium.java_client.AppiumDriver;
import test.java.appModules.AppiumUtils;
import test.java.pageObjects.SendTokenPage;

import org.openqa.selenium.WebElement;

/**
 * The SendTokenPage class is to hold webelement for SendToken Page for Android
 * 
 */
public class SendTokenPageAndroid implements SendTokenPage  {

	
	public WebElement selectRecipient_Button(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "//android.view.ViewGroup[@content-desc='wallet-send-token-button']", "Select Recipient");
	}

	
	public WebElement input_TextBox(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	
	public WebElement send_Tab(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "//*[@text='SEND']", "Send Tab");
	}

	
	public WebElement one_Button(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "//android.view.ViewGroup[@content-desc='wallet-keyboard-id-label-1']/android.widget.TextView", "One Button");
	}

	
	public WebElement invalidPaymentAddressText(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
		return AppiumUtils.findElement(driver, "//*[contains(@text, 'Invalid Payment Address')]",
				"Invalid PaymentAddress Text",2);	}

	
	public WebElement send_Button(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "//android.widget.TextView[@content-desc='wallet-send-tokens-to-address']", "Send Button");
	}

	
	public WebElement amountSend_Text(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "//*[@text='10,000']", "Amount Send Text");
	}

	
	public WebElement zero_Button(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "//android.view.ViewGroup[@content-desc='wallet-keyboard-id-label-0']/android.widget.TextView", "Zero Button");
	}

	
	public WebElement tokenToSend_TextBox(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "//android.widget.EditText[@content-desc='To-token-send-details-label']", "tokenToSend TextBox",2);
	}

	
	public WebElement tokenForSend_TextBox(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "//android.widget.EditText[@content-desc='For-token-send-details-label']", "tokenForSend TextBox",2);
	}
	
	
	public WebElement tokenScreenClose_Button(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
