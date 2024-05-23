package com.example.quizonline

data class QuizModel(
    val id: String,
    val title: String,
    val subtitle: String,
    val time: String,
    val questionList: List<QuestionModel>
) {
    constructor() : this("", "", "", "", emptyList())
}

data class QuestionModel(
    val question: String,
    val image: String,
    val option: List<String>,
    val correct: String,
    val difficulty: Difficulty // Thêm difficulty vào đây
) {
    constructor() : this("", "", emptyList(), "", Difficulty.EASY) // Khởi tạo mặc định là Difficulty.EASY
}

enum class Difficulty {
    EASY,
    MEDIUM,
    HARD
}
