package com.example.neighborfriend.object;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class chatting {
    @Expose
    @SerializedName("chatRoom_seq")
    private int chatRoom_seq;
    @Expose
    @SerializedName("seq")
    private int seq;
    @Expose
    @SerializedName("user_id")
    private String user_id;
    @Expose
    @SerializedName("nickname")
    private String nickname;
    @Expose
    @SerializedName("txt_contents")
    private String txt_contents;
    @Expose
    @SerializedName("msg_uri")
    private String msg_uri;
    @Expose
    @SerializedName("msg_type")
    private int msg_type;
    @Expose
    @SerializedName("unread_list")
    private String unread_list;
    @Expose
    @SerializedName("created_at")
    private String msg_created_at;
    private int viewType;
    private int 읽은지않은멤버수;

    public int getChatRoom_seq() {
        return chatRoom_seq;
    }

    public void setChatRoom_seq(int chatRoom_seq) {
        this.chatRoom_seq = chatRoom_seq;
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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getTxt_contents() {
        return txt_contents;
    }

    public void setTxt_contents(String txt_contents) {
        this.txt_contents = txt_contents;
    }

    public String getMsg_uri() {
        return msg_uri;
    }

    public void setMsg_uri(String msg_uri) {
        this.msg_uri = msg_uri;
    }

    public int getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(int msg_type) {
        this.msg_type = msg_type;
    }

    public String getUnread_list() {
        return unread_list;
    }

    public void setUnread_list(String unread_list) {
        this.unread_list = unread_list;
    }

    public String getMsg_created_at() {
        return msg_created_at;
    }

    public void setMsg_created_at(String msg_created_at) {
        this.msg_created_at = msg_created_at;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public int get읽은지않은멤버수() {
        return 읽은지않은멤버수;
    }

    public void set읽은지않은멤버수(int 읽은지않은멤버수) {
        this.읽은지않은멤버수 = 읽은지않은멤버수;
    }
}
