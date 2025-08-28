package com.amazonaws.ivs.player.customui.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.amazonaws.ivs.player.customui.common.Configuration

@Entity(tableName = "source_table")
data class SourceDataItem(
    @PrimaryKey
    val url: String,
    val title: String = "",
    val isDefault: Boolean = false
) {
    fun isDefaultOption(): Boolean = (title == Configuration.LIVE_OPTION || title == Configuration.RECORDED_OPTION)
}
