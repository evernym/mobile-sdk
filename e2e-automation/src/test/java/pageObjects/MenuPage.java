package test.java.pageObjects;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;

public interface MenuPage {
    public WebElement menuContainer(AppiumDriver driver) throws Exception;
    public WebElement connectMeBanner(AppiumDriver driver) throws Exception;
    public WebElement userAvatar(AppiumDriver driver) throws Exception;
    public WebElement okButton(AppiumDriver driver) throws Exception;
    public WebElement menuAllowButton(AppiumDriver driver) throws Exception;
    public WebElement homeButton(AppiumDriver driver) throws Exception;
    public WebElement myConnectionsButton(AppiumDriver driver) throws Exception;
    public WebElement myCredentialsButton(AppiumDriver driver) throws Exception;
    public WebElement settingsButton(AppiumDriver driver) throws Exception;
    public WebElement connectMeLogo(AppiumDriver driver) throws Exception;
    public WebElement builtByFooter(AppiumDriver driver) throws Exception;
    public WebElement versionFooter(AppiumDriver driver) throws Exception;
}
