package com.example.remotevisualassistant;

public class NssDetails {
    String id, entry_no;
    double hours;
    boolean registered;

    public NssDetails(){

    }

    public NssDetails(String id, String entry_no, double hours, boolean registered) {
        this.id = id;
        this.entry_no = entry_no;
        this.hours = hours;
        this.registered = registered;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEntry_no() {
        return entry_no;
    }

    public void setEntry_no(String entry_no) {
        this.entry_no = entry_no;
    }

    public double getHours() {
        return hours;
    }

    public void setHours(double hours) {
        this.hours = hours;
    }

    public void addHours(double hours){
        this.hours += hours;
    }
}
