package com.example.groupchatnologin;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class GroupsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private CollectionReference mGroupsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_groups);

        mAuth = FirebaseAuth.getInstance();
        mGroupsRef = FirebaseFirestore.getInstance().collection("groups");

        findViewById(R.id.activity_group_btn_join).setOnClickListener(v -> joinGroup());
        findViewById(R.id.activity_group_btn_create).setOnClickListener(v -> createGroup());


    }

    private void createGroup() {
        Map<String, Object> user = new HashMap<>();
        user.put(mAuth.getCurrentUser().getUid(), true);
        mGroupsRef.add(user).addOnSuccessListener(docRef -> {
            String groupId = docRef.getId();
            copyToClipboard(groupId);
            StartGame(groupId);
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed Creating A Group", Toast.LENGTH_SHORT).show());
    }

    private void StartGame(String groupId) {
        Intent intent = new Intent(GroupsActivity.this, MainActivity.class);
        intent.putExtra("groupId", groupId);
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
        //Get Group ID
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