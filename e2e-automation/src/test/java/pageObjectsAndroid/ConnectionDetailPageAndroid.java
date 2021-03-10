package test.java.pageObjectsAndroid;

import org.openqa.selenium.WebElement;

import io.appium.java_client.AppiumDriver;
import test.java.appModules.AppiumUtils;
import test.java.pageObjects.ConnectionDetailPage;

import java.util.List;

/**
 * The ConnectionDetailPageAndroid class is to hold webelement for ConnectionDetail Page for Android
 *
 */
public class ConnectionDetailPageAndroid implements ConnectionDetailPage {

	public WebElement close_Button(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver,  "//*[@text=\"Cancel\"]","Cancel Button");
	}

	public WebElement delete_Button(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text=\"Delete Connection\"]","Delete Button");
	}

}
