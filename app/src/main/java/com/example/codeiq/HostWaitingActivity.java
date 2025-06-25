package com.example.codeiq;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HostWaitingActivity extends AppCompatActivity {
    private TextView tvTimer;
    private ProgressBar progressBar;
    private DatabaseReference quizRef;
    private String quizPin;
    private int selectedTime = 60; // Default 60 seconds
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_waiting);

        // UI Elements
        tvTimer = findViewById(R.id.tv_timer);
        progressBar = findViewById(R.id.progress_bar);

        // Get Quiz PIN from Intent
        quizPin = getIntent().getStringExtra("quizPin");
        quizRef = FirebaseDatabase.getInstance().getReference("quizzes").child(quizPin);

        // Fetch the selected time from Firebase
        quizRef.child("selected_time").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String timeString = snapshot.getValue(String.class);
                    selectedTime = parseTime(timeString); // Extract the numeric value
                }
                startCountdown(selectedTime); // Start countdown after fetching time
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                startCountdown(selectedTime); // Start default countdown if error occurs
            }
        });

        // Check quiz status in Firebase
        checkQuizStatus();
    }

    // Function to extract number from time string
    private int parseTime(String timeString) {
        try {
            if (timeString == null || timeString.isEmpty()) return 60; // Default 60 sec
            String[] parts = timeString.split(" ");
            int timeValue = Integer.parseInt(parts[0]); // Extract the number

            // Convert minutes to seconds if needed
            if (timeString.contains("min")) {
                timeValue *= 60; // Convert minutes to seconds
            }
            return timeValue;
        } catch (Exception e) {
            return 60; // Default 60 sec if parsing fails
        }
    }

    // Function to start countdown
    private void startCountdown(int durationInSeconds) {
        progressBar.setMax(durationInSeconds);

        countDownTimer = new CountDownTimer(durationInSeconds * 1000L, 1000) {
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000);
                tvTimer.setText("Time Left: " + secondsLeft + "s");
                progressBar.setProgress(secondsLeft);
            }

            public void onFinish() {
                tvTimer.setText("Time Over!");
                moveToResultActivity();  // Move host to result page when time is over
            }
        }.start();
    }

    // Function to check the quiz status and move to result when completed
    private void checkQuizStatus() {
        quizRef.child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status = snapshot.getValue(String.class);
                if ("completed".equals(status)) {
                    countDownTimer.cancel(); // Stop the countdown if quiz is completed early
                    moveToResultActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });
    }

    // Function to move to the result activity
    private void moveToResultActivity() {
        Intent intent = new Intent(HostWaitingActivity.this, ResultActivity.class);
        intent.putExtra("quizPin", quizPin);
        startActivity(intent);
        finish(); // Close the waiting activity
    }
}
