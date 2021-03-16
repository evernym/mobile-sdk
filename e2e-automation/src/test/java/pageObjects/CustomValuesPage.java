package test.java.pageObjects;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;

/**
 * The CustomValuesPage Interface is to hold webelement for Custom Values Page
 *
 */
public interface CustomValuesPage {

	public  WebElement title(AppiumDriver driver) throws Exception;
	public  WebElement description(AppiumDriver driver) throws Exception;
	public  WebElement attributeNameLabel(AppiumDriver driver, String attribute) throws Exception;
	public  WebElement customValueInput(AppiumDriver driver) throws Exception;
	public  WebElement cancelButton(AppiumDriver driver) throws Exception;
	public  WebElement doneButton(AppiumDriver driver) throws Exception;

}
