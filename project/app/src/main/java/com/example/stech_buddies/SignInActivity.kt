package com.example.stech_buddies

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.stech_buddies.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.registerLink.setOnClickListener {
            // Create an Intent to start SignUpActivity
            Log.d("main_activity", "Go to Sign Un")
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)

        }

        // Sign in button click listener
        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val userId = user?.uid

                        if (userId != null) {
                            db.collection("users").document(userId).get()
                                .addOnSuccessListener { document ->
                                    if (document != null) {
                                        val fullName = document.getString("fullName")
                                        val major = document.getString("major")
                                        val username = document.getString("username")
                                        val email = document.getString("email")

                                        // Pass user information to UserInfoActivity
                                        val intent = Intent(this, UserInfoActivity::class.java).apply {
                                            putExtra("fullName", fullName)
                                            putExtra("major", major)
                                            putExtra("username", username)
                                            putExtra("email", email)
                                        }
                                        startActivity(intent)
                                    } else {
                                        Log.d("SignInActivity", "No such document")
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Log.d("SignInActivity", "get failed with ", exception)
                                }
                        }
                    } else {
                        Log.e("SignInActivity", "Error: ${task.exception?.message}")
                        Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}