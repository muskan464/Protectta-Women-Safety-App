package com.example.protectta_womensafetyapp;

import android.Manifest;
import android.content.Intent;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private Button sosButton;
    private TextView addContactBtn, youBtn, helpBtn, aboutBtn;
    private List<String> contactNumbers = new ArrayList<>();
    private static final int PERMISSION_REQUEST_CODE = 100;
    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseReference contactsRef;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (uid == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        sosButton = findViewById(R.id.sosButton);
        addContactBtn = findViewById(R.id.add_contact_btn);
        youBtn = findViewById(R.id.you_btn);
        helpBtn = findViewById(R.id.help_btn);
        aboutBtn = findViewById(R.id.about_btn);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        contactsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("contacts");

        loadContacts();

        sosButton.setOnClickListener(v -> checkPermissionsAndSendSOS());
        addContactBtn.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddContactActivity.class))
        );
        youBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UserInfo.class);
            intent.putExtra("uid", uid);
            startActivity(intent);
        });
        helpBtn.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, WomenHelp.class))
        );
        aboutBtn.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, About.class))
        );
    }

    private void loadContacts() {
        contactsRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                contactNumbers.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String number = ds.child("contactNumber").getValue(String.class);
                    if (number != null) contactNumbers.add(number);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                String link = "https://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();
                String dateTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

                FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(uid)
                        .child("location")
                        .setValue(new LocationData(link, dateTime));

                SmsManager smsManager = SmsManager.getDefault();
                String msg = "SOS! I need help. My location: " + link;
                for (String number : contactNumbers) {
                    smsManager.sendTextMessage(number, null, msg, null, null);
                }
                Toast.makeText(this, "SOS sent with location!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override public void onRequestPermissionsResult(int code, @NonNull String[] perms, @NonNull int[] results) {
        super.onRequestPermissionsResult(code, perms, results);
        if (code == PERMISSION_REQUEST_CODE) {
            boolean granted = true;
            for (int r : results) {
                if (r != PackageManager.PERMISSION_GRANTED) { granted = false; break; }
            }
            if (granted) sendSOSWithLocation();
            else Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
        }
    }

    // Nested model for location updates
    public static class LocationData {
        public String link, dateTime;
        public LocationData() { }
        public LocationData(String link, String dateTime) {
            this.link = link;
            this.dateTime = dateTime;
        }
    }
}
