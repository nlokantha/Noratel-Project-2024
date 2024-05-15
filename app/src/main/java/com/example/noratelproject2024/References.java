package com.example.noratelproject2024;

public class References {
    public static String url = "http://192.168.2.3/noratel/api/testDataCollection/";

    public static class Login {
        public static String methodName = url + "Login/";
    }
    public static class GetLines {
        public static String methodName = url + "GetLines";
    }
    public static class GetShift {
        public static String methodName = url + "GetShift";
    }
    public static class GetJobCard {
        public static String methodName = url + "GetJobCards";
    }
    public static class GetJobCardDetail {
        public static String methodName = url + "GetJobCardDetail?jobSrNo=";
    }
    public static class SaveJobCard {
        public static String methodName = url + "SaveJobCard";
    }
    public static class GetReasonCodes {
        public static String methodName = url + "GetReasonCodes";
    }
    public static class HoldJobCard {
        public static String methodName = url + "HoldJobCard";
    }
    public static class CompleteJobCard {
        public static String methodName = url + "CompleteJobCard";
    }
    public static class HoldToOpen {
        public static String methodName = url + "HoldToOpen";
    }
    public static class NewToOpen {
        public static String methodName = url + "NewToOpen";
    }



}
