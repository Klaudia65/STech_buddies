package com.example.stech_buddies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import okhttp3.OkHttpClient
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class TutoringFragment : Fragment() {

    private lateinit var userInput: EditText
    private lateinit var sendButton: Button
    private lateinit var tutoringResponse: TextView

    // API key for accessing OpenAI's GPT
    private val apiKey = "Bearer API-Key-Here" // API key removed provided that's why it doesn't work

    // Retrofit service for making API calls
    private val apiService: OpenAIAPIService by lazy {
        val client = OkHttpClient.Builder().build()

        Retrofit.Builder()
            .baseUrl("https://api.openai.com/v1/") // OpenAI API base URL
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAIAPIService::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tutoring, container, false)

        // Find views in the layout
        userInput = view.findViewById(R.id.userInput)
        sendButton = view.findViewById(R.id.sendButton)
        tutoringResponse = view.findViewById(R.id.tutoringResponse)

        // Set up send button to process user input
        sendButton.setOnClickListener {
            val question = userInput.text.toString().trim()
            if (question.isNotEmpty()) {
                sendMessageToGPT(question)
            } else {
                Toast.makeText(requireContext(), "Please enter a question", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    // Sends a message to OpenAI's GPT API and handles the response
    private fun sendMessageToGPT(userMessage: String) {
        tutoringResponse.text = "Processing your request..."

        val requestBody = GPTRequestBody(
            model = "gpt-3.5-turbo", // Specify model to use
            messages = listOf(
                GPTMessage("system", "You are a helpful assistant."),
                GPTMessage("user", userMessage)
            ),
            max_tokens = 1000 // Limit response length
        )

        apiService.getChatCompletion(apiKey, requestBody).enqueue(object : Callback<GPTResponse> {
            override fun onResponse(call: Call<GPTResponse>, response: Response<GPTResponse>) {
                if (response.isSuccessful) {
                    // Extract assistant's response from the API
                    val assistantResponse = response.body()?.choices?.get(0)?.message?.content ?: "No response"
                    tutoringResponse.text = assistantResponse
                } else {
                    tutoringResponse.text = "Error: ${response.message()}"
                }
            }

            override fun onFailure(call: Call<GPTResponse>, t: Throwable) {
                // Display an error message if the API call fails
                tutoringResponse.text = "Error: ${t.message}"
            }
        })
    }
}
