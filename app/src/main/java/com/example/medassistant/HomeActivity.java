package com.example.medassistant;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.medassistant.adapters.ReminderAdapter;
import com.example.medassistant.alarm.AlarmReceiver;
import com.example.medassistant.database.DBHelper;
import com.example.medassistant.entity.Reminder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    private List<Reminder> reminders = new ArrayList<>();
    private List<Integer> days = new ArrayList<>();
    private DBHelper dbHelper;
    private FirebaseAuth auth;
    Button addReminderButton;
    Button monday;
    Button tuesday;
    Button wednesday;
    Button thursday;
    Button friday;
    Button saturday;
    Button sunday;
    ListView reminderListView;

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
        addReminderButton = findViewById(R.id.btnAddReminder);
        createNotificationChannel();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            if (menuItem.getItemId() == R.id.menu_home) {
                // Handle Home button click
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
                return true;
            } else if (menuItem.getItemId() == R.id.menu_ocr) {
                Intent intent = new Intent(this, OCR.class);
                startActivity(intent);
                return true;
            } else if (menuItem.getItemId() == R.id.menu_chat) {
                // Handle Chat button click
                Toast.makeText(HomeActivity.this, "Chat clicked", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        addReminderButton.setOnClickListener(view -> {
            addReminder();
        });
        try {
            reminders = dbHelper.getAllReminders(auth.getCurrentUser().getEmail());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        ReminderAdapter adapter = new ReminderAdapter(this, reminders);
        reminderListView.setAdapter(adapter);
    }

    private void createNotificationChannel() {
        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        String channelId = "default";
        CharSequence channelName = "MedAssistant";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        channel.setDescription("MedAssistant notifications");
        notificationManager.createNotificationChannel(channel);
    }

    public void addReminder() {
        days = new ArrayList<>();
        @SuppressLint("ScheduleExactAlarm") AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add Reminder")
                .setPositiveButton("Add", (dialog1, which) -> {

                    EditText titleEditText = ((AlertDialog) dialog1).findViewById(R.id.titleEditText);
                    TimePicker picker = ((AlertDialog) dialog1).findViewById(R.id.timePicker);
                    int hour = picker.getHour();
                    int minute = picker.getMinute();

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);

                    String title = String.valueOf(titleEditText.getText());

                    if (!title.isEmpty()  && !days.isEmpty()) {
                        Reminder reminder = new Reminder(title, calendar.getTimeInMillis(), days,true, Objects.requireNonNull(auth.getCurrentUser()).getEmail());


//                        calendar.setTimeInMillis(reminder.getTime());

                        for (int day : reminder.getFrequency()) {
                            Calendar alarmCalendar = Calendar.getInstance(); // Create a new Calendar instance for each day
                            alarmCalendar.setTimeInMillis(reminder.getTime());
                            alarmCalendar.set(Calendar.HOUR_OF_DAY, hour);
                            alarmCalendar.set(Calendar.MINUTE, minute);
                            alarmCalendar.set(Calendar.SECOND, 0);
                            alarmCalendar.set(Calendar.MILLISECOND, 0);
                            alarmCalendar.set(Calendar.DAY_OF_WEEK, day);

                            Intent intent = new Intent(this, AlarmReceiver.class);
                            intent.putExtra("reminder", reminder);
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) ((int) reminder.getId() * 100 + day), intent, PendingIntent.FLAG_IMMUTABLE);

                            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmCalendar.getTimeInMillis(), pendingIntent);
                        }


                        try {
                            dbHelper.addReminder(reminder);
                            reminders = dbHelper.getAllReminders(auth.getCurrentUser().getEmail());
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                        ReminderAdapter adapter = new ReminderAdapter(HomeActivity.this, reminders);
                        reminderListView.setAdapter(adapter);
                        Toast.makeText(this, "Reminder successfully enabled", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(HomeActivity.this, "Please enter all the fields", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .setView(R.layout.dialog_add_reminder)
                .create();


        dialog.setOnShowListener(dialogInterface -> {
            monday = dialog.findViewById(R.id.day_of_week_1);
            tuesday = dialog.findViewById(R.id.day_of_week_2);
            wednesday = dialog.findViewById(R.id.day_of_week_3);
            thursday = dialog.findViewById(R.id.day_of_week_4);
            friday = dialog.findViewById(R.id.day_of_week_5);
            saturday = dialog.findViewById(R.id.day_of_week_6);
            sunday = dialog.findViewById(R.id.day_of_week_7);

            monday.setOnClickListener(view -> {
                toggleDayButton(monday, Calendar.MONDAY);
            });
            tuesday.setOnClickListener(view -> {
                toggleDayButton(tuesday, Calendar.TUESDAY);
            });
            wednesday.setOnClickListener(view -> {
                toggleDayButton(wednesday,Calendar.WEDNESDAY);
            });
            thursday.setOnClickListener(view -> {
                toggleDayButton(thursday,Calendar.THURSDAY);
            });
            friday.setOnClickListener(view -> {
                toggleDayButton(friday, Calendar.FRIDAY);
            });
            saturday.setOnClickListener(view -> {
                toggleDayButton(saturday, Calendar.SATURDAY);
            });
            sunday.setOnClickListener(view -> {
                toggleDayButton(sunday, Calendar.SUNDAY);
            });
        });

        dialog.show();

    }

    private void toggleDayButton(Button button, int day) {
        Typeface boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD);
        Typeface normalTypeface = Typeface.defaultFromStyle(Typeface.NORMAL);
        if (button.isSelected()) {
            // Button is already selected, deselect it
            button.setSelected(false);
            days.remove(Integer.valueOf(day));
            button.setTypeface(normalTypeface);
        } else {
            // Button is not selected, select it
            button.setSelected(true);
            days.add(day);
            button.setTypeface(boldTypeface);
        }
    }



}
