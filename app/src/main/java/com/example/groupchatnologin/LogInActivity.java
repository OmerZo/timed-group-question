package com.example.groupchatnologin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LogInActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore mDb;
    Button mBtnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_log_in);

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();

        mBtnStart = findViewById(R.id.activity_log_in_btn_start);

        mBtnStart.setOnClickListener(v -> {
            String userName = ((EditText)findViewById(R.id.activity_log_in_et_name)).getText().toString().trim();

            if(userName.isEmpty()) {
                Toast.makeText(LogInActivity.this, "Please choose a name", Toast.LENGTH_SHORT).show();
            } else {
                logIn(userName);
            }
        });
    }

    private void logIn(String userName) {
        //TODO: ProgressBar
        //TODO: Separate to functions
        mBtnStart.setEnabled(false);
        mAuth.signInAnonymously()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(userName).build();
                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(updateTask -> {
                                    if(updateTask.isSuccessful()) {
                                        updateDb(user, userName);
                                        startActivity(new Intent(LogInActivity.this, MainActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(LogInActivity.this, "Update Name Failed", Toast.LENGTH_SHORT).show();
                                        user.delete();
                                        mBtnStart.setEnabled(true);
                                    }
                        });
                    } else {
                        Toast.makeText(LogInActivity.this, "SignIn Failed", Toast.LENGTH_SHORT).show();
                        mBtnStart.setEnabled(true);
                    }
                });
    }

    private void updateDb(FirebaseUser user, String userName) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", userName);
        mDb.collection("users").document(user.getUid()).set(userData);
    }
}