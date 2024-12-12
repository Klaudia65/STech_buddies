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
            "Architectural Design", "Architectural Engineering", "Architecture",
            "Business Administration", "Chemical and Biomolecular Engineering",
            "Civil Engineering", "Computer Science and Engineering",
            "Electrical & Information Engineering", "Electronical and Information Engineering",
            "Electronic Engineering", "English Language & Literature",
            "Fine Arts", "Global Technology Management", "Graduate School",
            "Industrial & IS Engineering", "Industrial Design", "International College",
            "IT Convergence Software", "IT Management", "Liberal Arts",
            "Manufacturing Systems & Design Engineering", "Materials Science & Engineering",
            "Mathematics", "Mechanical & Automotive Engineering",
            "Mechanical Engineering", "Mechanical System Design Engineering",
            "Media", "Metal Arts & Design", "Physics", "Public Administration",
            "Smart ICT Convergence Engineering", "Sports Sciences", "Virtual Design"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, majors)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.majorSpinner.adapter = adapter

        // Sign Up button click listener
        binding.signUpButton.setOnClickListener {
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
                            val userId = auth.currentUser?.uid
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
                                        startActivity(Intent(this, SignInActivity::class.java))
                                        finish()
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
                Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
            }
        }

        // Cancel button click listener
        binding.cancelButton.setOnClickListener {
            finish()
        }
    }
}
