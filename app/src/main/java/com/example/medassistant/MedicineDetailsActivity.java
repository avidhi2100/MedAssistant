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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.medassistant.adapters.ReminderAdapter;
import com.example.medassistant.alarm.AlarmReceiver;
import com.example.medassistant.database.DBHelper;
import com.example.medassistant.entity.Medicine;
import com.example.medassistant.entity.Reminder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class MedicineDetailsActivity extends AppCompatActivity {

    DBHelper dbHelper;
    Button addReminderButton;
    private List<Integer> days = new ArrayList<>();
    private FirebaseAuth auth;
    Button monday;
    Button tuesday;
    Button wednesday;
    Button thursday;
    Button friday;
    Button saturday;
    Button sunday;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medicine_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.medicineDetails), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dbHelper = new DBHelper(this);
        auth = FirebaseAuth.getInstance();
        createNotificationChannel();
        TextView textViewMedicineName = findViewById(R.id.textViewMedicineName);
        TextView textViewMedicineDosage = findViewById(R.id.medicineDosageTextView);
        TextView textViewMedicineRoute = findViewById(R.id.routeTextView);
        TextView textViewMedicineRefillDate = findViewById(R.id.refillDateTextView);
        TextView textViewMedicineDoctorName = findViewById(R.id.doctorNameTextView);

        Intent intent = this.getIntent();
        Long medicineId = intent.getLongExtra("medicineId", 0);
//        Medicine medicine = dbHelper.getMedicineById(medicineId);
        Medicine medicine = new Medicine(1,"Amoxylin","2x PER Day","ORAL","08-12-2024","Not present","vidhi0821@gmail.com");

        textViewMedicineName.setText(medicine.getMedicineName());
        textViewMedicineDosage.setText(medicine.getMedicineDosage());
        textViewMedicineRoute.setText(medicine.getRoute());
        textViewMedicineRefillDate.setText(medicine.getRefillDate());
        textViewMedicineDoctorName.setText(medicine.getDoctorName());

        addReminderButton = findViewById(R.id.buttonAddReminder);
        addReminderButton.setOnClickListener(view -> {
            addReminder();
        });
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
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                            Toast.makeText(this, "Reminder successfully enabled", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MedicineDetailsActivity.this, "Please enter all the fields", Toast.LENGTH_SHORT).show();
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

    private void createNotificationChannel() {
        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        String channelId = "default";
        CharSequence channelName = "MedAssistant";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        channel.setDescription("MedAssistant notifications");
        notificationManager.createNotificationChannel(channel);
    }
}