package bignerd.geoquiz.Model

data class Question(val textID: Int, val answer: Boolean, var isAnswered: Boolean = false, var isCorrect:Boolean = false)
