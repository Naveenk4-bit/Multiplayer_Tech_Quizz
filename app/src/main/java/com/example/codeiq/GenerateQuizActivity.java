package com.example.codeiq;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GenerateQuizActivity extends AppCompatActivity {

    private RecyclerView recyclerViewQuestions;
    private List<Question> generatedQuestions = new ArrayList<>();
    private QuestionAdapter questionAdapter;
    private String selectedFile;
    private Button btnSaveQuiz;
    private DatabaseReference quizRef;
    private String quizPin;
    private boolean isPinGenerated = false; // Track if PIN is generated

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_quiz);

        recyclerViewQuestions = findViewById(R.id.recyclerView_questions);
        recyclerViewQuestions.setLayoutManager(new LinearLayoutManager(this));
        btnSaveQuiz = findViewById(R.id.btn_save_quiz);

        selectedFile = getIntent().getStringExtra("selectedFile");

        if (selectedFile != null && !selectedFile.isEmpty()) {
            fetchQuestionsFromCSV(selectedFile);
        } else {
            Toast.makeText(this, "No language selected!", Toast.LENGTH_SHORT).show();
        }

        // Generate a unique quiz PIN
        generateUniqueQuizPin();

        btnSaveQuiz.setOnClickListener(v -> saveQuiz());
    }

    private void fetchQuestionsFromCSV(String fileName) {
        generatedQuestions.clear();

        try {
            InputStream inputStream = getAssets().open(fileName);
            InputStreamReader streamReader = new InputStreamReader(inputStream);
            CSVReader reader = new CSVReader(streamReader);

            List<String[]> allRows = reader.readAll();
            reader.close();

            List<Question> allQuestions = new ArrayList<>();

            boolean firstLine = true;
            for (String[] data : allRows) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }

                if (data.length < 6) continue; // Ensure at least 6 columns

                String questionText = data[0];
                String option1 = data[1];
                String option2 = data[2];
                String option3 = data[3];
                String option4 = data[4];
                int correctIndex;

                try {
                    correctIndex = Integer.parseInt(data[5].trim());
                } catch (NumberFormatException e) {
                    correctIndex = 1;
                }

                allQuestions.add(new Question(questionText, option1, option2, option3, option4, correctIndex));
            }

            // Shuffle and pick 5 random questions
            Collections.shuffle(allQuestions, new Random());
            int numQuestions = Math.min(allQuestions.size(), 5);
            generatedQuestions.addAll(allQuestions.subList(0, numQuestions));

            questionAdapter = new QuestionAdapter(generatedQuestions);
            recyclerViewQuestions.setAdapter(questionAdapter);

        } catch (IOException | CsvException e) {
            Toast.makeText(this, "Error reading CSV file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void generateUniqueQuizPin() {
        DatabaseReference pinRef = FirebaseDatabase.getInstance().getReference("quizzes");
        int generatedPin = 1000 + new Random().nextInt(9000);
        String pinString = String.valueOf(generatedPin);

        pinRef.child(pinString).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    quizPin = pinString;
                    isPinGenerated = true;
                } else {
                    generateUniqueQuizPin(); // Retry if PIN exists
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(GenerateQuizActivity.this, "Error generating PIN", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveQuiz() {
        if (generatedQuestions.isEmpty()) {
            Toast.makeText(this, "No questions to save!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isPinGenerated) {
            Toast.makeText(this, "Generating quiz PIN, please wait...", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(this::saveQuiz, 1000);
            return;
        }

        quizRef = FirebaseDatabase.getInstance().getReference("quizzes").child(quizPin);
        DatabaseReference questionsRef = quizRef.child("questions");

        for (int i = 0; i < generatedQuestions.size(); i++) {
            Question question = generatedQuestions.get(i);
            DatabaseReference questionRef = questionsRef.child(String.valueOf(i));

            questionRef.child("questionText").setValue(question.getQuestionText());
            questionRef.child("answer1").setValue(question.getOption1());
            questionRef.child("answer2").setValue(question.getOption2());
            questionRef.child("answer3").setValue(question.getOption3());
            questionRef.child("answer4").setValue(question.getOption4());
            questionRef.child("correctAnswerIndex").setValue(question.getCorrectAnswerIndex());
        }

        quizRef.child("quizTitle").setValue("Tech Quiz");
        quizRef.child("status").setValue("ready")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Quiz Saved! PIN: " + quizPin, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(GenerateQuizActivity.this, Lobby.class);
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
