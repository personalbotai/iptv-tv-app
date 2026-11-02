package com.personalbotai.iptv

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.VideoView

class PlayerActivity : Activity() {

    private lateinit var videoView: VideoView
    private lateinit var titleView: TextView
    private lateinit var bufferView: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        val root = android.widget.FrameLayout(this).apply {
            setBackgroundColor(0xFF000000.toInt())
        }

        videoView = VideoView(this).apply {
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        root.addView(videoView)

        bufferView = ProgressBar(this).apply {
            val size = (60 * resources.displayMetrics.density).toInt()
            layoutParams = android.widget.FrameLayout.LayoutParams(size, size).apply {
                gravity = android.view.Gravity.CENTER
            }
        }
        root.addView(bufferView)

        titleView = TextView(this).apply {
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 20f
            setBackgroundColor(0x88000000.toInt())
            setPadding(32, 16, 32, 16)
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.TOP or android.view.Gravity.START
                setMargins(40, 40, 0, 0)
            }
        }
        root.addView(titleView)

        setContentView(root)

        val name = intent.getStringExtra("name") ?: "Live TV"
        val url = intent.getStringExtra("url") ?: ""

        titleView.text = name

        val mc = MediaController(this)
        mc.setAnchorView(videoView)
        videoView.setMediaController(mc)

        videoView.setOnPreparedListener {
            bufferView.visibility = View.GONE
            videoView.start()
        }

        videoView.setOnErrorListener { _, _, _ ->
            bufferView.visibility = View.GONE
            titleView.text = "$name (Stream Offline / Geo-blocked)"
            true
        }

        if (url.isNotEmpty()) {
            bufferView.visibility = View.VISIBLE
            videoView.setVideoURI(Uri.parse(url))
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
