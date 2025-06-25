package com.example.codeiq;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UploadActivity extends AppCompatActivity {

    private static final int FILE_PICKER_REQUEST = 1;

    private Button buttonUploadFile, buttonHelp, buttonSave, buttonStartQuiz;
    private RecyclerView recyclerViewQuestions;
    private EditableQuestionAdapter adapter;
    private List<EditableQuestion> questionList = new ArrayList<>();
    private DatabaseReference databaseRef;
    private String quizPin;
    private boolean isPinGenerated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // Initialize Firebase
        databaseRef = FirebaseDatabase.getInstance().getReference("quizzes");

        // Initialize UI components
        buttonUploadFile = findViewById(R.id.button_upload_file);
        buttonHelp = findViewById(R.id.button_help);
        buttonSave = findViewById(R.id.button_save);
        buttonStartQuiz = findViewById(R.id.button_start_quiz);
        recyclerViewQuestions = findViewById(R.id.recycler_view_questions);

        // Setup RecyclerView
        recyclerViewQuestions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EditableQuestionAdapter(questionList);
        recyclerViewQuestions.setAdapter(adapter);

        // Upload Button Click - Open file picker
        buttonUploadFile.setOnClickListener(v -> openFilePicker());

        // Help Button Click - Show file format guide
        buttonHelp.setOnClickListener(v -> showHelpDialog());

        // Save Button Click - Save to Firebase
        buttonSave.setOnClickListener(v -> saveQuestionsToFirebase());

        // Start Quiz Button Click - Move to Lobby
        buttonStartQuiz.setOnClickListener(v -> {
            if (isPinGenerated) {
                Intent intent = new Intent(UploadActivity.this, Lobby.class);
                intent.putExtra("quizPin", quizPin);
                intent.putExtra("isHost", true);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Please save the quiz first!", Toast.LENGTH_SHORT).show();
            }
        });

        // Generate Unique Quiz PIN
        generateUniqueQuizPin();
    }

    // Open file picker to select TXT file
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/plain");
        startActivityForResult(intent, FILE_PICKER_REQUEST);
    }

    // Handle file selection result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_PICKER_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                readTxtFile(uri);
            }
        }
    }

    // Read and parse TXT file
    private void readTxtFile(Uri uri) {
        questionList.clear();

        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            List<String> questionData = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    questionData.add(line.trim());
                }

                if (questionData.size() == 6) { // Expecting: Question + 4 Options + Correct Answer Index
                    String questionText = questionData.get(0);
                    String option1 = questionData.get(1);
                    String option2 = questionData.get(2);
                    String option3 = questionData.get(3);
                    String option4 = questionData.get(4);
                    int correctAnswerIndex = Integer.parseInt(questionData.get(5));

                    questionList.add(new EditableQuestion(questionText, option1, option2, option3, option4, correctAnswerIndex));
                    questionData.clear(); // Reset for next question
                }
            }

            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Questions Loaded Successfully!", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Toast.makeText(this, "Error reading file!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // Generate a unique quiz PIN
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
                Toast.makeText(UploadActivity.this, "Error generating PIN", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Save questions to Firebase
    private void saveQuestionsToFirebase() {
        if (questionList.isEmpty()) {
            Toast.makeText(this, "No questions to save!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isPinGenerated) {
            Toast.makeText(this, "Generating quiz PIN, please wait...", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(this::saveQuestionsToFirebase, 1000);
            return;
        }

        DatabaseReference quizRef = databaseRef.child(quizPin);
        DatabaseReference questionsRef = quizRef.child("questions");

        for (int i = 0; i < questionList.size(); i++) {
            EditableQuestion question = questionList.get(i);
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
                    } else {
                        Toast.makeText(this, "Failed to Save Quiz!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Show Help Dialog with Image
    private void showHelpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_help, null);
        builder.setView(dialogView);

        // Find ImageView and set the guide image
        ImageView imageView = dialogView.findViewById(R.id.image_help);
        imageView.setImageResource(R.drawable.format_guide); // Make sure to place format_guide.png in res/drawable

        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
