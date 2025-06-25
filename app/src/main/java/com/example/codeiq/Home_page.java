package com.example.codeiq;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
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

public class Home_page extends AppCompatActivity {

    private Button buttonCreate, buttonJoin, buttonGenerate, buttonUpload;
    private EditText editTextPin, editTextName;
    private DatabaseReference quizRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        buttonCreate = findViewById(R.id.button_create);
        buttonJoin = findViewById(R.id.button_join);
        buttonGenerate = findViewById(R.id.button_generate);
        buttonUpload = findViewById(R.id.button_upload);
        editTextPin = findViewById(R.id.textbox_pin);
        editTextName = findViewById(R.id.textbox_name);

        buttonCreate.setOnClickListener(v -> startActivity(new Intent(Home_page.this, Create_page.class)));
        buttonJoin.setOnClickListener(v -> joinQuiz());
        buttonGenerate.setOnClickListener(v -> startActivity(new Intent(Home_page.this, ChooseLanguageActivity.class)));
        buttonUpload.setOnClickListener(v -> startActivity(new Intent(Home_page.this, UploadActivity.class)));
    }

    private void joinQuiz() {
        String quizPin = editTextPin.getText().toString().trim();
        String playerName = editTextName.getText().toString().trim();

        if (playerName.isEmpty()) {
            showToast("Enter your name!");
            return;
        }

        if (!quizPin.matches("\\d{4}")) {
            showToast("Enter a valid 4-digit PIN!");
            return;
        }

        quizRef = FirebaseDatabase.getInstance().getReference("quizzes").child(quizPin);

        quizRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = snapshot.child("status").getValue(String.class);
                    if ("completed".equals(status)) {
                        moveToResult(quizPin, playerName);
                    } else if ("ready".equals(status)) {
                        addPlayerToQuiz(quizPin, playerName);
                    } else {
                        showToast("Quiz has already started or is unavailable.");
                    }
                } else {
                    showToast("Invalid PIN, try again!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Database Error! " + error.getMessage());
            }
        });
    }

    private void addPlayerToQuiz(String quizPin, String playerName) {
        DatabaseReference playerRef = FirebaseDatabase.getInstance()
                .getReference("quizzes").child(quizPin).child("players").child(playerName);

        playerRef.setValue(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                moveToLobby(quizPin, playerName);
            } else {
                showToast("Failed to add player!");
            }
        });
    }

    private void moveToLobby(String quizPin, String playerName) {
        Intent intent = new Intent(Home_page.this, Lobby.class);
        intent.putExtra("quizPin", quizPin);
        intent.putExtra("playerName", playerName);
        intent.putExtra("isHost", false);
        startActivity(intent);
        finish();
    }

    private void moveToResult(String quizPin, String playerName) {
        Intent intent = new Intent(Home_page.this, ResultActivity.class);
        intent.putExtra("quizPin", quizPin);
        intent.putExtra("playerName", playerName);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(Home_page.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        navigateToMain();
        super.onBackPressed();
    }

    private void navigateToMain() {
        Intent intent = new Intent(Home_page.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
