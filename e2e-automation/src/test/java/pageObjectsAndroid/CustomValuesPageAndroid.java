package test.java.pageObjectsAndroid;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import test.java.appModules.AppiumUtils;
import test.java.pageObjects.CustomValuesPage;

/**
 * The CustomValuesPageAndroid class is to hold webelement for Custom Values Page for Android
 *
 */
public class CustomValuesPageAndroid implements CustomValuesPage {

	@Override
	public WebElement title(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text='Custom Values']", "Custom Values Header");
	}

	@Override
	public WebElement description(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text='Please provide values for the following attributes']", "Custom Values Description");
	}

	@Override
	public WebElement attributeNameLabel(AppiumDriver driver, String attribute) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text='" + attribute + "']", "Attribute Name");
	}

	@Override
	public WebElement customValueInput(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@content-desc=\"custom-value-input\"]", "Custom Values Input");
	}

	@Override
	public WebElement cancelButton(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text='Cancel']", "Cancel Button");
	}

	@Override
	public WebElement doneButton(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text='Done']", "Done Button");
	}
}
