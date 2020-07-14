package com.amazonaws.ivs.player.quizdemo.common

import android.util.Log
import com.amazonaws.ivs.player.Cue
import com.amazonaws.ivs.player.Player
import com.amazonaws.ivs.player.PlayerException
import com.amazonaws.ivs.player.Quality
import kotlinx.coroutines.*
import java.nio.ByteBuffer

private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

fun launchIO(block: suspend CoroutineScope.() -> Unit) = ioScope.launch(
    context = CoroutineExceptionHandler { _, e -> Log.d(Configuration.TAG,"Coroutine failed ${e.localizedMessage}") },
    block = block
)

fun launchMain(block: suspend CoroutineScope.() -> Unit) = mainScope.launch(
    context = CoroutineExceptionHandler { _, e -> Log.d(Configuration.TAG,"Coroutine failed ${e.localizedMessage}") },
    block = block
)

/**
 * Player listener extension function
 */
inline fun Player.setListener(
    crossinline onAnalyticsEvent: (key: String, value: String) -> Unit = { _,_ -> Unit },
    crossinline onRebuffering: () -> Unit = {},
    crossinline onSeekCompleted: (value: Long) -> Unit = { _ -> Unit },
    crossinline onQualityChanged: (quality: Quality) -> Unit = { _ -> Unit },
    crossinline onVideoSizeChanged: (width: Int, height: Int) -> Unit = { _,_ -> Unit },
    crossinline onCue: (cue: Cue) -> Unit = { _ -> Unit },
    crossinline onDurationChanged: (duration: Long) -> Unit = { _ -> Unit },
    crossinline onStateChanged: (state: Player.State) -> Unit = { _ -> Unit },
    crossinline onError: (exception: PlayerException) -> Unit = { _ -> Unit },
    crossinline onMetadata: (data: String, buffer: ByteBuffer) -> Unit = { _,_ -> Unit }
): Player.Listener {
    val listener = playerListener(
        onAnalyticsEvent, onRebuffering, onSeekCompleted, onQualityChanged, onVideoSizeChanged,
        onCue, onDurationChanged, onStateChanged, onError, onMetadata
    )

    addListener(listener)
    return listener
}

/**
 * Player.Listener provides an implementation of this interface to addListener(Listener) to receive events from a Player instance.
 */
inline fun playerListener(
    crossinline onAnalyticsEvent: (key: String, value: String) -> Unit = { _,_ -> Unit },
    crossinline onRebuffering: () -> Unit = {},
    crossinline onSeekCompleted: (value: Long) -> Unit = { _ -> Unit },
    crossinline onQualityChanged: (quality: Quality) -> Unit = { _ -> Unit },
    crossinline onVideoSizeChanged: (width: Int, height: Int) -> Unit = { _,_ -> Unit },
    crossinline onCue: (cue: Cue) -> Unit = { _ -> Unit },
    crossinline onDurationChanged: (duration: Long) -> Unit = { _ -> Unit },
    crossinline onStateChanged: (state: Player.State) -> Unit = { _ -> Unit },
    crossinline onError: (exception: PlayerException) -> Unit = { _ -> Unit },
    crossinline onMetadata: (data: String, buffer: ByteBuffer) -> Unit = { _,_ -> Unit }
): Player.Listener = object : Player.Listener() {
    // Indicates that a video analytics tracking event occurred.
    override fun onAnalyticsEvent(key: String, value: String) = onAnalyticsEvent(key, value)
    // Indicates that the player is buffering from a previous PLAYING state.
    override fun onRebuffering() = onRebuffering()
    // Indicates that the player has seeked to a given position as requested from seekTo(long).
    override fun onSeekCompleted(value: Long) = onSeekCompleted(value)
    // Indicates that the playing quality changed either from a user action or from an internal adaptive quality switch.
    override fun onQualityChanged(quality: Quality) = onQualityChanged(quality)
    // Indicates that the video dimensions changed.
    override fun onVideoSizeChanged(width: Int, height: Int) = onVideoSizeChanged(width, height)
    // Indicates that a timed cue was received.
    override fun onCue(cue: Cue) = onCue(cue)
    // Indicates that source duration changed
    override fun onDurationChanged(duration: Long) = onDurationChanged(duration)
    // Indicates that the player state changed.
    override fun onStateChanged(state: Player.State) = onStateChanged(state)
    // Indicates that an error occurred.
    override fun onError(exception: PlayerException) = onError(exception)
    // Indicates that a metadata event occurred.
    override fun onMetadata(data: String, buffer: ByteBuffer) = onMetadata(data, buffer)
}
