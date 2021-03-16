package test.java.pageObjects;

import org.openqa.selenium.WebElement;

import io.appium.java_client.AppiumDriver;
import test.java.appModules.AppiumUtils;

import java.util.List;

/**
 * The ConnectionDetailPage Interface is to hold webelement for ConnectionDetail Page
 *
 */
public interface ConnectionDetailPage {

	public WebElement close_Button(AppiumDriver driver) throws Exception;
	public WebElement delete_Button(AppiumDriver driver) throws Exception;
}
