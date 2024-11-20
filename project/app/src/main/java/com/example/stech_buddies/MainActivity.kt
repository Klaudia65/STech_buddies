package com.example.stech_buddies
import com.example.stech_buddies.databinding.ActivityMainBinding
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // init and get an instance of FirebaseAuth
        FirebaseApp.initializeApp(this)
        val auth = FirebaseAuth.getInstance()

        // to verify if dev mode is active
        if (BuildConfig.DEBUG) {
            auth.useEmulator("127.0.0.1", 9099)  // 127.0.0.1 is the adress localhost in Android emulator
        }

/*
        val email = "test@example.com"
        val password = "password123"
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseAuth", "Utilisateur créé avec succès")
                } else {
                    // !!!!Error
                    Log.e("FirebaseAuth", "Error : ${task.exception?.message}")
                }
            }*/


        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            val msg = getString(R.string.textMessage, token)
            Log.d(TAG, msg)
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })

    }
}