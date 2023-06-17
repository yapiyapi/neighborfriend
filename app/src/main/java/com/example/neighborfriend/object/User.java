package com.example.neighborfriend.object;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class User {
    @Expose
    @SerializedName("thumnail_url")
    private String thumnail_url;
    @Expose
    @SerializedName("nickname")
    private String nickname;
    @Expose
    @SerializedName("password")
    private String password;
    @Expose
    @SerializedName("old")
    private String old;
    @Expose
    @SerializedName("sex")
    private String sex;
    @Expose
    @SerializedName("phone_num")
    private String phone_num;

    public String getThumnail_url() {
        return thumnail_url;
    }

    public void setThumnail_url(String thumnail_url) {
        this.thumnail_url = thumnail_url;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOld() {
        return old;
    }

    public void setOld(String old) {
        this.old = old;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getPhone_num() {
        return phone_num;
    }

    public void setPhone_num(String phone_num) {
        this.phone_num = phone_num;
    }
}