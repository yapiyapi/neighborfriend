package com.example.neighborfriend.object;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class memberToInvite {
    @Expose
    @SerializedName("user_id")
    String user_id;
    @Expose
    @SerializedName("created_at")
    String created_at;
    @Expose
    @SerializedName("nickname")
    String nickname;
    @Expose
    @SerializedName("thumnail_url")
    String thumnail_url;


    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getThumnail_url() {
        return thumnail_url;
    }

    public void setThumnail_url(String thumnail_url) {
        this.thumnail_url = thumnail_url;
    }
}
