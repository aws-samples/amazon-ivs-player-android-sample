package com.amazonaws.ivs.player.customui.common

import java.util.*

fun Long.timeString(): String {
    var totalSeconds: Int = this.toInt() / 1000
    val hours: Int = totalSeconds / 3600
    totalSeconds -= hours * 3600
    val minutes: Int = totalSeconds / 60
    totalSeconds -= minutes * 60
    return String.format(Locale.ENGLISH, "%02d:%02d:%02d", hours, minutes, totalSeconds)
}
