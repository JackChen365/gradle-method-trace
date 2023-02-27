package com.jack.android.gradle.method.trace.app

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.util.DebugTextViewHelper
import com.jack.android.gradle.method.trace.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    // val videoUrl = "https://edge.tikicdn.com/data/hls/902297/1/3/1478/manifest.m3u8"
    private val videoUrl =
        "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_4x3/bipbop_4x3_variant.m3u8"
    private val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
    private var debugViewHelper: DebugTextViewHelper? = null
    private var playerView: StyledPlayerView? = null
    private var player: ExoPlayer? = null
    private var startAutoPlay = false
    private var startItemIndex = 0
    private var startPosition: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.play.setOnClickListener {
            it.isEnabled = false
            initializePlayer(this)
        }
        playerView = binding.playerView

        binding.buttonPanel.post {
            binding.buttonPanel.fullScroll(View.FOCUS_RIGHT)
        }
    }

    private fun initializePlayer(context: Context) {
        // ImmutableList.of(5_400_000L, 3_300_000L, 2_000_000L, 1_300_000L, 760_000L);  Wifi
        // ImmutableList.of(1_700_000L, 820_000L, 450_000L, 180_000L, 130_000L);        2G
        // ImmutableList.of(2_300_000L, 1_300_000L, 1_000_000L, 820_000L, 570_000L);    3G
        // ImmutableList.of(3_400_000L, 2_000_000L, 1_400_000L, 1_000_000L, 620_000L);  4G
        // ImmutableList.of(7_500_000L, 5_200_000L, 3_700_000L, 1_800_000L, 1_100_000L) 5G_NSA
        // ImmutableList.of(3_300_000L, 1_900_000L, 1_700_000L, 1_500_000L, 1_200_000L) 5G_SA
        val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter.Builder(context)
            // Use 2G to use the lowest bitrate 130_000L
            .setInitialBitrateEstimate(130_000L)
            .build()

        val trackSelector = DefaultTrackSelector(
            context
        )
        trackSelector.parameters = DefaultTrackSelector.ParametersBuilder(context).build()

        val exoPlayer = ExoPlayer.Builder(context)
            .setLoadControl(DefaultLoadControl())
            .setTrackSelector(trackSelector)
            .setBandwidthMeter(bandwidthMeter).build()
        player = exoPlayer
        playerView?.player = player
        debugViewHelper = DebugTextViewHelper(exoPlayer, binding.playerDebugText)
        debugViewHelper?.start()
        exoPlayer.playWhenReady = true
        exoPlayer.seekTo(0, 0)
        exoPlayer.repeatMode = Player.REPEAT_MODE_OFF
        val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
        //Only read cache.
        val cacheMediaSourceFactory = DefaultMediaSourceFactory(this)
        exoPlayer.setMediaSource(cacheMediaSourceFactory.createMediaSource(mediaItem), true)
        exoPlayer.prepare()
    }

    override fun onStart() {
        super.onStart()
        player?.playWhenReady = startAutoPlay
        playerView?.onResume()
    }

    override fun onStop() {
        super.onStop()
        startAutoPlay = true == player?.playWhenReady
        player?.pause()
        playerView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    private fun updateStartPosition() {
        val exoPlayer = player ?: return
        startAutoPlay = exoPlayer.playWhenReady
        startItemIndex = exoPlayer.currentMediaItemIndex
        startPosition = 0L.coerceAtLeast(exoPlayer.contentPosition)
    }

    private fun releasePlayer() {
        updateStartPosition()
        debugViewHelper?.stop()
        debugViewHelper = null
        player?.release()
        player = null
    }

}