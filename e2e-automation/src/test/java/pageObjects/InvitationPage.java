package test.java.pageObjects;

import org.openqa.selenium.WebElement;

import io.appium.java_client.AppiumDriver;
import test.java.appModules.AppiumUtils;

/**
 * The InvitationPage Interface is to hold webelement for Invitation Page
 *
 */
public interface InvitationPage {

	public WebElement title(AppiumDriver driver) throws Exception;
	public WebElement inviteeAvatar(AppiumDriver driver) throws Exception;
	public WebElement inviterAvatar(AppiumDriver driver) throws Exception;
	public WebElement deny_Button(AppiumDriver driver) throws Exception;
	public WebElement connect_Button(AppiumDriver driver) throws Exception;
}
