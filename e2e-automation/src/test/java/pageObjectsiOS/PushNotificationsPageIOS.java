package test.java.pageObjectsiOS;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import test.java.appModules.AppiumUtils;
import test.java.pageObjects.PushNotificationsPage;
/**
 * The ChooseLockPageAndroid class is to hold webelement for Push Notifications Page for Android
 *
 */
public class PushNotificationsPageIOS implements PushNotificationsPage {

  @Override
  public WebElement allow_Button(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, "allow-notifications-button", "Allow Push Notifications");
  }

  @Override
  public WebElement not_now_Button(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, "not-now-notifications-button", "Not Now Push Notifications");
  }

  @Override
  public WebElement ok_Button(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElement(driver, "//XCUIElementTypeButton[@name=\"Allow\"]", "OK Button");
  }
}

