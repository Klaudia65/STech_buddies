package com.example.stech_buddies

import com.example.stech_buddies.models.WeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

// Defines the interface for accessing the weather API
public interface WeatherService {

    // Makes a GET request to fetch weather data for a specific city
    @GET("weather")
    fun getWeather(
        @Query("q") city: String, // Name of the city to fetch weather for
        @Query("appid") apiKey: String, // API key for authentication
        @Query("units") units: String = "metric" // Temperature units (default is Celsius)
    ): Call<WeatherResponse> // Returns a Retrofit call with the weather response
}
