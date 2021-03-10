package test.java.pageObjectsAndroid;

import io.appium.java_client.AppiumDriver;
import test.java.appModules.AppiumUtils;
import test.java.pageObjects.ProofRequestPage;

import org.openqa.selenium.WebElement;

/**
 * The ProofRequestPageAndroid class is to hold webelement for ProofRequest Page for Android
 *
 */
public class ProofRequestPageAndroid implements ProofRequestPage {


	public WebElement shareButton(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text='Share Attributes']", "Share Button");
	}


	public WebElement rejectButton(AppiumDriver driver) throws Exception {

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
	public WebElement proofRequestSenderName(AppiumDriver driver, String connectionName) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text=\"" + connectionName + "\"]", "Sender Name");
	}

	@Override
	public WebElement proofRequestSenderLogo(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//android.widget.ImageView[@content-desc=\"sender-avatar-image\"]", "Sender Logo");
	}

	@Override
	public WebElement proofRequestName(AppiumDriver driver, String proofName) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text=\"" + proofName + "\"]", "Proof Request Name");
	}

	@Override
	public WebElement attributeName(AppiumDriver driver, String name) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text=\"" + name + "\"]", "Shared Attribute: " + name );
	}

	@Override
	public WebElement attributeValue(AppiumDriver driver, String value) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text=\"" + value + "\"]", "Shared Attribute Value: " + value );
	}

	@Override
	public WebElement closeButton(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text=\"Close\"]", "Close Button");
	}

	@Override
	public WebElement selectedCredentialIcon(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@content-desc=\"selected-credential-icon\"]", "Selected Credential Icon");
	}

	@Override
	public WebElement arrowForwardIcon(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@content-desc=\"arrow-forward-icon\"]", "Arrow Forward Icon");
	}

	public WebElement missingAttributePlaceholder(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text='Missing - Tap to fix']", "Missing Attribute Placeholder");
	}

	@Override
	public WebElement missingCredentialsError(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text=\"Missing Credentials\"]", "Missing Credentials Error Title");
	}

	@Override
	public WebElement okButton(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text=\"OK\"]", "Ok Button");
	}

	@Override
	public WebElement notFoundError(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text=\"Not found\"]", "Not Found Error");
	}

	@Override
	public WebElement unresolvedPredicateError(AppiumDriver driver, String text) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text='" + text + "']", "Unresolved predicate Error");
	}

	@Override
	public WebElement notFoundIcon(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@content-desc=\"alert-icon\"]", "Not Found Icon");
	}
}
