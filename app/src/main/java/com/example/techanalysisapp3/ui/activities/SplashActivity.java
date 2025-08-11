package com.example.techanalysisapp3.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.techanalysisapp3.R;

public class SplashActivity extends AppCompatActivity {

    private static final long DELAY_AFTER_FLY_IN = 2000; // Delay before starting fly out

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Request notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        100
                );
            }
        }

        setContentView(R.layout.activity_splash);

        final ImageView logo = findViewById(R.id.ivSplashLogo);

        Animation flyIn = AnimationUtils.loadAnimation(this, R.anim.logo_fly_in);
        final Animation flyOut = AnimationUtils.loadAnimation(this, R.anim.logo_fly_out);

        flyOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                new Handler().postDelayed(() -> {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }, (long)(flyOut.getDuration() * 0.75));
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                finish();
            }
            @Override
            public void onAnimationRepeat(Animation animation) { }
        });

        flyIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }
            @Override
            public void onAnimationEnd(Animation animation) {
                new Handler().postDelayed(() -> logo.startAnimation(flyOut), DELAY_AFTER_FLY_IN);
            }
            @Override
            public void onAnimationRepeat(Animation animation) { }
        });

        logo.startAnimation(flyIn);
    }
}