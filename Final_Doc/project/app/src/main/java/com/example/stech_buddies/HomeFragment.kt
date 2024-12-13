package com.example.stech_buddies.fragments

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
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
import retrofit2.Retrofit
import java.io.File
import java.io.FileOutputStream
import com.example.stech_buddies.models.WeatherResponse
import com.example.stech_buddies.WeatherService
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan

class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var fullnameText: TextView
    private lateinit var usernameText: TextView
    private lateinit var emailText: TextView
    private lateinit var majorText: TextView
    private lateinit var profileImage: ImageView
    private lateinit var changePhotoButton: Button

    private lateinit var weatherText: TextView
    private lateinit var calendarView: MaterialCalendarView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Link UI components
        weatherText = view.findViewById(R.id.weather_text)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        fullnameText = view.findViewById(R.id.fullname_text)
        usernameText = view.findViewById(R.id.username_text)
        emailText = view.findViewById(R.id.email_text)
        majorText = view.findViewById(R.id.major_text)
        profileImage = view.findViewById(R.id.profile_image)
        changePhotoButton = view.findViewById(R.id.change_photo_button)
        calendarView= view.findViewById(R.id.calendarView)


        loadUserData()
        loadProfilePhoto()

        changePhotoButton.setOnClickListener {
            checkPermissionsAndOpenGallery()
        }

        loadWeatherData("Seoul")

        calendarView.setOnDateChangedListener { widget, date, selected ->
            Toast.makeText(context, "Selected Date : ${date.date}", Toast.LENGTH_SHORT).show()
            if (selected) {
                // Check if there is an event on the selected date
                val plannedEvents = false
                if (plannedEvents == false) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Events")
                        .setMessage("No events found for the selected date. Would you like to plan a meeting with your group?")
                        .setPositiveButton("Create Event") { dialog, _ ->
                            // Handle event creation
                            createEvent(calendarView,date, "Group Meeting")
                            dialog.dismiss()
                        }
                        .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                        .create()
                        .show()
                }
            }

        }

        return view
    }

    // Adds event to calendar with red dot
    fun createEvent(calendarView: MaterialCalendarView, date: CalendarDay, eventName: String) {
        val eventDecorator = object : DayViewDecorator {
            override fun shouldDecorate(day: CalendarDay?): Boolean {
                return day == date
            }

            override fun decorate(view: DayViewFacade?) {
                view?.addSpan(DotSpan(10f, Color.RED))
            }
        }

        calendarView.setOnDateChangedListener { widget, selectedDate, selected ->
            if (selectedDate == date) {
                Toast.makeText(
                    widget.context,
                    "Événement : $eventName",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        calendarView.addDecorator(eventDecorator)
    }


    // Fetches weather data using OpenWeather API and update weather text
    private fun loadWeatherData(city: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val weatherService = retrofit.create(WeatherService::class.java)

        val call = weatherService.getWeather(city, "025e9e717c099263fe00f447274b2fab")
        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(
                call: Call<WeatherResponse>,
                response: Response<WeatherResponse>
            ) {
                if (response.isSuccessful) {
                    val weather = response.body()
                    weatherText.text = "City: ${weather?.name}\n" +
                            "Temp: ${weather?.main?.temp}°C\n" +
                            "Description: ${weather?.weather?.get(0)?.description}"
                } else {
                    weatherText.text = "Failed to load weather data."
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                weatherText.text = "Error: ${t.message}"
            }
        })
    }

    // Loads user data (from Firestore) and displays them
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

    // Loads saved profile photo or sets default image
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

    // Save photo URI to internal storage
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

    // Check permissions before opening gallery
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

    // Open gallery to select profile picture
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 101)
    }

    // Handles result from gallery selection
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

    // Handles gallery permission results
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
