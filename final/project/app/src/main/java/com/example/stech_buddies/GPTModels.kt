package com.example.stech_buddies

data class GPTRequestBody(
    val model: String,
    val messages: List<GPTMessage>,
    val max_tokens: Int
)

data class GPTMessage(
    val role: String,
    val content: String
)

data class GPTResponse(
    val choices: List<GPTChoice>
)

data class GPTChoice(
    val message: GPTMessage
)
