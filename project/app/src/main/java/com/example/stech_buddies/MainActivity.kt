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

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)
        val auth = FirebaseAuth.getInstance()

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d(TAG, "FCM Registration Token: $token")
        }

        binding.button.setOnClickListener {
            val message = binding.msgToSend.text.toString()
            sendMessageToFCM(message)
        }

        val filter = IntentFilter("com.example.stech_buddies.FCM_MESSAGE")
        registerReceiver(messageReceiver, filter, RECEIVER_NOT_EXPORTED)
    }

    private fun sendMessageToFCM(message: String) {
        val url = "https://fcm.googleapis.com/v1/projects/1:270849123411:android:15824a8a640c5ade89d2a6/messages:send"
        val requestBody = JSONObject()
        val messageBody = JSONObject()
        messageBody.put("token", "BKTi9AatzkLnXuH6iwKTdoQ5lxB9nW-oB6ooAxvTRyWiNo9Nb_Gwuq1POsprPfvmyWToox39mzIN4ct9N-M-Z2I")
        messageBody.put("data", JSONObject().put("message", message))
        requestBody.put("message", messageBody)

        val accessToken = getAccessToken()

        val request = object : JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            { response ->
                Log.d(TAG, "Message sent successfully: $response")
            },
            { error ->
                Log.e(TAG, "Error sending message: ${error.message}")
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $accessToken"
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun getAccessToken(): String {
        val credentials = GoogleCredentials.fromStream(FileInputStream("app/google-services.json"))
            .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
        credentials.refreshIfExpired()
        return credentials.accessToken.tokenValue
    }

    private val messageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val message = intent?.getStringExtra("message")
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