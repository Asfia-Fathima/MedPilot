package com.example.medace.api

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface GroqApiService {

    // Correct endpoint path for Groq Chat API
    @Headers("Content-Type: application/json")
    @POST("Mixtral-8x7B-Instruct-v0.1")

    suspend fun getChatResponse(
        @Body request: GroqRequest
    ): GroqResponse
}
