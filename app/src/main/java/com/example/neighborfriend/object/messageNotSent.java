package com.example.neighborfriend.object;

import java.util.List;

public class messageNotSent {
    String user_id_sent;
    int msg_type;
    String msg_text;
    String file_name;
    String created_at;

    public String getUser_id_sent() {
        return user_id_sent;
    }

    public void setUser_id_sent(String user_id_sent) {
        this.user_id_sent = user_id_sent;
    }

    public int getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(int msg_type) {
        this.msg_type = msg_type;
    }

    public String getMsg_text() {
        return msg_text;
    }

    public void setMsg_text(String msg_text) {
        this.msg_text = msg_text;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
}
