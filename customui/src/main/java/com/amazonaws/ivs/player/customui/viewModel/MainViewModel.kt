package com.amazonaws.ivs.player.customui.viewModel

import android.app.Application
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Surface
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amazonaws.ivs.player.MediaType
import com.amazonaws.ivs.player.Player
import com.amazonaws.ivs.player.TextMetadataCue
import com.amazonaws.ivs.player.customui.common.*
import com.amazonaws.ivs.player.customui.common.Configuration.TAG
import com.amazonaws.ivs.player.customui.common.enums.PlayingState
import com.amazonaws.ivs.player.customui.data.LocalCacheProvider
import com.amazonaws.ivs.player.customui.data.entity.OptionDataItem
import com.amazonaws.ivs.player.customui.data.entity.SourceDataItem
import java.nio.charset.StandardCharsets

class MainViewModel(
    private val context: Application,
    private val cacheProvider: LocalCacheProvider
) : ViewModel() {

    private var player: Player? = null
    private var playerListener: Player.Listener? = null

    private val handler = Handler(Looper.getMainLooper())
    private val updateSeekBarTask = object : Runnable {
        override fun run() {
            progress.value = player?.position?.timeString()
            seekBarProgress.value = player?.position?.toInt()
            seekBarSecondaryProgress.value = player?.bufferedPosition?.toInt()
            if (liveStream.value == false) handler.postDelayed(this, 500)
        }
    }
    private val url = MutableLiveData<String>()
    val playerState = MutableLiveData<Player.State>()
    val buttonState = MutableLiveData<PlayingState>()
    val liveStream = MutableLiveData<Boolean>()
    val selectedRateValue = MutableLiveData<String>()
    val selectedQualityValue = MutableLiveData<String>()
    val duration = MutableLiveData<String>()
    val progress = MutableLiveData<String>()

    val durationVisible = MutableLiveData<Boolean>()
    val progressVisible = MutableLiveData<Boolean>()
    val seekBarVisible = MutableLiveData<Boolean>()
    val showControls = MutableLiveData<Boolean>()
    val buffering = MutableLiveData<Boolean>()
    val isPlaying = MutableLiveData<Boolean>()

    val seekBarMax = MutableLiveData<Int>()
    val seekBarProgress = MutableLiveData<Int>()
    val seekBarSecondaryProgress = MutableLiveData<Int>()

    val playerParamsChanged = MutableLiveData<Pair<Int, Int>>()
    val errorHappened = MutableLiveData<Pair<String, String>>()

    val qualities = MutableLiveData<List<OptionDataItem>>()
    val sources = MutableLiveData<List<SourceDataItem>>()

    init {
        initPlayer()
        setPlayerListener()
        initDefault()
        getSources()
    }

    private fun initDefault() {
        buttonState.value = PlayingState.PLAYING
        url.value = Configuration.LINK
        selectedRateValue.value = Configuration.PLAYBACK_RATE_DEFAULT
        selectedQualityValue.value = Configuration.AUTO
    }

    private fun initPlayer() {
        // Media player initialization
        player = Player.Factory.create(context)
    }

    private fun setPlayerListener() {
        // Media player listener creation and initialization
        playerListener = player?.setListener(
            onVideoSizeChanged = { width, height ->
                Log.d(TAG, "Video size changed: $width $height")
                playerParamsChanged.value = Pair(width, height)
            },
            onCue = { cue ->
                if (cue is TextMetadataCue) {
                    Log.d(TAG, "Received Text Metadata: ${cue.text}")
                }
            },
            onDurationChanged = { durationValue ->
                Log.d(TAG, "Duration changed: $durationValue")
                if (player?.duration != null && player!!.duration > 0L) {
                    // Switch to VOD player controls
                    seekBarMax.value = durationValue.toInt()
                    liveStream.value = false
                    duration.value = durationValue.timeString()
                    durationVisible.value = true
                    progressVisible.value = true
                    updateSeekBarTask.run()
                } else {
                    // Switch to Live player controls
                    liveStream.value = true
                    durationVisible.value = false
                    progressVisible.value = false
                    seekBarVisible.value = false
                }
            },
            onStateChanged = { state ->
                Log.d(TAG, "State changed: $state")
                playerState.value = state
            },
            onMetadata = { data, buffer ->
                if (MediaType.TEXT_PLAIN == data) {
                    val textData = StandardCharsets.UTF_8.decode(buffer)
                    Log.d(TAG, "Received Timed Metadata: $textData")
                }
            },
            onError = { exception ->
                Log.d(TAG, "Error happened: $exception")
                errorHappened.value = Pair(exception.code.toString(), exception.errorMessage)
                isPlaying.value = false
            }
        )
    }

    fun toggleControls(show: Boolean) {
        Log.d(TAG, "Toggling controls: $show")
        showControls.value = show
        seekBarVisible.value = show
    }

    fun play() {
        Log.d(TAG, "Starting playback")
        // Starts or resumes playback of the stream.
        player?.play()
    }

    fun pause() {
        Log.d(TAG, "Pausing playback")
        // Pauses playback of the stream.
        player?.pause()
    }

    fun playerRelease() {
        Log.d(TAG, "Releasing player")
        // Removes a playback state listener
        playerListener?.let { player?.removeListener(it) }
        // Releases the player instance
        player?.release()
        player = null
    }

    fun playerStart(surface: Surface) {
        Log.d(TAG, "Starting player")
        initPlayer()
        updateSurface(surface)
        setPlayerListener()
        playerLoadStream(Uri.parse(url.value))
        play()
    }

    fun playerLoadStream(uri: Uri) {
        Log.d(TAG, "Loading stream URI: $uri")
        // Loads the specified stream
        player?.load(uri)
    }

    fun updateSurface(surface: Surface?) {
        Log.d(TAG, "Updating player surface: $surface")
        // Sets the Surface to use for rendering video
        player?.setSurface(surface)
    }

    fun playerSeekTo(position: Long) {
        Log.d(TAG, "Updating player position: $position")
        // Seeks to a specified position in the stream, in milliseconds
        player?.seekTo(position)
        progress.value = player?.position?.timeString()
    }

    fun selectQuality(option: String) {
        Log.d(TAG, "Set player quality: $option")
        selectedQualityValue.value = option
        player?.qualities?.find { it.name == option }?.let { quality ->
            // Sets the quality of the stream.
            player?.quality = quality
        }
    }

    fun selectAuto() {
        // Enables automatic quality selection (ABR Adaptive bitrate)
        player?.isAutoQualityMode = true
        selectedQualityValue.value = Configuration.AUTO
    }

    fun getPlayerQualities() {
        val qualityList: MutableList<OptionDataItem> =
            mutableListOf(
                OptionDataItem(
                    Configuration.AUTO,
                    selectedQualityValue.value == Configuration.AUTO || selectedQualityValue.value == null
                )
            )
        val list = player?.qualities?.map {
            OptionDataItem(it.name, selectedQualityValue.value == it.name)
        } ?: listOf()
        qualityList.addAll(list)
        qualities.value = qualityList
    }

    fun getPlayBackRates(): List<OptionDataItem> {
        return Configuration.PlaybackRate.toMutableList().map {
            OptionDataItem(
                it,
                selectedRateValue.value == it || selectedQualityValue.value == Configuration.PLAYBACK_RATE_DEFAULT
            )
        }
    }

    fun setPlaybackRate(option: String) {
        Log.d(TAG, "Setting playback rate: $option")
        player?.playbackRate = option.toFloat()
        selectedRateValue.value = option
    }

    private fun getSources() {
        Log.d(TAG, "Collecting sources")
        launchMain {
            cacheProvider.sourcesDao().getAll().collect {
                val itemList: MutableList<SourceDataItem> = mutableListOf(
                    SourceDataItem(Configuration.LIVE_LANDSCAPE_LINK, Configuration.LIVE_OPTION),
                    SourceDataItem(Configuration.RECORDED_LANDSCAPE_LINK, Configuration.RECORDED_OPTION)
                )
                itemList.addAll(it)
                sources.value = itemList
            }
        }
    }

    fun deleteSource(url: String) {
        Log.d(TAG, "Deleting source: $url")
        launchIO {
            cacheProvider.sourcesDao().delete(url)
        }
    }

    fun addSource(source: SourceDataItem) {
        Log.d(TAG, "Adding source: $source")
        launchIO {
            cacheProvider.sourcesDao().insert(source)
        }
    }

}
