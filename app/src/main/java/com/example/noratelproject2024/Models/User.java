package com.example.noratelproject2024.Models;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {
    String Status;
    String Username;
    List<Detail> Detail;
    String Message;

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

    public List<com.example.noratelproject2024.Models.Detail> getDetail() {
        return Detail;
    }

    public void setDetail(List<com.example.noratelproject2024.Models.Detail> detail) {
        Detail = detail;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }
}
