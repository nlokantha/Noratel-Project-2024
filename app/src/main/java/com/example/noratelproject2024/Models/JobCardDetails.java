package com.example.noratelproject2024.Models;

import java.util.List;

public class JobCardDetails {
    /*
    {
    "Operations": [
        "L03374",
        "L03375",
        "L03376",
        "L03377",
        "L03378"
    ],
    "Employees": [
        "90",
        "100",
        "110",
        "120",
        "130"
    ],
    "Completed": "11",
    "LastRec": "33",
    "Target": "22"
}
     */
    List<String> Operations;
    List<String> Employees;
    String Completed;
    String LastRec;
    String Target;

    public JobCardDetails() {
    }

    public List<String> getOperations() {
        return Operations;
    }

    public void setOperations(List<String> operations) {
        Operations = operations;
    }

    public List<String> getEmployees() {
        return Employees;
    }

    public void setEmployees(List<String> employees) {
        Employees = employees;
    }

    public String getCompleted() {
        return Completed;
    }

    public void setCompleted(String completed) {
        Completed = completed;
    }

    public String getLastRec() {
        return LastRec;
    }

    public void setLastRec(String lastRec) {
        LastRec = lastRec;
    }

    public String getTarget() {
        return Target;
    }

    public void setTarget(String target) {
        Target = target;
    }
}
