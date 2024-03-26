package com.amazonaws.ivs.player.quizdemo.activities.dialogs

import android.net.Uri
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.ivs.player.quizdemo.R
import com.amazonaws.ivs.player.quizdemo.activities.MainActivity
import com.amazonaws.ivs.player.quizdemo.activities.adapters.SourceOptionAdapter
import com.amazonaws.ivs.player.quizdemo.common.*
import com.amazonaws.ivs.player.quizdemo.data.entity.SourceDataItem
import com.amazonaws.ivs.player.quizdemo.viewModels.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior

class SourceDialog(
    private val activity: MainActivity,
    private val viewModel: MainViewModel
) : SourceOptionAdapter.PlayerOptionCallback {

    private val sourceMenu by lazy { BottomSheetBehavior.from(activity.findViewById(R.id.source_sheet)) }
    private val optionsAdapter by lazy { SourceOptionAdapter(this) }

    init {
        sourceMenu.isFitToContents = false
        initViews()
    }

    private fun initViews() {
        val sourceView = activity.findViewById<EditText>(R.id.source)
        val okBtn = activity.findViewById<View>(R.id.ok_btn)
        val closeBtn = activity.findViewById<View>(R.id.close_btn)

        okBtn.setOnClickListener {
            val selectedItem = sourceView.text.toString()
            val source = SourceDataItem(selectedItem, selectedItem)
            viewModel.addSource(source)
            onOptionClicked(selectedItem)
            dismiss()
        }

        closeBtn.setOnClickListener{
            dismiss()
        }

        sourceView.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val selectedItem = view.text.toString()
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
        activity.findViewById<RecyclerView>(R.id.option_list).apply {
            adapter = optionsAdapter
        }

        viewModel.sources.observe(activity) {
            optionsAdapter.items = it
        }
    }

    override fun onOptionClicked(url: String) {
        Log.d(Configuration.TAG,"Url selected $url")
        viewModel.url.value = url
        viewModel.playerLoadStream(Uri.parse(url))
        dismiss()
    }

    override fun onOptionDelete(url: String) {
        Log.d(Configuration.TAG,"Url deleted $url")
        viewModel.deleteSource(url)
    }
}
