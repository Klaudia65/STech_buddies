package com.example.stech_buddies

import com.google.firebase.Timestamp

// Data model representing a chat message
data class Message(
    val sender: String = "",
    val type: String = "text",
    val content: String = "",
    val timestamp: Timestamp? = null
)
