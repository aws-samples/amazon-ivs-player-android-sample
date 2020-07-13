package com.amazonaws.ivs.player.quizdemo.activities

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.amazonaws.ivs.player.ViewUtil
import com.amazonaws.ivs.player.quizdemo.App
import com.amazonaws.ivs.player.quizdemo.R
import com.amazonaws.ivs.player.quizdemo.activities.adapters.AnswerAdapter
import com.amazonaws.ivs.player.quizdemo.activities.dialogs.SourceDialog
import com.amazonaws.ivs.player.quizdemo.common.*
import com.amazonaws.ivs.player.quizdemo.common.Configuration.TAG
import com.amazonaws.ivs.player.quizdemo.data.LocalCacheProvider
import com.amazonaws.ivs.player.quizdemo.databinding.ActivityMainBinding
import com.amazonaws.ivs.player.quizdemo.viewModels.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(), SurfaceHolder.Callback {

    @Inject
    lateinit var cacheProvider: LocalCacheProvider

    private val sourceDialog by lazy { SourceDialog(this, viewModel) }
    private val answerAdapter by lazy {
        AnswerAdapter { position ->
            viewModel.checkAnswer(position)
        }
    }

    private val viewModel: MainViewModel by lazyViewModel {
        MainViewModel(application, cacheProvider)
    }

    val sheetListener by lazy {
        object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                sheetBackground.visibility = View.VISIBLE
                sheetBackground.alpha = slideOffset
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN || newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    sheetBackground.visibility = View.GONE
                    sheetBackground.alpha = 0f
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        changeQuizMargin(newConfig)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main).apply {
            data = viewModel
            lifecycleOwner = this@MainActivity
        }

        viewModel.playerParamsChanged.observe(this, Observer {
            Log.d(TAG, "Player layout params changed ${it.first} ${it.second}")
            ViewUtil.setLayoutParams(surface_view, it.first, it.second)
        })

        viewModel.errorHappened.observe(this, Observer {
            Log.d(TAG, "Error dialog is shown")
            showDialog(it.first, it.second)
        })

        viewModel.showQuestions.observe(this, Observer {
            if (it == true) {
                quiz_root.fadeIn()
            } else {
                if (viewModel.questionChanged.value == false) {
                    quiz_root.fadeOut()
                }
            }
        })

        initUi()
        viewModel.playerStart(surface_view.holder.surface)
    }

    override fun onBackPressed() {
        when {
            sourceDialog.isOpened() -> sourceDialog.dismiss()
            else -> super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.play()
    }

    override fun onPause() {
        super.onPause()
        viewModel.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.release()
        sourceDialog.release()
        surface_view.holder.removeCallback(this)
    }

    private fun changeQuizMargin(newConfig: Configuration) {
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            quiz_root.setBottomMargin(resources.getDimension(R.dimen.quiz_root_portrait_margin).toInt())
        } else {
            quiz_root.setBottomMargin(resources.getDimension(R.dimen.quiz_root_landscape_margin).toInt())
        }
    }

    private fun initUi() {
        surface_view.holder.addCallback(this)
        changeQuizMargin(resources.configuration)

        answer_view.apply {
            adapter = answerAdapter
        }

        viewModel.answers.observe(this, Observer { items ->
            Log.d(TAG, "Answers updated: $items")
            answerAdapter.items = items
        })

        sheetBackground.setOnClickListener {
            sourceDialog.dismiss()
        }

        source_button.setOnClickListener {
            sourceDialog.show()
        }

        // Surface view listener for rotation handling
        surface_view.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            val width = viewModel.playerParamsChanged.value?.first
            val height = viewModel.playerParamsChanged.value?.second
            surface_view.post {
                if (width != null && height != null) {
                    Log.d(TAG, "On rotation player layout params changed $width $height")
                    ViewUtil.setLayoutParams(surface_view, width, height)
                }
            }
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        /* Ignored */
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        Log.d(TAG, "Surface destroyed")
        viewModel.updateSurface(null)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        Log.d(TAG, "Surface created")
        viewModel.updateSurface(holder?.surface)
    }

}
