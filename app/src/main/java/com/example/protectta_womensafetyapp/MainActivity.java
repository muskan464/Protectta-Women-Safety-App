package com.example.protectta_womensafetyapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Button sosButton;
    private TextView addContactBtn,youBtn,helpBtn,aboutBtn;
    private List<String> contactNumbers = new ArrayList<>();
    private String loggedInUsername;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sosButton = findViewById(R.id.sosButton);
        addContactBtn = findViewById(R.id.add_contact_btn);
        youBtn=findViewById(R.id.you_btn);
        helpBtn=findViewById(R.id.help_btn);
        aboutBtn=findViewById(R.id.about_btn);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        loggedInUsername = prefs.getString("username", null);

        if (loggedInUsername == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        loadContacts();

        sosButton.setOnClickListener(v -> checkPermissionsAndSendSOS());

        addContactBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddContactActivity.class);
            intent.putExtra("username", loggedInUsername);
            startActivity(intent);
        });

        youBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UserInfo.class);
            startActivity(intent);
        });

        helpBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WomenHelp.class);
            startActivity(intent);
        });

        aboutBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, About.class);
            startActivity(intent);
        });
    }

    private void loadContacts() {
        reference = FirebaseDatabase.getInstance().getReference("users").child(loggedInUsername).child("contacts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contactNumbers.clear();
                for (DataSnapshot contactSnapshot : snapshot.getChildren()) {
                    String number = contactSnapshot.child("contactNumber").getValue(String.class);
                    if (number != null) {
                        contactNumbers.add(number);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load contacts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkPermissionsAndSendSOS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, PERMISSION_REQUEST_CODE);
        } else {
            sendSOSWithLocation();
        }
    }

    private void sendSOSWithLocation() {
        if (contactNumbers.isEmpty()) {
            Toast.makeText(this, "No contacts available", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                String locationLink = "https://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();
                String dateTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

                // Store location + datetime in Firebase
                DatabaseReference locationRef = FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(loggedInUsername)
                        .child("location");

                LocationData data = new LocationData(locationLink, dateTime);
                locationRef.setValue(data);

                // Send SMS
                String message = "SOS! I need help. My location: " + locationLink;
                SmsManager smsManager = SmsManager.getDefault();
                for (String number : contactNumbers) {
                    smsManager.sendTextMessage(number, null, message, null, null);
                }

                Toast.makeText(this, "SOS sent with location!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                sendSOSWithLocation();
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Firebase Location Data model
    public static class LocationData {
        public String link;
        public String dateTime;

        public LocationData() {} // Needed for Firebase

        public LocationData(String link, String dateTime) {
            this.link = link;
            this.dateTime = dateTime;
        }
    }
}