package com.example.stech_buddies

import com.google.firebase.Timestamp

data class Message(
    val sender: String = "",
    val type: String = "text",
    val content: String = "",
    val timestamp: Timestamp? = null
)
