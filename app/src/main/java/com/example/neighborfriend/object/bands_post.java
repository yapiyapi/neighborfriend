package com.example.neighborfriend.object;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class bands_post {
    @Expose
    @SerializedName("band_seq")
    private int band_seq;
    @Expose
    @SerializedName("seq")
    private int seq;
    @Expose
    @SerializedName("user_id")
    private String user_id;
    @Expose
    @SerializedName("image_uri")
    private String image_uri;
    @Expose
    @SerializedName("게시글")
    private String 게시글;
    @Expose
    @SerializedName("updated_at")
    private String updated_at;
    @Expose
    @SerializedName("created_at")
    private String created_at;
    @Expose
    @SerializedName("nickname")
    private String nickname;
    @Expose
    @SerializedName("thumnail_url")
    private String thumnail_url;

    private Integer viewType;

    public int getBand_seq() {
        return band_seq;
    }

    public void setBand_seq(int band_seq) {
        this.band_seq = band_seq;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage_uri() {
        return image_uri;
    }

    public void setImage_uri(String image_uri) {
        this.image_uri = image_uri;
    }

    public String get게시글() {
        return 게시글;
    }

    public void set게시글(String 게시글) {
        this.게시글 = 게시글;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
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

    public Integer getViewType() {
        return viewType;
    }

    public void setViewType(Integer viewType) {
        this.viewType = viewType;
    }
}
