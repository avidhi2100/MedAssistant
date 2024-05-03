package com.example.medassistant.alarm;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.medassistant.HomeActivity;
import com.example.medassistant.R;
import com.example.medassistant.entity.Reminder;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {
    @SuppressLint({"MissingPermission", "ScheduleExactAlarm"})
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i("IN-ALARM", "onReceive:Created Notification channel");
//        Reminder reminder = intent.getParcelableExtra("reminder");
//        long alarmTime = 0;
//        if(reminder != null) {
//            alarmTime = reminder.getTime();
//        }

//        long currentTime = System.currentTimeMillis();
//        if (currentTime >= alarmTime && currentTime < alarmTime + 60000 || (currentTime < alarmTime && currentTime > alarmTime - 60000)) { // Allow a 1-minute window
            Intent notificationIntent = new Intent(context, HomeActivity.class);
            PendingIntent notificationPendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default")
                    .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
                    .setContentTitle("MedAssistant")
                    .setContentText("Time for your medication!")
                    .setContentIntent(notificationPendingIntent)
                    .setAutoCancel(true)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            Log.i("IN-ALARM", "onReceive:Created Notification");
            notificationManagerCompat.notify(0, builder.build());
//        } else {
//            Log.i("IN-ALARM", "onReceive:Notification time does not match");
//
//        }




    }
}
