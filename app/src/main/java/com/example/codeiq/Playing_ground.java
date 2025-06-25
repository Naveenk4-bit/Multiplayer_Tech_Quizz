package com.example.codeiq;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Playing_ground extends AppCompatActivity {

    private TextView tvQuestion, tvTimer;
    private RadioGroup radioGroup;
    private RadioButton rbOption1, rbOption2, rbOption3, rbOption4;
    private Button btnNext;
    private DatabaseReference quizRef, playerAnswerRef, finishedPlayersRef, playerRef;
    private String quizPin, playerName;
    private int correctAnswerIndex, selectedAnswerIndex, currentQuestionIndex = 0;
    private CountDownTimer countDownTimer;
    private long timerDuration = 60000; // Will be overwritten dynamically
    private List<DataSnapshot> questionList = new ArrayList<>();
    private boolean hasFinished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing_ground);

        tvQuestion = findViewById(R.id.tv_question);
        tvTimer = findViewById(R.id.tv_timer);
        radioGroup = findViewById(R.id.radio_group_answers);
        rbOption1 = findViewById(R.id.rb_option_1);
        rbOption2 = findViewById(R.id.rb_option_2);
        rbOption3 = findViewById(R.id.rb_option_3);
        rbOption4 = findViewById(R.id.rb_option_4);
        btnNext = findViewById(R.id.btn_next_submit);

        btnNext.setEnabled(false);

        quizPin = getIntent().getStringExtra("quizPin");
        playerName = getIntent().getStringExtra("playerName");

        quizRef = FirebaseDatabase.getInstance().getReference("quizzes").child(quizPin);
        playerAnswerRef = FirebaseDatabase.getInstance().getReference("quiz_results").child(quizPin).child("players").child(playerName).child("answers");
        finishedPlayersRef = FirebaseDatabase.getInstance().getReference("quiz_results").child(quizPin).child("finishedPlayers");
        playerRef = FirebaseDatabase.getInstance().getReference("quiz_results").child(quizPin).child("players").child(playerName);

        loadQuestions();
        fetchSelectedTimeAndStartTimer(); // Start dynamic timer

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> btnNext.setEnabled(true));
        btnNext.setOnClickListener(view -> handleNextOrSubmit());
    }

    private void fetchSelectedTimeAndStartTimer() {
        quizRef.child("selected_time").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String selectedTime = snapshot.getValue(String.class);
                timerDuration = parseTime(selectedTime) * 1000L; // convert to milliseconds
                startTimer(); // Now start the timer
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                timerDuration = 60000; // default to 60 sec if failed
                startTimer();
            }
        });
    }

    private int parseTime(String timeString) {
        try {
            if (timeString == null || timeString.isEmpty()) return 60;
            String[] parts = timeString.split(" ");
            int timeValue = Integer.parseInt(parts[0]);
            if (timeString.contains("min")) {
                timeValue *= 60; // convert min to sec
            }
            return timeValue;
        } catch (Exception e) {
            return 60; // default
        }
    }

    private void loadQuestions() {
        quizRef.child("questions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot questionSnap : snapshot.getChildren()) {
                    questionList.add(questionSnap);
                }
                if (!questionList.isEmpty()) {
                    loadQuestion(0);
                } else {
                    Toast.makeText(Playing_ground.this, "No questions found!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Playing_ground.this, "Error loading questions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadQuestion(int index) {
        if (index < questionList.size()) {
            DataSnapshot questionSnap = questionList.get(index);
            tvQuestion.setText(questionSnap.child("questionText").getValue(String.class));
            rbOption1.setText(questionSnap.child("answer1").getValue(String.class));
            rbOption2.setText(questionSnap.child("answer2").getValue(String.class));
            rbOption3.setText(questionSnap.child("answer3").getValue(String.class));
            rbOption4.setText(questionSnap.child("answer4").getValue(String.class));
            correctAnswerIndex = questionSnap.child("correctAnswerIndex").getValue(Integer.class);
            radioGroup.clearCheck();
        } else {
            submitQuiz();
        }
    }

    private void handleNextOrSubmit() {
        int selectedId = radioGroup.getCheckedRadioButtonId();
        if (selectedId == rbOption1.getId()) selectedAnswerIndex = 0;
        else if (selectedId == rbOption2.getId()) selectedAnswerIndex = 1;
        else if (selectedId == rbOption3.getId()) selectedAnswerIndex = 2;
        else if (selectedId == rbOption4.getId()) selectedAnswerIndex = 3;
        else {
            Toast.makeText(this, "Please select an answer!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isCorrect = (selectedAnswerIndex == correctAnswerIndex);
        long answerTime = System.currentTimeMillis();

        DatabaseReference questionRef = playerAnswerRef.child("q" + currentQuestionIndex);
        Map<String, Object> answerData = new HashMap<>();
        answerData.put("isCorrect", isCorrect);
        answerData.put("timeTaken", answerTime);

        questionRef.setValue(answerData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (currentQuestionIndex < questionList.size() - 1) {
                    currentQuestionIndex++;
                    loadQuestion(currentQuestionIndex);
                } else {
                    submitQuiz();
                }
            } else {
                Toast.makeText(Playing_ground.this, "Error saving answer!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitQuiz() {
        hasFinished = true;
        finishedPlayersRef.child(playerName).setValue("completed");
        Intent intent = new Intent(Playing_ground.this, ResultActivity.class);
        intent.putExtra("quizPin", quizPin);
        startActivity(intent);
        finish();
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timerDuration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (!hasFinished) {
                    tvTimer.setText("Time Left: " + millisUntilFinished / 1000 + "s");
                }
            }

            @Override
            public void onFinish() {
                if (!hasFinished) {
                    tvTimer.setText("Time's up!");
                    submitQuiz();
                }
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit Quiz")
                .setMessage("Are you sure you want to leave? You will be disqualified!")
                .setPositiveButton("Exit", (dialog, which) -> {
                    hasFinished = true;
                    finishedPlayersRef.child(playerName).setValue("left");
                    super.onBackPressed();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
