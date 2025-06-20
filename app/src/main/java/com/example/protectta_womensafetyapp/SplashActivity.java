package com.example.protectta_womensafetyapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            SharedPreferences onboardingPrefs = getSharedPreferences("OnboardingPrefs", MODE_PRIVATE);
            boolean isFirstTime = onboardingPrefs.getBoolean("isFirstTime", true);

            if (isFirstTime) {
                // Mark onboarding as shown
                SharedPreferences.Editor editor = onboardingPrefs.edit();
                editor.putBoolean("isFirstTime", false);
                editor.apply();

                // Show onboarding screen
                startActivity(new Intent(SplashActivity.this, MainOnBoardingScreenActivity.class));
            } else {
                // Check if user is already logged in
                SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String username = userPrefs.getString("username", null);

                if (username != null) {
                    // User is logged in, go to main screen
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                } else {
                    // User not logged in, show login screen
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
            }

            finish();
        }, SPLASH_DELAY);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
