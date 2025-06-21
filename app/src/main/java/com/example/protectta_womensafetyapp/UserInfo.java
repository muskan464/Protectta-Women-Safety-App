package com.example.protectta_womensafetyapp;

import android.os.Bundle;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.ArrayList;

public class UserInfo extends AppCompatActivity {
    private TextView userName, userMail;
    private ListView contactList;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> contactData;

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

        String uid = getIntent().getStringExtra("uid");
        if (uid != null) {
            loadUserInfo(uid);
        }
    }

    private void loadUserInfo(String uid) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    userName.setText(name);
                    userMail.setText(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        userRef.child("contacts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contactData.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String cname = ds.child("contactName").getValue(String.class);
                    String cnum = ds.child("contactNumber").getValue(String.class);
                    contactData.add(cname + " – " + cnum);
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
