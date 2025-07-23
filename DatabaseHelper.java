package com.example.sleepapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "SleepApp.db";
    private static final int DATABASE_VERSION = 1;

    // Table and column names
    public static final String TABLE_SLEEP_HOURS = "sleepHours";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_SLEEP_HOURS = "sleepHours";
    public static final String COLUMN_SLEEP_MINUTES = "sleepMinutes";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_IS_ALARM = "isAlarm";



    private static final String CREATE_TABLE_SLEEP_HOURS =
            "CREATE TABLE " + TABLE_SLEEP_HOURS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_SLEEP_HOURS + " INTEGER NOT NULL, " +
                    COLUMN_SLEEP_MINUTES + " INTEGER NOT NULL CHECK(sleepMinutes >= 0 AND sleepMinutes < 60), " +
                    COLUMN_TITLE + " TEXT NOT NULL, " +
                    COLUMN_IS_ALARM + " INTEGER NOT NULL, " +
                    COLUMN_DATE + " TEXT NOT NULL);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating table: " + TABLE_SLEEP_HOURS);
        db.execSQL(CREATE_TABLE_SLEEP_HOURS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SLEEP_HOURS);
        onCreate(db);
    }


    public long insertSleepRecord(int sleepHours, int sleepMinutes, String title, boolean isAlarm, String date) {
        if (sleepMinutes < 0 || sleepMinutes >= 60) {
            Log.e(TAG, "Invalid sleepMinutes: " + sleepMinutes);
            return -1;
        }
        if (!isValidDateFormat(date)) {
            Log.e(TAG, "Invalid date format: " + date);
            return -1;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SLEEP_HOURS, sleepHours);
        values.put(COLUMN_SLEEP_MINUTES, sleepMinutes);
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_IS_ALARM, isAlarm ? 1 : 0);
        values.put(COLUMN_DATE, date);

        long id = db.insert(TABLE_SLEEP_HOURS, null, values);
        db.close();
        Log.d(TAG, "Inserted sleep record with ID: " + id);
        return id;
    }



    public ArrayList<AlarmModel> getAllSleepRecords() {
        ArrayList<AlarmModel> alarmList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_SLEEP_HOURS;

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                AlarmModel alarm = new AlarmModel();
                alarm.setSleepHours(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SLEEP_HOURS)));
                alarm.setSleepMinutes(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SLEEP_MINUTES)));
                alarm.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                alarm.setAlarm(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ALARM)) == 1);
                alarm.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
                alarmList.add(alarm);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        Log.d(TAG, "Retrieved " + alarmList.size() + " sleep records");
        return alarmList;
    }


    private boolean isValidDateFormat(String date) {
        if (date == null) return false;
        return date.matches("\\d{2}/\\d{2}/\\d{4}");
    }


    public AlarmModel getLastSleepRecord() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_SLEEP_HOURS + " ORDER BY " + COLUMN_ID + " DESC LIMIT 1";

        Cursor cursor = db.rawQuery(query, null);
        AlarmModel alarm = null;
        if (cursor.moveToFirst()) {
            alarm = new AlarmModel();
            alarm.setSleepHours(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SLEEP_HOURS)));
            alarm.setSleepMinutes(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SLEEP_MINUTES)));
            alarm.setTitle(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TITLE))));
            alarm.setAlarm(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ALARM)) == 1);
            alarm.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
        }
        cursor.close();
        db.close();
        Log.d(TAG, "Retrieved last sleep record: " + (alarm != null ? alarm.getDate() : "null"));
        return alarm;
    }
}