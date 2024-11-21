package com.example.stech_buddies

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            // Update the UI with the received message
            val intent = Intent("com.example.stech_buddies.FCM_MESSAGE")
            intent.putExtra("message", it.body)
            sendBroadcast(intent)
        }
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}