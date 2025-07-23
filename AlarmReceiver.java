package com.example.sleepapplication;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "sleep_app";
    private static final int NOTIFICATION_ID = 1;

    @SuppressLint({"MissingPermission", "NotificationTrampoline"})
    @Override
    public void onReceive(Context context, Intent intent) {

        String label = intent.getStringExtra("label");
        if (label == null || label.isEmpty()) label = "Alarm";
        boolean isSmart = intent.getBooleanExtra("is_smart", false);

        // Create notification channel
        createNotificationChannel(context);

        // Create full-screen intent for AlarmTriggerActivity
        Intent fullScreenIntent = new Intent(context, AlarmActivity.class);
        fullScreenIntent.putExtra("label", label);
        fullScreenIntent.putExtra("is_smart", isSmart);
        fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                0,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle("Sleep Tracking")
                .setContentText(label)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                .setVibrate(new long[]{0, 500, 250, 500})
                .setLights(Color.BLUE, 500, 500);


        Intent dismissIntent = new Intent(context, AlarmReceiver.class);
        dismissIntent.setAction("DISMISS_ALARM");
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        builder.addAction(R.drawable.ic_alarm, "Dismiss", dismissPendingIntent);


        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build());


        context.startActivity(fullScreenIntent);
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Alarms",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Alarm notifications");
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 250, 500});

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}

