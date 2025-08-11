package com.example.techanalysisapp3.util;

import java.util.Calendar;

public class DateUtils {
    public static boolean isToday(long timestamp) {
        if (timestamp <= 0) return false;

        Calendar today = Calendar.getInstance();
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timestamp);

        return today.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR);
    }
}