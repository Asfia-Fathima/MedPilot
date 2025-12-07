package com.example.medace.api

data class HuggingFaceRequest(
    val model: String = "Qwen/Qwen3-4B-Instruct-2507",
    val messages: List<Message>,
    val maxtokens: Int = 300,
    val temperature: Double = 0.7
)

//data class Message(
//    val role: String,
//    val content: String
//)
