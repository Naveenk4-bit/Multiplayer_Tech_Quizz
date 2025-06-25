package com.example.codeiq;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
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

public class Lobby extends AppCompatActivity {
    private TextView pinDisplay, playerNameDisplay, waitingMessage, selectedTimeText;
    private Button startButton;
    private ListView playerListView;
    private Spinner timeSpinner;
    private DatabaseReference quizRef, playersRef;
    private String quizPin, playerName;
    private boolean isHost;
    private ArrayList<String> playersList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby2);

        // UI References
        pinDisplay = findViewById(R.id.tv_quiz_pin);
        playerNameDisplay = findViewById(R.id.tv_player_name);
        waitingMessage = findViewById(R.id.tv_waiting_message);
        selectedTimeText = findViewById(R.id.tv_selected_time);
        timeSpinner = findViewById(R.id.spinner_quiz_time);
        startButton = findViewById(R.id.btn_start_quiz);
        playerListView = findViewById(R.id.lv_players);

        // Get quiz PIN, player name, and host status from intent
        quizPin = getIntent().getStringExtra("quizPin");
        playerName = getIntent().getStringExtra("playerName");
        isHost = getIntent().getBooleanExtra("isHost", false);

        // Display the PIN and Player Name
        if (quizPin != null) pinDisplay.setText("Quiz PIN: " + quizPin);
        if (playerName != null) playerNameDisplay.setText("You: " + playerName);

        // Firebase References
        quizRef = FirebaseDatabase.getInstance().getReference("quizzes").child(quizPin);
        playersRef = quizRef.child("players");

        // Initialize player list and adapter
        playersList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, playersList);
        playerListView.setAdapter(adapter);

        // Set up the Spinner for quiz time selection
        String[] timeOptions = {"30 sec", "1 min", "3 min", "5 min"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, timeOptions);
        timeSpinner.setAdapter(spinnerAdapter);

        // Show time selection only for the host
        if (isHost) {
            timeSpinner.setVisibility(View.VISIBLE);
            findViewById(R.id.tv_select_time).setVisibility(View.VISIBLE);
        } else {
            timeSpinner.setVisibility(View.GONE);
            findViewById(R.id.tv_select_time).setVisibility(View.GONE);
        }

        // Show start button only for the host
        startButton.setVisibility(isHost ? View.VISIBLE : View.GONE);

        // Listen for players joining
        playersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                playersList.clear();
                for (DataSnapshot playerSnapshot : snapshot.getChildren()) {
                    String player = playerSnapshot.getKey();
                    if (player != null) playersList.add(player);
                }
                adapter.notifyDataSetChanged();
                waitingMessage.setText(playersList.isEmpty() ? "Waiting for players to join..." : "Players are joining...");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Lobby.this, "Error loading players!", Toast.LENGTH_SHORT).show();
            }
        });

        // Listen for Selected Quiz Time (For All Players)
        quizRef.child("selected_time").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String selectedTime = snapshot.getValue(String.class);
                if (selectedTime != null) {
                    selectedTimeText.setText("Quiz Time: " + selectedTime);
                } else {
                    selectedTimeText.setText("Quiz Time: Not Selected");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Lobby.this, "Error loading quiz time!", Toast.LENGTH_SHORT).show();
            }
        });

        // Start Quiz (Only Host)
        startButton.setOnClickListener(v -> {
            if (timeSpinner.getSelectedItem() == null) {
                Toast.makeText(Lobby.this, "Please select a quiz time!", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedTime = timeSpinner.getSelectedItem().toString();

            // Save selected time in Firebase before starting the quiz
            quizRef.child("selected_time").setValue(selectedTime).addOnSuccessListener(aVoid -> {
                // Start the quiz only after selected_time is saved
                quizRef.child("status").setValue("started");
            }).addOnFailureListener(e -> {
                Toast.makeText(Lobby.this, "Failed to save quiz time!", Toast.LENGTH_SHORT).show();
            });
        });

        // Listen for Quiz Status Change (For All Players & Host)
        quizRef.child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status = snapshot.getValue(String.class);

                if ("started".equals(status)) {
                    if (isHost) {
                        // Host moves to HostWaitingActivity
                        Intent intent = new Intent(Lobby.this, HostWaitingActivity.class);
                        intent.putExtra("quizPin", quizPin);
                        startActivity(intent);
                        finish();
                    } else {
                        // Players move to Playing_ground
                        Intent intent = new Intent(Lobby.this, Playing_ground.class);
                        intent.putExtra("quizPin", quizPin);
                        intent.putExtra("playerName", playerName);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Lobby.this, "Error checking quiz status!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();  // Calls the default back behavior

        // Navigate back to Home_page when back button is pressed
        Intent intent = new Intent(Lobby.this, Home_page.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

}
