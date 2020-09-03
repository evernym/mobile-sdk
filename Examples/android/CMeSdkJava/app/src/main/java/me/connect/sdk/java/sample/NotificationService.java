package me.connect.sdk.java.sample;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import me.connect.sdk.java.Logger;

public class NotificationService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Logger.getInstance().d(String.format("Received new token: %s", token));

        saveToken(token);
    }

    private void saveToken(@NonNull String token) {
        SharedPreferences prefs = getApplication().getApplicationContext().getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .putString(Constants.FCM_TOKEN, token)
                .apply();
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Logger.getInstance().d(String.format("Received new message: %s", remoteMessage));
    }
}
