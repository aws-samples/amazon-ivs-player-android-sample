package com.amazonaws.ivs.player.customui.activities.dialogs

import android.view.View
import androidx.lifecycle.Observer
import com.amazonaws.ivs.player.customui.R
import com.amazonaws.ivs.player.customui.activities.MainActivity
import com.amazonaws.ivs.player.customui.activities.adapters.PlayerOptionAdapter
import com.amazonaws.ivs.player.customui.common.hide
import com.amazonaws.ivs.player.customui.common.isOpened
import com.amazonaws.ivs.player.customui.common.open
import com.amazonaws.ivs.player.customui.viewModel.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
class RateDialog(
    private val activity: MainActivity,
    private val viewModel: MainViewModel
) : PlayerOptionAdapter.PlayerOptionCallback {

    private val rateMenu by lazy { BottomSheetBehavior.from(activity.findViewById<View>(R.id.rate_sheet)) }
    private val rateAdapter by lazy { PlayerOptionAdapter(this) }

    init {
        initViews()
    }

    private fun initViews() {

        viewModel.selectedRateValue.observe(activity, Observer {
            rateAdapter.items = viewModel.getPlayBackRates().toMutableList()
        })

        activity.binding.rateSheet.rateOptionList.apply {
            adapter = rateAdapter
        }

        rateAdapter.items = viewModel.getPlayBackRates().toMutableList()

        activity.binding.rateSheet.rateCloseBtn.setOnClickListener {
            dismiss()
        }

        rateMenu.addBottomSheetCallback(activity.sheetListener)
    }

    fun show() {
        rateMenu.open()
    }

    fun dismiss() {
        rateMenu.hide()
    }

    fun isOpened() = rateMenu.isOpened()

    fun release() {
        rateMenu.removeBottomSheetCallback(activity.sheetListener)
    }

    override fun onOptionClicked(position: Int) {
        viewModel.setPlaybackRate(rateAdapter.items[position].option)
        dismiss()
    }
}
