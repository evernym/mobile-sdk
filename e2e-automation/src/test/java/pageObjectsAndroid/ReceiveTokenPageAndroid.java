package test.java.pageObjectsAndroid;

import io.appium.java_client.AppiumDriver;
import test.java.appModules.AppiumUtils;
import test.java.pageObjects.ReceiveTokenPage;

import org.openqa.selenium.WebElement;

/**
 * The ReceiveTokenPageAndroid class is to hold webelement for Receive TokenPage for Android
 * 
 */
public class ReceiveTokenPageAndroid implements ReceiveTokenPage  {

	
	public WebElement receive_Tab(AppiumDriver driver) throws Exception {
		
		return AppiumUtils.findElement(driver, "//*[@text='RECEIVE']", "Receive Tab");
	}

	
	public WebElement copyAddress_Button(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "//android.view.ViewGroup[@content-desc='token-copy-to-clipboard-label']", "Copy Address Button");
	}
	


	
	public WebElement tokenAddress_Text(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "//android.view.ViewGroup[@content-desc='wallet-receive-container1']/android.widget.ScrollView/android.view.ViewGroup/android.widget.TextView[2]", "Address Container");
	}

}
