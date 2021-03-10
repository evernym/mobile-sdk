package test.java.pageObjectsAndroid;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import test.java.appModules.AppiumUtils;

import java.util.List;

public class MyConnectionsPageAndroid implements test.java.pageObjects.MyConnectionsPage {

    public WebElement myConnectionsHeader(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"My Connections\"]",
                "My Connections Header"
        );
    }

    public WebElement burgerMenuButton(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//android.view.ViewGroup[@content-desc=\"burger-menu\"]",
                "Burger Menu Button"
        );
    }

    public WebElement newConnection(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"NEW\"]",
                "New Connection Item"
        );
    }

    public WebElement testConnection(AppiumDriver driver, String name) throws Exception {
        return AppiumUtils.findElement(
                driver,
                "//*[@text=\"" + name + "\"]",
                "Test Connection Item"
        );
    }

    public List<WebElement> viewButtonsList(AppiumDriver driver) throws Exception {
        return AppiumUtils.findElements(
                driver,
                "//*[@text=\"View\"]",
                "View Button"
        );
    }

}
