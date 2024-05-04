package com.example.medassistant;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.medassistant.adapters.ReminderAdapter;
import com.example.medassistant.database.DBHelper;
import com.example.medassistant.entity.Medicine;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;
import java.util.List;

public class MedicinesActivity extends AppCompatActivity {

    DBHelper dbHelper;
    FirebaseAuth auth;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medicines);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.medicineActivity), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.);
//            return insets;
//        });

        dbHelper = new DBHelper(this);
        auth = FirebaseAuth.getInstance();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
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
                // Handle Chat button click
                Intent intent = new Intent(this, ChatbotInterfaceActivity.class);
                startActivity(intent);
                return true;
            }
            else if (menuItem.getItemId() == R.id.menu_reminders) {
                // Handle Chat button click
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
                return true;
            }
            else if (menuItem.getItemId() == R.id.menu_logout) {
                // Handle Chat button click
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });

        LinearLayout linearMedicines = findViewById(R.id.linearMedicines);

        List<Medicine> medicines = null;
        try {
            medicines = dbHelper.getAllMedicines(auth.getCurrentUser().getEmail());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        medicines.add(new Medicine(1,"Amoxylin","2x PER Day","ORAL","08-12-2024","Not present","vidhi0821@gmail.com"));

        for (Medicine medicine : medicines) {
            View medicineBlock = getLayoutInflater().inflate(R.layout.item_medicine_block, null);

            TextView textViewMedicineName = medicineBlock.findViewById(R.id.textViewMedicineName);
            textViewMedicineName.setText(medicine.getMedicineName());

            medicineBlock.setOnClickListener(view -> {
                Intent intent = new Intent(this, MedicineDetailsActivity.class);
                intent.putExtra("medicineId", medicine.getId());
                startActivity(intent);
            });

            linearMedicines.addView(medicineBlock);
        }


    }
}