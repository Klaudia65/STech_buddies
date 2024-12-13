package com.example.stech_buddies

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.stech_buddies.databinding.ActivityUserInfoBinding

class UserInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Fetch user data passed via Intent
        val fullName = intent.getStringExtra("fullName")
        val major = intent.getStringExtra("major")
        val username = intent.getStringExtra("username")
        val email = intent.getStringExtra("email")

        // Display the retrieved user information in a TextView
        binding.userInfoTextView.text = "Full Name: $fullName\nMajor: $major\nUsername: $username\nEmail: $email"
    }
}
