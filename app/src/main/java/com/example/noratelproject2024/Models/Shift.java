package com.example.noratelproject2024.Models;

public class Shift {
    /*
    "ShiftSrNo":"5","Roster":"Roster_A"
     */
    String ShiftSrNo;
    String Roster;

    public Shift() {
    }

    public String getShiftSrNo() {
        return ShiftSrNo;
    }

    public void setShiftSrNo(String shiftSrNo) {
        ShiftSrNo = shiftSrNo;
    }

    public String getRoster() {
        return Roster;
    }

    public void setRoster(String roster) {
        Roster = roster;
    }

    @Override
    public String toString() {
        return "Shift{" +
                "ShiftSrNo='" + ShiftSrNo + '\'' +
                ", Roster='" + Roster + '\'' +
                '}';
    }
}
