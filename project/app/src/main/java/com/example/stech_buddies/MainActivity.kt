package com.example.stech_buddies

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.stech_buddies.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject
import com.google.auth.oauth2.GoogleCredentials
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val messages = mutableListOf<Message>()
    private lateinit var adapter: MessageAdapter

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

        adapter = MessageAdapter(messages)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.button.setOnClickListener {
            val message = binding.msgToSend.text.toString()
            if (message.isNotEmpty()) {
                sendMessageToFCM(message)
                addMessageToUI(message)
                binding.msgToSend.text.clear()
            }
        }

        val filter = IntentFilter("com.example.stech_buddies.FCM_MESSAGE")
        registerReceiver(messageReceiver, filter, RECEIVER_NOT_EXPORTED)
    }

    private fun getAccessToken(): String {
        return try {
            val stream = resources.openRawResource(R.raw.service_account)
            val credentials = GoogleCredentials.fromStream(stream)
                .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
            credentials.refreshIfExpired()
            credentials.accessToken.tokenValue
        } catch (e: IOException) {
            Log.e(TAG, "Error with getting the token access", e)
            ""
        }
    }

    private fun sendMessageToFCM(message: String) {
        val url = "https://fcm.googleapis.com/v1/projects/stech-buddies/messages:send"
        val requestBody = JSONObject().apply {
            put("message", JSONObject().apply {
                put("token", "eUpfGHlrSTCZSRMmddVZN5:APA91bEGECJ52nLp5Ny4TVUreykTCdTSwJgsuKQqSfgMqdHGrnPbzaJ3etkd4uab40s571kYXDXk7UmOkfNQRVP3fdysJpA_FwvUGhkBy4uGUs3cEPG-ZXw")
                put("notification", JSONObject().apply {
                    put("title", "STech Buddies")
                    put("body", message)
                })
                put("data", JSONObject().apply {
                    put("message", message)
                })
            })
        }

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
                headers["Authorization"] = "Bearer ${getAccessToken()}"
                headers["Content-Type"] = "application/json"
                return headers
            }
        }
        Volley.newRequestQueue(this).add(request)
    }

    private fun addMessageToUI(message: String) {
        messages.add(Message(message))
        adapter.notifyItemInserted(messages.size - 1)
        binding.recyclerView.scrollToPosition(messages.size - 1)
    }

    private val messageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val message = intent?.getStringExtra("message")
            Log.d("Message","message Received $message")
            if (message != null) {
                addMessageToUI(message)
            }
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