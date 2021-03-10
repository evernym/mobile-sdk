package test.java.pageObjectsiOS;

import io.appium.java_client.AppiumDriver;
import test.java.appModules.AppiumUtils;
import test.java.pageObjects.SendTokenPage;

import org.openqa.selenium.WebElement;

/**
 * The SendTokenPageiOS class is to hold webelement for SendToken Page for iOS
 * 
 */
public class SendTokenPageiOS implements SendTokenPage  {

	public  WebElement selectRecipient_Button(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "//XCUIElementTypeOther[@name='wallet-send-token-button']",
				"Select Recipient Button");

	}

	public  WebElement input_TextBox(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver,
				"(//XCUIElementTypeOther[@name='10,000'])[4]/XCUIElementTypeOther[2]/XCUIElementTypeOther/XCUIElementTypeOther/XCUIElementTypeOther[1]",
				"Input TextBox");

	}

	public  WebElement send_Tab(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "//XCUIElementTypeButton[@name='SEND']", "Send Tab");

	}

	public  WebElement one_Button(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "//XCUIElementTypeOther[@name='wallet-keyboard-id-label-1-touchable']",
				"One Button");

	}
	
	public  WebElement invalidPaymentAddressText(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "//XCUIElementTypeOther[contains(@name, 'Invalid Payment Address')]",
				"Invalid PaymentAddress Text");

	}
	
	public  WebElement send_Button(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "//XCUIElementTypeText[@name='wallet-send-tokens-to-address']",
				"Send Button");

	}
	
	public  WebElement amountSend_Text (AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "//XCUIElementTypeOther[contains(@name, '1,000')]",
				"AmountSend Text");

	}

	public WebElement zero_Button(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public WebElement tokenToSend_TextBox(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public WebElement tokenForSend_TextBox(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public WebElement tokenScreenClose_Button(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
				return null;
	}
	

}
