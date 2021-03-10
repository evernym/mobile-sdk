package test.java.pageObjects;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;

/**
 * The CredentialsPage Interface is to hold webelement for Question Page
 *
 */
public interface QuestionPage {

	public  WebElement header(AppiumDriver driver) throws Exception;
	public  WebElement senderLogo(AppiumDriver driver) throws Exception;
	public  WebElement senderDefaultLogo(AppiumDriver driver) throws Exception;
	public  WebElement senderName(AppiumDriver driver, String name) throws Exception;
	public  WebElement title(AppiumDriver driver, String question) throws Exception;
	public  WebElement description(AppiumDriver driver, String description) throws Exception;
	public  WebElement answer_Button(AppiumDriver driver, String answer) throws Exception;
	public  WebElement answer_Option(AppiumDriver driver, String option) throws Exception;
	public  WebElement submit_Button(AppiumDriver driver) throws Exception;

}
