package test.java.pageObjectsiOS;

import org.openqa.selenium.WebElement;

import io.appium.java_client.AppiumDriver;
import test.java.appModules.AppiumUtils;
import test.java.pageObjects.InvitationPage;

/**
 * The InvitationPageiOS class is to hold webelement for Invitation Page for iOS
 *
 */
public class InvitationPageiOS implements InvitationPage  {

  @Override
  public WebElement title(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, "invitation-text-container-message-title", "Invitation Title");
  }

  @Override
  public WebElement inviteeAvatar(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, "invitation-avatars-invitee", "Invitation Invitee Avatar");
  }

  @Override
  public WebElement inviterAvatar(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, "invitation-avatars-inviter", "Invitation Inviter Avatar");
  }

  @Override
  public WebElement deny_Button(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, "invitation-deny", "Deny Button");
  }

  public WebElement connect_Button(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElementsByAccessibilityId(driver, "invitation-accept", "Connect Button");
	}

}
