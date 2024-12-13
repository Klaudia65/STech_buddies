package com.example.stech_buddies

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAIAPIService {

    // Endpoint for requesting chat completions from the OpenAI API
    @POST("chat/completions")
    fun getChatCompletion(
        @Header("Authorization") authHeader: String, // Bearer token for API authentication
        @Body requestBody: GPTRequestBody // Request payload with model and messages
    ): Call<GPTResponse> // Returns a Retrofit Call object for asynchronous handling
}
