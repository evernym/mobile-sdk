package test.java.pageObjectsAndroid;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import test.java.appModules.AppiumUtils;

public class QrScannerPageAndroid implements test.java.pageObjects.QrScannerPage {

    public WebElement scannerAllowButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findTextElementInDifferentCases(driver, "Allow", "Allow Button");
    }


    public WebElement scannerCloseButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(driver, "//android.view.ViewGroup[@content-desc=\"close-qr-scanner-icon\"]", "Close Scanner Button");
    }
}
