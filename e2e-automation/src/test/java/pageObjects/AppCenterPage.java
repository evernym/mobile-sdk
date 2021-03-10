package test.java.pageObjects;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;

/**
 * The AppCenterPage Interface is to hold webelement for AppCenter Page
 *
 */
public interface AppCenterPage {

    public WebElement signInGoogle(AppiumDriver driver) throws Exception;
//    public WebElement changeAccount(AppiumDriver driver) throws Exception;
    public  WebElement userNameText(AppiumDriver driver) throws Exception;
    public WebElement userNameNext(AppiumDriver driver) throws Exception;
    public  WebElement passwordText(AppiumDriver driver) throws Exception;
    public WebElement passwordNext(AppiumDriver driver) throws Exception;

}
