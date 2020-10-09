package com.example.our_capstone;

import java.util.ArrayList;

public class VoRoomInfo {
    private String key;
    private ArrayList users = new ArrayList();

    public VoRoomInfo(String key, ArrayList users){
        this.key  = key;
        this.users = users;
    }

    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public ArrayList getUsers() {
        return users;
    }
    public void setUsers(ArrayList users) {
        this.users = users;
    }
}
