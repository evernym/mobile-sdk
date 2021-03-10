package test.java.utility;

import io.appium.java_client.AppiumDriver;
import test.java.utility.Context;

public class IntSetup {
	public static AppiumDriver driverApp;
	public static AppiumDriver driverBrowser;

	public Context ctx = Context.getInstance();
	public static String tokenAddress;
}
