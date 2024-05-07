package com.example.medassistant.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.medassistant.entity.Medicine;
import com.example.medassistant.entity.Reminder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "reminders.db";
    private static final int DATABASE_VERSION = 1;

    //Reminders Table
    private static final String TABLE_REMINDERS = "reminders";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_TIME = "time";
    private static final String COLUMN_FREQUENCY = "frequency";
    private static final String COLUMN_ENABLED = "enabled";
    private static final String COLUMN_USER_EMAIL = "userEmail";

    //Medicine Table
    private static final String TABLE_MEDICINE = "medicines";
    private static final String M_COLUMN_ID = "_id";
    private static final String MEDICINE_NAME = "medicineName";
    private static final String MEDICINE_DOSAGE = "medicineDosage";
    private static final String MEDICINE_ROUTE = "medicineRoute";
    private static final String MEDICINE_REFILL_DATE = "refillDate";

    private static final String MEDICINE_DOCTOR_NAME = "doctorName";
    private static final String MEDICINE_USER = "userEmail";

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

        String createTableQuery2 = "CREATE TABLE " + TABLE_MEDICINE + "(" +
                M_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MEDICINE_NAME + " TEXT, " +
                MEDICINE_DOSAGE+ " TEXT, " +
                MEDICINE_ROUTE + " TEXT, " +
                MEDICINE_REFILL_DATE + " TEXT, " +
                MEDICINE_DOCTOR_NAME + " TEXT, " +
                MEDICINE_USER + " TEXT)";

        db.execSQL(createTableQuery);
        db.execSQL(createTableQuery2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DBHelper", "Upgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REMINDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICINE);
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

    public void addMedicine(Medicine medicine) throws JsonProcessingException {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MEDICINE_NAME, medicine.getMedicineName());
        values.put(MEDICINE_DOSAGE, medicine.getMedicineDosage()); // no need to convert to string
        values.put(MEDICINE_ROUTE, medicine.getRoute()); // convert to json string
        values.put(MEDICINE_REFILL_DATE, medicine.getRefillDate());
        values.put(MEDICINE_DOCTOR_NAME, medicine.getDoctorName());
        values.put(MEDICINE_USER, medicine.getUserEmail());
        db.insert(TABLE_MEDICINE, null, values);
        db.close();
    }

    @SuppressLint("Range")
    public List<Medicine> getAllMedicines(String userEmail) throws JsonProcessingException {
        List<Medicine> medicines = new ArrayList<>();
        String selectAllQuery = "SELECT * FROM " + TABLE_MEDICINE + " WHERE userEmail=?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectAllQuery, new String[]{userEmail});

        if (cursor.moveToFirst()) {
            do {
                Medicine medicine = new Medicine();

                medicine.setId(cursor.getInt(cursor.getColumnIndex(M_COLUMN_ID)));
                medicine.setMedicineName(cursor.getString(cursor.getColumnIndex(MEDICINE_NAME)));
                medicine.setMedicineDosage(cursor.getString(cursor.getColumnIndex(MEDICINE_DOSAGE)));
                medicine.setRoute(cursor.getString(cursor.getColumnIndex(MEDICINE_ROUTE)));
                medicine.setRefillDate(cursor.getString(cursor.getColumnIndex(MEDICINE_REFILL_DATE)));
                medicine.setDoctorName(cursor.getString(cursor.getColumnIndex(MEDICINE_DOCTOR_NAME)));
                medicine.setUserEmail(cursor.getString(cursor.getColumnIndex(MEDICINE_USER)));
                medicines.add(medicine);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return medicines;
    }

    @SuppressLint("Range")
    public Medicine getMedicineById(Long id) {
        Medicine medicine = new Medicine();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MEDICINE, null, M_COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            medicine = new Medicine();
            medicine.setId(cursor.getInt(cursor.getColumnIndex(M_COLUMN_ID)));
            medicine.setMedicineName(cursor.getString(cursor.getColumnIndex(MEDICINE_NAME)));
            medicine.setMedicineDosage(cursor.getString(cursor.getColumnIndex(MEDICINE_DOSAGE)));
            medicine.setRoute(cursor.getString(cursor.getColumnIndex(MEDICINE_ROUTE)));
            medicine.setRefillDate(cursor.getString(cursor.getColumnIndex(MEDICINE_REFILL_DATE)));
            medicine.setDoctorName(cursor.getString(cursor.getColumnIndex(MEDICINE_DOCTOR_NAME)));
            medicine.setUserEmail(cursor.getString(cursor.getColumnIndex(MEDICINE_USER)));
            cursor.close();
        }
        db.close();
        return medicine;
    }

    public void deleteMedicine(long medicineId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = M_COLUMN_ID + " = ?";
        String[] whereArgs = new String[]{String.valueOf(medicineId)};
        db.delete(TABLE_MEDICINE, whereClause, whereArgs);
        db.close();
    }



}
