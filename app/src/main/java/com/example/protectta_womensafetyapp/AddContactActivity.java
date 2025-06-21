package com.example.protectta_womensafetyapp;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class AddContactActivity extends AppCompatActivity {

    EditText contactName, contactNumber;
    Button addContact;
    DatabaseReference reference;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        contactName = findViewById(R.id.contact_name);
        contactNumber = findViewById(R.id.contact_num);
        addContact = findViewById(R.id.add_btn);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (uid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        reference = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("contacts");

        addContact.setOnClickListener(v -> saveContact());
    }

    private void saveContact() {
        String name = contactName.getText().toString().trim();
        String number = contactNumber.getText().toString().trim();

        if (name.isEmpty() || number.isEmpty()) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!number.matches("\\d{10}")) {
            Toast.makeText(this, "Enter a valid 10-digit number", Toast.LENGTH_SHORT).show();
            return;
        }

        String contactKey = (name + "_" + number).replaceAll("[^a-zA-Z0-9]", "_");

        reference.child(contactKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(AddContactActivity.this, "Contact already exists!", Toast.LENGTH_SHORT).show();
                } else {
                    ContactModel contact = new ContactModel(name, number);
                    reference.child(contactKey).setValue(contact)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(AddContactActivity.this, "Contact saved!", Toast.LENGTH_SHORT).show();
                                contactName.setText("");
                                contactNumber.setText("");
                                hideKeyboard();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AddContactActivity.this, "Failed to save contact!", Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddContactActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(contactNumber.getWindowToken(), 0);
        }
    }
}
