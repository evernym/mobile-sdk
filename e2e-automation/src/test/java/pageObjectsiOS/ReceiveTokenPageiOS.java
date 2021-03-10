package test.java.pageObjectsiOS;

import io.appium.java_client.AppiumDriver;
import test.java.appModules.AppiumUtils;
import test.java.pageObjects.ReceiveTokenPage;

import org.openqa.selenium.WebElement;

/**
 * The ReceiveTokenPageiOS class is to hold webelement for Receive TokenPage iOS
 * 
 */
public class ReceiveTokenPageiOS implements ReceiveTokenPage  {

	public WebElement receive_Tab(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "//XCUIElementTypeButton[@name='RECEIVE']", "Receive Tab");

	}

	public WebElement copyAddress_Button(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "//XCUIElementTypeOther[@name='token-copy-to-clipboard-label']",
				"CopyAddress Button");

	}

	public WebElement tokenAddress_Text(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
