package com.personalbotai.iptv

import android.app.Application
import android.os.Handler
import android.os.Looper
import java.net.URL
import java.util.regex.Pattern
import kotlin.concurrent.thread

data class Channel(val name: String, val url: String, val group: String, val logo: String)

object ChannelRepository {
    private val defaultChannels = listOf(
        Channel("Al-Bahjah TV", "http://202.150.161.117:8000/play/AlBahjahTV", "Indonesia", ""),
        Channel("Al-Iman TV", "https://tv.aliman.id/aliman/live.m3u8", "Indonesia", ""),
        Channel("ABC News", "https://www.twitch.tv/abcnewsal", "News", ""),
        Channel("Bloomberg TV", "https://live-bloomberg-comma.leanstream.co/bloomberg/comma/playlist.m3u8", "News", ""),
        Channel("NASA TV HD", "https://nasa-i.akamaihd.net/hls/live/509607/NASA-TV-HD/master.m3u8", "Documentary", ""),
        Channel("Red Bull TV", "https://rbmn-live.akamaized.net/hls/live/590964/BoRB-umu/master.m3u8", "Sports", ""),
        Channel("Fashion TV", "https://fash1st-live.hls.live-video.net/v1/manifest/a4b870d0-e176-47b0-8c29-3733bc13ffaa/index.m3u8", "Lifestyle", ""),
        Channel("Euronews English", "https://euronews-euronews-english-1-eu.rakuten.wurl.tv/playlist.m3u8", "News", "")
    )

    var channels = mutableListOf<Channel>()
    var onLoaded: (() -> Unit)? = null

    init {
        channels.addAll(defaultChannels)
        loadFromWeb()
    }

    private fun loadFromWeb() {
        thread {
            try {
                val urls = listOf(
                    "https://raw.githubusercontent.com/iptv-org/iptv/master/streams/id.m3u",
                    "https://raw.githubusercontent.com/iptv-org/iptv/master/streams/us.m3u",
                    "https://raw.githubusercontent.com/Free-TV/IPTV/master/playlist.m3u8"
                )
                val newChannels = mutableListOf<Channel>()
                for (u in urls) {
                    try {
                        val text = URL(u).readText()
                        val pattern = Pattern.compile("#EXTINF:-1(?:\\s+tvg-logo=\"(.*?)\")?(?:\\s+group-title=\"(.*?)\")?,(.*?)\n(http[s]?://[^\\s]+)")
                        val matcher = pattern.matcher(text)
                        while (matcher.find()) {
                            val logo = matcher.group(1) ?: ""
                            val group = matcher.group(2) ?: "General"
                            val name = matcher.group(3)?.trim() ?: "Channel"
                            val streamUrl = matcher.group(4)?.trim() ?: continue
                            newChannels.add(Channel(name, streamUrl, group, logo))
                        }
                    } catch (_: Exception) {}
                }
                if (newChannels.isNotEmpty()) {
                    channels.addAll(0, newChannels)
                    Handler(Looper.getMainLooper()).post {
                        onLoaded?.invoke()
                    }
                }
            } catch (_: Exception) {}
        }
    }
}
