package com.amazonaws.ivs.player.quizdemo.activities

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
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
                binding.sheetBackground.visibility = View.VISIBLE
                binding.sheetBackground.alpha = slideOffset
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN || newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    binding.sheetBackground.visibility = View.GONE
                    binding.sheetBackground.alpha = 0f
                }
            }
        }
    }

    lateinit var binding: ActivityMainBinding

    private var systemBarBottomInset = 0

    private fun setupWindowInsets() {
        val sourceButton = binding.sourceButton
        val liveView = binding.liveView
        val sourceSheet = binding.sourceSheet.root
        val sourceButtonParams = sourceButton.layoutParams as ViewGroup.MarginLayoutParams
        val liveParams = liveView.layoutParams as ViewGroup.MarginLayoutParams
        val sourceButtonTop = sourceButtonParams.topMargin
        val sourceButtonRight = sourceButtonParams.rightMargin
        val liveTop = liveParams.topMargin
        val liveLeft = liveParams.leftMargin
        val sheetTop = sourceSheet.paddingTop
        val sheetBottom = sourceSheet.paddingBottom
        findViewById<View>(android.R.id.content).doOnApplyWindowInsets { insets ->
            systemBarBottomInset = insets.bottom
            changeQuizMargin(resources.configuration)
            sourceButton.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = sourceButtonTop + insets.top
                rightMargin = sourceButtonRight + insets.right
            }
            liveView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = liveTop + insets.top
                leftMargin = liveLeft + insets.left
            }
            sourceSheet.updatePadding(top = sheetTop + insets.top, bottom = sheetBottom + insets.bottom)
        }
        BottomSheetBehavior.from(sourceSheet).state = BottomSheetBehavior.STATE_HIDDEN
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        changeQuizMargin(newConfig)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main).apply {
            binding = this
            data = viewModel
            lifecycleOwner = this@MainActivity
        }

        setupWindowInsets()

        viewModel.playerParamsChanged.observe(this, Observer {
            Log.d(TAG, "Player layout params changed ${it.first} ${it.second}")
            fitSurfaceToView(binding.surfaceView, it.first, it.second)
        })

        viewModel.errorHappened.observe(this, Observer {
            Log.d(TAG, "Error dialog is shown")
            showDialog(it.first, it.second)
        })

        viewModel.showQuestions.observe(this, Observer {
            if (it == true) {
                binding.quizRoot.fadeIn()
            } else {
                if (viewModel.questionChanged.value == false) {
                    binding.quizRoot.fadeOut()
                }
            }
        })

        initUi()
        viewModel.playerStart(binding.surfaceView.holder.surface)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    sourceDialog.isOpened() -> sourceDialog.dismiss()
                    else -> finish()
                }
            }
        })
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
        binding.surfaceView.holder.removeCallback(this)
    }

    private fun changeQuizMargin(newConfig: Configuration) {
        val baseMargin = if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            resources.getDimension(R.dimen.quiz_root_portrait_margin).toInt()
        } else {
            resources.getDimension(R.dimen.quiz_root_landscape_margin).toInt()
        }
        binding.quizRoot.setBottomMargin(baseMargin + systemBarBottomInset)
    }

    private fun initUi() {
        binding.surfaceView.holder.addCallback(this)
        changeQuizMargin(resources.configuration)

        binding.answerView.apply {
            adapter = answerAdapter
        }

        viewModel.answers.observe(this, Observer { items ->
            Log.d(TAG, "Answers updated: $items")
            answerAdapter.items = items
        })

        binding.sheetBackground.setOnClickListener {
            sourceDialog.dismiss()
        }

        binding.sourceButton.setOnClickListener {
            sourceDialog.show()
        }

        // Surface view listener for rotation handling
        binding.surfaceView.addOnLayoutChangeListener { _, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
                val width = viewModel.playerParamsChanged.value?.first
                val height = viewModel.playerParamsChanged.value?.second
                if (width != null && height != null) {
                    binding.surfaceView.post {
                        Log.d(TAG, "On rotation player layout params changed $width $height")
                        fitSurfaceToView(binding.surfaceView, width, height)
                    }
                }
            }
        }

    }

    private fun fitSurfaceToView(surfaceView: SurfaceView, width: Int, height: Int) {
        val parent = surfaceView.parent as View
        val oldWidth = parent.width
        val oldHeight = parent.height
        val newWidth: Int
        val newHeight: Int
        val ratio = height.toFloat() / width.toFloat()
        if (oldHeight.toFloat() > oldWidth.toFloat() * ratio) {
            newWidth = oldWidth
            newHeight = (oldWidth.toFloat() * ratio).toInt()
        } else {
            newWidth = (oldHeight.toFloat() / ratio).toInt()
            newHeight = oldHeight
        }
        val layoutParams = surfaceView.layoutParams
        layoutParams.width = newWidth
        layoutParams.height = newHeight
        surfaceView.layoutParams = layoutParams
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        /* Ignored */
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d(TAG, "Surface destroyed")
        viewModel.updateSurface(null)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d(TAG, "Surface created")
        viewModel.updateSurface(holder.surface)
    }

}
