package com.amazonaws.ivs.player.customui.activities.dialogs

import android.net.Uri
import android.util.Log
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.Observer
import com.amazonaws.ivs.player.customui.activities.MainActivity
import com.amazonaws.ivs.player.customui.activities.adapters.SourceOptionAdapter
import com.amazonaws.ivs.player.customui.common.*
import com.amazonaws.ivs.player.customui.data.entity.SourceDataItem
import com.amazonaws.ivs.player.customui.viewModel.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.player_source_sheet.*

class SourceDialog(
    private val activity: MainActivity,
    private val viewModel: MainViewModel
) : SourceOptionAdapter.PlayerOptionCallback {

    private val sourceMenu by lazy { BottomSheetBehavior.from(activity.source_sheet) }
    private val optionsAdapter by lazy { SourceOptionAdapter(this) }

    init {
        sourceMenu.isFitToContents = false
        initViews()
    }

    private fun initViews() {

        activity.ok_btn.setOnClickListener {
            val selectedItem = activity.source.text.toString()
            val source = SourceDataItem(selectedItem, selectedItem)
            if (selectedItem.isNotEmpty()) {
                viewModel.addSource(source)
                onOptionClicked(selectedItem)
                dismiss()
            }
        }

        activity.close_btn.setOnClickListener {
            dismiss()
        }

        activity.source.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val selectedItem = activity.source.text.toString()
                val source = SourceDataItem(selectedItem, selectedItem)
                viewModel.addSource(source)
                onOptionClicked(selectedItem)
                dismiss()
            }
            false
        }

        initAdapter()

        sourceMenu.addBottomSheetCallback(activity.sheetListener)
    }

    fun show() {
        sourceMenu.open()
    }

    fun dismiss() {
        sourceMenu.hide()
        activity.hideKeyboard()
    }

    fun isOpened() = sourceMenu.isOpened()

    fun release() {
        sourceMenu.removeBottomSheetCallback(activity.sheetListener)
    }

    private fun initAdapter() {
        // Default option list
        activity.option_list.apply {
            adapter = optionsAdapter
        }

        viewModel.sources.observe(activity, Observer {
            optionsAdapter.items = it
        })
    }

    override fun onOptionClicked(url: String) {
        Log.d(Configuration.TAG, "Url selected $url")
        viewModel.playerLoadStream(Uri.parse(url))
        viewModel.play()
        dismiss()
    }

    override fun onOptionDelete(url: String) {
        Log.d(Configuration.TAG, "Url deleted $url")
        viewModel.deleteSource(url)
    }
}
