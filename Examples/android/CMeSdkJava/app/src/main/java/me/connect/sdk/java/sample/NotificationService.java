package me.connect.sdk.java.sample;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.concurrent.Executors;

import me.connect.sdk.java.Messages;
import me.connect.sdk.java.message.Message;
import me.connect.sdk.java.sample.messages.MessageProcessor;

public class NotificationService extends FirebaseMessagingService {
    public static final String TAG = "Notifications";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, String.format("Received new token: %s", token));

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
        Log.d(TAG, String.format("Received new message: %s", remoteMessage));
        processMesssage(remoteMessage);
    }

    private void processMesssage(RemoteMessage remoteMessage) {
        final String messageId = remoteMessage.getData().get("messageId"); //Todo check actual data!
        final String connPwDid = remoteMessage.getData().get("pwDid"); //Todo check actual data!
        Executors.newSingleThreadExecutor().execute(() -> {
            Messages.downloadMessage(messageId).whenComplete((messages, err) -> {
                if (err != null) {
                    err.printStackTrace();
                } else {
                    MessageProcessor mp = new MessageProcessor(this, connPwDid);
                    for (Message m : messages) {
                        mp.process(m);
                    }
                }
            });
        });
    }
}
