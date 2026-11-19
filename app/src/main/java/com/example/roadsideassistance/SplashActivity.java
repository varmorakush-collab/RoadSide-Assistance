package com.example.roadsideassistance;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY_MS = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        ImageView imgLogo = findViewById(R.id.img_logo);
        TextView tvTitle = findViewById(R.id.tv_app_title);
        TextView tvTagline = findViewById(R.id.tv_app_tagline);
        ProgressBar progressBar = findViewById(R.id.progress_bar);

        // Load animations
        Animation fadeInZoom = AnimationUtils.loadAnimation(this, R.anim.fade_in_zoom);
        Animation bottomUp = AnimationUtils.loadAnimation(this, R.anim.bottom_up);

        // Start animations
        imgLogo.startAnimation(fadeInZoom);
        tvTitle.startAnimation(fadeInZoom);
        tvTagline.startAnimation(bottomUp);
        progressBar.startAnimation(bottomUp);

        // Delay for splash screen then navigate based on user login state
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;
            if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }, SPLASH_DELAY_MS);
    }
}