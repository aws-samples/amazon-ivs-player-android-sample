package com.amazonaws.ivs.player.basicplayback

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.ivs.player.*
import java.nio.ByteBuffer
import java.util.*


class MainActivity : AppCompatActivity() {

    // PlayerView is an easy to use wrapper around the Player object.
    // If you want to use the Player object directly, you can instantiate a
    // Player object and attach it to a SurfaceView with Player.setSurface()
    private lateinit var playerView : PlayerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        playerView = findViewById(R.id.playerView)

        // Load Uri to play
        playerView.player.load(Uri.parse("https://fcc3ddae59ed.us-west-2.playback.live-video.net/api/video/v1/us-west-2.893648527354.channel.DmumNckWFTqz.m3u8"))

        // Set PlaybackRate
        setPlaybackrate()

        // Set Listener for Player callback events
        handlePlayerEvents()
    }

    override fun onStart() {
        super.onStart()
        playerView.player.play()
    }

    override fun onStop() {
        super.onStop()
        playerView.player.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        playerView.player.release()
    }

    private fun setPlaybackrate() {
        val rateSpinner = findViewById(R.id.rate_spinner) as Spinner
        // Set playback rate, must be a floating point value
        val rates: List<Float> = Arrays.asList(0.5f, 1.0f, 1.5f, 2.0f)
        val rateAdapter: ArrayAdapter<Float> = ArrayAdapter<Float>(this,
                android.R.layout.simple_spinner_item, rates)
        rateAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        rateSpinner.adapter = rateAdapter
        rateSpinner.setSelection(1)
        rateSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val rate = rateAdapter.getItem(position)
                if (rate != null) {
                    playerView.player.setPlaybackRate(rate)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateQuality() {
        val qualitySpinner = findViewById(R.id.quality_spinner) as Spinner
        var auto = "auto"
        val currentQuality: String = playerView.player.getQuality().getName()
        if (playerView.player.isAutoQualityMode() && !TextUtils.isEmpty(currentQuality)) {
            auto += " ($currentQuality)"
        }
        var selected = 0
        val names: ArrayList<String?> = ArrayList()
        for (quality in playerView.player.getQualities()) {
            names.add(quality.name)
        }
        names.add(0, auto)
        if (!playerView.player.isAutoQualityMode()) {
            for (i in 0 until names.size) {
                if (names.get(i).equals(currentQuality)) {
                    selected = i
                }
            }
        }
        val qualityAdapter: ArrayAdapter<String?> = ArrayAdapter<String?>(this,
                android.R.layout.simple_spinner_item, names)
        qualityAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        qualitySpinner.setOnItemSelectedListener(null)
        qualitySpinner.setAdapter(qualityAdapter)
        qualitySpinner.setSelection(selected, false)
        qualitySpinner.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                val name = qualityAdapter.getItem(position)
                if (name != null && name.startsWith("auto")) {
                    playerView.player.setAutoQualityMode(true)
                } else {
                    for (quality in playerView.player.getQualities()) {
                        if (quality.name.equals(name, ignoreCase = true)) {
                            Log.i("IVSPlayer", "Quality Selected: " + quality);
                            playerView.player.setQuality(quality)
                            break
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })
    }

    /**
     * Demonstration for what callback APIs are available to Listen for Player events.
     */
    private fun handlePlayerEvents() {
        playerView.player.apply {
            // Listen to changes on the player
            addListener(object : Player.Listener() {
                override fun onAnalyticsEvent(p0: String, p1: String) {}
                override fun onDurationChanged(p0: Long) {
                    // If the video is a VOD, you can seek to a duration in the video
                    Log.i("IVSPlayer", "New duration: $duration")
                    seekTo(p0)
                }
                override fun onError(p0: PlayerException) {}
                override fun onMetadata(type: String, data: ByteBuffer) {}
                override fun onQualityChanged(p0: Quality) {
                    Log.i("IVSPlayer", "Quality changed to " + p0);
                    updateQuality()
                }
                override fun onRebuffering() {}
                override fun onSeekCompleted(p0: Long) {}
                override fun onVideoSizeChanged(p0: Int, p1: Int) {}
                override fun onCue(cue: Cue) {
                    when (cue) {
                        is TextMetadataCue -> Log.i("IVSPlayer","Received Text Metadata: ${cue.text}")
                    }
                }

                override fun onStateChanged(state: Player.State) {
                    Log.i("PlayerLog", "Current state: ${state}")
                    when (state) {
                        Player.State.BUFFERING,
                        Player.State.READY -> {
                            updateQuality()
                        }
                        Player.State.IDLE,
                        Player.State.ENDED -> {
                            // no-op
                        }
                        Player.State.PLAYING -> {
                            // Qualities will be dependent on the video loaded, and can
                            // be retrieved from the player
                            Log.i("IVSPlayer", "Available Qualities: ${qualities}")
                        }
                    }
                }
            })
        }
    }
}
