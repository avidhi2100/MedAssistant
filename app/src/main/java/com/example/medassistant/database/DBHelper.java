package com.example.medassistant.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.medassistant.entity.Reminder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "reminders.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_REMINDERS = "reminders";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_TIME = "time";
    private static final String COLUMN_FREQUENCY = "frequency";
    private static final String COLUMN_ENABLED = "enabled";
    private static final String COLUMN_USER_EMAIL = "userEmail";

    private ObjectMapper objectMapper = new ObjectMapper();
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_REMINDERS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_TIME + " INTEGER, " + // changed data type from TEXT to INTEGER
                COLUMN_FREQUENCY + " TEXT, " +
                COLUMN_USER_EMAIL + " TEXT, " +
                COLUMN_ENABLED + " INTEGER)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DBHelper", "Upgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REMINDERS);
        onCreate(db);

    }

    public void addReminder(Reminder reminder) throws JsonProcessingException {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, reminder.getTitle());
        values.put(COLUMN_TIME, reminder.getTime()); // no need to convert to string
        values.put(COLUMN_FREQUENCY, objectMapper.writeValueAsString(reminder.getFrequency())); // convert to json string
        values.put(COLUMN_USER_EMAIL, reminder.getUserEmail());
        values.put(COLUMN_ENABLED, 1);
        db.insert(TABLE_REMINDERS, null, values);
        db.close();
    }

    @SuppressLint("Range")
    public List<Reminder> getAllReminders(String userEmail) throws JsonProcessingException {
        List<Reminder> reminders = new ArrayList<>();
        String selectAllQuery = "SELECT * FROM " + TABLE_REMINDERS + " WHERE userEmail=?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectAllQuery, new String[]{userEmail});

        if (cursor.moveToFirst()) {
            do {
                Reminder reminder = new Reminder();
                reminder.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                reminder.setTitle(cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)));
                reminder.setTime(cursor.getLong(cursor.getColumnIndex(COLUMN_TIME)));
                try {
                    reminder.setFrequency(objectMapper.readValue(cursor.getString(cursor.getColumnIndex(COLUMN_FREQUENCY)), List.class));
                    reminder.setEnabled(cursor.getInt(cursor.getColumnIndex(COLUMN_ENABLED)) == 1);
                    reminder.setUserEmail(cursor.getString(cursor.getColumnIndex(COLUMN_USER_EMAIL)));
                    reminders.add(reminder);
                } catch (JsonProcessingException e) {
                    // Log the error or handle it as needed
                    Log.e("Error while conversion",e.toString());
                    // Optionally, skip adding the reminder or handle it differently
                }            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return reminders;
    }

    public void deleteReminder(long reminderId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = COLUMN_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(reminderId)};
        db.delete(TABLE_REMINDERS, whereClause, whereArgs);
        db.close();
    }

    public void updateReminder(Reminder reminder) throws JsonProcessingException {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, reminder.getTitle());
        values.put(COLUMN_TIME, reminder.getTime());
        values.put(COLUMN_FREQUENCY, objectMapper.writeValueAsString(reminder.getFrequency()));
        values.put(COLUMN_USER_EMAIL, reminder.getUserEmail());
        values.put(COLUMN_ENABLED, reminder.isEnabled() ? 1 : 0);
        String whereClause = COLUMN_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(reminder.getId())};
        db.update(TABLE_REMINDERS, values, whereClause, whereArgs);
        db.close();
    }

}
