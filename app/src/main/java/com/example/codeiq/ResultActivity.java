package com.example.codeiq;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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
import java.util.Collections;
import java.util.List;

public class ResultActivity extends AppCompatActivity {

    private TextView tvWinner, tvPlayer1, tvPlayer2, tvPlayer3;
    private Button btnHome;
    private DatabaseReference finishedPlayersRef, playersRef;
    private String quizPin;

    class Player {
        String name;
        int correctAnswers;
        long finishTime;

        public Player(String name, int correctAnswers, long finishTime) {
            this.name = name;
            this.correctAnswers = correctAnswers;
            this.finishTime = finishTime;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        tvWinner = findViewById(R.id.tv_winner);
        tvPlayer1 = findViewById(R.id.tv_player1);
        tvPlayer2 = findViewById(R.id.tv_player2);
        tvPlayer3 = findViewById(R.id.tv_player3);
        btnHome = findViewById(R.id.btn_home);

        quizPin = getIntent().getStringExtra("quizPin");

        finishedPlayersRef = FirebaseDatabase.getInstance()
                .getReference("quiz_results")
                .child(quizPin)
                .child("finishedPlayers");

        playersRef = FirebaseDatabase.getInstance()
                .getReference("quiz_results")
                .child(quizPin)
                .child("players");

        fetchLeaderboard();

        btnHome.setOnClickListener(v -> navigateToHome());
    }

    private void fetchLeaderboard() {
        finishedPlayersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot finishedSnapshot) {
                List<Player> players = new ArrayList<>();
                long totalPlayers = finishedSnapshot.getChildrenCount();

                for (DataSnapshot playerSnap : finishedSnapshot.getChildren()) {
                    String playerName = playerSnap.getKey(); // ✅ Use key as name
                    if (playerName == null) continue;

                    playersRef.child(playerName).child("answers").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot answersSnapshot) {
                            int correctAnswers = 0;
                            long latestTime = 0;

                            for (DataSnapshot questionSnap : answersSnapshot.getChildren()) {
                                Boolean isCorrect = questionSnap.child("isCorrect").getValue(Boolean.class);
                                Long timeTaken = questionSnap.child("timeTaken").getValue(Long.class);

                                if (isCorrect != null && isCorrect) correctAnswers++;
                                if (timeTaken != null && timeTaken > latestTime) {
                                    latestTime = timeTaken; // use latest answer time as finish time
                                }
                            }

                            players.add(new Player(playerName, correctAnswers, latestTime));

                            if (players.size() == totalPlayers) {
                                sortAndDisplayLeaderboard(players);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(ResultActivity.this, "Error loading player answers", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ResultActivity.this, "Error loading finished players", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void sortAndDisplayLeaderboard(List<Player> players) {
        // Sorting: Higher correct answers first, then lower finish time
        Collections.sort(players, (p1, p2) -> {
            if (p2.correctAnswers == p1.correctAnswers) {
                return Long.compare(p1.finishTime, p2.finishTime);
            }
            return Integer.compare(p2.correctAnswers, p1.correctAnswers);
        });

        displayLeaderboard(players);
    }

    private void displayLeaderboard(List<Player> players) {
        if (players.isEmpty()) {
            tvWinner.setText("No participants yet!");
            return;
        }

        Player winner = players.get(0);
        tvWinner.setText("🏆 Winner: " + winner.name + " (" + winner.correctAnswers + " correct)");

        if (players.size() > 0) {
            tvPlayer1.setText("🥇 " + players.get(0).name + " - " + players.get(0).correctAnswers + " correct");
        }
        if (players.size() > 1) {
            tvPlayer2.setText("🥈 " + players.get(1).name + " - " + players.get(1).correctAnswers + " correct");
        }
        if (players.size() > 2) {
            tvPlayer3.setText("🥉 " + players.get(2).name + " - " + players.get(2).correctAnswers + " correct");
        }
    }

    // Handle Back Button Press
    @Override
    public void onBackPressed() {
        navigateToHome();
        super.onBackPressed();
    }

    private void navigateToHome() {
        Intent intent = new Intent(ResultActivity.this, Home_page.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
