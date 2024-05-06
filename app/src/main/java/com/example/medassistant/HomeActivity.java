package com.example.medassistant;

import android.annotation.SuppressLint;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import android.widget.ListView;

import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.medassistant.adapters.ReminderAdapter;
import com.example.medassistant.database.DBHelper;
import com.example.medassistant.entity.Reminder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private List<Reminder> reminders = new ArrayList<>();
    private DBHelper dbHelper;
    private FirebaseAuth auth;
    ListView reminderListView;
    BottomNavigationView bottomNavigationView;

    @SuppressLint({"NonConstantResourceId", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        dbHelper = new DBHelper(this);
        auth = FirebaseAuth.getInstance();
        reminderListView = findViewById(R.id.recyclerViewReminders);

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            if (menuItem.getItemId() == R.id.menu_home) {
                // Handle Home button click
                Intent intent = new Intent(this, MedicinesActivity.class);
                startActivity(intent);
                return true;
            } else if (menuItem.getItemId() == R.id.menu_ocr) {
                Intent intent = new Intent(this, OCR.class);
                startActivity(intent);
                return true;
            } else if (menuItem.getItemId() == R.id.menu_chat) {
                Intent intent = new Intent(this, ChatbotInterfaceActivity.class);
                startActivity(intent);
                return true;
            }
            else if (menuItem.getItemId() == R.id.menu_logout) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("Confirmation")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            Intent intent = new Intent(this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            dialog.dismiss();
                        });
                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            }
            return false;
        });

        bottomNavigationView.getMenu().findItem(R.id.menu_reminders).setChecked(true);



        try {
            reminders = dbHelper.getAllReminders(auth.getCurrentUser().getEmail());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        ReminderAdapter adapter = new ReminderAdapter(this, reminders);
        reminderListView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.getMenu().findItem(R.id.menu_reminders).setChecked(true);

    }
}
