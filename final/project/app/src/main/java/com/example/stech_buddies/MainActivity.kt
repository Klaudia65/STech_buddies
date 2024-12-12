package com.example.stech_buddies

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            // User already logged in
            startActivity(Intent(this, HomePageActivity::class.java))
            finish()
        }

        // LOGIN Button
        val goToSignIn: Button = findViewById(R.id.goToSignIn)
        goToSignIn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        // SIGN UP Button
        val goToSignUp: Button = findViewById(R.id.goToSignUp)
        goToSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}
