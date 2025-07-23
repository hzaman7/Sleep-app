package com.example.sleepapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SavedAlarmsActivity extends AppCompatActivity {

    private RecyclerView alarmList;
    private ArrayList<AlarmModel> alarms;
    private AlarmAdapter alarmAdapter;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_alarms);

        // RecyclerView setup
        alarmList = findViewById(R.id.alarm_list);
        alarmList.setLayoutManager(new LinearLayoutManager(this));
        dbHelper = new DatabaseHelper(this);


        alarms = new ArrayList<>();


        alarms =  dbHelper.getAllSleepRecords();
//        alarms.add(new AlarmModel("07:30 AM", "Work", true));
//        alarms.add(new AlarmModel("08:45 AM", "Gym",  false));
//        alarms.add(new AlarmModel("10:00 PM", "Wind Down", true));

        // Attach adapter
        alarmAdapter = new AlarmAdapter(alarms);
        alarmList.setAdapter(alarmAdapter);


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


    }
}
