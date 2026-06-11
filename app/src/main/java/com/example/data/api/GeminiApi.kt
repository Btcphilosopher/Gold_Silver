package com.example.data.api

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// --- Gemini API Request/Response Data Classes ---

data class Part(
    val text: String? = null
)

data class Content(
    val parts: List<Part>
)

data class GenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null
)

data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

data class Candidate(
    val content: Content
)

data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

// --- Retrofit API Service ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

// --- Retrofit Client ---

object GeminiRetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

// --- Gemini Executive Helper ---

object GeminiAssistant {
    private const val SYSTEM_PROMPT = """
You are the AURUM Wealth Assistant, an expert AI wealth advisor specializing in physical precious metals (Gold, Silver, Platinum).
Your tone is professional, trustworthy, premium, and structured, like an institutional OTC dealer or private banker.

Rules to follow:
1. Provide accurate, clear, and educational responses regarding precious metals.
2. In buying / savings calculations:
   - Gold average spot: GBP 1840 / USD 2350 / EUR 2160 per troy ounce.
   - Silver average spot: GBP 23.10 / USD 29.50 / EUR 27.20 per troy ounce.
   - 1 troy ounce = 31.1035 grams.
3. If the user asks for financial forecasts, provide structured calculations with transparent premiums (usually 2-5% for Gold, 10-15% for Silver).
4. Always explain risks transparently (e.g., storage costs, price volatility, liquidity spreads).
5. Always address the user directly in a polished premium tone.
"""

    suspend fun askAssistant(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "AURUM Assistant Notice: The Gemini API Key is currently not set in the Secrets Panel. To activate the AI Assistant, please enter a valid GEMINI_API_KEY in Google AI Studio secrets.\n\nHowever, I can answer in sandbox mode:\n\nIf you invest £500/month over 10 years, you would accumulate approximately £60,000 in principal. At current rates, this would purchase roughly 32.6 ounces of gold, representing solid long-term physical bullion wealth storage."
        }

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            ),
            generationConfig = GenerationConfig(
                temperature = 0.5f
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = SYSTEM_PROMPT))
            )
        )

        try {
            val response = GeminiRetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "I was unable to formulate a response. Please try again."
        } catch (e: Exception) {
            "An error occurred while matching with AURUM Secure Vault Servers: ${e.localizedMessage}\n\nFallback explanation: Gold and Silver act as traditional hedges against inflation. The dealer premium on sovereigns is slightly higher than bars (around 4-6% vs 2%), but they have capital gains tax (CGT) benefits in some jurisdictions like the UK!"
        }
    }
}
