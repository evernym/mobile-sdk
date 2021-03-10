package test.java.Tests;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import appModules.AppInjector;
import test.java.funcModules.LockModules;
import test.java.utility.IntSetup;
import test.java.utility.AppDriver;
import io.appium.java_client.AppiumDriver;

public class StartUpTest extends IntSetup {
    Injector injector = Guice.createInjector(new AppInjector());

    private test.java.pageObjects.HomePage objHomePage = injector.getInstance(test.java.pageObjects.HomePage.class);
    private LockModules objLockModules = injector.getInstance(LockModules.class);


    @BeforeClass
    public void BeforeClassSetup() {
        driverApp = AppDriver.getDriver();
        driverApp.launchApp();
    }

    @Test
    public void setUpWizardTest() throws Exception {
        objLockModules.passStartUpWizard(driverApp);
    }

    @Test(dependsOnMethods = "setUpWizardTest")
    public void checkMenuElementsVisibility() throws Exception {
        objHomePage.homeHeader(driverApp).isDisplayed();
        objHomePage.burgerMenuButton(driverApp).isDisplayed();
        objHomePage.scanButton(driverApp).isDisplayed();
    }

    @AfterClass
    public void AfterClass() {
        driverApp.closeApp();
    }

}
