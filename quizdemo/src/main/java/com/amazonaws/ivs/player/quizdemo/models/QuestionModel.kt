package com.amazonaws.ivs.player.quizdemo.models

data class QuestionModel(
    val question: String,
    val answers: List<String>,
    val correctIndex: Int
)
