package com.telegram.tvplayer

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TestActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // Create a simple TextView programmatically
            val textView = TextView(this)
            textView.text = "App is working! This is a test screen."
            textView.textSize = 24f
            textView.setTextColor(0xFFFFFFFF.toInt())
            textView.setBackgroundColor(0xFF121212.toInt())
            textView.setPadding(32, 32, 32, 32)
            
            setContentView(textView)
            
        } catch (e: Exception) {
            // If even this fails, there's a major configuration issue
            val errorView = TextView(this)
            errorView.text = "Error: ${e.message}"
            errorView.textSize = 18f
            setContentView(errorView)
        }
    }
}