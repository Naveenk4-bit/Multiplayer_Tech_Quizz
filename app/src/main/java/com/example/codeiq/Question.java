package com.example.codeiq;

class Question {
    private String questionText;
    private String[] answers;
    private int correctAnswerIndex; // Store index (0-based)

    public Question() { }

    public Question(String questionText, String answer1, String answer2, String answer3, String answer4, int correctAnswerIndex) {
        this.questionText = questionText;
        this.answers = new String[]{answer1, answer2, answer3, answer4};
        this.correctAnswerIndex = correctAnswerIndex - 1; // Adjust for 0-based index
    }

    public String getQuestionText() {
        return questionText;
    }

    // Individual getters for each option to avoid "Cannot resolve method" error
    public String getOption1() {
        return answers.length > 0 ? answers[0] : "";
    }

    public String getOption2() {
        return answers.length > 1 ? answers[1] : "";
    }

    public String getOption3() {
        return answers.length > 2 ? answers[2] : "";
    }

    public String getOption4() {
        return answers.length > 3 ? answers[3] : "";
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    public String getCorrectAnswerText() {
        return getAnswer(correctAnswerIndex);
    }

    // Keep the original method for indexed access
    public String getAnswer(int index) {
        return (index >= 0 && index < answers.length) ? answers[index] : "";
    }
}
