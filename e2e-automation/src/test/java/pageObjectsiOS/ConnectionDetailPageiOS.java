package test.java.pageObjectsiOS;

import org.openqa.selenium.WebElement;

import io.appium.java_client.AppiumDriver;
import test.java.appModules.AppiumUtils;
import test.java.pageObjects.ConnectionDetailPage;

import java.util.List;

/**
 * The ConnectionDetailPageiOS class is to hold webelement for ConnectionDetail Page for iOS
 *
 */
public class ConnectionDetailPageiOS implements ConnectionDetailPage {

	public WebElement close_Button(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElementsByAccessibilityId(driver, "Cancel","Cancel Button");
	}

	public WebElement delete_Button(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElementsByAccessibilityId(driver, "Delete Connection","Delete Button");
	}
}
