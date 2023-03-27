package com.jack.android.gradle.method.trace.app

import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.source.LoadEventInfo
import com.google.android.exoplayer2.source.MediaLoadData
import com.google.android.exoplayer2.source.hls.playlist.HlsMasterPlaylist
import com.google.android.exoplayer2.source.hls.playlist.HlsMediaPlaylist
import com.google.android.exoplayer2.source.hls.playlist.HlsPlaylist
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.util.UriUtil
import com.google.android.exoplayer2.util.Util
import com.jack.android.gradle.method.trace.runtime.analyzer.Arg1
import com.jack.android.gradle.method.trace.runtime.analyzer.MethodExit
import com.jack.android.gradle.method.trace.runtime.analyzer.MethodResult
import com.jack.android.gradle.method.trace.runtime.analyzer.MethodTime
import com.jack.android.gradle.method.trace.runtime.analyzer.SimpleMethodAnalyzer
import com.jack.android.gradle.method.trace.runtime.analyzer.ThisRef
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class PlayerMethodAnalyzer : SimpleMethodAnalyzer() {

    private lateinit var loadedHlsMasterPlaylists: HlsMasterPlaylist
    private val loadedHlsMediaPlaylists = mutableListOf<HlsMediaPlaylist>()
    private var videoStartTime = SystemClock.elapsedRealtime()

    private fun debugLog(message: String) {
        Log.i("PlayerAnalyzer", message)
    }

    private fun formatLocalTime(usOfDay: Long, pattern: String = "HH:mm:ss"): String {
        if (usOfDay == C.TIME_UNSET) return "Nan"
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(pattern)
        val localTime = LocalTime.ofSecondOfDay(usOfDay / 1000L)
        return formatter.format(localTime)
    }

    @MethodExit("CacheDataSource#open")
    fun onFileDataSourceOpenDataSpec(@Arg1 dataSpec: DataSpec) {
        if (loadedHlsMasterPlaylists.baseUri == dataSpec.key) {
            // master
            debugLog(
                "Load master Hls playlist from cache " +
                        " time:${SystemClock.elapsedRealtime() - videoStartTime}" +
                        " ${dataSpec.uri.path}"
            )
        } else if (loadedHlsMediaPlaylists.any { it.baseUri == dataSpec.key }) {
            // Media variant
            debugLog(
                "Load media Hls playlist from cache " +
                        " time:${SystemClock.elapsedRealtime() - videoStartTime}" +
                        " ${dataSpec.uri.path}"
            )
        } else {
            loadedHlsMediaPlaylists.forEach { hlsMediaPlaylist ->
                val segment = hlsMediaPlaylist.segments.find {
                    UriUtil.resolve(hlsMediaPlaylist.baseUri, it.url) == dataSpec.key
                }
                if (null != segment) {
                    debugLog(
                        "Load segment from cache:${formatLocalTime(Util.usToMs(segment.relativeStartTimeUs))} ->" +
                                "${formatLocalTime(Util.usToMs(segment.relativeStartTimeUs + segment.durationUs))} " +
                                " time:${SystemClock.elapsedRealtime() - videoStartTime}" +
                                " ${dataSpec.uri.path}"
                    )
                    return@forEach
                }
            }
        }
    }

    @MethodExit("HlsPlaylistParser#parse")
    fun onPlaylistParse(
        @MethodResult result: HlsPlaylist,
        @Arg1 uri: Uri
    ) {
        if (result is HlsMasterPlaylist) {
            loadedHlsMasterPlaylists = result
            debugLog("Load MasterPlaylist")
            result.variants.forEach { variant ->
                debugLog("Variant:${variant.url.path} format:${variant.format}")
            }
        } else if (result is HlsMediaPlaylist) {
            if (!loadedHlsMediaPlaylists.contains(result)) {
                loadedHlsMediaPlaylists.add(result)
            }
            debugLog("Load MediaPlaylist:${result.playlistType} duration:${result.durationUs} ${result.segments}")
        }
        debugLog("PlaylistParser#parse:$uri")
    }

    @MethodExit("SimpleExoPlayer#prepare")
    fun onPlayerPrepare(@ThisRef player: ExoPlayer) {
        player.addAnalyticsListener(object : AnalyticsListener {
            override fun onLoadCompleted(
                eventTime: AnalyticsListener.EventTime,
                loadEventInfo: LoadEventInfo,
                mediaLoadData: MediaLoadData
            ) {
                super.onLoadCompleted(eventTime, loadEventInfo, mediaLoadData)
                // 10
                // loadEventInfo.loadDurationMs
            }
        })
        videoStartTime = SystemClock.elapsedRealtime()
        loadedHlsMediaPlaylists.clear()
        debugLog("SimpleExoPlayer#prepare:$player")
    }

    @MethodExit("AnalyticsCollector#onAudioDecoderInitialized")
    fun onAudioDecoderInitialized() {
        debugLog("AudioDecoder Timeline ${SystemClock.elapsedRealtime() - videoStartTime}")
    }

    @MethodExit("AnalyticsCollector#onVideoDecoderInitialized")
    private fun onVideoDecoderInitialized() {
        debugLog("VideoDecoder Timeline ${SystemClock.elapsedRealtime() - videoStartTime}")
    }

    @MethodExit("HlsMediaChunk#load")
    private fun onHlsMediaChunkLoad(@MethodTime startTime: Long) {
        val durationMs = System.currentTimeMillis() - startTime
        debugLog("HlsMediaChunkLoad:$durationMs")
    }
}