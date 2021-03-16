package test.java.pageObjects;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import test.java.appModules.AppiumUtils;
import test.java.pageObjects.QuestionPage;

/**
 * The QuestionPageAndroid class is to hold webelement for Question Page for Android
 * for Android
 *
 */
public class QuestionPageAndroid implements QuestionPage {

	@Override
	public WebElement header(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(
				driver,
        "//*[@text=\"Question\"]",
        "Question Header"
		);
	}

	@Override
	public WebElement senderLogo(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(
				driver,
				"//android.widget.ImageView[@content-desc=\"question-sender-logo\"]",
				"Sender Logo"
		);
	}

	@Override
	public WebElement senderDefaultLogo(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(
				driver,
				"//android.widget.ImageView[@content-desc=\"default-logo\"]",
				"Sender Default Logo"
		);
	}

	@Override
	public WebElement senderName(AppiumDriver driver, String name) throws Exception {
		return AppiumUtils.findElement(
				driver,
				"//android.widget.TextView[@content-desc=\"" + name + "\"]",
				"Sender Name"
		);
	}

	@Override
	public WebElement title(AppiumDriver driver, String question) throws Exception {
		return AppiumUtils.findElement(
				driver,
				"//*[@text='" + question + "']",
				"Question Text"
		);
	}

	@Override
	public WebElement description(AppiumDriver driver, String description) throws Exception {
		return AppiumUtils.findElement(
				driver,
				"//*[@text='" + description + "']",
				"Question Description"
		);
	}

	@Override
	public WebElement answer_Button(AppiumDriver driver, String answer) throws Exception {
		return AppiumUtils.findElement(
				driver,
				"//*[@text='" + answer + "']",
				"Answer Button"
		);
	}

	@Override
	public WebElement answer_Option(AppiumDriver driver, String option) throws Exception {
		return AppiumUtils.findElement(
				driver,
				"//*[@text='" + option + "']",
				"Answer Option"
		);
	}

	@Override
	public WebElement submit_Button(AppiumDriver driver) throws Exception {
		return AppiumUtils.findElement(
				driver,
				"//android.widget.Button[@content-desc=\"question-action-submit\"]",
				"Submit Button"
		);
	}
}
