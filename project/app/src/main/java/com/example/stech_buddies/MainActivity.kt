package com.example.stech_buddies

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.stech_buddies.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject
import com.google.auth.oauth2.GoogleCredentials
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)
        val auth = FirebaseAuth.getInstance()


        // Get the registration token
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d(TAG, "FCM Registration Token: $token")
        }

        // when clicking on the button, send the message to the FCM
        binding.button.setOnClickListener {
            val message = binding.msgToSend.text.toString()
            sendMessageToFCM(message)
        }

        val filter = IntentFilter("com.example.stech_buddies.FCM_MESSAGE") //Intent filter to listen to the broadcast
        registerReceiver(messageReceiver, filter, RECEIVER_NOT_EXPORTED) //Register the broadcast receiver for FCM messages
    }

private fun getAccessToken(): String {
    return try {
        val stream = resources.openRawResource(R.raw.service_account) // opening the service account file without the need of the file path
        val credentials = GoogleCredentials.fromStream(stream) // getting the credentials from the service account file
            .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging")) // saying that we want to use the Firebase Messaging API
        credentials.refreshIfExpired()
        credentials.accessToken.tokenValue
    } catch (e: IOException) {
        Log.e(TAG, "Error with getting the token access", e)
        ""
    }
}

    private fun sendMessageToFCM(message: String) {
        val url = "https://fcm.googleapis.com/v1/projects/stech-buddies/messages:send" // URL to send the message, accessing the Firebase Messaging API for the project
        // The request with the message to send with the token of the device (to change with the token of the device you want to send the message to)
        val requestBody = JSONObject().apply {
            put("message", JSONObject().apply {
                put("token", "eUpfGHlrSTCZSRMmddVZN5:APA91bEGECJ52nLp5Ny4TVUreykTCdTSwJgsuKQqSfgMqdHGrnPbzaJ3etkd4uab40s571kYXDXk7UmOkfNQRVP3fdysJpA_FwvUGhkBy4uGUs3cEPG-ZXw") // Remplace par le jeton FCM de l'appareil cible
                put("notification", JSONObject().apply {
                    put("title", "STech Buddies")
                    put("body", message)
                })
                put("data", JSONObject().apply {
                    put("message", message)
                })
            })
        }
        // Post request
        val request = object : JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            { response ->
                Log.d(TAG, "Message sent successfully: $response")
            },
            { error ->
                Log.e(TAG, "Error sending message: ${error.message}")
            }
        ) {
            override fun getHeaders(): Map<String, String> { // Header are supp information to send metadata to the server
                val headers = HashMap<String, String>() // Need these headers to send the message withe FCM V1 API
                headers["Authorization"] = "Bearer ${getAccessToken()}" // Bearer means we use access token OAuth 2.0
                headers["Content-Type"] = "application/json"
                return headers
            }
        }
        Volley.newRequestQueue(this).add(request)
    }

    private val messageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val message = intent?.getStringExtra("message")
            Log.d("Message","message Received $message")
            binding.msgReceived.text = message
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(messageReceiver)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}