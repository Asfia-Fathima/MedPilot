package com.example.medace.api

import com.google.gson.annotations.SerializedName

data class GroqRequest(
    @SerializedName("model") val model: String = "Qwen/Qwen3-4B-Instruct-2507",
    @SerializedName("messages") val messages: List<Message>,
    @SerializedName("temperature") val temperature: Double = 0.7,
    @SerializedName("max_tokens") val maxTokens: Int = 1024,
    @SerializedName("top_p") val topP: Double = 1.0,
    @SerializedName("stream") val stream: Boolean = false
)

// FINAL correct structure for Groq
data class MessageRequest(
    val role: String,
    val content: String    // MUST be a plain string
)
