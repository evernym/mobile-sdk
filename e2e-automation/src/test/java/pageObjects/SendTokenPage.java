package test.java.pageObjects;

import io.appium.java_client.AppiumDriver;
import test.java.appModules.AppiumUtils;

import org.openqa.selenium.WebElement;

/**
 * The SendTokenPage Interface is to hold webelement for SendToken Page
 * 
 */
public interface SendTokenPage {
	
	public  WebElement selectRecipient_Button(AppiumDriver driver) throws Exception;
	public  WebElement input_TextBox(AppiumDriver driver) throws Exception;
	public  WebElement send_Tab(AppiumDriver driver) throws Exception;
	public  WebElement one_Button(AppiumDriver driver) throws Exception;
	public  WebElement invalidPaymentAddressText(AppiumDriver driver) throws Exception;	
	public  WebElement send_Button(AppiumDriver driver) throws Exception;
	public  WebElement amountSend_Text (AppiumDriver driver) throws Exception;
	public  WebElement zero_Button(AppiumDriver driver) throws Exception;
	public  WebElement tokenToSend_TextBox(AppiumDriver driver) throws Exception;
	public  WebElement tokenForSend_TextBox(AppiumDriver driver) throws Exception;
	public  WebElement tokenScreenClose_Button(AppiumDriver driver) throws Exception;

}
