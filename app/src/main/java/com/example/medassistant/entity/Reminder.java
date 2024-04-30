package com.example.medassistant.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class Reminder implements Parcelable {

    public long id;
    public String title;
    public Long time;
    public List<Integer> frequency;
    public boolean enabled;

    public String userEmail;

    public Reminder() {
    }

    public Reminder(String title, Long time, List<Integer> frequency, boolean enabled, String userEmail) {
        this.title = title;
        this.time = time;
        this.frequency = frequency;
        this.enabled = enabled;
        this.userEmail = userEmail;
    }

    protected Reminder(Parcel in) {
        id = in.readLong();
        title = in.readString();
        time = in.readLong();
        in.readList(frequency, Integer.class.getClassLoader());
        enabled = in.readByte() != 0;
        userEmail = in.readString();
    }

    public static final Creator<Reminder> CREATOR = new Creator<Reminder>() {
        @Override
        public Reminder createFromParcel(Parcel in) {
            return new Reminder(in);
        }

        @Override
        public Reminder[] newArray(int size) {
            return new Reminder[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public List<Integer> getFrequency() {
        return frequency;
    }

    public void setFrequency(List<Integer> frequency) {
        this.frequency = frequency;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeLong(time);
        dest.writeList(frequency);
        dest.writeByte((byte) (enabled ? 1 : 0));
        dest.writeString(userEmail);
    }
}