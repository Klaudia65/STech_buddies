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

        // Check if the user is already logged in
        if (auth.currentUser != null) {
            // Redirect to homepage if the user is authenticated
            startActivity(Intent(this, HomePageActivity::class.java))
            finish()
        }

        // Navigate to SignInActivity when "Login" button is clicked
        val goToSignIn: Button = findViewById(R.id.goToSignIn)
        goToSignIn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        // Navigate to SignUpActivity when "Sign Up" button is clicked
        val goToSignUp: Button = findViewById(R.id.goToSignUp)
        goToSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}
