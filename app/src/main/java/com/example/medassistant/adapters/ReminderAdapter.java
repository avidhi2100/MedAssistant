package com.example.medassistant.adapters;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.medassistant.R;
import com.example.medassistant.alarm.AlarmReceiver;
import com.example.medassistant.database.DBHelper;
import com.example.medassistant.entity.Reminder;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ReminderAdapter extends ArrayAdapter<Reminder> {

    private List<Reminder> reminders;
    private Context context;
    DBHelper dbHelper;

    public ReminderAdapter(@NonNull Context context, List<Reminder> reminders) {
        super(context, 0, reminders);
        this.reminders = reminders;
        this.context = context;
    }

    @SuppressLint({"SetTextI18n", "ScheduleExactAlarm"})
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.list_item_reminder, parent, false);
        }

        dbHelper = new DBHelper(this.getContext());
        Reminder currentReminder = reminders.get(position);

        TextView titleTextView = listItemView.findViewById(R.id.title_text_view);
        titleTextView.setText(currentReminder.getTitle());

        TextView statusTextView = listItemView.findViewById(R.id.status_text_view);
        statusTextView.setText(currentReminder.isEnabled() ? "Enabled" : "Disabled");

        Date date = new Date(currentReminder.getTime());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        TextView timeTextView = listItemView.findViewById(R.id.time_text_view);
        timeTextView.setText(sdf.format(date));

        int frequency = currentReminder.getFrequency().size();
        TextView frequencyTextView = listItemView.findViewById(R.id.frequency_text_view);
        frequencyTextView.setText(frequency + " Times A Week");

//        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enabledSwitch = listItemView.findViewById(R.id.enabled_switch);
//        enabledSwitch.setOnCheckedChangeListener(null);
//        enabledSwitch.setChecked(currentReminder.isEnabled());
//
//        enabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            currentReminder.setEnabled(isChecked);
//            if (isChecked) {
//                // Reminder is being enabled
//                setAlarmsForReminder(currentReminder);
//                try {
//                    dbHelper.updateReminder(currentReminder);
//                } catch (JsonProcessingException e) {
//                    throw new RuntimeException(e);
//                }
//                Toast.makeText(this.getContext(), "Reminder successfully enabled", Toast.LENGTH_SHORT).show();
//            } else {
//                // Reminder is being disabled
//                Toast.makeText(this.getContext(), "Delete alarm to disable it", Toast.LENGTH_SHORT).show();
//            }
//            this.notifyDataSetChanged();
//        });

        ImageButton delete = listItemView.findViewById(R.id.delete_button);
        delete.setOnClickListener(view -> {
            cancelAlarmForAllDays(currentReminder);
            dbHelper.deleteReminder(currentReminder.getId());
            Toast.makeText(this.getContext(), "Reminder successfully deleted", Toast.LENGTH_SHORT).show();

            reminders.remove(currentReminder);
            this.notifyDataSetChanged();
        });

        return listItemView;
    }

    @SuppressLint("ScheduleExactAlarm")
    private void setAlarmsForReminder(Reminder reminder) {
        if (!reminder.isEnabled()) return;
        cancelAlarmForAllDays(reminder);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(reminder.getTime());
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        for (int day : reminder.getFrequency()) {
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.set(Calendar.DAY_OF_WEEK, day);

            Intent intent = new Intent(this.getContext(), AlarmReceiver.class);
            intent.putExtra("alarm_time", reminder.getTime());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getContext(), (int) ((int) reminder.getId() * 100 + day), intent, PendingIntent.FLAG_IMMUTABLE);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    private void cancelAlarmForAllDays(Reminder reminder) {
        for (int day : reminder.getFrequency()) {
            cancelAlarm(reminder, day);
        }

        try {
            dbHelper.updateReminder(reminder);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void cancelAlarm(Reminder reminder, int dayOfWeek) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) ((int) reminder.getId() * 100 + dayOfWeek), intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

}