package com.example.noratelproject2024.Models;

public class JobCard {
    /*
       {
        "JObCardNo": "PR365/202415/02/01",
        "Date": "4/9/2024 6:00:19 AM",
        "Status": "Completed",
        "JC_Serial_No": "311",
        "Job_No": "J097852701-200"
    }
     */
    String JObCardNo;
    String Date;
    String Status;
    String JC_Serial_No;
    String Job_No;


    public JobCard() {
    }

    public String getJObCardNo() {
        return JObCardNo;
    }

    public void setJObCardNo(String JObCardNo) {
        this.JObCardNo = JObCardNo;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getJC_Serial_No() {
        return JC_Serial_No;
    }

    public void setJC_Serial_No(String JC_Serial_No) {
        this.JC_Serial_No = JC_Serial_No;
    }

    public String getJob_No() {
        return Job_No;
    }

    public void setJob_No(String job_No) {
        Job_No = job_No;
    }

    @Override
    public String toString() {
        return "JobCard{" +
                "JObCardNo='" + JObCardNo + '\'' +
                ", Date='" + Date + '\'' +
                ", Status='" + Status + '\'' +
                ", JC_Serial_No='" + JC_Serial_No + '\'' +
                ", Job_No='" + Job_No + '\'' +
                '}';
    }
}
