package test.java.pageObjectsiOS;

import io.appium.java_client.AppiumDriver;
import test.java.appModules.AppiumUtils;
import test.java.pageObjects.PincodePage;

import org.openqa.selenium.WebElement;

/**
 * The PincodePageiOS class is to hold webelement for Pincode Page for iOS
 *
 */
public class PincodePageiOS implements PincodePage {

	/**
	 * to get all the password text box at pincode page
	 * @param  driver - appium driver available for session
	 * @param  i - to get the i the text box at pincode page
	 * @return void
	 */
	public WebElement pinCodeLock_TextBox(AppiumDriver driver, int i) throws Exception {

		return AppiumUtils.findElement(driver, "//*[@name='pin-code-digit-" + i + "']", "Pincode TextBox " + i + "");
	}

	public  WebElement close_Button(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "(//XCUIElementTypeOther[@name=\"close-button\"])[3]", "Close Button");

	}

//	public  WebElement pinCodeSe_TextBox(AppiumDriver driver) throws Exception {
//
//		return AppiumUtils.findElement(driver, "(//XCUIElementTypeOther[@name='Set up a pass code'])[3]/XCUIElementTypeOther[2]/XCUIElementTypeOther/XCUIElementTypeOther", "Pincode TextBox");
//	}

	public  WebElement pinCodeSe_TextBox(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(driver, "//XCUIElementTypeOther[@name=\"pin-code-digit-0-touchable pin-code-digit-1-touchable pin-code-digit-2-touchable pin-code-digit-3-touchable pin-code-digit-4-touchable pin-code-digit-5-touchable\"]", "Passcode Textbox");
	}

	public  WebElement pinCodeSeNew_TextBox(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "(//XCUIElementTypeOther[@name='Set up a new pass code'])[3]/XCUIElementTypeOther[2]/XCUIElementTypeOther/XCUIElementTypeOther", "Pincode TextBox");
	}

	public  WebElement pinCodeRe_TextBox(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "(//XCUIElementTypeOther[@name='Re-enter pass code'])[3]/XCUIElementTypeOther[2]/XCUIElementTypeOther/XCUIElementTypeOther", "Pincode TextBox");
	}

	public  WebElement pinCodeReNew_TextBox(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, "(//XCUIElementTypeOther[@name='Re-enter new pass code'])[3]/XCUIElementTypeOther[2]/XCUIElementTypeOther/XCUIElementTypeOther", "Pincode TextBox");
	}

	public  WebElement pinCode_TextBox(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, 	"(//XCUIElementTypeOther[@name='pin-code-digit-0-touchable pin-code-digit-1-touchable pin-code-digit-2-touchable pin-code-digit-3-touchable pin-code-digit-4-touchable pin-code-digit-5-touchable pin-code-input-box'])[1]", "Pincode TextBox");
	}

	public  WebElement pinCodeVerify_TextBox(AppiumDriver driver) throws Exception {

		return AppiumUtils.findElement(driver, 	"(//XCUIElementTypeOther[@name='Enter your pass code'])[3]/XCUIElementTypeOther[2]/XCUIElementTypeOther/XCUIElementTypeOther", "Pincode TextBox");
	}

	public WebElement pinCodeTitle(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, 	"Enter passcode", "Enter Pincode Title");
	}


}
