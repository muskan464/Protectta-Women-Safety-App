package com.example.protectta_womensafetyapp;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserInfo extends AppCompatActivity {

    private TextView userName, userMail;
    private ListView contactList;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> contactData;
    private String loggedInUsername; // This should be passed from SharedPreferences or intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        userName = findViewById(R.id.user_name);
        userMail = findViewById(R.id.user_mail);
        contactList = findViewById(R.id.contact_list);

        contactData = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactData);
        contactList.setAdapter(adapter);

        // Get logged in user from SharedPreferences
        loggedInUsername = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("username", null);

        if (loggedInUsername != null) {
            loadUserInfo(loggedInUsername);
        }
    }

    private void loadUserInfo(String username) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(username);

        // Load name and email
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);

                if (name != null) userName.setText(name);
                if (email != null) userMail.setText(email);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        // Load contacts
        userRef.child("contacts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contactData.clear();
                for (DataSnapshot contactSnapshot : snapshot.getChildren()) {
                    String contactName = contactSnapshot.child("contactName").getValue(String.class);
                    String contactNumber = contactSnapshot.child("contactNumber").getValue(String.class);
                    contactData.add(contactName + " - " + contactNumber);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }
}
