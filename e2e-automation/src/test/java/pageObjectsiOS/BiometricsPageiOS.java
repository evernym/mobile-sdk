package test.java.pageObjectsiOS;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import test.java.appModules.AppiumUtils;

public class BiometricsPageiOS implements test.java.pageObjects.BiometricsPage {

    public WebElement cancelButton(AppiumDriver driver) throws Exception {
        return null;
    }

    public WebElement okButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//XCUIElementTypeButton[@name=\"Ok\"]",
                "Biometrics Ok Button"
        );
    }

}
