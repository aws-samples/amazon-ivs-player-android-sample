package com.amazonaws.ivs.player.quizdemo.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.amazonaws.ivs.player.quizdemo.common.Configuration

@Entity(tableName = "source_table")
data class SourceDataItem(
    @PrimaryKey
    val url: String,
    val title: String = "",
    val isDefault: Boolean = false
) {
    fun isDefaultOption(): Boolean = (title == Configuration.DEFAULT) || (title == Configuration.PORTRAIT_OPTION)
}
