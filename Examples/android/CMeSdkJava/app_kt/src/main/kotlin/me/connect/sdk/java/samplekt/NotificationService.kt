package me.connect.sdk.java.samplekt

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import me.connect.sdk.java.Logger

public class NotificationService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token);
        Logger.getInstance().d("Received new token: $token")
        saveToken(token)
    }

    fun saveToken(token: String) {
        val prefs = application.applicationContext.getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
        prefs.edit()
                .putString(Constants.FCM_TOKEN, token)
                .apply()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Logger.getInstance().d("Received new message: $message")
    }
}