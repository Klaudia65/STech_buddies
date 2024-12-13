package com.example.stech_buddies.models

// Represents the main response from the weather API
data class WeatherResponse(
    val main: Main, // Contains temperature and humidity information
    val weather: List<Weather>, // List of weather conditions (e.g., clear, cloudy)
    val name: String // Name of the city
)

// Holds temperature and humidity details
data class Main(
    val temp: Float, // Current temperature
    val humidity: Int // Current humidity percentage
)

// Represents a specific weather condition
data class Weather(
    val description: String // Weather description (e.g., "clear sky")
)
