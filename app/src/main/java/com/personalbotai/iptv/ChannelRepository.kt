package com.personalbotai.iptv

import android.os.Handler
import android.os.Looper
import java.net.URL
import java.util.regex.Pattern
import kotlin.concurrent.thread

data class Channel(val name: String, val url: String, val group: String, val logo: String)

object ChannelRepository {
    var channels = mutableListOf<Channel>()
    var onLoaded: (() -> Unit)? = null
    var isLoading = false

    fun load() {
        if (channels.isNotEmpty() || isLoading) return
        isLoading = true
        thread {
            try {
                val url = "https://iptv-org.github.io/iptv/index.country.m3u"
                val text = URL(url).readText()
                val pattern = Pattern.compile("#EXTINF:-1(?:\\s+tvg-logo=\"(.*?)\")?(?:\\s+group-title=\"(.*?)\")?,(.*?)\n(http[s]?://[^\\s]+)")
                val matcher = pattern.matcher(text)
                val loaded = mutableListOf<Channel>()
                while (matcher.find()) {
                    val logo = matcher.group(1) ?: ""
                    val group = matcher.group(2) ?: "Worldwide"
                    val name = matcher.group(3)?.trim() ?: "Channel"
                    val streamUrl = matcher.group(4)?.trim() ?: continue
                    loaded.add(Channel(name, streamUrl, group, logo))
                }
                if (loaded.isNotEmpty()) {
                    channels.clear()
                    channels.addAll(loaded)
                    Handler(Looper.getMainLooper()).post {
                        onLoaded?.invoke()
                    }
                }
            } catch (_: Exception) {}
            isLoading = false
        }
    }
}
