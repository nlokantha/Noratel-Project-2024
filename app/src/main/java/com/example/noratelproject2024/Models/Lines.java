package com.example.noratelproject2024.Models;

public class Lines {
    /*
      {
        "SUB_UNICODE": "000091         ",
            "SUB_UNINAME": "Assembling LK1                                    "
    }
     */
    String SUB_UNICODE;
    String SUB_UNINAME;

    public Lines() {
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
