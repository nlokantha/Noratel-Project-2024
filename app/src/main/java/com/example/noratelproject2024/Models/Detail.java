package com.example.noratelproject2024.Models;

import java.io.Serializable;

public class Detail implements Serializable {
    private String JObCardNo;
    private String Shift;
    private String Line;

    public String getJObCardNo() {
        return JObCardNo;
    }

    public void setJObCardNo(String JObCardNo) {
        this.JObCardNo = JObCardNo;
    }

    public String getShift() {
        return Shift;
    }

    public void setShift(String shift) {
        Shift = shift;
    }

    public String getLine() {
        return Line;
    }

    public void setLine(String line) {
        Line = line;
    }

    @Override
    public String toString() {
        return "Detail{" +
                "JObCardNo='" + JObCardNo + '\'' +
                ", Shift='" + Shift + '\'' +
                ", Line='" + Line + '\'' +
                '}';
    }
}
