package test.java.pageObjectsiOS;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import test.java.appModules.AppiumUtils;

public class OnfidoPageiOS implements test.java.pageObjects.OnfidoPage {

    public WebElement onfidoContainer(AppiumDriver driver) throws Exception {
        return null;
    }

    public WebElement onfidoHeader(AppiumDriver driver) throws Exception {
        return null;
    }

    public WebElement backArrow(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "(//XCUIElementTypeOther[@name=\"left-icon\"])[2]",
                "Back Arrow"
        );
    }

    public WebElement acceptButton(AppiumDriver driver) throws Exception {
        return null;
    }

}
