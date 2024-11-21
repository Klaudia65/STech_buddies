package com.example.stech_buddies

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.data.isNotEmpty().let {
            val message = remoteMessage.data["message"]
            val intent = Intent("com.example.stech_buddies.FCM_MESSAGE")
            intent.putExtra("message", message)
            sendBroadcast(intent)
        }
    }
}