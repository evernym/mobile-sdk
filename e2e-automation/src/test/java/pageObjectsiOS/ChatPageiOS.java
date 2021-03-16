package test.java.pageObjectsiOS;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import test.java.appModules.AppiumUtils;

public class ChatPageiOS implements test.java.pageObjects.ChatPage {

    public WebElement chatContainer(AppiumDriver driver) throws Exception {
        return null;
    }

    public WebElement chatHeader(AppiumDriver driver) throws Exception {
        return null;
    }

    public WebElement backArrow(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//XCUIElementTypeButton[@name=\"Close\"]",
                "Close Button"
        );
    }

}
