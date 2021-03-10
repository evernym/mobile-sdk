package appModules;

import com.google.inject.AbstractModule;

import java.io.InputStream;
import java.util.Properties;

import test.java.pageObjects.*;
import test.java.pageObjectsAndroid.*;
import test.java.pageObjectsiOS.*;
import test.java.utility.Config;

/**
 * The AppInjector class is to implement configure for DI
 *
 */
public class AppInjector extends AbstractModule {


	protected void configure() {
		Properties prop = new Properties();
		InputStream input = null;
		if (Config.iOS_Devices.contains(Config.Device_Type)) {
			bind(InvitationPage.class).to(InvitationPageiOS.class);
			bind(PincodePage.class).to(PincodePageiOS.class);
			bind(ConnectionHistoryPage.class).to(ConnectionHistoryPageiOS.class);
			bind(AppCenterPage.class).to(AppCenterPageiOS.class);
			bind(HomePage.class).to(HomePageiOS.class);
			bind(QrScannerPage.class).to(QrScannerPageiOS.class);
			bind(MenuPage.class).to(MenuPageiOS.class);
			bind(MyConnectionsPage.class).to(MyConnectionsPageiOS.class);
			bind(SettingsPage.class).to(SettingsPageiOS.class);
			bind(BackupPage.class).to(BackupPageiOS.class);
			bind(BiometricsPage.class).to(BiometricsPageiOS.class);
			bind(PasscodePage.class).to(PasscodePageiOS.class);
			bind(ChatPage.class).to(ChatPageiOS.class);
			bind(AboutPage.class).to(AboutPageiOS.class);
			bind(OnfidoPage.class).to(OnfidoPageiOS.class);
			bind(RestorePage.class).to(RestorePageiOS.class);
			bind(CredentialPage.class).to(CredentialPageiOS.class);
			bind(ChooseLockPage.class).to(ChooseLockPageiOS.class);
			bind(SettingsPage.class).to(SettingsPageiOS.class);
			bind(ProofRequestPage.class).to(ProofRequestPageiOS.class);
			bind(SwitchEnviromentPage.class).to(SwitchEnviromentPageiOS.class);
			bind(ReceiveTokenPage.class).to(ReceiveTokenPageiOS.class);
			bind(SendTokenPage.class).to(SendTokenPageiOS.class);
			bind(ConnectionDetailPage.class).to(ConnectionDetailPageiOS.class);
			bind(BackupRestoreWalletPage.class).to(BackupRestoreWalletPageiOS.class);
			bind(StartUpPage.class).to(StartUpPageIOS.class);
			bind(MyCredentialsPage.class).to(MyCredentialsPageiOS.class);
			bind(PushNotificationsPage.class).to(PushNotificationsPageIOS.class);
			bind(QuestionPage.class).to(QuestionPageiOS.class);
			bind(CustomValuesPage.class).to(CustomValuesPageiOS.class);
		} else {
			bind(InvitationPage.class).to(InvitationPageAndroid.class);
			bind(PincodePage.class).to(PincodePageAndroid.class);
			bind(ConnectionHistoryPage.class).to(ConnectionHistoryPageAndroid.class);
			bind(AppCenterPage.class).to(AppCenterPageAndroid.class);
			bind(HomePage.class).to(HomePageAndroid.class);
			bind(HomePage.class).to(HomePageAndroid.class);
			bind(QrScannerPage.class).to(QrScannerPageAndroid.class);
			bind(MenuPage.class).to(MenuPageAndroid.class);
			bind(MyConnectionsPage.class).to(MyConnectionsPageAndroid.class);
			bind(SettingsPage.class).to(SettingsPageAndroid.class);
			bind(BackupPage.class).to(BackupPageAndroid.class);
			bind(BiometricsPage.class).to(BiometricsPageAndroid.class);
			bind(PasscodePage.class).to(PasscodePageAndroid.class);
			bind(ChatPage.class).to(ChatPageAndroid.class);
			bind(AboutPage.class).to(AboutPageAndroid.class);
			bind(OnfidoPage.class).to(OnfidoPageAndroid.class);
			bind(RestorePage.class).to(RestorePageAndroid.class);
			bind(CredentialPage.class).to(CredentialPageAndroid.class);
			bind(ChooseLockPage.class).to(ChooseLockPageAndroid.class);
			bind(StartUpPage.class).to(StartUpPageAndroid.class);
			bind(SettingsPage.class).to(SettingsPageAndroid.class);
			bind(ProofRequestPage.class).to(ProofRequestPageAndroid.class);
			bind(SwitchEnviromentPage.class).to(SwitchEnviromentPageAndroid.class);
			bind(ReceiveTokenPage.class).to(ReceiveTokenPageAndroid.class);
			bind(SendTokenPage.class).to(SendTokenPageAndroid.class);
			bind(ConnectionDetailPage.class).to(ConnectionDetailPageAndroid.class);
			bind(BackupRestoreWalletPage.class).to(BackupRestoreWalletPageAndroid.class);
			bind(MyCredentialsPage.class).to(MyCredentialsPageAndroid.class);
            bind(PushNotificationsPage.class).to(PushNotificationsPageAndroid.class);
            bind(QuestionPage.class).to(QuestionPageAndroid.class);
            bind(CustomValuesPage.class).to(CustomValuesPageAndroid.class);
		}

	}

}
