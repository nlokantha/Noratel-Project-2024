package com.example.noratelproject2024.Models;

import java.io.Serializable;

public class User implements Serializable {
    String Status;
    String Username;

    public User(String status, String username) {
        Status = status;
        Username = username;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }
}
