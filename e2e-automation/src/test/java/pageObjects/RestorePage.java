package test.java.pageObjects;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;

public interface RestorePage {
    public WebElement restoreFromBackupButton(AppiumDriver driver) throws Exception;
    public WebElement restoreHeader(AppiumDriver driver) throws Exception;
    public WebElement restoreFromCloudButton(AppiumDriver driver) throws Exception;
    public WebElement restoreFromDeviceButton(AppiumDriver driver) throws Exception;
    public WebElement zipFileSelector(AppiumDriver driver, String fileName) throws Exception;
    public WebElement recoveryPhraseBoxLocal(AppiumDriver driver) throws Exception;
    public WebElement recoveryPhraseBoxCloud(AppiumDriver driver) throws Exception;
    public WebElement recoveryWaitingMessage(AppiumDriver driver) throws Exception;
    public WebElement enterPasscodeMessage(AppiumDriver driver) throws Exception;
}
