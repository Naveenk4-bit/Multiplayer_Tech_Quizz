package com.example.codeiq;

class EditableQuestion {
    private String questionText;
    private String[] answers;
    private int correctAnswerIndex; // Store index (0-based)

    public EditableQuestion() { }

    public EditableQuestion(String questionText, String answer1, String answer2, String answer3, String answer4, int correctAnswerIndex) {
        this.questionText = questionText;
        this.answers = new String[]{answer1, answer2, answer3, answer4};
        this.correctAnswerIndex = correctAnswerIndex; // No need for -1 adjustment
    }

    // Getters
    public String getQuestionText() {
        return questionText;
    }

    public String getOption1() {
        return answers[0];
    }

    public String getOption2() {
        return answers[1];
    }

    public String getOption3() {
        return answers[2];
    }

    public String getOption4() {
        return answers[3];
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    public String getCorrectAnswerText() {
        return answers[correctAnswerIndex];
    }

    // **Setters** (For editing questions)
    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public void setOption1(String option) {
        this.answers[0] = option;
    }

    public void setOption2(String option) {
        this.answers[1] = option;
    }

    public void setOption3(String option) {
        this.answers[2] = option;
    }

    public void setOption4(String option) {
        this.answers[3] = option;
    }

    public void setCorrectAnswerIndex(int correctAnswerIndex) {
        this.correctAnswerIndex = correctAnswerIndex;
    }
}
