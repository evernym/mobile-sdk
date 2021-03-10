package test.java.pageObjectsiOS;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import test.java.appModules.AppiumUtils;
import test.java.pageObjects.CustomValuesPage;

/**
 * The CustomValuesPageiOS class is to hold webelement for CustomValues Page for iOS
 *
 */
public class CustomValuesPageiOS implements CustomValuesPage {
	@Override
	public WebElement title(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, "Custom Values", "Custom Values Header");
	}

	@Override
	public WebElement description(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, "Please provide values for the following attributes", "Custom Values Description");
	}

	@Override
	public WebElement attributeNameLabel(AppiumDriver driver, String attribute) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, attribute, "Attribute Name");
	}

	@Override
	public WebElement customValueInput(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, "custom-value-input", "Custom Values Input");
	}

	@Override
	public WebElement cancelButton(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, "Cancel", "Cancel Button");
	}

	@Override
	public WebElement doneButton(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, "Done", "Done Button");
	}
}
