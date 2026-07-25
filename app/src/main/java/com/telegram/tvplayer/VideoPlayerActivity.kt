package com.telegram.tvplayer

import android.app.Activity
import android.content.pm.ActivityInfo
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.launch
import java.io.File

class VideoPlayerActivity : Activity() {
    
    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var telegramManager: TelegramManager
    private lateinit var recordButton: Button
    private lateinit var titleText: TextView
    
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var recordingFile: File? = null
    
    private val TAG = "VideoPlayerActivity"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
        
        // Get intent data
        val videoUrl = intent.getStringExtra("video_url") ?: return
        val videoTitle = intent.getStringExtra("video_title") ?: "Unknown"
        val senderId = intent.getLongExtra("sender_id", 0)
        
        // Verify sender ID
        telegramManager = TelegramManager(this)
        lifecycleScope.launch {
            val isAuthorized = telegramManager.verifySender(senderId)
            if (!isAuthorized) {
                Toast.makeText(
                    this@VideoPlayerActivity,
                    "Unauthorized sender",
                    Toast.LENGTH_LONG
                ).show()
                finish()
                return@launch
            }
        }
        
        // Setup UI
        playerView = findViewById(R.id.player_view)
        recordButton = findViewById(R.id.record_button)
        titleText = findViewById(R.id.video_title)
        
        titleText.text = videoTitle
        
        // Setup player
        player = ExoPlayer.Builder(this).build()
        playerView.player = player
        
        val mediaItem = MediaItem.fromUri(videoUrl)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
        
        // Setup recording
        recordButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
        }
        
        // Lock orientation to landscape
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }
    
    private fun startRecording() {
        try {
            val outputDir = File(filesDir, "recordings")
            outputDir.mkdirs()
            
            recordingFile = File(outputDir, "recording_${System.currentTimeMillis()}.mp4")
            
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setOutputFile(recordingFile?.absolutePath)
                setVideoEncodingBitRate(8000000)
                setVideoFrameRate(30)
                setVideoSize(1920, 1080)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                
                prepare()
                start()
            }
            
            isRecording = true
            recordButton.text = "Stop Recording"
            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording", e)
            Toast.makeText(this, "Failed to start recording", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
            
            mediaRecorder = null
            isRecording = false
            recordButton.text = "Start Recording"
            
            Toast.makeText(this, "Recording saved: ${recordingFile?.absolutePath}", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording", e)
            Toast.makeText(this, "Failed to stop recording", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onPause() {
        super.onPause()
        player.pause()
        if (isRecording) {
            stopRecording()
        }
    }
    
    override fun onResume() {
        super.onResume()
        player.play()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        player.release()
        mediaRecorder?.release()
        telegramManager.cleanup()
    }
}