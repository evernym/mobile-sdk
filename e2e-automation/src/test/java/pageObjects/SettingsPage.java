package test.java.pageObjects;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;

public interface SettingsPage {
    public WebElement settingsContainer(AppiumDriver driver) throws Exception;
    public WebElement settingsHeader(AppiumDriver driver) throws Exception;
    public WebElement burgerMenuButton(AppiumDriver driver) throws Exception;
    public WebElement createBackupButton(AppiumDriver driver) throws Exception;
    public WebElement manualBackupButton(AppiumDriver driver) throws Exception;
    public WebElement automaticCloudBackupsButton(AppiumDriver driver) throws Exception;
    public WebElement biometricsButton(AppiumDriver driver) throws Exception;
    public WebElement passcodeButton(AppiumDriver driver) throws Exception;
    public WebElement chatButton(AppiumDriver driver) throws Exception;
    public WebElement aboutButton(AppiumDriver driver) throws Exception;
    public WebElement onfidoButton(AppiumDriver driver) throws Exception;
}
