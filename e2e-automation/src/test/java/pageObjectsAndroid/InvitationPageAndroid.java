package test.java.pageObjectsAndroid;

import org.openqa.selenium.WebElement;

import io.appium.java_client.AppiumDriver;
import test.java.appModules.AppiumUtils;
import test.java.pageObjects.InvitationPage;

/**
 * The InvitationPageAndroid class is to hold webelement for Invitation Page for Android
 *
 */
public class InvitationPageAndroid implements InvitationPage  {

  @Override
  public WebElement title(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElement(driver, "//android.view.ViewGroup[@content-desc=\"invitation-text-container-message-title\"]","Invitation Title");
  }

  @Override
  public WebElement inviteeAvatar(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElement(driver, "//android.widget.ImageView[@content-desc=\"invitation-avatars-invitee-image\"]","Invitation Invitee Avatar");
  }

  @Override
  public WebElement inviterAvatar(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElement(driver, "//android.widget.ImageView[@content-desc=\"invitation-avatars-inviter-image\"]","Invitation Inviter Avatar");
  }

  @Override
  public WebElement deny_Button(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElement(driver, "//android.view.ViewGroup[@content-desc=\"invitation-deny\"]","Deny Button");
  }

  public WebElement connect_Button(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//android.view.ViewGroup[@content-desc=\"invitation-accept\"]","Connect Button");
	}

}
