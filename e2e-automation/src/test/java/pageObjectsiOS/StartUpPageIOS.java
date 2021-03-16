package test.java.pageObjectsiOS;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import test.java.appModules.AppiumUtils;
import test.java.pageObjects.StartUpPage;

/**
 * The ChooseLockPageAndroid class is to hold webelement for StartUpPageAndroid Page for Android
 * 
 */
public class StartUpPageIOS implements StartUpPage {


	public WebElement set_up_button(AppiumDriver driver) throws Exception {
//		return AppiumUtils.findElementsByAccessibilityId(driver, 	"Set Up", "Set Up Button");
		return AppiumUtils.findElement(driver, "(//XCUIElementTypeOther[@name=\"Set Up\"])[27]", "Set Up Button");
	}
}
