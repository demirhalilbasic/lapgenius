package com.example.techanalysisapp3.util;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.FirebaseApp;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        FirebaseApp.initializeApp(this);
    }
}