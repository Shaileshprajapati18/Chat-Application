package com.example.chattingapplication.Model;

import java.util.ArrayList;

public class statusModel {

    private String name;
    private String profileImage;
    private long lastUpdated;
    private ArrayList<status> statuses;

    public statusModel() {

    }

    public statusModel(String name, String profileImage, long lastUpdated, ArrayList<status> statuses) {
        this.name = name;
        this.profileImage = profileImage;
        this.lastUpdated = lastUpdated;
        this.statuses = statuses;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public ArrayList<status> getStatuses() {
        return statuses;
    }

    public void setStatuses(ArrayList<status> statuses) {
        this.statuses = statuses;
    }
}
