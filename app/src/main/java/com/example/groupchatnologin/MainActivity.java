package com.example.groupchatnologin;


import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDb;
    private DocumentReference mCountdownRef, mGroupRef;

    private Button btnLock, btnStartTimer, btnShowAnswers;
    private EditText etAnswer;
    private TextView tvTimer;

    //TODO: Sign out button
    //TODO: Create atomic transactions

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        findViews();

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();

        btnStartTimer.setOnClickListener(v -> startCountdown());
        btnLock.setOnClickListener(v -> enableAnswering(false));
        btnShowAnswers.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AnswersActivity.class)));

        getDocRefs();
    }

    private void getDocRefs() {
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            String groupId = extras.getString("groupId");
            String countId = extras.getString("countId");
            Toast.makeText(this, "group id = " + groupId, Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "count id = " + countId, Toast.LENGTH_SHORT).show();
            mGroupRef = mDb.collection("groups").document(groupId);
            mCountdownRef = mDb.collection("countdowns").document(countId);
            mCountdownRef.addSnapshotListener(this::countdownRefUpdated);
        }
    }

    private void findViews() {
        etAnswer = findViewById(R.id.activity_main_et_answer);
        tvTimer = findViewById(R.id.activity_main_tv_timer);
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
        if(currentUser != null){
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                startActivity(new Intent(MainActivity.this, GroupsActivity.class));
                finish();
            }
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

        Map<String, Object> countdown = new HashMap<>();
        countdown.put("startTime", startTime);
        countdown.put("duration", duration);
        mCountdownRef.set(countdown).addOnFailureListener(command ->
                Toast.makeText(this, "Can't start countdown", Toast.LENGTH_LONG).show());
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

    private void enableAnswering(Boolean enable) {
        btnLock.setEnabled(enable);
        etAnswer.setEnabled(enable);
    }
}