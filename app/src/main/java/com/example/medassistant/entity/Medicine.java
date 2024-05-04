package com.example.medassistant.entity;

import java.util.Date;


public class Medicine {

    public long id;
    public String medicineName;
    public String medicineDosage;
    public String route;
    public String refillDate;
    public String doctorName;
    public String userEmail;

    public Medicine() {
    }

    public Medicine(long id, String medicineName, String medicineDosage, String route, String refillDate, String doctorName, String userEmail) {
        this.id = id;
        this.medicineName = medicineName;
        this.medicineDosage = medicineDosage;
        this.route = route;
        this.refillDate = refillDate;
        this.doctorName = doctorName;
        this.userEmail = userEmail;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getMedicineDosage() {
        return medicineDosage;
    }

    public void setMedicineDosage(String medicineDosage) {
        this.medicineDosage = medicineDosage;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getRefillDate() {
        return refillDate;
    }

    public void setRefillDate(String refillDate) {
        this.refillDate = refillDate;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
