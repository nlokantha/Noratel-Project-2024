package com.example.noratelproject2024.Models;

import java.util.List;

public class JobCardDetails {
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
