package test.java.pageObjects;

import io.appium.java_client.AppiumDriver;
import test.java.appModules.AppiumUtils;

import org.openqa.selenium.WebElement;

/**
 * The ChooseLockPage Interface is to hold webelement for ChooseLock Page
 * 
 */
public interface ChooseLockPage {
	
	public  WebElement pinCodeLock_Button(AppiumDriver driver) throws Exception ;
	public  WebElement or_Text(AppiumDriver driver) throws Exception ;
	public  WebElement stagingNetSwitcher(AppiumDriver driver) throws Exception ;
	public  WebElement eula_Accept(AppiumDriver driver) throws Exception ;
	public  WebElement ok_button(AppiumDriver driver) throws Exception ;
	public  WebElement cancel_button(AppiumDriver driver) throws Exception ;
	public  WebElement allow_button(AppiumDriver driver) throws Exception ;

}
