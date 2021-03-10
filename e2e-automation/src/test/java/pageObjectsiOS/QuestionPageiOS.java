package test.java.pageObjectsiOS;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import test.java.appModules.AppiumUtils;
import test.java.pageObjects.QuestionPage;

/**
 * The QuestionPageiOS class is to hold webelement for Question Page for iOS
 */
public class QuestionPageiOS implements QuestionPage {

	@Override
	public WebElement header(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, "Question", "Question Header");
	}
  @Override
  public WebElement senderLogo(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, "question-sender-logo", "Sender Logo");
  }

  @Override
  public WebElement senderDefaultLogo(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, "default-logo", "Sender Default Logo");
  }

  @Override
  public WebElement senderName(AppiumDriver driver, String name) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, name, "Sender Name");
  }

  @Override
  public WebElement title(AppiumDriver driver, String question) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, "question-title", "Question Text");
  }

  @Override
  public WebElement description(AppiumDriver driver, String description) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, description, "Question Description");
  }

  @Override
  public WebElement answer_Button(AppiumDriver driver, String answer) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, answer, "Answer Button");
  }

  @Override
  public WebElement answer_Option(AppiumDriver driver, String option) throws Exception {
    return AppiumUtils.findElement(driver, "(//XCUIElementTypeOther[@name=\"" + option + "\"])[3]", "Answer Option");
  }

  @Override
  public WebElement submit_Button(AppiumDriver driver) throws Exception {
    return AppiumUtils.findElementsByAccessibilityId(driver, "question-action-submit", "Submit Button");
  }
}
