package test.java.pageObjectsAndroid;

import io.appium.java_client.AppiumDriver;
import test.java.appModules.AppiumUtils;
import test.java.pageObjects.CredentialPage;

import org.openqa.selenium.WebElement;

/**
 * The CredentialPageAndroid class is to hold webelement for Credentials Page
 * for Android
 *
 */
public class CredentialPageAndroid implements CredentialPage {


	public WebElement accept_Button(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text=\"Accept Credential\"]", "Accept Button");
	}


	public WebElement reject_Button(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text=\"Reject\"]", "Reject Button");
	}

	@Override
	public WebElement header(AppiumDriver driver, String header) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text=\"" + header + "\"]", "Header");
	}

	@Override
	public WebElement title(AppiumDriver driver, String title) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text=\"" + title + "\"]", "Title");
	}

	@Override
	public WebElement credentialSenderName(AppiumDriver driver, String connectionName) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text=\"" + connectionName + "\"]", "Sender Name");
	}

	@Override
	public WebElement credentialSenderLogo(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//android.widget.ImageView[@content-desc=\"sender-avatar-image\"]", "Sender Logo");
	}

	@Override
	public WebElement credentialName(AppiumDriver driver, String credentialName) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text=\"" + credentialName + "\"]", "Credential Name");
	}

	@Override
	public WebElement credentialAttributeName(AppiumDriver driver, String name) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text=\"" + name + "\"]", "Credential Attribute: " + name );
	}

	@Override
	public WebElement credentialAttributeValue(AppiumDriver driver, String value) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text=\"" + value + "\"]", "Credential Attribute Value: " + value );
	}

	@Override
	public WebElement backArrow(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//android.view.ViewGroup[@content-desc=\"back-arrow\"]", "Back Arrow");
	}

	@Override
	public WebElement closeButton(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text=\"Close\"]", "Close Button");
	}

	@Override
	public WebElement deleteButton(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text=\"Delete Credential\"]", "Delete Button");
	}
}
