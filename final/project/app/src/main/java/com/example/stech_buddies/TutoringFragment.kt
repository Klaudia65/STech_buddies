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

    private val apiKey = "Bearer API KEY" // Replace with your actual OpenAI API Key

    private val apiService: OpenAIAPIService by lazy {
        val client = OkHttpClient.Builder().build()

        Retrofit.Builder()
            .baseUrl("https://api.openai.com/v1/")
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

        // Initialize views
        userInput = view.findViewById(R.id.userInput)
        sendButton = view.findViewById(R.id.sendButton)
        tutoringResponse = view.findViewById(R.id.tutoringResponse)

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

    private fun sendMessageToGPT(userMessage: String) {
        tutoringResponse.text = "Processing your request..."

        val requestBody = GPTRequestBody(
            model = "gpt-3.5-turbo",
            messages = listOf(
                GPTMessage("system", "You are a helpful assistant."),
                GPTMessage("user", userMessage)
            ),
            max_tokens = 1000
        )

        apiService.getChatCompletion(apiKey, requestBody).enqueue(object : Callback<GPTResponse> {
            override fun onResponse(call: Call<GPTResponse>, response: Response<GPTResponse>) {
                if (response.isSuccessful) {
                    val assistantResponse = response.body()?.choices?.get(0)?.message?.content ?: "No response"
                    tutoringResponse.text = assistantResponse
                } else {
                    tutoringResponse.text = "Error: ${response.message()}"
                }
            }

            override fun onFailure(call: Call<GPTResponse>, t: Throwable) {
                tutoringResponse.text = "Error: ${t.message}"
            }
        })
    }
}
