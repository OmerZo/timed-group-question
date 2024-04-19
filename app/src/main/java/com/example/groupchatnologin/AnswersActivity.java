package com.example.groupchatnologin;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class AnswersActivity extends AppCompatActivity {

    private FirebaseFirestore mDb;
    private DocumentReference mGroupRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_answers);

        mDb = FirebaseFirestore.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            String groupId = extras.getString("groupId");
            mGroupRef = mDb.collection("groups").document(groupId);
        }
        fetchAnswers();
    }

    private void fetchAnswers() {
        mGroupRef.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                DocumentSnapshot groupDoc = task.getResult();
                if(groupDoc.exists()) {
                    ArrayList<String> answersIds = (ArrayList<String>) groupDoc.get("answersIds");

                    if(answersIds != null && !answersIds.isEmpty()) {
                        CollectionReference answersRef = mDb.collection("answers");
                        answersRef.whereIn(FieldPath.documentId(), answersIds).get()
                                .addOnCompleteListener(answersTask -> {
                                    QuerySnapshot answersSnap = answersTask.getResult();
                                    if(answersSnap != null) {
                                        StringBuilder answers = new StringBuilder();
                                        for (DocumentSnapshot answerDoc : answersSnap.getDocuments()) {
                                            answers.append(answerDoc.getData().get("name")).append(" => ").append(answerDoc.getData().get("answer")).append(System.getProperty("line.separator"));
                                            System.out.println(String.format("omer_dbg: %s -> %s", answerDoc.get("name"), answerDoc.get("answer")));
                                        }
                                        ((TextView)findViewById(R.id.activity_answers_tv_answers)).setText(answers);
                                    }
                                });
                    }
                }
            }
        });
    }
}