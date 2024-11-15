package com.example.stech_buddies

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // finding the spinner
        val majorSpinner: Spinner = findViewById(R.id.majorSpinner)
        val majors = arrayOf("Computer Engineering", "Electrical Engineering", "Mechanical Engineering", "Mathematics", "Physics", "Media", "Design")

        // the adapter serves as a link between the UI Component and the Data Source
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, majors)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // asociation of the adapter to the spinner
        majorSpinner.adapter = adapter
    }
}
