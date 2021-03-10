package test.java.Tests;

import appModules.AppInjector;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import test.java.appModules.AppUtils;
import test.java.appModules.VASApi;
import test.java.pageObjects.QuestionPage;
import test.java.utility.IntSetup;
import test.java.utility.LocalContext;
import test.java.pageObjects.MyConnectionsPage;
import test.java.pageObjects.ConnectionHistoryPage;
import test.java.pageObjects.HomePage;
import test.java.pageObjects.MenuPage;
import test.java.utility.Helpers;
import test.java.utility.AppDriver;

import java.util.Arrays;
import java.util.List;

public class QuestionTest extends IntSetup {

	Injector injector = Guice.createInjector(new AppInjector());

	private AppUtils objAppUtlis = injector.getInstance(AppUtils.class);
	private HomePage homePage = injector.getInstance(HomePage.class);
	private MenuPage menuPage = injector.getInstance(MenuPage.class);
	private QuestionPage questionPage = injector.getInstance(QuestionPage.class);
	private MyConnectionsPage myConnectionsPage = injector.getInstance(MyConnectionsPage.class);
	private ConnectionHistoryPage connectionHistoryPage = injector.getInstance(ConnectionHistoryPage.class);

	private LocalContext context = LocalContext.getInstance();

	private VASApi VAS;
	private String DID;
	private String connectionName;
	private String text = "How much?";
	private String detail = "How much do you want";
	private List<String> oneOption = Arrays.asList(Helpers.randomString());
	private List<String> twoOptions = Arrays.asList(Helpers.randomString(), Helpers.randomString());
	private List<String> threeOptions = Arrays.asList(Helpers.randomString(), Helpers.randomString(), Helpers.randomString());

	@BeforeClass
	public void BeforeClassSetup() throws Exception {
		DID = context.getValue("DID");
		connectionName = context.getValue("connectionName");

		driverApp = AppDriver.getDriver();
		objAppUtlis.openApp(driverApp);

		VAS = VASApi.getInstance();
	}

	private void answerQuestionFromHome(List<String> validResponses) throws Exception {
    AppUtils.DoSomethingEventually(
        () -> VAS.askQuestion(DID, text, detail, validResponses),
//      () -> questionPage.header(driverApp).isDisplayed()
		() -> AppUtils.waitForElement(driverApp, () -> questionPage.header(driverApp)).isDisplayed()
    );
		validateQuestionWindow(validResponses);
	}

	private void answerQuestionFromConnectionHistory(List<String> validResponses) throws Exception {
		VAS.askQuestion(DID, text, detail, validResponses);

    	connectionHistoryPage.questionReceivedRecord(driverApp, text).isDisplayed();
		connectionHistoryPage.questionReceivedRecordDescription(driverApp, detail).isDisplayed();
		connectionHistoryPage.viewReceivedQuestionButton(driverApp).click();

		validateQuestionWindow(validResponses);
	}


	private void validateQuestionWindow(List<String> validResponses) throws Exception {
		questionPage.senderLogo(driverApp).isDisplayed();
		questionPage.senderName(driverApp, connectionName).isDisplayed();
		questionPage.title(driverApp, text).isDisplayed();
		questionPage.description(driverApp, detail).isDisplayed();

		for (String validResponse : validResponses) {
			questionPage.answer_Button(driverApp, validResponse).isDisplayed();
		}
	}

	@Test
	public void answerQuestionWithOneOptionFromHome() throws Exception {
		answerQuestionFromHome(oneOption);

		String answer = oneOption.get(0);
		questionPage.answer_Button(driverApp, answer).click();
		homePage.questionRespondedEvent(driverApp, answer).isDisplayed();
	}

	@Test(dependsOnMethods = "answerQuestionWithOneOptionFromHome")
	public void answerQuestionWithTwoOptionsFromHome() throws Exception {
		answerQuestionFromHome(twoOptions);

		String answer = twoOptions.get(0);
		questionPage.answer_Button(driverApp, answer).click();
		homePage.questionRespondedEvent(driverApp, answer).isDisplayed();
	}

	@Test(dependsOnMethods = "answerQuestionWithTwoOptionsFromHome")
	public void answerQuestionWithThreeOptionsFromHome() throws Exception {
		answerQuestionFromHome(threeOptions);

		String answer = threeOptions.get(0);
		questionPage.answer_Option(driverApp, answer).click();
		questionPage.submit_Button(driverApp).click();
		homePage.questionRespondedEvent(driverApp, answer).isDisplayed();
	}

	@Test(dependsOnMethods = "answerQuestionWithThreeOptionsFromHome")
	public void validateConnectionHistory() throws Exception {
		homePage.burgerMenuButton(driverApp).click();
		menuPage.myConnectionsButton(driverApp).click();
		myConnectionsPage.testConnection(driverApp, connectionName).click();
//		// TODO: move this logic to helper
//		try {
			connectionHistoryPage.questionAnswerRecord(driverApp).isDisplayed();
//		} catch (Exception ex) {
//			AppUtils.pullScreenUp(driverApp);
//			connectionHistoryPage.questionAnswerRecord(driverApp).isDisplayed();
//		}

		connectionHistoryPage.questionAnswerRecordDescription(driverApp, oneOption.get(0)).isDisplayed();
		connectionHistoryPage.questionAnswerRecordDescription(driverApp, twoOptions.get(0)).isDisplayed();
		connectionHistoryPage.questionAnswerRecordDescription(driverApp, threeOptions.get(0)).isDisplayed();
	}

	/*
	* NOTE: These tests don't work for iOS simulator because of lack of push notifications
	* */
	/*
	@Test(dependsOnMethods = "validateConnectionHistory")
	public void answerQuestionWithOneOptionFromConnectionHistory() throws Exception {
	  List<String> oneOption = Arrays.asList(Helpers.randomString());
		answerQuestionFromConnectionHistory(oneOption);

		String answer = oneOption.get(0);
		questionPage.answer_Button(driverApp, answer).click();

		connectionHistoryPage.questionAnswerRecordDescription(driverApp, answer).isDisplayed();
	}

	@Test(dependsOnMethods = "answerQuestionWithOneOptionFromConnectionHistory")
	public void answerQuestionWithTwoOptionsFromConnectionHistory() throws Exception {
	  List<String> twoOptions = Arrays.asList(Helpers.randomString(), Helpers.randomString());
		answerQuestionFromConnectionHistory(twoOptions);

		String answer = twoOptions.get(0);
		questionPage.answer_Button(driverApp, answer).click();

		connectionHistoryPage.questionAnswerRecordDescription(driverApp, answer).isDisplayed();
	}

	@Test(dependsOnMethods = "answerQuestionWithTwoOptionsFromConnectionHistory")
	public void answerQuestionWithThreeOptionsFromConnectionHistory() throws Exception {
	  List<String> threeOptions = Arrays.asList(Helpers.randomString(), Helpers.randomString(), Helpers.randomString());
		answerQuestionFromConnectionHistory(threeOptions);

		String answer = threeOptions.get(0);
		questionPage.answer_Option(driverApp, answer).click();
		questionPage.submit_Button(driverApp).click();

		connectionHistoryPage.questionAnswerRecordDescription(driverApp, answer).isDisplayed();
	}
	*/

	@AfterClass
	public void AfterClass() {
		driverApp.closeApp();
	}
}
