package com.example.stech_buddies

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAIAPIService {
    @POST("chat/completions")
    fun getChatCompletion(
        @Header("Authorization") authHeader: String,
        @Body requestBody: GPTRequestBody
    ): Call<GPTResponse>
}
