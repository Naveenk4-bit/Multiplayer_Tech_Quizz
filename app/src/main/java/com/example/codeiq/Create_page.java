package com.example.codeiq;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

public class Create_page extends AppCompatActivity {

    private EditText etQuestion, etAnswer1, etAnswer2, etAnswer3, etAnswer4;
    private Spinner spinnerCorrectAnswer;
    private Button btnAddQuestion, btnSaveQuiz;
    private String quizPin; // Stores the generated quiz PIN
    private DatabaseReference quizRef; // Reference to Firebase Realtime Database
    private List<Question> questions; // Stores list of questions
    private boolean isPinGenerated = false; // Flag to track PIN generation

    // Inner class representing a Quiz object
    public class Quiz {
        public String quizTitle;
        public List<Question> questions;
        public String status;

        public Quiz() {
            // Default constructor required for Firebase
        }

        public Quiz(String quizTitle, List<Question> questions, String status) {
            this.quizTitle = quizTitle;
            this.questions = questions;
            this.status = status;
        }
    }

    // Inner class representing a Question object
    public class Question {
        public String questionText;
        public String answer1, answer2, answer3, answer4;
        public int correctAnswerIndex;

        public Question() {
            // Default constructor required for Firebase
        }

        public Question(String questionText, String answer1, String answer2, String answer3, String answer4, int correctAnswerIndex) {
            this.questionText = questionText;
            this.answer1 = answer1;
            this.answer2 = answer2;
            this.answer3 = answer3;
            this.answer4 = answer4;
            this.correctAnswerIndex = correctAnswerIndex;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_page);

        // Initializing UI elements
        etQuestion = findViewById(R.id.et_question);
        etAnswer1 = findViewById(R.id.et_answer_1);
        etAnswer2 = findViewById(R.id.et_answer_2);
        etAnswer3 = findViewById(R.id.et_answer_3);
        etAnswer4 = findViewById(R.id.et_answer_4);
        spinnerCorrectAnswer = findViewById(R.id.spinner_correct_answer);
        btnAddQuestion = findViewById(R.id.btn_add_question);
        btnSaveQuiz = findViewById(R.id.btn_save_quiz);

        // Retrieving existing quiz PIN if provided, otherwise generate a new one
        quizPin = getIntent().getStringExtra("quizPin");
        if (quizPin == null) {
            generateUniqueQuizPin();
        }

        questions = new ArrayList<>();

        btnAddQuestion.setOnClickListener(v -> addQuestion()); // Add question button
        btnSaveQuiz.setOnClickListener(v -> saveQuiz()); // Save quiz button

        setupSpinner(); // Setup answer choice dropdown
    }

    // Sets up the spinner dropdown for selecting the correct answer
    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Answer 1", "Answer 2", "Answer 3", "Answer 4"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCorrectAnswer.setAdapter(adapter);
    }

    // Generates a unique 4-digit quiz PIN and ensures it does not exist in Firebase
    private void generateUniqueQuizPin() {
        DatabaseReference pinRef = FirebaseDatabase.getInstance().getReference("quizzes");
        int generatedPin = 1000 + (int) (Math.random() * 9000); // Generates a random 4-digit PIN
        String pinString = String.valueOf(generatedPin);

        pinRef.child(pinString).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    quizPin = pinString;
                    isPinGenerated = true; // Mark PIN as generated
                } else {
                    generateUniqueQuizPin(); // Retry if PIN already exists
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(Create_page.this, "Error generating PIN", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Adds a question to the list
    private void addQuestion() {
        String questionText = etQuestion.getText().toString().trim();
        String answer1 = etAnswer1.getText().toString().trim();
        String answer2 = etAnswer2.getText().toString().trim();
        String answer3 = etAnswer3.getText().toString().trim();
        String answer4 = etAnswer4.getText().toString().trim();
        int correctAnswerIndex = spinnerCorrectAnswer.getSelectedItemPosition();

        if (TextUtils.isEmpty(questionText) || TextUtils.isEmpty(answer1) || TextUtils.isEmpty(answer2) ||
                TextUtils.isEmpty(answer3) || TextUtils.isEmpty(answer4)) {
            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create question object and add it to the list
        Question question = new Question(questionText, answer1, answer2, answer3, answer4, correctAnswerIndex);
        questions.add(question);

        // Clear input fields
        etQuestion.setText("");
        etAnswer1.setText("");
        etAnswer2.setText("");
        etAnswer3.setText("");
        etAnswer4.setText("");

        Toast.makeText(this, "Question Added!", Toast.LENGTH_SHORT).show();
    }

    // Saves the quiz to Firebase
    private void saveQuiz() {
        if (questions.isEmpty()) {
            Toast.makeText(this, "No questions added!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isPinGenerated) {
            Toast.makeText(this, "Generating quiz PIN, please wait...", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(this::saveQuiz, 1000); // Retry after 1 second
            return;
        }

        Quiz quiz = new Quiz("Sample Quiz", questions, "ready");
        quizRef = FirebaseDatabase.getInstance().getReference("quizzes").child(quizPin);

        quizRef.setValue(quiz)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Quiz Saved! PIN: " + quizPin, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Create_page.this, Lobby.class);
                        intent.putExtra("quizPin", quizPin);
                        intent.putExtra("isHost", true);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to Save Quiz!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
