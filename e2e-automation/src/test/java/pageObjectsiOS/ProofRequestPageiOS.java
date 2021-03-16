package test.java.pageObjectsiOS;

import io.appium.java_client.AppiumDriver;
import test.java.appModules.AppiumUtils;
import test.java.pageObjects.ProofRequestPage;

import org.openqa.selenium.WebElement;

/**
 * The ProofRequestPageiOS class is to hold webelement for ProofRequest Page for iOS
 *
 */
public class ProofRequestPageiOS implements ProofRequestPage {

	public  WebElement shareButton(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElementsByAccessibilityId(driver, "Share Attributes", "Send Button");

	}

	public  WebElement rejectButton(AppiumDriver driver) throws Exception {
	// FIXME
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
	public WebElement proofRequestSenderName(AppiumDriver driver, String connectionName) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, connectionName, "Sender Name");
	}

	@Override
	public WebElement proofRequestSenderLogo(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, "sender-avatar", "Sender Logo");
	}

	@Override
	public WebElement proofRequestName(AppiumDriver driver, String proofName) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, proofName, "Proof Request Name");
	}

	@Override
	public WebElement attributeName(AppiumDriver driver, String name) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, name, "Shared Attribute: " + name );
	}

	@Override
	public WebElement attributeValue(AppiumDriver driver, String value) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, value, "Shared Attribute Value: " + value );
	}

	@Override
	public WebElement closeButton(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, "Close", "Close Button");
	}

	@Override
	public WebElement selectedCredentialIcon(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElementsByAccessibilityId(driver, "selected-credential-icon", "Selected Credential Icon");
	}

	@Override
	public WebElement arrowForwardIcon(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElementsByAccessibilityId(driver, "arrow-forward-icon", "Arrow Forward Icon");
	}

	@Override
	public WebElement missingAttributePlaceholder(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElementsByAccessibilityId(driver, "Missing - Tap to fix", "Missing Attribute");
	}

	@Override
	public WebElement missingCredentialsError(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElementsByAccessibilityId(driver, "Missing Credentials", "Missing Credentials Error Title");
	}

	@Override
	public WebElement okButton(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElementsByAccessibilityId(driver, "OK", "OK Button");
	}

	@Override
	public WebElement notFoundError(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElementsByAccessibilityId(driver, "Not found", "Not Found Error");
	}

	@Override
	public WebElement unresolvedPredicateError(AppiumDriver driver, String text) throws Exception {
		return AppiumUtils.findElementsByAccessibilityId(driver, text, "Unresolved predicate Error");
	}

	@Override
	public WebElement notFoundIcon(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElementsByAccessibilityId(driver, "alert-icon", "Not Found Icon");
	}
}
