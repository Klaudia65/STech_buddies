package com.example.stech_buddies

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // init and get an instance of FirebaseAuth
        FirebaseApp.initializeApp(this)
        val auth = FirebaseAuth.getInstance()

        // to verify if dev mode is active
        if (BuildConfig.DEBUG) {
            auth.useEmulator("127.0.0.1", 9099)  // 127.0.0.1 is the adress localhost in Android emulator
        }


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
            }

    }
}