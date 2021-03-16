package test.java.pageObjects;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;

/**
 * The CredentialsPage Interface is to hold webelement for Credentials Page
 *
 */
public interface CredentialPage {

	public  WebElement accept_Button(AppiumDriver driver) throws Exception;
	public  WebElement reject_Button(AppiumDriver driver) throws Exception;
	public  WebElement header(AppiumDriver driver, String header) throws Exception;
	public  WebElement title(AppiumDriver driver, String title) throws Exception;
	public  WebElement credentialSenderName(AppiumDriver driver, String connectionName) throws Exception;
	public  WebElement credentialSenderLogo(AppiumDriver driver) throws Exception;
	public  WebElement credentialName(AppiumDriver driver, String credentialName) throws Exception;
	public  WebElement credentialAttributeName(AppiumDriver driver, String name) throws Exception;
	public  WebElement credentialAttributeValue(AppiumDriver driver, String value) throws Exception;
	public  WebElement backArrow(AppiumDriver driver) throws Exception;
	public  WebElement closeButton(AppiumDriver driver) throws Exception;
	public  WebElement deleteButton(AppiumDriver driver) throws Exception;

}
