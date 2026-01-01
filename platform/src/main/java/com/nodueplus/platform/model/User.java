package com.nodueplus.platform.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String id;
    private String name;
    private String role;
    private String department;
    private String year;
    private String password;
    private String status = "IDLE"; 
    
    private List<Activity> myActivities = new ArrayList<>();

    public User() {}

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<Activity> getMyActivities() { return myActivities; }
    public void setMyActivities(List<Activity> myActivities) { this.myActivities = myActivities; }

    // --- INNER CLASS: ACTIVITY ---
    public static class Activity {
        private String type;
        private String eventName;
        private String place;
        private String date;
        private String college;
        private String certificateImage;
        private String status = "PENDING"; 
        private String rejectionReason; 

        public Activity() {}

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getEventName() { return eventName; }
        public void setEventName(String eventName) { this.eventName = eventName; }
        public String getPlace() { return place; }
        public void setPlace(String place) { this.place = place; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getCollege() { return college; }
        public void setCollege(String college) { this.college = college; }
        public String getCertificateImage() { return certificateImage; }
        public void setCertificateImage(String certificateImage) { this.certificateImage = certificateImage; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getRejectionReason() { return rejectionReason; }
        public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    }
}