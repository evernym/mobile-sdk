package test.java.pageObjectsAndroid;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import test.java.appModules.AppiumUtils;
import test.java.pageObjects.StartUpPage;

/**
 * The ChooseLockPageAndroid class is to hold webelement for StartUpPageAndroid Page for Android
 * 
 */
public class StartUpPageAndroid implements StartUpPage {


	public WebElement set_up_button(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text='Set Up']", "Set Up Button");
	
	}
}

