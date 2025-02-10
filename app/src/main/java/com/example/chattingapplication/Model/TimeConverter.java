package com.example.chattingapplication.Model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeConverter {
    public static String convertMillisToTime(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata")); // Set to IST
        return sdf.format(new Date(millis));
    }

    public static void main(String[] args) {
        long timestamp = 1738842077687L; // Example timestamp
        String formattedTime = convertMillisToTime(timestamp);
        System.out.println(formattedTime); // Output: 05:11 PM
    }
}
