package com.example.noratelproject2024.Models;

public class ReasonCodes {
    String Sr_No;
    String Category;
    public ReasonCodes() {
    }

    public String getSr_No() {
        return Sr_No;
    }

    public void setSr_No(String sr_No) {
        Sr_No = sr_No;
    }

    public String getCategory() {
        return Category;
    }

    public void setCategory(String category) {
        Category = category;
    }

    @Override
    public String toString() {
        return Category;
    }
}
