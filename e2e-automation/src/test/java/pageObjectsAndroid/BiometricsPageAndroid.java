package test.java.pageObjectsAndroid;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import test.java.appModules.AppiumUtils;

public class BiometricsPageAndroid implements test.java.pageObjects.BiometricsPage {

    public WebElement cancelButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"CANCEL\"]",
                "Cancel Button"
        );
    }

    public WebElement okButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"OK\"]",
                "Ok Button"
        );
    }

}
