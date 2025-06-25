package com.example.codeiq;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class ChooseLanguageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_language);

        Button btnJava = findViewById(R.id.btn_java);
        Button btnPython = findViewById(R.id.btn_python);
        Button btnC = findViewById(R.id.btn_c);
        Button btnCpp = findViewById(R.id.btn_cpp); // Added C++ button

        View.OnClickListener languageClickListener = v -> {
            String selectedFile = "";

            if (v.getId() == R.id.btn_java) {
                selectedFile = "java_quiz.csv";
            } else if (v.getId() == R.id.btn_python) {
                selectedFile = "python_quiz.csv";
            } else if (v.getId() == R.id.btn_c) {
                selectedFile = "c_quiz.csv";
            } else if (v.getId() == R.id.btn_cpp) { // Handle C++ button click
                selectedFile = "cpp_quiz.csv";
            }

            Intent intent = new Intent(ChooseLanguageActivity.this, GenerateQuizActivity.class);
            intent.putExtra("selectedFile", selectedFile);
            startActivity(intent);
        };

        btnJava.setOnClickListener(languageClickListener);
        btnPython.setOnClickListener(languageClickListener);
        btnC.setOnClickListener(languageClickListener);
        btnCpp.setOnClickListener(languageClickListener); // Set click listener for C++
    }
}
