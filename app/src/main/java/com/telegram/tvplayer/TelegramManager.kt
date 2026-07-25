package com.telegram.tvplayer

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File

class TelegramManager(private val context: Context) {
    
    private var isAuthenticated = false
    private var authorizedUserId: Long = 0
    
    companion object {
        private const val TAG = "TelegramManager"
        private const val TELEGRAM_API_URL = "https://api.telegram.org/bot"
    }
    
    private val API_ID: Int by lazy {
        context.resources.getInteger(R.integer.telegram_api_id)
    }
    
    private val API_HASH: String by lazy {
        context.getString(R.string.telegram_api_hash)
    }
    
    private val AUTHORIZED_USER_ID: Long by lazy {
        context.getString(R.string.authorized_user_id).toLong()
    }
    
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("Content-Type", "application/json")
            chain.proceed(requestBuilder.build())
        }
        .build()

    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Simple initialization - in a real app, this would handle Telegram bot setup
            Log.d(TAG, "TelegramManager initialized with API ID: $API_ID")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Telegram", e)
            false
        }
    }

    suspend fun sendVerificationCode(code: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Simplified verification - in production, implement proper Telegram auth
            Log.d(TAG, "Verification code: $code")
            isAuthenticated = true
            true
        } catch (e: Exception) {
            Log.e(TAG, "Verification error", e)
            false
        }
    }

    suspend fun verifySender(userId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            // Verify that the sender matches the authorized user ID
            val success = userId == AUTHORIZED_USER_ID
            if (success) {
                authorizedUserId = userId
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Sender verification error", e)
            false
        }
    }

    suspend fun makeTelegramRequest(botToken: String, method: String, params: JSONObject = JSONObject()): String? = withContext(Dispatchers.IO) {
        try {
            val url = "$TELEGRAM_API_URL$botToken/$method"
            val mediaType = "application/json".toMediaType()
            val body = params.toString().toRequestBody(mediaType)
            
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()
            
            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string()
            } else {
                handleTelegramError(response.code, response.message)
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Telegram request error", e)
            null
        }
    }

    private fun handleTelegramError(code: Int, message: String) {
        when (code) {
            403 -> {
                Log.e(TAG, "403 Forbidden: $message")
                // Handle 403 errors specifically
                when {
                    message.contains("USER_PRIVACY_RESTRICTED") -> {
                        Log.e(TAG, "User privacy restrictions apply")
                    }
                    message.contains("CHAT_WRITE_FORBIDDEN") -> {
                        Log.e(TAG, "Cannot write to this chat")
                    }
                    message.contains("PEER_FLOOD") -> {
                        Log.e(TAG, "Too many requests, please wait")
                    }
                }
            }
            429 -> {
                Log.e(TAG, "Rate limit exceeded: $message")
            }
            else -> {
                Log.e(TAG, "Telegram error $code: $message")
            }
        }
    }

    suspend fun retryWithBackoff(operation: suspend () -> Unit, maxRetries: Int = 3): Boolean {
        var retryCount = 0
        var delay = 1000L // Start with 1 second
        
        while (retryCount < maxRetries) {
            try {
                operation()
                return true
            } catch (e: Exception) {
                retryCount++
                if (retryCount >= maxRetries) {
                    Log.e(TAG, "Operation failed after $maxRetries retries", e)
                    return false
                }
                Thread.sleep(delay)
                delay *= 2 // Exponential backoff
            }
        }
        return false
    }

    fun isAuthenticated(): Boolean = isAuthenticated
    
    fun getAuthorizedUserId(): Long = authorizedUserId
    
    fun cleanup() {
        // Cleanup resources
        Log.d(TAG, "TelegramManager cleaned up")
    }
}