package com.telegram.tvplayer

import android.graphics.drawable.Drawable
import androidx.leanback.widget.Presenter
import androidx.core.content.ContextCompat
import android.view.ViewGroup
import android.widget.TextView

class VideoPresenter : Presenter() {
    
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = TextView(parent.context)
        view.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        view.setPadding(32, 16, 32, 16)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val video = item as VideoItem
        val textView = viewHolder.view as TextView
        textView.text = video.title
        textView.setTextColor(0xFFFFFFFF.toInt())
        textView.textSize = 18f
    }
    
    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        // Clean up if needed
    }
}