package test.java.pageObjectsiOS;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import test.java.appModules.AppiumUtils;

import java.util.List;

public class MyConnectionsPageiOS implements test.java.pageObjects.MyConnectionsPage {

    public WebElement myConnectionsHeader(AppiumDriver driver) throws Exception {
      return AppiumUtils.findElement(
        driver,
        "//XCUIElementTypeStaticText[@name=\"My Connections\"]",
        "My Connections Header"
      );    }

    public WebElement burgerMenuButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//XCUIElementTypeOther[@name=\"burger-menu\"]",
                "Burger Menu Button"
        );
    }

    public WebElement newConnection(AppiumDriver driver) throws Exception {
        return null;
    }

    public WebElement testConnection(AppiumDriver driver, String name) throws Exception {
      return AppiumUtils.findElementsByAccessibilityId(
        driver,
        name + "-title",
        "Test Connection Item"
      );
    }

    public List<WebElement> viewButtonsList(AppiumDriver driver) throws Exception {
        return null;
    }

}
