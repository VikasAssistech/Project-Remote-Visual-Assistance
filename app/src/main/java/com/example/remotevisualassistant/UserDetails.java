package com.example.remotevisualassistant;

public class UserDetails {
    String id, name, email, number, type;
    boolean set;
    String device_ip;

    public UserDetails(){

    }

    public UserDetails(String id, String name, String email, String number, String type){
        this.id = id;
        this.name = name;
        this.email = email;
        this.number = number;
        this.type = type;
        set = false;
        device_ip="";
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getNumber() {
        return number;
    }

    public String getType() {
        return type;
    }

    public boolean isSet() {
        return set;
    }

    public String getDevice_ip() {
        return device_ip;
    }

    public void setSet(boolean set) {
        this.set = set;
    }

    public void setDevice_ip(String device_ip) {
        this.device_ip = device_ip;
    }
}
