package com.example.stech_buddies.fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.stech_buddies.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream

class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var fullnameText: TextView
    private lateinit var usernameText: TextView
    private lateinit var emailText: TextView
    private lateinit var majorText: TextView
    private lateinit var profileImage: ImageView
    private lateinit var changePhotoButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        fullnameText = view.findViewById(R.id.fullname_text)
        usernameText = view.findViewById(R.id.username_text)
        emailText = view.findViewById(R.id.email_text)
        majorText = view.findViewById(R.id.major_text)
        profileImage = view.findViewById(R.id.profile_image)
        changePhotoButton = view.findViewById(R.id.change_photo_button)

        loadUserData()
        loadProfilePhoto()

        changePhotoButton.setOnClickListener {
            checkPermissionsAndOpenGallery()
        }

        return view
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        fullnameText.text = "Full Name: ${document.getString("fullName")}"
                        usernameText.text = "Username: ${document.getString("username")}"
                        emailText.text = "Email: ${document.getString("email")}"
                        majorText.text = "Major: ${document.getString("major")}"
                    } else {
                        Toast.makeText(context, "User data not found.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to load user data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "User not authenticated.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadProfilePhoto() {
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val photoUriString = sharedPreferences.getString("profilePhotoUri", null)

        if (photoUriString != null) {
            val photoUri = Uri.parse(photoUriString)
            profileImage.setImageURI(photoUri)
        } else {
            profileImage.setImageResource(R.drawable.home) // Default image
        }
    }

    private fun savePhotoToInternalStorage(uri: Uri): Uri? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val file = File(requireContext().filesDir, "profile_photo.jpg") // Save as "profile_photo.jpg"
            val outputStream = FileOutputStream(file)

            inputStream?.copyTo(outputStream)

            inputStream?.close()
            outputStream.close()

            Uri.fromFile(file) // Return the URI of the saved file
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to save photo.", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun saveProfilePhoto(uri: Uri) {
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("profilePhotoUri", uri.toString()).apply()
    }

    private fun checkPermissionsAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val hasReadPermission = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    102
                )
                return
            }
        }
        openGallery()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 101)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
            val selectedImageUri = data?.data
            if (selectedImageUri != null) {
                val savedUri = savePhotoToInternalStorage(selectedImageUri)
                if (savedUri != null) {
                    profileImage.setImageURI(savedUri)
                    saveProfilePhoto(savedUri) // Save the permanent URI
                    Toast.makeText(context, "Profile photo updated.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 102) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                Toast.makeText(context, "Permission denied to access gallery", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
