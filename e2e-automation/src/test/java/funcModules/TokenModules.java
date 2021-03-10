package test.java.funcModules;

import io.appium.java_client.AppiumDriver;
import test.java.appModules.AppPageInjector;
import test.java.utility.IntSetup;


/**
 * The TokenModules class is to implement method related to token
 *
 */
public class TokenModules extends AppPageInjector {

	/**
	 * Navigate to Token screen
	 * @param  driver - appium driver available for session
	 * @return void
	 */
	public void navigateTokenScreen(AppiumDriver driver) throws Exception {
//		homePage.sovrinToken_Button(driver).click();
		receiveTokenPage.receive_Tab(driver);
	}

	/**
	 * Copy address from token screen to clipboard
	 * @param  driver - appium driver available for session
	 * @return void
	 */
	public void copyToClipboard(AppiumDriver driver) throws Exception {
		receiveTokenPage.copyAddress_Button(driver).click();
		IntSetup.tokenAddress=receiveTokenPage.tokenAddress_Text(driver).getAttribute("text");
		System.out.println("Token address "+IntSetup.tokenAddress);

	}

	/**
	 * validate address @ Sender token screen
	 * @param  driver - appium driver available for session
	 * @return void
	 */
	public void validateSenderAddress(AppiumDriver driver) throws Exception {
		sendTokenPage.send_Tab(driver).click();
		sendTokenPage.selectRecipient_Button(driver).click();
		sendTokenPage.tokenToSend_TextBox(driver).sendKeys(IntSetup.tokenAddress+"Invalid");
		sendTokenPage.invalidPaymentAddressText(driver);

	}

	/**
	 * send valid amount of token to valid address
	 * @param  driver - appium driver available for session
	 * @return void
	 */
	public void sendTokenValidAddress(AppiumDriver driver) throws Exception {
		sendTokenPage.send_Tab(driver).click();
		sendTokenPage.one_Button(driver).click();
		for (int i = 0; i < 5; i++) {
			sendTokenPage.zero_Button(driver).click();
		}
		sendTokenPage.amountSend_Text(driver);//verify amount in screen after entering more amount than balance
		sendTokenPage.selectRecipient_Button(driver).click();
		sendTokenPage.tokenToSend_TextBox(driver).sendKeys(IntSetup.tokenAddress);
		sendTokenPage.tokenForSend_TextBox(driver).sendKeys("Gift");
		sendTokenPage.send_Button(driver).click();
		sendTokenPage.amountSend_Text(driver);//verify amount in history
	}

}
