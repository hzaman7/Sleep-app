package com.example.sleepapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;



import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReportActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    BarChart sleepHourChart;
    private static final String TAG = "ReportActivity";

    ImageView sleepHourShareIV,sleepHourDownloadIV;
    LineChart spo2_level_chart;
    ImageView spo2LevelShareIV,spo2LevelDownloadIV;
    ImageView heartRateShareIV,heartRateDownloadIV;
    LineChart heart_rate_chart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_new);


        db = FirebaseFirestore.getInstance();

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());


        setupSleepHoursChart();
        setupSpO2LevelChart();
        setupHeartRateChart();

        sleepHourChart = findViewById(R.id.sleep_hours_chart);
        spo2_level_chart = findViewById(R.id.spo2_level_chart);
        heart_rate_chart = findViewById(R.id.heart_rate_chart);




        sleepHourShareIV = findViewById(R.id.sleepHourShareIV);
        sleepHourShareIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = sleepHourChart.getChartBitmap();
                shareGraphImageAllOver(bitmap);
            }
        });
        sleepHourDownloadIV = findViewById(R.id.sleepHourDownloadIV);
        sleepHourDownloadIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = sleepHourChart.getChartBitmap();
                downloadGraphImageLocally(bitmap);
            }
        });




        spo2LevelShareIV = findViewById(R.id.spo2LevelShareIV);
        spo2LevelShareIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = spo2_level_chart.getChartBitmap();
                shareGraphImageAllOver(bitmap);
            }
        });
        spo2LevelDownloadIV = findViewById(R.id.spo2LevelDownloadIV);
        spo2LevelDownloadIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = spo2_level_chart.getChartBitmap();
                downloadGraphImageLocally(bitmap);
            }
        });


        heartRateShareIV = findViewById(R.id.heartRateShareIV);
        heartRateShareIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = heart_rate_chart.getChartBitmap();
                shareGraphImageAllOver(bitmap);
            }
        });
        heartRateDownloadIV = findViewById(R.id.heartRateDownloadIV);
        heartRateDownloadIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = heart_rate_chart.getChartBitmap();
                downloadGraphImageLocally(bitmap);
            }
        });


        findViewById(R.id.nav_home).setOnClickListener(v ->
                startActivity(new Intent(this, HomeActivity.class)));

        findViewById(R.id.nav_alarm).setOnClickListener(v ->
                startActivity(new Intent(this, AlarmActivity.class)));

        findViewById(R.id.nav_sounds).setOnClickListener(v ->
                startActivity(new Intent(this, SoundsActivity.class)));

        findViewById(R.id.nav_home).setOnClickListener(v ->
                startActivity(new Intent(this, HomeActivity.class)));

        findViewById(R.id.nav_report).setOnClickListener(v ->
                startActivity(new Intent(this, ReportActivity.class)));

        findViewById(R.id.nav_sleepHistory).setOnClickListener(v ->
                startActivity(new Intent(this, SavedAlarmsActivity.class)));




    }


    private void downloadGraphImageLocally(Bitmap bitmap) {
        try {

            File dir = new File(getExternalFilesDir("Pictures"), "SleepApp");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String fileName = "SleepChart_" + System.currentTimeMillis() + ".png";
            File file = new File(dir, fileName);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
            }


            MediaStore.Images.Media.insertImage(
                    getContentResolver(),
                    file.getAbsolutePath(),
                    fileName,
                    "Sleep Hours Chart"
            );

            Toast.makeText(this, "Chart saved to Pictures/SleepApp", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "Error saving chart", e);
            Toast.makeText(this, "Failed to save chart", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareGraphImageAllOver(Bitmap bitmap) {
        try {

            File cacheDir = new File(getCacheDir(), "images");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            File file = new File(cacheDir, "sleep_chart.png");

            try (FileOutputStream fos = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
            }


            Uri uri = FileProvider.getUriForFile(
                    this,
                    "com.example.sleepapplication.fileprovider",
                    file
            );

            // Create Share Intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "My Sleep Hours Chart");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Share Chart"));
        } catch (IOException e) {
            Log.e(TAG, "Error sharing chart", e);
            Toast.makeText(this, "Failed to share chart", Toast.LENGTH_SHORT).show();
        }
    }


    private void setupSleepHoursChart() {

        List<BarEntry> entries = new ArrayList<>();
        List<String> days = new ArrayList<>();

        db.collection("sleep_records")
                .orderBy("day")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int index = 0;
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String day = document.getString("day");
                        float hours = document.getDouble("hours").floatValue();
                        entries.add(new BarEntry(index++, hours));
                        days.add(day);
                    }

                    BarDataSet dataSet = new BarDataSet(entries, "Sleep Hours");
                    dataSet.setColor(Color.parseColor("#6200EA"));
                    dataSet.setValueTextColor(Color.WHITE);
                    dataSet.setValueTextSize(10f);

                    BarData barData = new BarData(dataSet);
                    barData.setBarWidth(0.4f);

                    sleepHourChart.setData(barData);
                    sleepHourChart.getDescription().setEnabled(false);
                    sleepHourChart.getLegend().setTextColor(Color.WHITE);
                    sleepHourChart.setFitBars(true);

                    XAxis xAxis = sleepHourChart.getXAxis();
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setTextColor(Color.WHITE);
                    xAxis.setGranularity(1f);

                    sleepHourChart.getAxisLeft().setTextColor(Color.WHITE);
                    sleepHourChart.getAxisRight().setEnabled(false);
                    sleepHourChart.invalidate();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching sleep data", e);
                    Toast.makeText(this, "Failed to load sleep data", Toast.LENGTH_SHORT).show();
                    // Fallback to dummy data
                    float[] sleepHours = {7.5f, 6.8f, 8.0f, 7.2f, 6.5f, 7.8f, 7.0f};
                    String[] fallbackDays = {"Jun 1", "Jun 2", "Jun 3", "Jun 4", "Jun 5", "Jun 6", "Jun 7"};
                    for (int i = 0; i < sleepHours.length; i++) {
                        entries.add(new BarEntry(i, sleepHours[i]));
                        days.add(fallbackDays[i]);
                    }
                    updateBarChart(sleepHourChart, entries, days);
                });
    }

    private void setupSpO2LevelChart() {

        List<Entry> entries = new ArrayList<>();
        List<String> days = new ArrayList<>();

        db.collection("spo2_records")
                .orderBy("day")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int index = 0;
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String day = document.getString("day");
                        float level = document.getDouble("level").floatValue();
                        entries.add(new Entry(index++, level));
                        days.add(day);
                    }

                    updateLineChart(spo2_level_chart, entries, days, "SpO2 Level (%)", "#4DB6AC");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching SpO2 data", e);
                    Toast.makeText(this, "Failed to load SpO2 data", Toast.LENGTH_SHORT).show();
                    // Fallback to dummy data
                    float[] spo2Levels = {97.5f, 96.8f, 98.0f, 97.2f, 95.5f, 98.5f, 97.0f};
                    String[] fallbackDays = {"Jun 1", "Jun 2", "Jun 3", "Jun 4", "Jun 5", "Jun 6", "Jun 7"};
                    for (int i = 0; i < spo2Levels.length; i++) {
                        entries.add(new Entry(i, spo2Levels[i]));
                        days.add(fallbackDays[i]);
                    }
                    updateLineChart(spo2_level_chart, entries, days, "SpO2 Level (%)", "#4DB6AC");
                });
    }

    private void setupHeartRateChart() {

        List<Entry> entries = new ArrayList<>();
        List<String> days = new ArrayList<>();

        db.collection("heart_rate_records")
                .orderBy("day")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int index = 0;
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String day = document.getString("day");
                        float rate = document.getDouble("rate").floatValue();
                        entries.add(new Entry(index++, rate));
                        days.add(day);
                    }

                    updateLineChart(heart_rate_chart, entries, days, "Heart Rate (BPM)", "#6200EA");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching heart rate data", e);
                    Toast.makeText(this, "Failed to load heart rate data", Toast.LENGTH_SHORT).show();
                    // Fallback to dummy data
                    float[] heartRates = {72f, 68f, 75f, 70f, 65f, 78f, 73f};
                    String[] fallbackDays = {"Jun 1", "Jun 2", "Jun 3", "Jun 4", "Jun 5", "Jun 6", "Jun 7"};
                    for (int i = 0; i < heartRates.length; i++) {
                        entries.add(new Entry(i, heartRates[i]));
                        days.add(fallbackDays[i]);
                    }
                    updateLineChart(heart_rate_chart, entries, days, "Heart Rate (BPM)", "#6200EA");
                });
    }

    private void updateBarChart(BarChart chart, List<BarEntry> entries, List<String> days) {
        BarDataSet dataSet = new BarDataSet(entries, "Sleep Hours");
        dataSet.setColor(Color.parseColor("#6200EA"));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.4f);

        chart.setData(barData);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setTextColor(Color.WHITE);
        chart.setFitBars(true);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setGranularity(1f);

        chart.getAxisLeft().setTextColor(Color.WHITE);
        chart.getAxisRight().setEnabled(false);
        chart.invalidate();
    }

    private void updateLineChart(LineChart chart, List<Entry> entries, List<String> days, String label, String color) {
        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(Color.parseColor(color));
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(Color.WHITE);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10f);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setTextColor(Color.WHITE);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setGranularity(1f);

        chart.getAxisLeft().setTextColor(Color.WHITE);
        chart.getAxisRight().setEnabled(false);
        chart.invalidate();
    }


}



