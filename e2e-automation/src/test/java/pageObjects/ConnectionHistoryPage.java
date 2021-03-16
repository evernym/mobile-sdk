package test.java.pageObjects;

import io.appium.java_client.AppiumDriver;
import test.java.appModules.AppiumUtils;

import org.openqa.selenium.WebElement;

/**
 * The ConnectionHistoryPage Interface is to hold webelement for ConnectionHistory Page
 *
 */
public interface ConnectionHistoryPage {

	public  WebElement close_Button(AppiumDriver driver) throws Exception;
	public  WebElement received_Status(AppiumDriver driver) throws Exception;
	public  WebElement delete_Icon(AppiumDriver driver) throws Exception;
	public  WebElement delete_Button(AppiumDriver driver) throws Exception;
	public  WebElement shared_Button(AppiumDriver driver) throws Exception;
	public  WebElement atrribute_Text(AppiumDriver driver) throws Exception;
	public  WebElement connectedRecord(AppiumDriver driver) throws Exception;
	public  WebElement connectedRecordDescription(AppiumDriver driver, String name) throws Exception;
	public  WebElement questionReceivedRecord(AppiumDriver driver, String question) throws Exception;
	public  WebElement questionReceivedRecordDescription(AppiumDriver driver, String description) throws Exception;
	public  WebElement viewReceivedQuestionButton(AppiumDriver driver) throws Exception;
	public  WebElement questionAnswerRecord(AppiumDriver driver) throws Exception;
	public  WebElement questionAnswerRecordDescription(AppiumDriver driver, String answer) throws Exception;
	public  WebElement backButton(AppiumDriver driver) throws Exception;
	public  WebElement connectionLogo(AppiumDriver driver) throws Exception;
	public  WebElement connectionName(AppiumDriver driver, String name) throws Exception;
	public  WebElement threeDotsButton(AppiumDriver driver) throws Exception;
	public  WebElement sharedProofRecord(AppiumDriver driver, String name) throws Exception;
	public  WebElement rejectedProofRequestRecord(AppiumDriver driver, String name) throws Exception;
	public  WebElement viewProofRequestDetailsButton(AppiumDriver driver) throws Exception;
	public  WebElement acceptedCredentialRecord(AppiumDriver driver) throws Exception;
	public  WebElement acceptedCredentialViewButton(AppiumDriver driver) throws Exception;
	public  WebElement rejectedCredentialRecord(AppiumDriver driver, String name) throws Exception;
}
