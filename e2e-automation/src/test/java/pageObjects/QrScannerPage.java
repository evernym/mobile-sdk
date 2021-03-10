package test.java.pageObjects;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;

public interface QrScannerPage {
    public WebElement scannerAllowButton(AppiumDriver driver) throws Exception;
    public WebElement scannerCloseButton(AppiumDriver driver) throws Exception;
}
