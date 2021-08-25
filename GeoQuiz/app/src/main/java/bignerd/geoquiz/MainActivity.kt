package bignerd.geoquiz

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import bignerd.geoquiz.Model.QuizViewModel
import java.lang.Math.abs
import java.math.BigDecimal

private const val TAG = "MainActivity"
private const val KEY_INDEX = "index"
private const val REQUEST_CODE_CHEAT = 0

class MainActivity : AppCompatActivity() {
    lateinit var questionText: TextView
    lateinit var trueButton: Button
    lateinit var falseButton: Button
    lateinit var nextButton: ImageButton
    lateinit var cheatButton: Button
    lateinit var previousButton: ImageButton
//    lateinit var questions: List<Question>
//    var currentQuestion: Int = 0
//    var counterAnwered: Int = 0

    private val quizViewModel: QuizViewModel by lazy {
        ViewModelProvider(this).get(QuizViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate")

//        val provider:ViewModelProvider = ViewModelProviders.of(this)
//        val quizViewModel = provider.get(QuizViewModel::class.java)
        Log.d(TAG, "Got a QuizViewModel: $quizViewModel")


        questionText = findViewById(R.id.questionText)
        trueButton = findViewById(R.id.true_button)
        falseButton = findViewById(R.id.false_button)
        nextButton = findViewById(R.id.nextButton)
        previousButton = findViewById(R.id.previousButton)
        cheatButton = findViewById(R.id.cheat_button)
//        questions = listOf(
//            Question(R.string.question_moscow, true),
//            Question(R.string.question_saintPeter, true),
//            Question(R.string.question_america, false)
//        )
        quizViewModel.currentQuestion = savedInstanceState?.getInt(KEY_INDEX, 0) ?: 0
        Log.d(TAG, "FROM SAVED INSTANCE: ${savedInstanceState?.getInt(KEY_INDEX, 0) ?: 0}")
        checkCurrentPosition()
        refreshText()

        trueButton.setOnClickListener { view: View ->
            checkAnswer(true)
        }

        falseButton.setOnClickListener { view: View ->
            checkAnswer(false)
        }

        nextButton.setOnClickListener { view: View ->
            quizViewModel.currentQuestion =
                (quizViewModel.currentQuestion + 1) % quizViewModel.questions.size
            checkCurrentPosition()
            refreshText();
        }

        previousButton.setOnClickListener { view: View ->
            quizViewModel.currentQuestion =
                abs(quizViewModel.currentQuestion - 1) % quizViewModel.questions.size
            checkCurrentPosition()
            refreshText()
        }

        cheatButton.setOnClickListener { view: View ->
//            startActivity(Intent(this,CheatActivity::class.java))
//            startActivity(CheatActivity.newIntent(this@MainActivity,quizViewModel.getCurrentQuestion().answer))
            startActivityForResult(
                CheatActivity.newIntent(
                    this@MainActivity,
                    quizViewModel.getCurrentQuestion().answer
                ),
                REQUEST_CODE_CHEAT,ActivityOptions.makeClipRevealAnimation(view,0,0,view.width,view.height).toBundle()
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK)
            return
        if (requestCode == REQUEST_CODE_CHEAT)
            quizViewModel.isCheater = data?.getBooleanExtra(EXTRA_ANSWER_SHOWN,false) ?: false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, "Saving instance")
        outState.putInt(KEY_INDEX, quizViewModel.currentQuestion)
    }


    fun checkCurrentPosition() {
        if (quizViewModel.currentQuestion <= 0)
            previousButton.visibility = View.GONE;
        else
            previousButton.visibility = View.VISIBLE;
        if (quizViewModel.currentQuestion >= quizViewModel.questions.size - 1)
            nextButton.visibility = View.GONE;
        else
            nextButton.visibility = View.VISIBLE;
        checkIsAnswered()
    }

    fun checkIsAnswered() {
        if (quizViewModel.questions.get(quizViewModel.currentQuestion).isAnswered) {
            trueButton.visibility = View.GONE
            falseButton.visibility = View.GONE
        } else {
            trueButton.visibility = View.VISIBLE
            falseButton.visibility = View.VISIBLE
        }
    }

    fun refreshText() {
        questionText.setText(quizViewModel.getCurrentQuestion().textID)
    }

    fun checkAnswer(answer: Boolean) {
        val messageResId = when{
            quizViewModel.isCheater -> R.string.judgment_toast
            answer == quizViewModel.getCurrentQuestion().answer -> {quizViewModel.getCurrentQuestion().isCorrect = true
                R.string.correct_answer}
            else -> R.string.wrong_answer
        }
        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show()

        quizViewModel.getCurrentQuestion().isAnswered = true
        checkIsAnswered()
        quizViewModel.counterAnwered++
        if (quizViewModel.counterAnwered == quizViewModel.questions.size) {
            Log.d(TAG, "ALL ANSWERED")
            Toast.makeText(
                this,
                "You answered all questions! \n" +
                        "Your score is: ${
                            (quizViewModel.questions.count { e -> e.isCorrect }
                                .toDouble() / quizViewModel.questions.size * 100).toBigDecimal()
                                .setScale(
                                    3,
                                    BigDecimal.ROUND_HALF_DOWN
                                )
                        } %",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }
}