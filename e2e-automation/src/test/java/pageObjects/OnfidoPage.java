package test.java.pageObjects;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;

public interface OnfidoPage {
    public WebElement onfidoContainer(AppiumDriver driver) throws Exception;
    public WebElement onfidoHeader(AppiumDriver driver) throws Exception;
    public WebElement backArrow(AppiumDriver driver) throws Exception;
    public WebElement acceptButton(AppiumDriver driver) throws Exception;
}
