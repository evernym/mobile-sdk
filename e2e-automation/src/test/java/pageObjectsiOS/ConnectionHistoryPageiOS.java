package test.java.pageObjectsiOS;

import io.appium.java_client.AppiumDriver;
import test.java.appModules.AppiumUtils;
import test.java.pageObjects.ConnectionHistoryPage;

import org.openqa.selenium.WebElement;

/**
 * The ConnectionHistoryPageiOS class is to hold webelement for ConnectionHistory Page iOS
 *
 */
public class ConnectionHistoryPageiOS implements ConnectionHistoryPage {

	public  WebElement close_Button(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver,
				"//XCUIElementTypeOther[@name='connection-history-icon-close-touchable']", "Close Button");

	}

	public  WebElement received_Status(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "//XCUIElementTypeOther[contains(@name, 'RECEIVED')]",
				"Received Status");

	}

	public  WebElement delete_Icon(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "//XCUIElementTypeOther[@name='connection-history-icon-delete-touchable']",
				"Delete Icon");

	}

	public  WebElement delete_Button(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "//XCUIElementTypeButton[@name='Delete']",
				"Delete Button");

	}

	public  WebElement shared_Button(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "	//XCUIElementTypeOther[contains(@name, 'SHARED ProofTestAuto')]",
				"SHARED Proof Button");
	}

	public  WebElement atrribute_Text(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "//XCUIElementTypeText[@name='custom-list-data-0']",
				"Attribute Text");
	}

	public WebElement connectedRecord(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElementsByAccessibilityId(driver, "Added Connection", "Connected Status");
	}

	public WebElement connectedRecordDescription(AppiumDriver driver, String name) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, "Added Connection", "Connected Status");
	}

	@Override
	public WebElement questionReceivedRecord(AppiumDriver driver, String question) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, question, "Question Received Title");
	}

	@Override
	public WebElement questionReceivedRecordDescription(AppiumDriver driver, String description) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, description, "Question Received Description");
	}

	@Override
	public WebElement viewReceivedQuestionButton(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElement(driver, "(//XCUIElementTypeOther[@name=\"View\"])[2]", "View Question Button");
	}

	@Override
	public WebElement questionAnswerRecord(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, "YOU ANSWERED", "Question Answered Record");
	}

	@Override
	public WebElement questionAnswerRecordDescription(AppiumDriver driver, String answer) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, "\"" + answer + "\"", "Question Answered Record Description");
	}

	public WebElement backButton(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElementsByAccessibilityId(driver, "back-arrow-touchable", "Back button");
	}

	public WebElement connectionLogo(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElementsByAccessibilityId(driver, "logo", "Connection logo");
	}

	public WebElement connectionName(AppiumDriver driver, String name) throws Exception {
		return AppiumUtils.findElementsByAccessibilityId(driver, name, "Connection name");
	}

	public WebElement threeDotsButton(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElementsByAccessibilityId(driver, "three-dots", "Three dots");
	}

	@Override
	public WebElement sharedProofRecord(AppiumDriver driver, String name) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, name, "Shared Proof Record");	}

	@Override
	public WebElement rejectedProofRequestRecord(AppiumDriver driver, String name) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, "YOU REJECTED", "Proof Rejected Record");
	}

	@Override
	public WebElement viewProofRequestDetailsButton(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, "VIEW REQUEST DETAILS", "View Proof Button");
	}

  @Override
  public WebElement acceptedCredentialRecord(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, "ACCEPTED CREDENTIAL", "Accepted Credential Record");
  }

  @Override
	public WebElement acceptedCredentialViewButton(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, "VIEW CREDENTIAL", "View Credential Button");
	}

	@Override
	public WebElement rejectedCredentialRecord(AppiumDriver driver, String name) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, "YOU REJECTED", "Credential Rejected Record");
	}
}
