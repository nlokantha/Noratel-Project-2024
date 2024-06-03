package com.example.noratelproject2024.Models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "lines")
public class Lines {
    String SUB_UNICODE;
    String SUB_UNINAME;
    public String userName;

    @PrimaryKey(autoGenerate = true)
    public int id;

    public Lines() {
    }

    public Lines(String SUB_UNICODE, String SUB_UNINAME) {
        this.SUB_UNICODE = SUB_UNICODE;
        this.SUB_UNINAME = SUB_UNINAME;
    }

    public Lines(String SUB_UNICODE, String SUB_UNINAME, String userName) {
        this.SUB_UNICODE = SUB_UNICODE;
        this.SUB_UNINAME = SUB_UNINAME;
        this.userName = userName;
    }

    public String getSUB_UNICODE() {
        return SUB_UNICODE;
    }

    public void setSUB_UNICODE(String SUB_UNICODE) {
        this.SUB_UNICODE = SUB_UNICODE;
    }

    public String getSUB_UNINAME() {
        return SUB_UNINAME;
    }

    public void setSUB_UNINAME(String SUB_UNINAME) {
        this.SUB_UNINAME = SUB_UNINAME;
    }

    @Override
    public String toString() {
        return "Lines{" +
                "SUB_UNICODE='" + SUB_UNICODE + '\'' +
                ", SUB_UNINAME='" + SUB_UNINAME + '\'' +
                '}';
    }
}
