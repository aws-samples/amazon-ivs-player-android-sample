package com.amazonaws.ivs.player.customui.activities.dialogs

import androidx.lifecycle.Observer
import com.amazonaws.ivs.player.customui.activities.MainActivity
import com.amazonaws.ivs.player.customui.activities.adapters.PlayerOptionAdapter
import com.amazonaws.ivs.player.customui.common.Configuration
import com.amazonaws.ivs.player.customui.common.hide
import com.amazonaws.ivs.player.customui.common.isOpened
import com.amazonaws.ivs.player.customui.common.open
import com.amazonaws.ivs.player.customui.viewModel.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.player_quality_sheet.*

class QualityDialog(
    private val activity: MainActivity,
    private val viewModel: MainViewModel
) : PlayerOptionAdapter.PlayerOptionCallback {

    private val qualityMenu by lazy { BottomSheetBehavior.from(activity.quality_sheet) }
    private val qualityAdapter by lazy { PlayerOptionAdapter(this) }

    init {
        initViews()
    }

    private fun initViews() {

        viewModel.selectedQualityValue.observe(activity, Observer {
            viewModel.getPlayerQualities()
        })

        activity.quality_option_list.apply {
            adapter = qualityAdapter
        }

        viewModel.qualities.observe(activity, Observer {
            qualityAdapter.items = it
        })

        activity.quality_close_btn.setOnClickListener {
            dismiss()
        }

        qualityMenu.addBottomSheetCallback(activity.sheetListener)
    }

    fun show() {
        qualityMenu.open()
    }

    fun dismiss() {
        qualityMenu.hide()
    }

    fun isOpened() = qualityMenu.isOpened()

    fun release() {
        qualityMenu.removeBottomSheetCallback(activity.sheetListener)
    }

    override fun onOptionClicked(position: Int) {
        val option = qualityAdapter.items[position].option
        if (option == Configuration.AUTO) {
            viewModel.selectAuto()
        } else {
            viewModel.selectQuality(option)
        }
        dismiss()
    }
}
