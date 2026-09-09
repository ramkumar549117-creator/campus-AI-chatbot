package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiGenerateRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerateResponse(
    val candidates: List<GeminiCandidate>? = null
)

object GeminiClient {
    private const val TAG = "GeminiClient"
    private val CANDIDATE_MODELS = listOf("gemini-3.6-flash", "gemini-3.5-flash", "gemini-flash-latest")

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val requestAdapter = moshi.adapter(GeminiGenerateRequest::class.java)
    private val responseAdapter = moshi.adapter(GeminiGenerateResponse::class.java)

    fun hasValidApiKey(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return !key.isNullOrBlank() && key != "MY_GEMINI_API_KEY" && !key.contains("MY_GEMINI")
    }

    suspend fun generateRagResponse(
        userPrompt: String,
        collegeContext: String,
        chatHistory: List<Pair<String, String>> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
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
            )
        )

        val jsonBody = requestAdapter.toJson(requestPayload)
        var lastException: Exception? = null

        for (model in CANDIDATE_MODELS) {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val responseString = response.body?.string() ?: ""
                val parsed = responseAdapter.fromJson(responseString)
                val responseText = parsed?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "I'm sorry, I couldn't process that query at the moment."
                return@withContext responseText
            } else {
                val errorBody = response.body?.string() ?: "Unknown error"
                Log.w(TAG, "Gemini API failed with model $model (HTTP ${response.code}): $errorBody")
                lastException = Exception("API Error ${response.code} ($model): $errorBody")
                if (response.code == 404 || errorBody.contains("not found", ignoreCase = true) || errorBody.contains("no longer available", ignoreCase = true)) {
                    continue
                } else {
                    throw lastException
                }
            }
        }

        throw lastException ?: Exception("Failed to receive response from Gemini API")
    }
}
