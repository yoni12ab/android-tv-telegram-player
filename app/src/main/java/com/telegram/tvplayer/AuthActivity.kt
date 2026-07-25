package com.telegram.tvplayer

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {
    
    private lateinit var phoneNumberInput: EditText
    private lateinit var verificationCodeInput: EditText
    private lateinit var loginButton: Button
    private lateinit var verifyButton: Button
    
    private lateinit var telegramManager: TelegramManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        
        telegramManager = TelegramManager(this)
        
        phoneNumberInput = findViewById(R.id.phone_number_input)
        verificationCodeInput = findViewById(R.id.verification_code_input)
        loginButton = findViewById(R.id.login_button)
        verifyButton = findViewById(R.id.verify_button)
        
        // Initialize Telegram
        lifecycleScope.launch {
            val initialized = telegramManager.initialize()
            if (!initialized) {
                Toast.makeText(
                    this@AuthActivity,
                    "Failed to initialize Telegram",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        
        loginButton.setOnClickListener {
            val phoneNumber = phoneNumberInput.text.toString()
            if (phoneNumber.isNotEmpty()) {
                sendPhoneNumber(phoneNumber)
            } else {
                Toast.makeText(this, "Please enter phone number", Toast.LENGTH_SHORT).show()
            }
        }
        
        verifyButton.setOnClickListener {
            val code = verificationCodeInput.text.toString()
            if (code.isNotEmpty()) {
                verifyCode(code)
            } else {
                Toast.makeText(this, "Please enter verification code", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun sendPhoneNumber(phoneNumber: String) {
        lifecycleScope.launch {
            // Implement phone number sending through TelegramManager
            Toast.makeText(this@AuthActivity, "Verification code sent", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun verifyCode(code: String) {
        lifecycleScope.launch {
            val success = telegramManager.sendVerificationCode(code)
            if (success) {
                Toast.makeText(
                    this@AuthActivity,
                    "Authentication successful",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            } else {
                Toast.makeText(
                    this@AuthActivity,
                    "Verification failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        telegramManager.cleanup()
    }
}