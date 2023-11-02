package com.amazonaws.ivs.player.quizdemo.viewModels

import android.app.Application
import android.net.Uri
import android.util.Log
import android.view.Surface
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amazonaws.ivs.player.MediaType
import com.amazonaws.ivs.player.Player
import com.amazonaws.ivs.player.quizdemo.common.*
import com.amazonaws.ivs.player.quizdemo.common.Configuration.TAG
import com.amazonaws.ivs.player.quizdemo.data.LocalCacheProvider
import com.amazonaws.ivs.player.quizdemo.models.QuestionModel
import com.amazonaws.ivs.player.quizdemo.data.entity.SourceDataItem
import com.amazonaws.ivs.player.quizdemo.models.AnswerViewItem
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect

class MainViewModel(
    private val context: Application,
    private val cacheProvider: LocalCacheProvider
) : ViewModel() {

    private var player: Player? = null
    private var playerListener: Player.Listener? = null

    val liveStream = MutableLiveData<Boolean>()
    val showLabel = MutableLiveData<Boolean>()
    val url = MutableLiveData<String>()
    val buffering = MutableLiveData<Boolean>()
    val showQuestions = MutableLiveData<Boolean>()
    val playerParamsChanged = MutableLiveData<Pair<Int, Int>>()
    val errorHappened = MutableLiveData<Pair<String, String>>()
    val sources = MutableLiveData<List<SourceDataItem>>()
    val answers = MutableLiveData<List<AnswerViewItem>>()
    val question = MutableLiveData<String>()
    val questionChanged = MutableLiveData<Boolean>()

    init {
        url.value = Configuration.LINK
        initPlayer()
        getSources()
    }

    private fun initPlayer() {
        // Media player initialization
        player = Player.Factory.create(context)

        player?.setListener(
            onVideoSizeChanged = { width, height ->
                Log.d(TAG, "Video size changed: $width $height")
                playerParamsChanged.value = Pair(width, height)
            },
            onDurationChanged = { durationValue ->
                Log.d(TAG, "Duration changed: $durationValue")
                liveStream.value = !(player?.duration != null && player!!.duration > 0L)
            },
            onStateChanged = { state ->
                Log.d(TAG, "State changed: $state")
                when (state) {
                    Player.State.BUFFERING -> {
                        buffering.value = true
                        showLabel.value = false
                    }
                    else -> {
                        buffering.value = false
                        showLabel.value = true
                    }
                }
            },
            onMetadata = { data, buffer ->
                if (MediaType.TEXT_PLAIN == data) {
                    try {
                        // Get question data item from buffer
                        val questionModel = Gson().fromJson(
                            String(buffer.array(), Charsets.UTF_8),
                            QuestionModel::class.java
                        )
                        Log.d(TAG, "Received quiz data: $questionModel")
                        question.value = questionModel.question
                        answers.value = questionModel.toAnswerList()
                        showQuestions()
                    } catch (exception: Exception) {
                        Log.d(TAG, "Error happened: $exception")
                    }
                }
            },
            onError = { exception ->
                Log.d(TAG, "Error happened: $exception")
                errorHappened.value = Pair(exception.code.toString(), exception.errorMessage)
            }
        )
    }

    fun playerStart(surface: Surface) {
        Log.d(TAG, "Starting player")
        updateSurface(surface)
        playerLoadStream(Uri.parse(url.value))
        play()
    }

    fun playerLoadStream(uri: Uri) {
        Log.d(TAG, "Loading stream URI: $uri")
        // Loads the specified stream
        player?.load(uri)
        hideQuestions()
        player?.play()
    }

    fun updateSurface(surface: Surface?) {
        Log.d(TAG, "Updating player surface: $surface")
        // Sets the Surface to use for rendering video
        player?.setSurface(surface)
    }

    private fun showQuestions() = launchMain {
        questionChanged.value = true
        showQuestions.value = true
    }

    private fun hideQuestions() = launchMain {
        questionChanged.value = false
        showQuestions.value = false
    }

    fun checkAnswer(position: Int) = launchMain {
        // Check answers if no answer is already selected
        answers.value?.takeIf { it.find { answer -> answer.isSelected } == null }?.let { answerList ->
            Log.d(TAG,"Selecting answer")
            answers.value = answerList.apply {
                forEachIndexed { index, item ->
                    if (index == position || item.isCorrect) item.isSelected = true
                    item.isAnsweredCorrect = item.isCorrect
                }
            }
        }
        questionChanged.value = false
        if (showQuestions.value == true) {
            delay(Configuration.ANSWER_DELAY)
            showQuestions.value = questionChanged.value
        }
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

    fun release() {
        Log.d(TAG, "Releasing player")
        // Removes a playback state listener
        playerListener?.let { player?.removeListener(it) }
        // Releases the player instance
        player?.release()
        player = null
    }

    private fun getSources() {
        Log.d(TAG, "Collecting sources")
        launchMain {
            cacheProvider.sourcesDao().getAll().collect {
                val itemList: MutableList<SourceDataItem> = mutableListOf(
                    SourceDataItem(Configuration.LINK, Configuration.DEFAULT),
                    SourceDataItem(Configuration.LIVE_PORTRAIT_LINK, Configuration.PORTRAIT_OPTION)
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
