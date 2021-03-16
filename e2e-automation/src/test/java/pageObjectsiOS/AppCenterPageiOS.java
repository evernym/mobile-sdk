package test.java.pageObjectsiOS;

import io.appium.java_client.AppiumDriver;
import test.java.appModules.AppiumUtils;
import test.java.pageObjects.AppCenterPage;
import org.openqa.selenium.WebElement;

/**
 * The AppCenterPageiOS class is to hold webelement for AppCenter Page for
 * iOS
 *
 */
public class AppCenterPageiOS implements AppCenterPage {

    private WebElement element = null;

    public WebElement signInGoogle(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(driver, "", "Sign In via Google");

    }

//    public WebElement changeAccount(AppiumDriver driver) throws Exception {
//        return AppiumUtils.findElement(driver, "", "Change Account");
//    }

    public WebElement userNameText(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(driver, "", "UserID TextBox");

    }

    public WebElement userNameNext(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(driver, "", "UserID Next");

    }

    public WebElement passwordText(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(driver, "", "UserPassword TextBox");
    }

    public WebElement passwordNext(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(driver, "", "UserPassword Next");
    }

}
