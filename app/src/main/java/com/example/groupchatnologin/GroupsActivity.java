package com.example.groupchatnologin;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class GroupsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private CollectionReference mGroupsRef, mCountsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_groups);

        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        mGroupsRef = db.collection("groups");
        mCountsRef = db.collection("countdowns");

        findViewById(R.id.activity_group_btn_join).setOnClickListener(v -> joinGroup());
        findViewById(R.id.activity_group_btn_create).setOnClickListener(v -> createCountdown());
    }

    private void createCountdown() {
        Map<String, Object> countdown = new HashMap<>();
        countdown.put("startTime", 0);
        countdown.put("duration", 0);
        mCountsRef.add(countdown).addOnSuccessListener(countRef -> createGroup(countRef.getId()));
    }

    private void createGroup(String countId) {
        Map<String, Object> group = new HashMap<>();
        group.put(mAuth.getCurrentUser().getUid(), true);
        group.put("countId", countId);

        mGroupsRef.add(group).addOnSuccessListener(groupRef -> {
            String groupId = groupRef.getId();
            copyToClipboard(groupId);
            StartGame(groupId, countId);
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed Creating A Group", Toast.LENGTH_SHORT).show());
    }

    private void StartGame(String groupId, String countId) {
        Intent intent = new Intent(GroupsActivity.this, MainActivity.class);
        intent.putExtra("groupId", groupId);
        intent.putExtra("countId", countId);
        startActivity(intent);
    }

    private void copyToClipboard(String groupId) {
        // Get clipboard manager
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        // Create a clip data with label and data
        ClipData clip = ClipData.newPlainText("Copied Text", groupId);

        // Set the clipboard data
        clipboard.setPrimaryClip(clip);

        // Notify user
        Toast.makeText(getApplicationContext(), "Group ID Copied to Clipboard", Toast.LENGTH_SHORT).show();

    }

    private void joinGroup() {
        //TODO: handle empty et
        String groupId = ((EditText)findViewById(R.id.activity_group_et_groupId)).getText().toString().trim();
        mGroupsRef.document(groupId).get().addOnCompleteListener(task -> {
           if(task.isSuccessful()) {
               DocumentSnapshot groupDoc = task.getResult();
               if (groupDoc.exists()) {
                   Toast.makeText(this, "Found Group", Toast.LENGTH_SHORT).show();
                   if(groupDoc.contains("countId")) {
                       StartGame(groupId, groupDoc.get("countId").toString());
                   }
               } else {
                   Toast.makeText(this, "Group Not Found", Toast.LENGTH_SHORT).show();
               }
           } else {
               Toast.makeText(this, "Failed To Fetch Data", Toast.LENGTH_SHORT).show();
           }
        });
        //Search DB and update
        //Intent with Data
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //TODO: Sign out button
        if(currentUser != null){
            String message = String.format("Welcome %s", currentUser.getDisplayName());
            ((TextView)findViewById(R.id.activity_group_tv_welcome)).setText(message);
        } else {
            startActivity(new Intent(GroupsActivity.this, LogInActivity.class));
            finish();
        }
    }
}