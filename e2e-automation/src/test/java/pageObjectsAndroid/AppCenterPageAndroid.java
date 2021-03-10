package test.java.pageObjectsAndroid;

import io.appium.java_client.AppiumDriver;
import test.java.appModules.AppiumUtils;
import test.java.pageObjects.AppCenterPage;
import org.openqa.selenium.WebElement;

/**
 * The AppCenterPageAndroid class is to hold webelement for AppCenter Page for
 * Android
 *
 */
public class AppCenterPageAndroid implements AppCenterPage {

    private WebElement element = null;

    public WebElement signInGoogle(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(driver, "//*[@id=\"content\"]/div/div[3]/div/div[1]/div[2]/div/div/a[2]/div/span/span", "Sign In via Google");
    }

//    public WebElement changeAccount(AppiumDriver driver) throws Exception {
//        return AppiumUtils.findElement(driver, "", "Change Account");
//    }

    public WebElement userNameText(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(driver, "//*[@name=\"identifier\"]", "UserID TextBox");
    }

    public WebElement userNameNext(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(driver, "//*[@id=\"identifierNext\"]", "UserID Next");
    }

    public WebElement passwordText(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(driver, "//*[@name=\"password\"]", "UserPassword TextBox");
    }

    public WebElement passwordNext(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(driver, "//*[@id=\"passwordNext\"]", "UserPassword Next");
    }

}
