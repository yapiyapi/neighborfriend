package com.example.neighborfriend.object;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class chattingRoom {
    @Expose
    @SerializedName("seq")
    private int seq;
    @Expose
    @SerializedName("band_seq")
    private int band_seq;
    @Expose
    @SerializedName("band_title")
    private String band_title;
    @Expose
    @SerializedName("user_id")
    private String user_id;
    @Expose
    @SerializedName("thumnail_uri")
    private String thumnail;
    @Expose
    @SerializedName("title")
    private String title;
    @Expose
    @SerializedName("introduction")
    private String introduction;
    @Expose
    @SerializedName("room_type")
    private int room_type;
    @Expose
    @SerializedName("created_at")
    private String created_at;
    @Expose
    @SerializedName("member")
    private int member;
    @Expose
    @SerializedName("txt_contents")
    private String txt_contents;
    @Expose
    @SerializedName("msg_type")
    private int msg_type;
    @Expose
    @SerializedName("msg_created_at")
    private String msg_created_at;
    @Expose
    @SerializedName("isMine")
    private int isMine;
    @Expose
    @SerializedName("unreadNum")
    private int unreadNum;

    private int viewType;

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public int getBand_seq() {
        return band_seq;
    }

    public void setBand_seq(int band_seq) {
        this.band_seq = band_seq;
    }

    public String getBand_title() {
        return band_title;
    }

    public void setBand_title(String band_title) {
        this.band_title = band_title;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getThumnail() {
        return thumnail;
    }

    public void setThumnail(String thumnail) {
        this.thumnail = thumnail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public int getRoom_type() {
        return room_type;
    }

    public void setRoom_type(int room_type) {
        this.room_type = room_type;
    }


    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public int getMember() {
        return member;
    }

    public void setMember(int member) {
        this.member = member;
    }



    public String getTxt_contents() {
        return txt_contents;
    }

    public void setTxt_contents(String txt_contents) {
        this.txt_contents = txt_contents;
    }

    public int getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(int msg_type) {
        this.msg_type = msg_type;
    }

    public String getMsg_created_at() {
        return msg_created_at;
    }

    public void setMsg_created_at(String msg_created_at) {
        this.msg_created_at = msg_created_at;
    }

    public int getIsMine() {
        return isMine;
    }

    public void setIsMine(int isMine) {
        this.isMine = isMine;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public int getUnreadNum() {
        return unreadNum;
    }

    public void setUnreadNum(int unreadNum) {
        this.unreadNum = unreadNum;
    }
}
