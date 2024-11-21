package com.example.stech_buddies

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.JsonObjectRequest
import com.example.stech_buddies.databinding.ActivityMainBinding
import com.google.firebase.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)
        val auth = FirebaseAuth.getInstance()

        if (BuildConfig.DEBUG) {
            auth.useEmulator("127.0.0.1", 9099)
        }

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
        val url = "https://<your-cloud-function-url>/sendMessage"
        val requestBody = JSONObject()
        requestBody.put("message", message)
        requestBody.put("token", "<your-fcm-token>")

        val request = JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            { response ->
                Log.d(TAG, "Message sent successfully: $response")
            },
            { error ->
                Log.e(TAG, "Error sending message: ${error.message}")
            }
        )

        Volley.newRequestQueue(this).add(request)
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