package com.example.remotevisualassistant;

import java.util.List;

public class UserContacts {
    String id;
    List<String> clist;
    String contactNumbers, contactNames;

    public UserContacts(){

    }

//    public UserContacts(String id, List<String> clist) {
//        this.id = id;
//        this.clist = clist;
//    }

    public UserContacts(String id, List<String> clist, String contactNumbers, String contactNames) {
        this.id = id;
        this.clist = clist;
        this.contactNumbers = contactNumbers;
        this.contactNames = contactNames;
    }

    public String getContactNumbers() {
        return contactNumbers;
    }

    public String getContactNames() {
        return contactNames;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getClist() {
        return clist;
    }

    public void addContact(String s, String n1, String n2){
        clist.add(s);
        contactNames = contactNames+n1+";";
        contactNumbers = contactNumbers+n2+";";
    }

    public boolean hasContact(String n){
        for(int i=1;i<clist.size();i++){
            if(clist.get(i).toString().equals(n)){
                return true;
            }
        }
        return false;
    }

    public String getith(int n){
        return clist.get(n+1);
    }

    public int getSize(){
        return clist.size()-1;
    }

    public void setClist(List<String> clist) {
        this.clist = clist;
    }
}
