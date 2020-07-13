package com.amazonaws.ivs.player.customui.common

import android.app.Activity
import android.app.Dialog
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import com.amazonaws.ivs.player.customui.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.view_dialog.*

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
    ContextCompat.getSystemService(this, InputMethodManager::class.java)?.hideSoftInputFromWindow(token, 0)
}

fun Activity.showDialog(title: String, message: String) {
    val dialog = Dialog(this)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCancelable(false)
    dialog.setContentView(R.layout.view_dialog)
    dialog.title.text = getString(R.string.error_happened_template, title)
    dialog.message.text = message
    dialog.dismiss_btn.setOnClickListener {
        dialog.dismiss()
    }
    dialog.show()
}
