package test.java.pageObjectsiOS;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import test.java.appModules.AppiumUtils;

public class QrScannerPageiOS implements test.java.pageObjects.QrScannerPage {

    public WebElement scannerAllowButton(AppiumDriver driver) throws Exception {
      return AppiumUtils.findElement(driver, "//XCUIElementTypeButton[@name=\"OK\"]", "OK Button");
    }

    public WebElement scannerCloseButton(AppiumDriver driver) throws Exception {
      return AppiumUtils.findElementsByAccessibilityId(driver, "close-qr-scanner-icon", "Close Scanner Button");
    }

}
