package test.java.pageObjects;

import io.appium.java_client.AppiumDriver;
import test.java.appModules.AppiumUtils;

import org.openqa.selenium.WebElement;

/**
 * The ReceiveTokenPage Interface is to hold webelement for Receive TokenPage
 * 
 */
public interface ReceiveTokenPage {

	public  WebElement receive_Tab(AppiumDriver driver) throws Exception;
	public  WebElement copyAddress_Button(AppiumDriver driver) throws Exception;
	public  WebElement tokenAddress_Text(AppiumDriver driver) throws Exception;


}
