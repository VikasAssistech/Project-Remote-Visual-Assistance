package com.example.remotevisualassistant;

public class CommunicationOut {
    String id_from, id_to, name_from, name_to, number_to;
    int waiting;

    public CommunicationOut(){

    }

//    public CommunicationOut(String id_from, String id_to, String name_from, String number_from, String vid_url){
//        this.id_from = id_from;
//        this.id_to = id_to;
//        this.name_from = name_from;
//        this.number_from = number_from;
//        this.vid_url = vid_url;
//        waiting = true;
//    }

    public CommunicationOut(String id_from, String id_to, String name_from, String name_to, String number_to, int waiting) {
        this.id_from = id_from;
        this.id_to = id_to;
        this.name_from = name_from;
        this.name_to = name_to;
        this.number_to = number_to;
        this.waiting = waiting;
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

//    public String getNumber_from() {
//        return number_from;
//    }
//
//    public String getVid_url() {
//        return vid_url;
//    }


    public void setId_from(String id_from) {
        this.id_from = id_from;
    }

    public void setId_to(String id_to) {
        this.id_to = id_to;
    }

    public void setName_from(String name_from) {
        this.name_from = name_from;
    }

    public String getName_to() {
        return name_to;
    }

    public void setName_to(String name_to) {
        this.name_to = name_to;
    }

    public String getNumber_to() {
        return number_to;
    }

    public void setNumber_to(String number_to) {
        this.number_to = number_to;
    }

    public int getWaiting() {
        return waiting;
    }

    public void setWaiting(int waiting) {
        this.waiting = waiting;
    }
}
