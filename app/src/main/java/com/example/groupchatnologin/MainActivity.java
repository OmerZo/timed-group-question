package com.example.groupchatnologin;


import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb;
    private DocumentReference mCountdownRef;
    private CollectionReference mAnswersRef;

    private Button btnLock, btnStartTimer, btnShowAnswers;
    private EditText etAnswer;
    private TextView tvTimer, tvAnswers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        findViews();

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();
        mCountdownRef = mDb.collection("settings").document("countdown");
//        mAnswersRef = mDb.collection("answers");
        // Listen for real-time updates
        mCountdownRef.addSnapshotListener(this::countdownRefUpdated);
//        mAnswersRef.addSnapshotListener((value, error) -> btnShowAnswers.setEnabled(true));

        btnStartTimer.setOnClickListener(v -> startCountdown());
        btnLock.setOnClickListener(v -> enableAnswering(false));
        btnShowAnswers.setOnClickListener(v -> fetchAnswers());
    }

    private void findViews() {
        etAnswer = findViewById(R.id.activity_main_et_answer);
        tvTimer = findViewById(R.id.activity_main_tv_timer);
        tvAnswers = findViewById(R.id.activity_main_tv_answers);
        btnLock = findViewById(R.id.activity_main_btn_lock_answer);
        btnStartTimer = findViewById(R.id.activity_main_btn_start_timer);
        btnShowAnswers = findViewById(R.id.activity_main_btn_show_answers);
    }

    private void countdownRefUpdated(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
        if (e != null || snapshot == null || !snapshot.exists()) {
            Toast.makeText(this, "Can't start countdown", Toast.LENGTH_LONG).show();
            return;
        }

        Long startTime = snapshot.getLong("startTime");
        Long duration = snapshot.getLong("duration");

        if (startTime != null && duration != null) {
            updateCountdownUI(startTime, duration);
        }
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

    private void startCountdown() {

        long duration = 30000;
        long startTime = System.currentTimeMillis();

        // Run a transaction to ensure atomic update
        mDb.runTransaction((Transaction.Function<Void>) transaction -> {
            transaction.update(mCountdownRef, "startTime", startTime, "duration", duration);
            return null; // Success
        }).addOnFailureListener(e -> {
            // Handle failure
            Toast.makeText(this, "Can't start countdown", Toast.LENGTH_LONG).show();
        });
    }

    private void updateCountdownUI(long startTime, long duration) {
        long currentTime = System.currentTimeMillis();
        long diff = startTime + duration - currentTime;

        if (diff <= 0) {
            tvTimer.setText("");
            return;
        }

        btnStartTimer.setEnabled(false);
        enableAnswering(true);
        new CountDownTimer(diff, 1000) {
            public void onTick(long millisUntilFinished) {
                tvTimer.setText(String.format("Seconds remaining: %d", millisUntilFinished / 1000));
            }

            public void onFinish() {
                tvTimer.setText("Done!");
                String answer = etAnswer.getText().toString().trim();
                etAnswer.setText("");
                btnStartTimer.setEnabled(true);
                enableAnswering(false);

                sendAnswer(answer);
            }
        }.start();
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

    private void enableAnswering(Boolean enable) {
        btnLock.setEnabled(enable);
        etAnswer.setEnabled(enable);
        btnShowAnswers.setEnabled(!enable);
    }
}