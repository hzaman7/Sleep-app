package com.example.sleepapplication;


import android.annotation.SuppressLint;
import android.app.AlarmManager;


import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AlarmActivity extends AppCompatActivity {
    private Switch smartSwitch;

    private boolean isSmartAlarm = false;
    private EditText labelEt;
    private Button saveBtn, cancelBtn;
    private ImageButton btnMinus, btnPlus;
    private TextView tvHours;
    private int sleepHours = 8;
    private AlarmManager alarmManager;
    private PendingIntent alarmPendingIntent;
    private ImageButton btnReduceMinus, btnReducePlus;
    private TextView tvReduction;
    private int reductionMinutes = 0;
    private DatabaseHelper dbHelper;
//    @Override
//    protected void onStart() {
//        super.onStart();
//        checkAlarmPermission();
//    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (alarmManager.canScheduleExactAlarms()) {
                // Permission granted, you can proceed
            }
        }
    }

    private void checkAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                new AlertDialog.Builder(this)
                        .setTitle("Alarm Permission Needed")
                        .setMessage("This app needs special permission to set exact alarms. Please grant this permission in settings.")
                        .setPositiveButton("Open Settings", (d, w) -> requestAlarmPermission())
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_alram);

        dbHelper = new DatabaseHelper(this);
        // Initialize views
        btnReduceMinus = findViewById(R.id.btn_reduce_minus);
        btnReducePlus = findViewById(R.id.btn_reduce_plus);
        tvReduction = findViewById(R.id.tv_reduction);


        smartSwitch = findViewById(R.id.smart_alarm_switch);
        labelEt = findViewById(R.id.alarm_label);
        btnMinus = findViewById(R.id.btn_minus);
        btnPlus = findViewById(R.id.btn_plus);
        tvHours = findViewById(R.id.tv_hours);
        saveBtn = findViewById(R.id.save_alarm);
        cancelBtn = findViewById(R.id.cancel_alarm);

        // Bottom nav actions
        findViewById(R.id.nav_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(AlarmActivity.this, HomeActivity.class));
            }
        });

        findViewById(R.id.nav_home).setOnClickListener(v ->
                startActivity(new Intent(this, HomeActivity.class)));

        findViewById(R.id.nav_alarm).setOnClickListener(v ->
                startActivity(new Intent(this, AlarmActivity.class)));

        findViewById(R.id.nav_sounds).setOnClickListener(v ->
                startActivity(new Intent(this, SoundsActivity.class)));

        findViewById(R.id.nav_home).setOnClickListener(v ->
                startActivity(new Intent(this, HomeActivity.class)));;

        findViewById(R.id.nav_report).setOnClickListener(v ->
                startActivity(new Intent(this, ReportActivity.class)));

        findViewById(R.id.nav_sleepHistory).setOnClickListener(v ->
                startActivity(new Intent(this, SavedAlarmsActivity.class)));


        // Set click listeners
        btnReduceMinus.setOnClickListener(v -> decreaseReduction());
        btnReducePlus.setOnClickListener(v -> increaseReduction());


        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);


        tvHours.setText(String.valueOf(sleepHours));


        btnMinus.setOnClickListener(v -> decreaseHours());
        btnPlus.setOnClickListener(v -> increaseHours());
        cancelBtn.setOnClickListener(v -> finish());


        saveBtn.setOnClickListener(v -> {


            String title = "Alarm Note";
            if (!labelEt.getText().toString().isEmpty()) {


                title = labelEt.getText().toString();

                if (smartSwitch.isChecked()) {
                    setSmartAlarm();
                    isSmartAlarm = true;
                } else {
                    isSmartAlarm = false;
                }


                String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());



                long id = dbHelper.insertSleepRecord(Integer.parseInt(tvHours.getText().toString()),
                        Integer.parseInt(tvReduction.getText().toString()), title, isSmartAlarm, currentDate);
                if (id != -1) {
                    Toast.makeText(this, "Inserted sleep record with ID: " + id, Toast.LENGTH_SHORT).show();
                    isSmartAlarm = false;
                    tvReduction.setText("0");
                    tvHours.setText("0");
                    labelEt.setText("");
                } else {
                    Toast.makeText(this, "Failed to insert sleep record", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(this, "Label is mandatory ", Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void decreaseHours() {
        if (sleepHours > 1) {
            sleepHours--;
            tvHours.setText(String.valueOf(sleepHours));
        } else {
            Toast.makeText(this, "Minimum sleep time is 1 hour", Toast.LENGTH_SHORT).show();
        }
    }

    private void increaseHours() {
        if (sleepHours < 12) {
            sleepHours++;
            tvHours.setText(String.valueOf(sleepHours));
        } else {
            Toast.makeText(this, "Maximum sleep time is 12 hours", Toast.LENGTH_SHORT).show();
        }
    }


    // Add these methods to your class
    private void decreaseReduction() {
        if (reductionMinutes > 0) {
            reductionMinutes -= 5;
            tvReduction.setText(String.valueOf(reductionMinutes));
        } else {
            Toast.makeText(this, "Minimum reduction is 0 minutes", Toast.LENGTH_SHORT).show();
        }
    }

    private void increaseReduction() {
        if (reductionMinutes < 30) {
            reductionMinutes += 5;
            tvReduction.setText(String.valueOf(reductionMinutes));
        } else {
            Toast.makeText(this, "Maximum reduction is 30 minutes", Toast.LENGTH_SHORT).show();
        }
    }


    private void requestAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("This app needs to schedule exact alarms. Please grant this permission in settings.")
                    .setPositiveButton("Open Settings", (dialog, which) -> {
                        try {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            startActivity(intent);
                        } catch (Exception e) {
                            // Fallback if the intent fails
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.fromParts("package", getPackageName(), null));
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    private void setSmartAlarm() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            Log.d("AlarmTest", "Can schedule exact alarms: " + am.canScheduleExactAlarms());
        }


        String action = "ALARM_ACTION_" + System.currentTimeMillis();


        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        alarmIntent.setAction(action);

        alarmIntent.putExtra("label", labelEt.getText().toString());
        alarmIntent.putExtra("sleep_hours", sleepHours);
        alarmIntent.putExtra("sleep_minutes", reductionMinutes);


        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_MUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) System.currentTimeMillis(),
                alarmIntent,
                flags
        );


        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, reductionMinutes);
        calendar.add(Calendar.HOUR_OF_DAY, sleepHours);
        long triggerAtMillis = calendar.getTimeInMillis();


        try {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                );
            }

            Log.d("AlarmTest", "Alarm set for: " + calendar.getTime());
            Toast.makeText(this, "Alarm set for 1 minute from now", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("AlarmTest", "Error setting alarm", e);
            Toast.makeText(this, "Failed to set alarm: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!smartSwitch.isChecked() && alarmPendingIntent != null) {
            alarmManager.cancel(alarmPendingIntent);
        }
    }
}