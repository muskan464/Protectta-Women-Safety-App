package com.example.protectta_womensafetyapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(this::checkLaunchLogic, SPLASH_DELAY);
    }

    private void checkLaunchLogic() {
        // 1️⃣ Check onboarding status
        boolean isFirstTime = getSharedPreferences("OnboardingPrefs", MODE_PRIVATE)
                .getBoolean("isFirstTime", true);
        if (isFirstTime) {
            getSharedPreferences("OnboardingPrefs", MODE_PRIVATE)
                    .edit().putBoolean("isFirstTime", false).apply();
            startActivity(new Intent(this, MainOnBoardingScreenActivity.class));
            finish();
            return;
        }

        // 2️⃣ The user has completed onboarding → check Firebase login status
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // ✅ Already logged in — skip login
            startActivity(new Intent(this, MainActivity.class));
        } else {
            // ❌ Not logged in — show login screen
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }
}
