package test.java.pageObjectsAndroid;

import io.appium.java_client.AppiumDriver;
import test.java.appModules.AppiumUtils;
import test.java.pageObjects.ConnectionHistoryPage;

import org.openqa.selenium.WebElement;

/**
 * The ConnectionHistoryPageAndroid class is to hold webelement for ConnectionHistory Page for Android
 *
 */
public class ConnectionHistoryPageAndroid implements ConnectionHistoryPage {


	public WebElement close_Button(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//android.widget.ImageView[@content-desc='connection-history-icon-close']",
				"Close Button");
	}


	public WebElement received_Status(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//*[contains(@name, 'RECEIVED')]",
				"Received Status");
	}


	public WebElement delete_Icon(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}


	public WebElement delete_Button(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}


	public WebElement shared_Button(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}


	public WebElement atrribute_Text(AppiumDriver driver) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public WebElement connectedRecord(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text=\"Added Connection\"]",
				"Connected Status");
	}

	public WebElement connectedRecordDescription(AppiumDriver driver, String name) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text=\"You added " + name + " as a Connection\"]",
				"Connected Description");
	}

	@Override
	public WebElement questionReceivedRecord(AppiumDriver driver, String question) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text=\"" + question + "\"]",
				"Question Received Title");
	}

	@Override
	public WebElement questionReceivedRecordDescription(AppiumDriver driver, String description) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text=\"" + description + "\"]",
				"Question Received Description");
	}

	@Override
	public WebElement viewReceivedQuestionButton(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text=\"View\"]", "View Question Button");
	}

	@Override
	public WebElement questionAnswerRecord(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text=\"YOU ANSWERED\"]",
				"Question Answered Record");
	}

	@Override
	public WebElement questionAnswerRecordDescription(AppiumDriver driver, String answer) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text='\"" + answer + "\"']",
				"Question Answered Record Description");
	}

	public WebElement backButton(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//android.view.ViewGroup[@content-desc=\"back-arrow\"]",
				"Back button");
	}

	public WebElement connectionLogo(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//android.view.ViewGroup[@content-desc=\"logo\"]",
				"Connection logo");
	}

	public WebElement connectionName(AppiumDriver driver, String name) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text=\"" + name + "\"]", "Connection name");
	}

	public WebElement threeDotsButton(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//android.view.ViewGroup[@content-desc=\"three-dots\"]",
				"Three dots");
	}

	@Override
	public WebElement sharedProofRecord(AppiumDriver driver, String name) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text=\"" + name + "\"]",
				"Shared Proof Record");
	}

	@Override
	public WebElement rejectedProofRequestRecord(AppiumDriver driver, String name) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text=\"YOU REJECTED\"]", "Proof Rejected Record");
	}

	@Override
	public WebElement viewProofRequestDetailsButton(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text=\"VIEW REQUEST DETAILS\"]", "View Proof Button");
	}

  @Override
  public WebElement acceptedCredentialRecord(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElement(driver, "//*[@text=\"ACCEPTED CREDENTIAL\"]", "Accepted Credential Record");
  }

  @Override
	public WebElement acceptedCredentialViewButton(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text=\"VIEW CREDENTIAL\"]", "View Credential Button");
	}

	@Override
	public WebElement rejectedCredentialRecord(AppiumDriver driver, String name) throws Exception {
		return AppiumUtils.findElement(driver, "//*[@text=\"YOU REJECTED\"]", "Credential Rejected Record");
	}

}
