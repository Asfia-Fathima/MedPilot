package com.example.medace.api

import com.google.gson.annotations.SerializedName

data class GroqResponse(
    @SerializedName("choices") val choices: List<GroqChoice>
)

data class GroqChoice(
    @SerializedName("message") val message: MessageResponse
)

data class MessageResponse(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String   // Groq returns plain string
)
