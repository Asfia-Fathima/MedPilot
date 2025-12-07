package com.example.medace.api

// Groq does NOT use ContentItem arrays.
// Keeping this file clean and correct for Groq.

data class Message(
    val role: String,
    val content: String   // MUST be a plain string
)
