package com.example.techanalysisapp3.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class TriviaReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Ensure channel exists
        NotificationHelper.createNotificationChannel(context);
        // Check if notifications are enabled
        SharedPreferences prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE);
        boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", true);

        if (!notificationsEnabled) {
            return; // Don't show notification if disabled
        }

        TriviaRepository repository = new TriviaRepository(context);
        String trivia = repository.getRandomTrivia();

        if (trivia != null) {
            NotificationHelper.showTriviaNotification(context, trivia);
        }
    }
}