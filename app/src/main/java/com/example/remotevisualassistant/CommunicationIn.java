package com.example.remotevisualassistant;

public class CommunicationIn {
    String id_from, id_to, name_from, number_from, vid_url;
    boolean active;

    public CommunicationIn(){

    }

    public CommunicationIn(String id_from, String id_to, String name_from, String number_from, String vid_url){
        this.id_from = id_from;
        this.id_to = id_to;
        this.name_from = name_from;
        this.number_from = number_from;
        this.vid_url = vid_url;
        active = false;
    }

    public String getId_from() {
        return id_from;
    }

    public String getId_to() {
        return id_to;
    }

    public String getName_from() {
        return name_from;
    }

    public String getNumber_from() {
        return number_from;
    }

    public String getVid_url() {
        return vid_url;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
