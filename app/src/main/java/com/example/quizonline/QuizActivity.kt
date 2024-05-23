package com.example.quizonline

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.quizonline.databinding.ActivityQuizBinding
import com.example.quizonline.databinding.ScoreDialogBinding
import com.example.quizonline.Difficulty

class QuizActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        var questionModelList: List<QuestionModel> = listOf()
        var time: String = ""
    }

    lateinit var binding: ActivityQuizBinding

    var currentQuestionIndex = 0
    val userAnswers = mutableMapOf<Int, String>() // Danh sách lưu câu trả lời của người dùng
    var score = 0
    lateinit var allQuestions: List<QuestionModel>

    // Số lượng câu hỏi cho mỗi mức độ khó
    var maxEasyQuestions = 3
    var maxMediumQuestions = 5
    var maxHardQuestions = 7

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            btn0.setOnClickListener(this@QuizActivity)
            btn1.setOnClickListener(this@QuizActivity)
            btn2.setOnClickListener(this@QuizActivity)
            btn3.setOnClickListener(this@QuizActivity)
            nextBtn.setOnClickListener(this@QuizActivity)
        }
        loadQuestions()
        startTimer()
    }

    private fun startTimer() {
        val totalTimeInMillis = time.toInt() * 60 * 1000L
        object : CountDownTimer(totalTimeInMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                binding.timerIndicatorTextview.text = String.format("%02d:%02d", minutes, remainingSeconds)
            }

            override fun onFinish() {
                // Khi hết thời gian, hoàn thành quiz
                finishQuiz()
            }
        }.start()
    }

    private fun loadQuestions() {
        val easyQuestions = questionModelList.filter { it.difficulty == Difficulty.EASY }.shuffled().take(maxEasyQuestions)
        val mediumQuestions = questionModelList.filter { it.difficulty == Difficulty.MEDIUM }.shuffled().take(maxMediumQuestions)
        val hardQuestions = questionModelList.filter { it.difficulty == Difficulty.HARD }.shuffled().take(maxHardQuestions)

        allQuestions = easyQuestions + mediumQuestions + hardQuestions

        if (currentQuestionIndex >= allQuestions.size) {
            finishQuiz()
            return
        }

        val currentQuestion = allQuestions[currentQuestionIndex]
        binding.apply {
            questionIndicatorTextview.text = "Question ${currentQuestionIndex + 1}/${allQuestions.size}"
            questionProgressIndicator.progress = ((currentQuestionIndex.toFloat() / allQuestions.size.toFloat()) * 100).toInt()
            questionTextview.text = currentQuestion.question
            val imageName = currentQuestion.image
            val imageResourceId = resources.getIdentifier(imageName, "drawable", packageName)
            if (imageResourceId != 0) {
                questionImage.setImageResource(imageResourceId)
            } else {
                questionImage.setImageResource(0) // Clear the image if there is none
            }
            btn0.text = currentQuestion.option[0]
            btn1.text = currentQuestion.option[1]
            btn2.text = currentQuestion.option[2]
            btn3.text = currentQuestion.option[3]
        }
    }


    override fun onClick(view: View?) {
        binding.apply {
            btn0.setBackgroundColor(getColor(R.color.gray))
            btn1.setBackgroundColor(getColor(R.color.gray))
            btn2.setBackgroundColor(getColor(R.color.gray))
            btn3.setBackgroundColor(getColor(R.color.gray))
        }

        val clickedBtn = view as Button
        if (clickedBtn.id == R.id.next_btn) {
            // Kiểm tra xem người dùng đã chọn câu trả lời hay chưa
            if (userAnswers.containsKey(currentQuestionIndex)) {
                // Nếu đã chọn, chuyển sang câu hỏi tiếp theo
                currentQuestionIndex++
                if (currentQuestionIndex < allQuestions.size) {
                    loadQuestions()
                } else {
                    finishQuiz()
                }
            } else {
                // Nếu chưa chọn, thông báo cho người dùng chọn trước khi chuyển sang câu hỏi tiếp theo
                Toast.makeText(applicationContext, "Please select answer to continue", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Người dùng chọn câu trả lời, lưu vào danh sách câu trả lời của người dùng
            val selectedAnswer = clickedBtn.text.toString()
            userAnswers[currentQuestionIndex] = selectedAnswer
            clickedBtn.setBackgroundColor(getColor(R.color.orange))
        }
    }
    private fun finishQuiz() {
        val totalQuestions = allQuestions.size
        var score = 0 // Khởi tạo biến score ở đây để đảm bảo tính toàn vẹn trong mỗi lần kết thúc quiz

        val dialogBinding = ScoreDialogBinding.inflate(layoutInflater)
        val resultStringBuilder = StringBuilder()

        for (i in 0 until totalQuestions) {
            val question = allQuestions[i]
            val userAnswer = userAnswers[i]
            val isCorrect = userAnswer == question.correct
            resultStringBuilder.append("Question ${i + 1}: ")
            if (isCorrect) {
                resultStringBuilder.append("Correct\n")
                score++
            } else {
                resultStringBuilder.append("Fail\n")
            }
        }

        val percentage = ((score.toFloat() / totalQuestions.toFloat()) * 100).toInt() // Tính toán percentage ở đây

        dialogBinding.apply {
            scoreProgressIndicator.progress = percentage
            scoreProgressText.text = "$percentage%" // Thêm dấu % vào sau giá trị của percentage

            if (percentage > 60) {
                scoreTitle.text = "Congrats! You have passed"
                scoreTitle.setTextColor(Color.BLUE)
            } else {
                scoreTitle.text = "Oops! You have failed"
                scoreTitle.setTextColor(Color.RED)
            }
            scoreSubtitle.text = "$score out of $totalQuestions are correct!"
            questionResultsText.text = resultStringBuilder.toString() // Hiển thị kết quả từng câu hỏi
            finishBtn.setOnClickListener {
                finish()
            }
        }

        AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .show()
    }
}
