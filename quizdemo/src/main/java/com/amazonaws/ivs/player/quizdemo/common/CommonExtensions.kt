package com.amazonaws.ivs.player.quizdemo.common

import android.app.Activity
import android.app.Dialog
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AlphaAnimation
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import com.amazonaws.ivs.player.quizdemo.R
import com.amazonaws.ivs.player.quizdemo.databinding.ViewDialogBinding
import com.amazonaws.ivs.player.quizdemo.models.AnswerViewItem
import com.amazonaws.ivs.player.quizdemo.models.QuestionModel
import com.google.android.material.bottomsheet.BottomSheetBehavior


fun BottomSheetBehavior<View>.isOpened() =
    state == BottomSheetBehavior.STATE_EXPANDED || state == BottomSheetBehavior.STATE_HALF_EXPANDED

fun BottomSheetBehavior<View>.open() {
    if (!isOpened()) {
        state = BottomSheetBehavior.STATE_EXPANDED
    }
}

fun BottomSheetBehavior<View>.hide() {
    if (isOpened()) {
        state = BottomSheetBehavior.STATE_HIDDEN
    }
}

fun Activity.hideKeyboard() {
    val view = currentFocus ?: window.decorView
    val token = view.windowToken
    view.clearFocus()
    ContextCompat.getSystemService(this, InputMethodManager::class.java)
        ?.hideSoftInputFromWindow(token, 0)
}

fun Activity.showDialog(title: String, message: String) {
    val dialog = Dialog(this)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCancelable(false)

    val binding = ViewDialogBinding.inflate(layoutInflater)
    dialog.setContentView(binding.root)


    binding.title.text = getString(R.string.error_happened_template, title)
    binding.message.text = message
    binding.dismissBtn.setOnClickListener {
        dialog.dismiss()
    }
    dialog.show()
}

fun View.fadeIn() {
    if (this.visibility == View.INVISIBLE) {
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.interpolator = DecelerateInterpolator()
        fadeIn.duration = Configuration.ANIMATION_DURATION
        startAnimation(fadeIn)
        this.visibility = View.VISIBLE
    }
}

fun View.fadeOut() {
    if (this.visibility == View.VISIBLE) {
        val fadeOut = AlphaAnimation(1f, 0f)
        fadeOut.interpolator = DecelerateInterpolator()
        fadeOut.startOffset = Configuration.ANIMATION_DURATION
        fadeOut.duration = Configuration.ANIMATION_DURATION
        startAnimation(fadeOut)
        this.visibility = View.INVISIBLE
    }
}

fun QuestionModel.toAnswerList(): List<AnswerViewItem> =
    answers.mapIndexed { index, answer ->  AnswerViewItem(answer, correctIndex == index) }

fun View.setBottomMargin(margin: Int) {
    val params = this.layoutParams as ViewGroup.MarginLayoutParams
    params.bottomMargin = margin
    layoutParams = params
}
