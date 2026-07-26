package com.telegram.tvplayer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.RowsSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.SparseArrayObjectAdapter
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    
    private lateinit var telegramManager: TelegramManager
    private lateinit var rowsAdapter: SparseArrayObjectAdapter
    private val TAG = "MainActivity"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_main)
            
            telegramManager = TelegramManager(this)
            setupUI()
            
            // Check authentication
            if (!telegramManager.isAuthenticated()) {
                startActivity(Intent(this, AuthActivity::class.java))
                finish()
                return
            }
            
            loadVideos()
        } catch (e: Exception) {
            Log.e(TAG, "Error in MainActivity onCreate", e)
            // Navigate to auth activity on error
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }
    }
    
    private fun setupUI() {
        val rowsFragment = RowsSupportFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_browse_fragment, rowsFragment)
            .commit()
        
        val listRowPresenter = ListRowPresenter()
        rowsAdapter = SparseArrayObjectAdapter(listRowPresenter)
        rowsFragment.adapter = rowsAdapter
    }
    
    private fun loadVideos() {
        lifecycleScope.launch {
            try {
                // Load videos from Telegram
                // This would typically load from specific chats or channels
                val videos = loadTelegramVideos()
                displayVideos(videos)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load videos", e)
            }
        }
    }
    
    private suspend fun loadTelegramVideos(): List<VideoItem> {
        // Implement actual video loading from Telegram
        // For now, return sample data
        return listOf(
            VideoItem(1, "Sample Video 1", "https://example.com/video1.mp4", 100L),
            VideoItem(2, "Sample Video 2", "https://example.com/video2.mp4", 200L),
            VideoItem(3, "Sample Video 3", "https://example.com/video3.mp4", 150L)
        )
    }
    
    private fun displayVideos(videos: List<VideoItem>) {
        val adapter = ArrayObjectAdapter(VideoPresenter())
        adapter.addAll(0, videos)
        
        val header = HeaderItem(0, "Telegram Videos")
        val row = ListRow(header, adapter)
        rowsAdapter.set(0, row)
    }
    
    fun onVideoSelected(video: VideoItem) {
        val intent = Intent(this, VideoPlayerActivity::class.java).apply {
            putExtra("video_url", video.url)
            putExtra("video_title", video.title)
            putExtra("sender_id", video.senderId)
        }
        startActivity(intent)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        telegramManager.cleanup()
    }
}

data class VideoItem(
    val id: Long,
    val title: String,
    val url: String,
    val senderId: Long
)