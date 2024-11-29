package com.example.stech_buddies

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.stech_buddies.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize the spinner with the list of majors
        val majors = arrayOf(
            "Computer Engineering", "Electrical Engineering", "Mechanical Engineering",
            "Mathematics", "Physics", "Media", "Design"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, majors)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.majorSpinner.adapter = adapter

        // Register button click listener
        binding.registerButton.setOnClickListener {
            Log.d("SignUpActivity", "Register button clicked")
            val fullName = binding.fullNameInput.text.toString()
            val major = binding.majorSpinner.selectedItem.toString()
            val username = binding.usernameInput.text.toString()
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()
            val confirmPassword = binding.confirmPasswordInput.text.toString()

            if (password == confirmPassword) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val userId = user?.uid

                            val userMap = hashMapOf(
                                "fullName" to fullName,
                                "major" to major,
                                "username" to username,
                                "email" to email
                            )

                            if (userId != null) {
                                db.collection("users").document(userId)
                                    .set(userMap)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            this,
                                            "User registered successfully. Please sign in.",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        // Redirect to Sign In page after successful registration
                                        val intent = Intent(this, SignInActivity::class.java)
                                        startActivity(intent)
                                        finish() // Close SignUpActivity
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w("SignUpActivity", "Error adding document", e)
                                    }
                            }
                        } else {
                            Log.e("SignUpActivity", "Error: ${task.exception?.message}")
                            Toast.makeText(this, "Registration failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        }

        // Login link click listener
        binding.loginLink.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
    }
}
