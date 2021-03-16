package test.java.appModules;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidKeyCode;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import test.java.utility.Config;
import test.java.utility.AppDriver;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.testng.SkipException;

/**
 * The AppUtlis class is to implement ConnectMe app utility methods
 */
public class AppUtils extends AppPageInjector {

  public static boolean Success = false;

  public void openApp(AppiumDriver driver) throws Exception {
      driver.launchApp();
      pincodePage.pinCodeTitle(driver).isDisplayed();
      enterPincode(driver);
  }

  public void unlockApp(AppiumDriver driver) throws Exception {
    pincodePage.pinCodeTitle(driver).isDisplayed();
    enterPincode(driver);
  }

  /**
   * enters the pincode on pincode page
   *
   * @param driver - appium driver available for session
   * @return void
   */
  public void enterPincode(AppiumDriver driver) throws Exception {
    Thread.sleep(3000);  //  sync issue
    if (Config.iOS_Devices.contains(Config.Device_Type)) {
      pincodePage.pinCodeSe_TextBox(driver).sendKeys("000000");
    } else {
      AndroidDriver androidDriver = (AndroidDriver) driver;
      for (int i = 0; i < 6; i++) {
        androidDriver.pressKeyCode(AndroidKeyCode.KEYCODE_0);
      }
    }
  }

  /**
   * enters the reverse pincode  on pincode page
   *
   * @param driver - appium driver available for session
   * @return void
   */
  public void enterPincodeReverse(AppiumDriver driver) throws Exception {
    pincodePage.pinCode_TextBox(driver).sendKeys("654321");
  }

  /**
   * To Check previous test case passed or not
   */
  public void checkSkip() {
    if (!AppUtils.Success) {
      throw new SkipException("Skipping this exception");  //  if previous test case ,skipping current test case
    }
    AppUtils.Success = false;
  }

  public interface Func {
    void call() throws Exception;
  }

  public static void DoSomethingEventually(Func... fns) {
      for (Func fn: fns) {
        try {
          fn.call();
        } catch (Exception e) {
          System.out.println(e.getMessage());
          try {
            fn.call();
          } catch (Exception ex) {
            System.out.println(ex.getMessage());
          }
        }
      }
  }

  public static void isNotDisplayed(Func fn) throws Exception {
    boolean isDisplayed = false;

    try {
      fn.call();
      isDisplayed = true;
    } catch (Exception ex) {
      System.out.println("Entity was deleted!");
    } finally {
      if (isDisplayed)
        throw new Exception("Entity was NOT deleted!");
    }
  }

  public interface ElementChecker {
    WebElement check() throws Exception;
  }

  public static WebElement waitForElement(AppiumDriver driver, ElementChecker checker) {
    System.out.println("Wait for element to be available");
    driver.manage().timeouts().implicitlyWait(AppDriver.SMALL_TIMEOUT, TimeUnit.SECONDS);
    for (int i = 0; i < 5; i++) {
      try {
        return checker.check();
      } catch (Exception e) {
        System.out.println(e.getMessage() + " Retry...");
        AppUtils.pullScreenDown(driver);
      } finally {
        driver.manage().timeouts().implicitlyWait(AppDriver.LARGE_TIMEOUT, TimeUnit.SECONDS);
      }
    }
    throw new ElementNotFoundException("Expected element not found", "", "");
  }

  public static void pullScreenDown(AppiumDriver driver) {
    System.out.println("Pull screen down to refresh");
    Dimension dims = driver.manage().window().getSize();
    try {
      new TouchAction(driver)
        .press(dims.width / 2 - 50, dims.height / 2)
        .waitAction(Duration.ofMillis(100))
        .moveTo(dims.width / 2 - 50, dims.height - 20)
        .release().perform();
    } catch (Exception e) {
      System.err.println("Pull screen down to refresh FAILED with Error:\n" + e.getMessage());
    }
  }

  public static void pullScreenUp(AppiumDriver driver) {
    System.out.println("Pull screen up");
    Dimension dims = driver.manage().window().getSize();
    try {
      new TouchAction(driver)
              .press(dims.width / 2 - 50, dims.height / 2)
              .waitAction(Duration.ofMillis(200))
              .moveTo(dims.width / 3, 200)
              .release().perform();
    } catch (Exception e) {
      System.err.println("Pull screen up FAILED with Error:\n" + e.getMessage());
    }
  }

//  public static void swipe(AppiumDriver driver, String direction) {
//    Dimension dims = driver.manage().window().getSize();
//    int[] centerPoint = {dims.width / 2 - 50, dims.height / 2};
//    int[] topPoint = {dims.width / 2 - 50, dims.height / 10};
//    int[] bottomPoint = {dims.width / 2 - 50, dims.height - 20};
//    int[] rightPoint = {dims.width - 20, dims.height / 2};
//    int[] leftPoint = {dims.width / 10, dims.height / 2};
//  }

  public void acceptCredential(AppiumDriver driver) throws Exception {
    credentialPage.accept_Button(driver).click();
    authForAction(driver);
  }

  public void rejectCredential(AppiumDriver driver) throws Exception {
    credentialPage.reject_Button(driver).click();
    authForAction(driver);
  }

  public void shareProof(AppiumDriver driver) throws Exception {
    proofRequestPage.shareButton(driver).click();
    authForAction(driver);
  }

  public void rejectProof(AppiumDriver driver) throws Exception {
    proofRequestPage.rejectButton(driver).click();
    authForAction(driver);
  }

  public void authForAction(AppiumDriver driver) throws Exception {
    try {
      driver.manage().timeouts().implicitlyWait(AppDriver.SUPER_SMALL_TIMEOUT, TimeUnit.SECONDS);
      passcodePage.passcodeTitle(driver).isDisplayed();
      enterPincode(driver);
    }
    catch (NoSuchElementException e) {
      System.out.println("No authentication is required");
    }
    finally {
      driver.manage().timeouts().implicitlyWait(AppDriver.LARGE_TIMEOUT, TimeUnit.SECONDS);
    }
  }
}
