package com.example.medace.api

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface HuggingFaceApiService {

    @Headers("Content-Type: application/json")
    @POST("chat/completions")
    suspend fun generateChat(
        @Body request: HuggingFaceRequest
    ): HuggingFaceResponse
}
