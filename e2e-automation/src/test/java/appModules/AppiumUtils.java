package test.java.appModules;

import java.util.HashMap;
import java.util.List;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidKeyCode;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.Reporter;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.TouchAction;

import static java.time.Duration.ofSeconds;


/**
 * The AppiumUtils class is to implement appium utility methods
 * All Appium action overrides are defined here
 * 
 */

public class AppiumUtils {
	private static WebElement element = null;
	private static List<WebElement> elements = null;

	/**
	 * clicks on a webelement n times
	 * @param n - how many time to be clicked
	 * @param element -webelemnt which need to be clicked
	 * @return void
	 */
	public static void nClick(int n, WebElement element) throws Exception {

		for (int i = 0; i < n; i++) {
			Thread.sleep(500);
			element.click();// click action on element
		}
	}

	/**
	 * wait for a element until its not visible
	 * @param driver - appium driver available for session
	 * @param element -webelement for which we need to wait
	 * @param seconds - how many seconds we want to wait webelemnt
	 * @return void
	 */
	public static void waitForScreenToLoad(AppiumDriver driver, WebElement element, int seconds) {

		WebDriverWait wait = new WebDriverWait(driver, seconds);
		//wait.until(ExpectedConditions.visibilityOf(element));// Visibilty condition check
	}

	/**
	 * finds element by xpath
	 * @param driver - appium driver available for session
	 * @param expression -path for webelement
	 * @param elementName -name of the element to be displayed at console
	 * @return webelement on which we need to peform action
	 */
	public static WebElement findElement (AppiumDriver driver, String expression, String elementName) throws Exception {
		try {
			element = driver.findElement(By.xpath(expression));
			System.out.println(elementName + " is displayed");
			return element;
		} catch (Exception e) {
			System.out.println(elementName + " is not displayed");
			Reporter.log(elementName + " is not displayed");
			throw (e);
		}

	}

	public static WebElement findElementsByAccessibilityId(AppiumDriver driver, String id, String elementName) throws Exception {
		try {
			element = driver.findElementByAccessibilityId(id);
			System.out.println(elementName + " is displayed");
			return element;
		} catch (Exception e) {
			System.out.println(elementName + " is not displayed");
			Reporter.log(elementName + " is not displayed");
			throw (e);
		}

	}

	private static By buildStringForSearchByText(String text) {
		return By.xpath("//*[@text='" + text + "']");
	}

	public static WebElement findTextElementInDifferentCases(AppiumDriver driver, String text, String elementName) throws Exception {
		// as is
		try {
			element = driver.findElement(buildStringForSearchByText(text));
			System.out.println(elementName + " is displayed");
			return element;
		} catch (Exception e) { }

		// upper case
		try {
			element = driver.findElement(buildStringForSearchByText(text.toUpperCase()));
			System.out.println(elementName + " is displayed");
			return element;
		} catch (Exception e) { }

		// lower case
		try {
			element = driver.findElement(buildStringForSearchByText(text.toLowerCase()));
			System.out.println(elementName + " is displayed");
			return element;
		} catch (Exception e) { }

		// capitalize first latter
		try {
			element = driver.findElement(buildStringForSearchByText(text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase()));
			System.out.println(elementName + " is displayed");
			return element;
		} catch (Exception e) {
			System.out.println(elementName + " is not displayed");
			Reporter.log(elementName + " is not displayed");
			throw (e);
		}
	}

	public static List<WebElement> findElements (AppiumDriver driver, String expression, String elementName) throws Exception {
		try {
			elements = driver.findElements(By.xpath(expression));
			System.out.println(elementName + " is displayed");
			return elements;
		} catch (Exception e) {
			System.out.println(elementName + " is not displayed");
			Reporter.log(elementName + " is not displayed");
			throw (e);
		}

	}

	/**
	 * check element is not present on screen
	 * @param driver - appium driver available for session
	 * @param expression -path for webelement
	 * @param elementName -name of the element to be displayed at console
	 * @return void
	 */

	public static void elementNotPresent(AppiumDriver driver, String expression, String elementName) {
		try {
			element = driver.findElement(By.xpath(expression));
		} catch (Exception e) {
			System.out.println(elementName + "is not displayed");
			Assert.assertTrue(true);
		}

	}

	/**
	 * long press on element
	 * @param driver  - appium driver available for session
	 * @param element -element on which action need to be peformed
	 * @return void
	 */

	public static void longPress(AppiumDriver driver, WebElement element) {
		TouchAction action = new TouchAction(driver);
		action.longPress(element);
		action.perform();

	}

	/**
	 * swipe on element
	 * @param driver -appium driver available for session
	 * @param element -element on which action need to be peformed
	 * @param direction-on which direction swipe need to be peformed
	 * @return void
	 */

	public static void swipe(AppiumDriver driver, WebElement element, String direction) throws Exception {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		HashMap<String, String> scrollObject = new HashMap<String, String>();
		scrollObject.put("direction", direction);
		scrollObject.put("element", ((RemoteWebElement) element).getId());
		js.executeScript("mobile:swipe", scrollObject);

	}

	/**
	 * retry finding the element till timeOut
	 * @param driver - appium driver available for session
	 * @param expression -path for webelement
	 * @param elementName -name of the element to be displayed at console
	 * @param timeOut-how many time user want to retry
	 * @return webelement on which we need to peform action
	 */
	public static WebElement findElement(AppiumDriver driver, String expression, String elementName, int timeOut)throws Exception {
		{

			for (int i = 0; i < timeOut; i++) {
				try {
					element = driver.findElement(By.xpath(expression));
					break;
				} catch (Exception e) {
					try {
						if (i == timeOut) {
							System.out.println(elementName + " is not displayed");
							Reporter.log(elementName + " is not displayed");
							throw (e);
						}
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
				}

			}
			return element;
		}

	}

	public static void scroll(AppiumDriver driver, int startx, int starty, int endx, int endy) {

		TouchAction touchAction = new TouchAction(driver);

		touchAction.longPress(startx, starty)
				.waitAction(1000)
				.moveTo(endx, endy)
				.release()
				.perform();

	}

	public static void swipeRight(AppiumDriver driver) {

		//The viewing size of the device
		Dimension size = driver.manage().window().getSize();

		//Starting x location set to 5% of the width (near left)
		int startx = (int) (size.width * 0.05);
		//Ending x location set to 95% of the width (near right)
		int endx = (int) (size.width * 0.95);
		//y position set to mid-screen vertically
		int starty = size.height / 2;

		scroll(driver, startx, starty, endx, starty);

	}

	public static void tapBack(AppiumDriver driver, int n) {
		AndroidDriver androidDriver = (AndroidDriver) driver;
		for (int i = 0; i < n; i++) {
			androidDriver.pressKeyCode(AndroidKeyCode.BACK);
		}
	}

}
