package com.example.raah;

import java.util.HashMap;

public class Student {
    private String name;
    private String username;
    HashMap<String,HashMap<String,Integer>> scoreList;
    public Student(String name, String username){
        this.name=name;
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
