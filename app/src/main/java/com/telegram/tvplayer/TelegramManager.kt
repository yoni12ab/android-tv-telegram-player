package com.telegram.tvplayer

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.drinkless.tdlib.TdApi
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi.*
import java.io.File

class TelegramManager(private val context: Context) {
    
    private var client: Client? = null
    private var isAuthenticated = false
    private var authorizedUserId: Long = 0
    
    companion object {
        private const val TAG = "TelegramManager"
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

    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            val filesDir = File(context.filesDir, "tdlib")
            filesDir.mkdirs()
            
            val handler = object : Client.ResultHandler {
                override fun onResult(obj: TdApi.Object) {
                    when (obj.constructor) {
                        UPDATEAuthorizationState.CONSTRUCTOR -> handleAuthorizationState(obj as UPDATEAuthorizationState)
                        else -> Log.d(TAG, "Received update: ${obj.constructor}")
                    }
                }
            }
            
            val errorHandler = object : Client.ExceptionHandler {
                override fun onError(e: Throwable) {
                    Log.e(TAG, "TDLib error", e)
                }
            }
            
            val request = SetTdlibParameters(
                false,
                filesDir.absolutePath,
                filesDir.absolutePath,
                filesDir.absolutePath,
                API_ID,
                API_HASH,
                "en",
                "",
                true,
                true,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                0,
                false,
                false,
                true
            )
            
            client = Client.create(handler, errorHandler, null)
            client?.send(request) { 
                Log.d(TAG, "TDLib initialized") 
            }
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Telegram", e)
            false
        }
    }

    private fun handleAuthorizationState(update: UPDATEAuthorizationState) {
        when (update.authorizationState.constructor) {
            AuthorizationStateWaitPhoneNumber.CONSTRUCTOR -> {
                // Request phone number
                val request = SetAuthenticationPhoneNumber("+1234567890", null)
                client?.send(request) { result ->
                    Log.d(TAG, "Phone number set: ${result.constructor}")
                }
            }
            AuthorizationStateWaitCode.CONSTRUCTOR -> {
                // Wait for verification code
                Log.d(TAG, "Waiting for verification code")
            }
            AuthorizationStateWaitPassword.CONSTRUCTOR -> {
                // Handle 2FA if enabled
                Log.d(TAG, "Waiting for password")
            }
            AuthorizationStateReady.CONSTRUCTOR -> {
                isAuthenticated = true
                Log.d(TAG, "Authentication successful")
            }
            AuthorizationStateClosing.CONSTRUCTOR -> {
                Log.d(TAG, "Closing...")
            }
            AuthorizationStateClosed.CONSTRUCTOR -> {
                isAuthenticated = false
                Log.d(TAG, "Closed")
            }
        }
    }

    suspend fun sendVerificationCode(code: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = CheckAuthenticationCode(code)
            var success = false
            
            client?.send(request) { result ->
                success = when (result.constructor) {
                    Ok.CONSTRUCTOR -> true
                    Error.CONSTRUCTOR -> {
                        val error = result as Error
                        Log.e(TAG, "Verification failed: ${error.message}")
                        handleTelegramError(error)
                        false
                    }
                    else -> false
                }
            }
            
            success
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

    suspend fun getChatMessages(chatId: Long, limit: Int = 20): List<Message> = withContext(Dispatchers.IO) {
        try {
            val messages = mutableListOf<Message>()
            val request = GetChatHistory(chatId, 0, 0, limit, false, false)
            
            client?.send(request) { result ->
                when (result.constructor) {
                    Messages.CONSTRUCTOR -> {
                        val messagesResult = result as Messages
                        messages.addAll(messagesResult.messages.toList())
                    }
                    Error.CONSTRUCTOR -> {
                        val error = result as Error
                        Log.e(TAG, "Failed to get messages: ${error.message}")
                        handleTelegramError(error)
                    }
                }
            }
            
            messages
        } catch (e: Exception) {
            Log.e(TAG, "Get messages error", e)
            emptyList()
        }
    }

    suspend fun downloadFile(fileId: Int): String? = withContext(Dispatchers.IO) {
        try {
            var filePath: String? = null
            val request = DownloadFile(fileId, 1, 0, 0, false)
            
            client?.send(request) { result ->
                when (result.constructor) {
                    File.CONSTRUCTOR -> {
                        val file = result as File
                        if (file.local.isDownloadingCompleted) {
                            filePath = file.local.path
                        }
                    }
                    Error.CONSTRUCTOR -> {
                        val error = result as Error
                        Log.e(TAG, "Download failed: ${error.message}")
                        handleTelegramError(error)
                    }
                }
            }
            
            filePath
        } catch (e: Exception) {
            Log.e(TAG, "Download error", e)
            null
        }
    }

    private fun handleTelegramError(error: Error) {
        when (error.code) {
            403 -> {
                Log.e(TAG, "403 Forbidden: ${error.message}")
                // Handle 403 errors specifically
                when {
                    error.message.contains("USER_PRIVACY_RESTRICTED") -> {
                        Log.e(TAG, "User privacy restrictions apply")
                    }
                    error.message.contains("CHAT_WRITE_FORBIDDEN") -> {
                        Log.e(TAG, "Cannot write to this chat")
                    }
                    error.message.contains("PEER_FLOOD") -> {
                        Log.e(TAG, "Too many requests, please wait")
                    }
                }
            }
            429 -> {
                Log.e(TAG, "Rate limit exceeded: ${error.message}")
            }
            else -> {
                Log.e(TAG, "Telegram error ${error.code}: ${error.message}")
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
        client?.send(Close()) { 
            Log.d(TAG, "Client closed") 
        }
    }
}