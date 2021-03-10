package test.java.pageObjectsAndroid;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import test.java.appModules.AppiumUtils;

public class OnfidoPageAndroid implements test.java.pageObjects.OnfidoPage {

    public WebElement onfidoContainer(AppiumDriver driver) throws Exception {
        return null;
    }

    public WebElement onfidoHeader(AppiumDriver driver) throws Exception {
        return null;
    }

    public WebElement backArrow(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//android.view.ViewGroup[@content-desc=\"left-icon\"]",
                "Back Arrow"
        );
    }

    public WebElement acceptButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"I accept\"]",
                "Accept Button"
        );
    }

}
