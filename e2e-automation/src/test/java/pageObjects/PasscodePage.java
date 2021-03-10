package test.java.pageObjects;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;

public interface PasscodePage {
    public WebElement passcodeTitle(AppiumDriver driver) throws Exception;
    public WebElement backArrow(AppiumDriver driver) throws Exception;
}
