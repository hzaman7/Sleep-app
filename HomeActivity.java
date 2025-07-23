package com.example.sleepapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.tasks.OnSuccessListener;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    private CircularProgressBar progressCircle;
    private TextView sleepScoreText;
    TextView sleepDurTV;
    private static final float MAX_SLEEP_MINUTES = 480f;
    private static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1001;

    private AlarmModel lastEntry;
    TextView sleepFitTimeTv,heartRateTv,stepsTv;
    private DatabaseHelper dbHelper;
    private GoogleSignInOptions googleSignInOptions;
    private FitnessOptions fitnessOptions;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                Log.d(TAG, "Notification permission result: " + isGranted);
                if (isGranted) {
                    Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Notifications disabled. Alarms may not work.", Toast.LENGTH_LONG).show();
                    if (!shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {

                        Toast.makeText(this, "Please enable notifications in app settings", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.fromParts("package", getPackageName(), null));
                        startActivity(intent);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_SleepApplication);
        setContentView(R.layout.activity_home);

        // Initialize Google Fit
        initializeGoogleFit();

        progressCircle = findViewById(R.id.progress_circle);
        sleepScoreText = findViewById(R.id.sleep_score);
        sleepFitTimeTv = findViewById(R.id.sleepFitTimeTv);
        heartRateTv = findViewById(R.id.heartRateTv);
        stepsTv = findViewById(R.id.stepsTv);

        dbHelper = new DatabaseHelper(this);
        lastEntry = dbHelper.getLastSleepRecord();

        int progress = 0;
        if (lastEntry != null) {
            float totalMinutes = lastEntry.getSleepHours() * 60 + lastEntry.getSleepMinutes();
            progress = (int) ((totalMinutes / MAX_SLEEP_MINUTES) * 100f);
            Log.d(TAG, "Sleep Progress: " + progress + "% (Hours=" + lastEntry.getSleepHours() +
                    ", Minutes=" + lastEntry.getSleepMinutes() + ")");
        } else {
            Log.d(TAG, "No sleep records found, setting progress to 0");
        }
        progressCircle.setProgressWithAnimation((float) progress, 1000L);
        sleepScoreText.setText(progress + "%");

        checkNotificationPermission();

        sleepDurTV = findViewById(R.id.sleepDurTV);
        if (lastEntry != null) {
            sleepDurTV.setText("Sleep Duration\n" + lastEntry.sleepHours + "h " + lastEntry.sleepMinutes + "m");
        }else{
            sleepDurTV.setText("Sleep Duration\n" + "Not recorded yet)");
        }


        findViewById(R.id.nav_alarm).setOnClickListener(v ->
                startActivity(new Intent(this, AlarmActivity.class)));

        findViewById(R.id.nav_sounds).setOnClickListener(v ->
                startActivity(new Intent(this, SoundsActivity.class)));

        findViewById(R.id.nav_home).setOnClickListener(v ->
                Toast.makeText(this, "Already in Home", Toast.LENGTH_SHORT).show());

        findViewById(R.id.nav_report).setOnClickListener(v ->
                startActivity(new Intent(this, ReportActivity.class)));

        findViewById(R.id.nav_sleepHistory).setOnClickListener(v ->
                startActivity(new Intent(this, SavedAlarmsActivity.class)));
    }

    private void fetchSleepData(GoogleSignInAccount account) {

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .read(DataType.TYPE_SLEEP_SEGMENT)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        Fitness.getHistoryClient(this, account)
                .readData(readRequest)
                .addOnSuccessListener(response -> {
                    for (DataSet dataSet : response.getDataSets()) {
                        for (DataPoint dp : dataSet.getDataPoints()) {
                            int sleepStage = dp.getValue(Field.FIELD_SLEEP_SEGMENT_TYPE).asInt();
                            sleepFitTimeTv.setText(sleepStage + "");
                            long start = dp.getStartTime(TimeUnit.MILLISECONDS);
                            long end = dp.getEndTime(TimeUnit.MILLISECONDS);

                            String stageName = getSleepStageName(sleepStage);
                            Log.d(TAG, "Sleeppppp: " + stageName + " from " +
                                    new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(start)) +
                                    " to " +
                                    new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(end)));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to read sleep data", e);
                });
    }

    private String getSleepStageName(int sleepStage) {
        switch (sleepStage) {
            case 1:  // Sleep
                return "Sleep";
            case 2:  // Out of bed
                return "Out of Bed";
            case 3:  // Light sleep
                return "Light Sleep";
            case 4:  // Deep sleep
                return "Deep Sleep";
            case 5:  // REM sleep
                return "REM Sleep";
            case 6:  // Awake
                return "Awake";
            default:
                return "Unknown (" + sleepStage + ")";
        }
    }


    private void fetchHeartRateData(GoogleSignInAccount account) {

        Calendar cal = Calendar.getInstance();
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .read(DataType.TYPE_HEART_RATE_BPM)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .bucketByTime(1, TimeUnit.HOURS) // Group by hour
                .build();

        Fitness.getHistoryClient(this, account)
                .readData(readRequest)
                .addOnSuccessListener(response -> {

                    for (Bucket bucket : response.getBuckets()) {
                        for (DataSet dataSet : bucket.getDataSets()) {
                            for (DataPoint dp : dataSet.getDataPoints()) {
                                float heartRate = dp.getValue(Field.FIELD_BPM).asFloat();
                                heartRateTv.setText(heartRate+"");
                                long timestamp = dp.getTimestamp(TimeUnit.MILLISECONDS);
                                Log.d(TAG, "Heart Rate: " + heartRate + " bpm at " +
                                        new SimpleDateFormat("HH:mm", Locale.getDefault())
                                                .format(new Date(timestamp)));
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to read heart rate data", e);
                });
    }
    private void initializeGoogleFit() {
        fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_HEART_RATE_SUMMARY, FitnessOptions.ACCESS_READ)
                .build();

        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .addExtension(fitnessOptions)
                .build();

        checkGoogleFitPermissions();
    }

    private void checkGoogleFitPermissions() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null || !GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions
            );
        }
        else {

            fetchHeartRateData(account);
            fetchSleepData(account);


            Fitness.getHistoryClient(this, account)
                    .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                    .addOnSuccessListener(dataSet -> {

                        int totalSteps = dataSet.isEmpty()
                                ? 0
                                : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();

                        Log.d("GFit", "Steps: " + totalSteps);
                        stepsTv.setText(totalSteps +"");
                        runOnUiThread(() ->
                                Toast.makeText(this, "Today's steps: " + totalSteps, Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e -> {
                        Log.e("GFit", "Error", e);
                        if (e instanceof ApiException && ((ApiException) e).getStatusCode() == 5012) {

                            Toast.makeText(this,
                                    "Warning: Test mode active. Data may be limited.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                accessGoogleFitData();
            } else {
                Toast.makeText(this, "Google Fit permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void accessGoogleFitData() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null) return;


        Fitness.getHistoryClient(this, account)
                .readDailyTotal(DataType.TYPE_SLEEP_SEGMENT)
                .addOnSuccessListener(dataSet -> {
                    Log.d(TAG, "Successfully read sleep data from Google Fit");

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to read sleep data", e);
                    Toast.makeText(this, "Failed to access Google Fit data", Toast.LENGTH_SHORT).show();
                });
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d(TAG, "Checking POST_NOTIFICATIONS permission");
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission not granted, requesting...");
                if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    Toast.makeText(this, "Notifications are required for alarms to work", Toast.LENGTH_LONG).show();
                }
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                Log.d(TAG, "Permission already granted");
            }
        } else {
            Log.d(TAG, "Android version below 13, no permission needed");
        }
    }
}


