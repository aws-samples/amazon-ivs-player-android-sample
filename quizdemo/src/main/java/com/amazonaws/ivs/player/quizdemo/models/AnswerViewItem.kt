package com.amazonaws.ivs.player.quizdemo.models

data class AnswerViewItem(
    val answer: String,
    val isCorrect: Boolean,
    var isSelected: Boolean = false,
    var isAnsweredCorrect: Boolean = false
)
