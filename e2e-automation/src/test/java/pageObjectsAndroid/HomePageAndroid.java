package test.java.pageObjectsAndroid;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import test.java.appModules.AppiumUtils;

public class HomePageAndroid implements test.java.pageObjects.HomePage {

  public WebElement homeHeader(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElement(
      driver,
      "//*[@text=\"Home\"]",
      "Home Header"
    );
  }

  public WebElement burgerMenuButton(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElement(
      driver,
      "//android.view.ViewGroup[@content-desc=\"burger-menu\"]",
      "Burger Menu Button"
    );
  }

  public WebElement scanButton(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElement(
      driver,
      "//*[@text=\"Scan\"]",
      "Scan Button"
    );
  }

  public WebElement newMessage(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElement(
      driver,
      "//*[@text=\"NEW MESSAGE - TAP TO OPEN\"]",
      "New Message"
    );
  }

  public WebElement recentEventsSection(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElement(
      driver,
      "//*[@text=\"Recent\"]",
      "Recent Section"
    );
  }

  public WebElement makingConnectionEvent(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElement(
      driver,
      "//*[@text=\"Making secure connection...\"]",
      "Making Connection Event"
    );
  }

  public WebElement connectedEvent(AppiumDriver driver, String name) throws Exception {
    return AppiumUtils.findElement(
      driver,
      "//*[@text='You connected with \"" + name + "\".']",
      "Connected Event"
    );
  }

  public WebElement issuingCredentialEvent(AppiumDriver driver, String name) throws Exception {
    return AppiumUtils.findElement(
      driver,
      "//*[@text='\"" + name + "\" will be issued to you shortly.']",
      "Issuing Credential Event"
    );
  }

  public WebElement credentialIssuedEvent(AppiumDriver driver, String name) throws Exception {
    return AppiumUtils.findElement(
      driver,
      "//*[@text='You have been issued a \"" + name + "\".']",
      "Credential Issued Event"
    );
  }

  @Override
  public WebElement credentialRejectedEvent(AppiumDriver driver, String name) throws Exception {
    return AppiumUtils.findElement(
            driver,
            "//*[@text='You rejected \"" + name + "\".']",
            "Credential Rejected Event"
    );
  }

  public WebElement sendingProofEvent(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElement(
      driver,
      "//*[@text=\"Sending...\"]",
      "Sending Proof Event"
    );
  }

  public WebElement proofSharedEvent(AppiumDriver driver, String name) throws Exception {
    return AppiumUtils.findElement(
      driver,
      "//*[@text='You shared \"" + name + "\".']",
      "Proof Shared Event"
    );
  }

  @Override
  public WebElement proofRequestRejectedEvent(AppiumDriver driver, String name) throws Exception {
    return AppiumUtils.findElement(
            driver,
            "//*[@text='You rejected \"" + name + "\".']",
            "Proof Request Rejected Event"
    );
  }

  public WebElement questionRespondedEvent(AppiumDriver driver, String answer) throws Exception {
    return AppiumUtils.findElement(
      driver,
      "//*[@text='You responded with: " + answer + ".']",
      "Question Responded Event"
    );
  }

  public WebElement credentialOfferNotification(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElement(
            driver,
            "//*[@text=\"Remote connection sent you a Credential Offer\"]",
            "Credential Offer Notification"
    );
  }

}
