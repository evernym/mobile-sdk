package test.java.pageObjects;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;

public interface HomePage {
    public WebElement homeHeader(AppiumDriver driver) throws Exception;
    public WebElement burgerMenuButton(AppiumDriver driver) throws Exception;
    public WebElement scanButton(AppiumDriver driver) throws Exception;
    public WebElement newMessage(AppiumDriver driver) throws Exception;
    public WebElement recentEventsSection(AppiumDriver driver) throws Exception;
    public WebElement makingConnectionEvent(AppiumDriver driver) throws Exception;
    public WebElement connectedEvent(AppiumDriver driver, String name) throws Exception;
    public WebElement issuingCredentialEvent(AppiumDriver driver, String name) throws Exception;
    public WebElement credentialIssuedEvent(AppiumDriver driver, String name) throws Exception;
    public WebElement credentialRejectedEvent(AppiumDriver driver, String name) throws Exception;
    public WebElement sendingProofEvent(AppiumDriver driver) throws Exception;
    public WebElement proofSharedEvent(AppiumDriver driver, String name) throws Exception;
    public WebElement proofRequestRejectedEvent(AppiumDriver driver, String name) throws Exception;
    public WebElement questionRespondedEvent(AppiumDriver driver, String answer) throws Exception;
    public WebElement credentialOfferNotification(AppiumDriver driver) throws Exception;
}
