package test.java.pageObjects;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;

public interface PushNotificationsPage {
    public WebElement allow_Button(AppiumDriver driver) throws Exception;
    public WebElement not_now_Button(AppiumDriver driver) throws Exception;
    public WebElement ok_Button(AppiumDriver driver) throws Exception;
}
