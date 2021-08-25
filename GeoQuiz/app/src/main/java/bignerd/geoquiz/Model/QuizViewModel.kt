package bignerd.geoquiz.Model

import android.util.Log
import androidx.lifecycle.ViewModel
import bignerd.geoquiz.R

private const val  TAG = "QuizViewModel"

class QuizViewModel:ViewModel() {
    init{
        Log.d(TAG, "ViewModel instance created")
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel instance is going top be destroyed")
    }

    var currentQuestion: Int = 0
    var counterAnwered: Int = 0
    var isCheater:Boolean = false

    val questions = listOf(
    Question(R.string.question_moscow, true),
    Question(R.string.question_saintPeter, true),
    Question(R.string.question_america, false)
    )

    fun getCurrentQuestion() =  questions.get(currentQuestion)
}