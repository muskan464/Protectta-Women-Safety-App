package com.example.protectta_womensafetyapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddContactActivity extends AppCompatActivity {

    EditText contactName, contactNumber;
    Button addContact;
    FirebaseDatabase database;
    DatabaseReference reference;
    String loggedInUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        contactName = findViewById(R.id.contact_name);
        contactNumber = findViewById(R.id.contact_num);
        addContact = findViewById(R.id.add_btn);

        loggedInUsername = getIntent().getStringExtra("username");
        if (loggedInUsername == null || loggedInUsername.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            loggedInUsername = prefs.getString("username", null);
        }

        if (loggedInUsername == null || loggedInUsername.isEmpty()) {
            Toast.makeText(this, "Error: User not found!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users").child(loggedInUsername).child("contacts");

        addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = contactName.getText().toString().trim();
                String number = contactNumber.getText().toString().trim();

                if (name.isEmpty() || number.isEmpty()) {
                    Toast.makeText(AddContactActivity.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Basic number validation (10 digits)
                if (!number.matches("\\d{10}")) {
                    Toast.makeText(AddContactActivity.this, "Enter a valid 10-digit number", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create sanitized key
                String contactKey = (name + "_" + number).replaceAll("[^a-zA-Z0-9]", "_");

                // Check if contact already exists
                reference.child(contactKey).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(AddContactActivity.this, "Contact already exists!", Toast.LENGTH_SHORT).show();
                        } else {
                            HelperClass helperClass = new HelperClass(name, number);
                            reference.child(contactKey).setValue(helperClass)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(AddContactActivity.this, "Contact Saved Successfully!", Toast.LENGTH_SHORT).show();
                                        contactName.setText("");
                                        contactNumber.setText("");

                                        // Hide keyboard
                                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                                        if (imm != null) {
                                            imm.hideSoftInputFromWindow(contactNumber.getWindowToken(), 0);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(AddContactActivity.this, "Failed to save contact!", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AddContactActivity.this, "Database error!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
