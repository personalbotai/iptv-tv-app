package com.personalbotai.iptv

import android.os.Handler
import android.os.Looper
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

data class Channel(val name: String, val url: String, val group: String, val logo: String)

object ChannelRepository {
    val channels = mutableListOf<Channel>()
    var onLoaded: (() -> Unit)? = null
    var isLoading = false

    private val staticChannels = listOf(
        Channel("Al-Bahjah TV", "http://202.150.161.117:8000/play/AlBahjahTV", "Indonesia", ""),
        Channel("Al-Iman TV", "https://tv.aliman.id/aliman/live.m3u8", "Indonesia", ""),
        Channel("Metro TV", "https://stream.metrotvnews.com/live/smil:metrotvnews.smil/playlist.m3u8", "Indonesia", ""),
        Channel("TVRI Nasional", "https://live.tvri.go.id/hls/tvri.m3u8", "Indonesia", ""),
        Channel("CNN Indonesia", "https://live.cnnindonesia.com/live/smil:cnnindonesia.smil/playlist.m3u8", "Indonesia", ""),
        Channel("Trans TV", "https://live.transtv.co.id/live/smil:transtv.smil/playlist.m3u8", "Indonesia", ""),
        Channel("Trans7", "https://live.trans7.co.id/live/smil:trans7.smil/playlist.m3u8", "Indonesia", ""),
        Channel("NET. TV", "https://live.netmedia.co.id/live/smil:netmedia.smil/playlist.m3u8", "Indonesia", ""),
        Channel("Kompas TV", "https://video.kompas.tv/live/smil:kompastv.smil/playlist.m3u8", "Indonesia", ""),
        Channel("RTV", "https://video.rtv.co.id/live/smil:rtv.smil/playlist.m3u8", "Indonesia", ""),
        Channel("ABC News", "https://www.twitch.tv/abcnewsal", "News", ""),
        Channel("Bloomberg TV", "https://live-bloomberg-comma.leanstream.co/bloomberg/comma/playlist.m3u8", "News", ""),
        Channel("NASA TV HD", "https://nasa-i.akamaihd.net/hls/live/509607/NASA-TV-HD/master.m3u8", "Documentary", ""),
        Channel("Red Bull TV", "https://rbmn-live.akamaized.net/hls/live/590964/BoRB-umu/master.m3u8", "Sports", ""),
        Channel("Fashion TV", "https://fash1st-live.hls.live-video.net/v1/manifest/a4b870d0-e176-47b0-8c29-3733bc13ffaa/index.m3u8", "Lifestyle", "")
    )

    fun load() {
        if (channels.isNotEmpty() || isLoading) return
        isLoading = true
        // Langsung muat channel instan agar user tidak menunggu
        channels.addAll(staticChannels)
        onLoaded?.invoke()

        thread {
            try {
                val urls = listOf(
                    "https://raw.githubusercontent.com/iptv-org/iptv/master/streams/id.m3u",
                    "https://raw.githubusercontent.com/Free-TV/IPTV/master/playlist.m3u8",
                    "https://iptv-org.github.io/iptv/index.category.m3u"
                )
                val newChannels = mutableListOf<Channel>()
                for (u in urls) {
                    try {
                        val conn = URL(u).openConnection() as HttpURLConnection
                        conn.connectTimeout = 5000
                        conn.readTimeout = 5000
                        conn.setRequestProperty("User-Agent", "VLC")
                        val reader = BufferedReader(InputStreamReader(conn.inputStream))
                        var line: String?
                        var currentName = ""
                        var currentGroup = "General"
                        var currentLogo = ""
                        while (reader.readLine().also { line = it } != null) {
                            val l = line!!.trim()
                            if (l.startsWith("#EXTINF")) {
                                currentGroup = "General"
                                currentLogo = ""
                                if (l.contains("group-title=\"")) {
                                    val start = l.indexOf("group-title=\"") + 13
                                    val end = l.indexOf("\"", start)
                                    if (end > start) currentGroup = l.substring(start, end)
                                }
                                if (l.contains("tvg-logo=\"")) {
                                    val start = l.indexOf("tvg-logo=\"") + 10
                                    val end = l.indexOf("\"", start)
                                    if (end > start) currentLogo = l.substring(start, end)
                                }
                                val comma = l.lastIndexOf(',')
                                if (comma != -1 && comma < l.length - 1) {
                                    currentName = l.substring(comma + 1).trim()
                                }
                            } else if (l.isNotEmpty() && !l.startsWith("#") && (l.startsWith("http://") || l.startsWith("https://"))) {
                                if (currentName.isNotEmpty()) {
                                    newChannels.add(Channel(currentName, l, currentGroup, currentLogo))
                                }
                                currentName = ""
                            }
                        }
                        reader.close()
                    } catch (_: Exception) {}
                }
                if (newChannels.isNotEmpty()) {
                    channels.addAll(0, newChannels)
                    Handler(Looper.getMainLooper()).post {
                        onLoaded?.invoke()
                    }
                }
            } catch (_: Exception) {}
            isLoading = false
        }
    }
}
