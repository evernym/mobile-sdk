package test.java.pageObjects;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;

public interface MyCredentialsPage {
    public WebElement myCredentialsHeader(AppiumDriver driver) throws Exception;
    public WebElement burgerMenuButton(AppiumDriver driver) throws Exception;
    public WebElement testCredential(AppiumDriver driver, String name) throws Exception;
    public WebElement testCredentialTitle(AppiumDriver driver, String name) throws Exception;
    public WebElement testCredentialAvatar(AppiumDriver driver, String name) throws Exception;
}
