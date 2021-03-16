package test.java.pageObjects;

import io.appium.java_client.AppiumDriver;
import test.java.appModules.AppiumUtils;

import org.openqa.selenium.WebElement;

/**
 * The ProofRequestPage Interface is to hold webelement for ProofRequest Page
 *
 */
public interface ProofRequestPage {

	public  WebElement shareButton(AppiumDriver driver) throws Exception;
	public  WebElement rejectButton(AppiumDriver driver) throws Exception;
	public  WebElement header(AppiumDriver driver, String header) throws Exception;
	public  WebElement title(AppiumDriver driver, String title) throws Exception;
	public  WebElement proofRequestSenderName(AppiumDriver driver, String connectionName) throws Exception;
	public  WebElement proofRequestSenderLogo(AppiumDriver driver) throws Exception;
	public  WebElement proofRequestName(AppiumDriver driver, String proofName) throws Exception;
	public  WebElement attributeName(AppiumDriver driver, String name) throws Exception;
	public  WebElement attributeValue(AppiumDriver driver, String value) throws Exception;
	public  WebElement closeButton(AppiumDriver driver) throws Exception;
	public  WebElement selectedCredentialIcon(AppiumDriver driver) throws Exception;
	public  WebElement arrowForwardIcon(AppiumDriver driver) throws Exception;
	public  WebElement missingAttributePlaceholder(AppiumDriver driver) throws Exception;
	public  WebElement missingCredentialsError(AppiumDriver driver) throws Exception;
	public  WebElement okButton(AppiumDriver driver) throws Exception;
	public  WebElement notFoundError(AppiumDriver driver) throws Exception;
	public  WebElement unresolvedPredicateError(AppiumDriver driver, String text) throws Exception;
	public  WebElement notFoundIcon(AppiumDriver driver) throws Exception;
}
