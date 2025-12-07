package com.example.medace.api

data class HuggingFaceResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: HFMessage
)

data class HFMessage(
    val role: String,
    val content: String
)
