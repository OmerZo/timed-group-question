package com.example.groupchatnologin;


import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore mDb;
    Button btnLock, btnStartTimer;
    EditText etAnswer;
    TextView tvTimer, tvAnswers;
    CountDownTimer mCountDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();

        etAnswer = findViewById(R.id.activity_main_et_answer);
        tvTimer = findViewById(R.id.activity_main_tv_timer);
        btnLock = findViewById(R.id.activity_main_btn_lock_answer);
        btnStartTimer = findViewById(R.id.activity_main_btn_start_timer);
        tvAnswers = findViewById(R.id.activity_main_tv_answers);

        mCountDownTimer = new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("Seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                tvTimer.setText("Timer Finished!");
                String answer = etAnswer.getText().toString().trim();
                btnStartTimer.setEnabled(true);
                btnLock.setEnabled(false);
                etAnswer.setText("");

                sendAnswer(answer);
                fetchAnswers();
            }
        };

        btnStartTimer.setOnClickListener(v -> startTimer());
        btnLock.setOnClickListener(v -> lockAnswer());
    }

    private void sendAnswer(String answer) {
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null) {
            Map<String, Object> answerData = new HashMap<>();
            answerData.put("name", user.getDisplayName());
            answerData.put("userId", user.getUid());
            answerData.put("answer", answer);
            mDb.collection("answers").add(answerData);
        }
    }

    private void fetchAnswers() {
        mDb.collection("answers").get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                StringBuilder answers = new StringBuilder();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    answers.append(document.getData().get("name")).append(" => ").append(document.getData().get("answer")).append(System.getProperty("line.separator"));
                    Log.d("TAG", document.getData().get("name") + " => " + document.getData().get("answer"));
                }
                tvAnswers.setText(answers);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //TODO: Sign out button
        if(currentUser != null){
            String message = String.format("Welcome %s", currentUser.getDisplayName());
            ((TextView)findViewById(R.id.activity_main_tv_welcome)).setText(message);
        } else {
            startActivity(new Intent(MainActivity.this, LogInActivity.class));
            finish();
        }
    }

    private void lockAnswer() {
        String answer = etAnswer.getText().toString().trim();
        btnLock.setEnabled(false);
        etAnswer.setEnabled(false);
    }

    private void startTimer() {
        btnStartTimer.setEnabled(false);
        btnLock.setEnabled(true);
        etAnswer.setEnabled(true);
        mCountDownTimer.start();
    }
}