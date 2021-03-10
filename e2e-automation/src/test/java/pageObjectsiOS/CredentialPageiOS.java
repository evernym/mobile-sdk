package test.java.pageObjectsiOS;

import io.appium.java_client.AppiumDriver;
import test.java.appModules.AppiumUtils;
import test.java.pageObjects.CredentialPage;

import org.openqa.selenium.WebElement;

/**
 * The CredentialPageiOS class is to hold webelement for Credentials Page for iOS
 *
 */
public class CredentialPageiOS implements CredentialPage  {

	public  WebElement accept_Button(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElementsByAccessibilityId(driver, "Accept Credential", "Accept Button");
	}

	public  WebElement reject_Button(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, "Reject", "Reject Button");
	}

	@Override
	public WebElement header(AppiumDriver driver, String header) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, header, "Header");
	}

	@Override
	public WebElement title(AppiumDriver driver, String title) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, title, "Title");
	}

	@Override
	public WebElement credentialSenderName(AppiumDriver driver, String connectionName) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, connectionName, "Sender Name");
	}

	@Override
	public WebElement credentialSenderLogo(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, "sender-avatar", "Sender Logo");
	}

	@Override
	public WebElement credentialName(AppiumDriver driver, String credentialName) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, credentialName, "Credential Name");
	}

	@Override
	public WebElement credentialAttributeName(AppiumDriver driver, String name) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, name, "Credential Attribute: " + name );
	}

	@Override
	public WebElement credentialAttributeValue(AppiumDriver driver, String value) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, value, "Credential Attribute Value: " + value );
	}

	@Override
	public WebElement backArrow(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, "back-arrow", "Back Arrow");
	}

	@Override
	public WebElement closeButton(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElementsByAccessibilityId(driver, "Close", "Close Button");
	}

	@Override
	public WebElement deleteButton(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElementsByAccessibilityId(driver, "Delete Credential", "Delete Button");
	}
}
