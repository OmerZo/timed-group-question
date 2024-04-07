package com.example.groupchatnologin;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class AnswersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_answers);

        fetchAnswers();
    }

    private void fetchAnswers() {
        FirebaseFirestore.getInstance().collection("answers").get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                StringBuilder answers = new StringBuilder();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    answers.append(document.getData().get("name")).append(" => ").append(document.getData().get("answer")).append(System.getProperty("line.separator"));
                    Log.d("TAG", document.getData().get("name") + " => " + document.getData().get("answer"));
                }
                ((TextView)findViewById(R.id.activity_answers_tv_answers)).setText(answers);
            }
        });
    }
}