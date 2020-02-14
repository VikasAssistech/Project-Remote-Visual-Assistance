package com.example.remotevisualassistant;

import android.telecom.Call;

import java.util.ArrayList;
import java.util.List;

public class AllLogs {
    List<CallLog> logList;

    public AllLogs(){

    }

    public AllLogs(CallLog callLog){
        this.logList = new ArrayList<CallLog>();
        this.logList.add(callLog);
    }

    public AllLogs(List<CallLog> logList) {
        this.logList = logList;
    }

    public List<CallLog> getLogList() {
        return logList;
    }

    public CallLog get_ith_log(int i){
        return logList.get(i);
    }

    public void setLogList(List<CallLog> logList) {
        this.logList = logList;
    }

    public int getSize() {
        return logList.size()-1;
    }

}
