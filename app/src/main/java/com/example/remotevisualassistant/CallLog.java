package com.example.remotevisualassistant;

public class CallLog {
    String from_id, to_id, from_name, to_name;
    String call_time;
    String rating;

    public CallLog(){

    }

    public CallLog(String from_id, String to_id, String from_name, String to_name, String call_time, String rating) {
        this.from_id = from_id;
        this.to_id = to_id;
        this.from_name = from_name;
        this.to_name = to_name;
        this.call_time = call_time;
        this.rating = rating;
    }

    public String getFrom_id() {
        return from_id;
    }

    public void setFrom_id(String from_id) {
        this.from_id = from_id;
    }

    public String getTo_id() {
        return to_id;
    }

    public void setTo_id(String to_id) {
        this.to_id = to_id;
    }

    public String getFrom_name() {
        return from_name;
    }

    public void setFrom_name(String from_name) {
        this.from_name = from_name;
    }

    public String getTo_name() {
        return to_name;
    }

    public void setTo_name(String to_name) {
        this.to_name = to_name;
    }

    public String getCall_time() {
        return call_time;
    }

    public void setCall_time(String call_time) {
        this.call_time = call_time;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }
}
