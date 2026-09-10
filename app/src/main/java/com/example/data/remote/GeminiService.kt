package com.example.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Legacy bridge object that routes requests to the Retrofit [GeminiApiService].
 */
object GeminiClient {
    private const val TAG = "GeminiClient"
    private val CANDIDATE_MODELS = listOf("gemini-3.5-flash", "gemini-3.6-flash", "gemini-flash-latest")

    fun hasValidApiKey(): Boolean = GeminiRetrofitClient.hasValidApiKey()

    suspend fun generateRagResponse(
        userPrompt: String,
        collegeContext: String,
        chatHistory: List<Pair<String, String>> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        if (!hasValidApiKey()) {
            throw IllegalStateException("API_KEY_NOT_CONFIGURED")
        }

        val systemPrompt = """
            You are CampusAI, an advanced futuristic holographic 3D College AI Assistant.
            Your purpose is to assist students, prospective applicants, faculty, and visitors.
            Always provide accurate, polite, and helpful information based on the verified College Knowledge Base provided below.
            Always express all fees, costs, placement packages, dues, scholarships, and monetary amounts in Indian Rupees (₹ / LPA). Never use dollars ($).
            If the requested information is not in the knowledge base, provide the best helpful guidance and advise the student to contact the respective college department or admin.
            
            VERIFIED COLLEGE KNOWLEDGE BASE CONTEXT:
            $collegeContext
        """.trimIndent()

        val contentsList = mutableListOf<GeminiContent>()

        // Add recent conversation history (up to last 4 turns)
        for ((role, text) in chatHistory.takeLast(4)) {
            val geminiRole = if (role == "user") "user" else "model"
            contentsList.add(
                GeminiContent(
                    role = geminiRole,
                    parts = listOf(GeminiPart(text = text))
                )
            )
        }

        // Add current user prompt
        contentsList.add(
            GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(text = userPrompt))
            )
        )

        val requestPayload = GeminiGenerateRequest(
            contents = contentsList,
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = systemPrompt))
            ),
            generationConfig = GeminiGenerationConfig()
        )

        val apiKey = GeminiRetrofitClient.getApiKey()
        var lastException: Exception? = null

        val apiService = GeminiRetrofitClient.apiService

        for (model in CANDIDATE_MODELS) {
            try {
                val response = apiService.generateContent(
                    model = model,
                    apiKey = apiKey,
                    request = requestPayload
                )
                val text = response.extractText()
                if (!text.isNullOrBlank()) {
                    return@withContext text
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown error"
                Log.w(TAG, "Gemini Retrofit call failed for model $model: $errorMsg")
                lastException = e
                if (errorMsg.contains("404") || errorMsg.contains("not found", ignoreCase = true) || errorMsg.contains("no longer available", ignoreCase = true)) {
                    continue
                } else {
                    throw lastException
                }
            }
        }

        throw lastException ?: Exception("Failed to receive response from Gemini API")
    }
}
