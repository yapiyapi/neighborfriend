package com.example.neighborfriend.object;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;

public class band {
    @Expose
    @SerializedName("seq")
    private int seq;
    @Expose
    @SerializedName("user_id")
    private String user_id;
    @Expose
    @SerializedName("title")
    private String 제목;
    @Expose
    @SerializedName("thumnail_url")
    private String thumnail_url;
    @Expose
    @SerializedName("contents")
    private String 소개글;
    @Expose
    @SerializedName("category") // 게임/음식/운동/공부/친구/취미/미션/그외
    private int 카테고리;
    @Expose
    @SerializedName("publicSet") // 공개/비공개
    private int 공개여부;
    @Expose
    @SerializedName("old_limit_from")
    private int 나이제한_시작;
    @Expose
    @SerializedName("old_limit_to")
    private int 나이제한_끝;
    @Expose
    @SerializedName("sex_limit") // 상관없음/남자/여자
    private int 성별제한;
    @Expose
    @SerializedName("created_at")
    private String 생성일자;
    @Expose
    @SerializedName("member")
    private String 멤버수;

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

    public String get제목() {
        return 제목;
    }

    public void set제목(String 제목) {
        this.제목 = 제목;
    }

    public String getThumnail_url() {
        return thumnail_url;
    }

    public void setThumnail_url(String thumnail_url) {
        this.thumnail_url = thumnail_url;
    }

    public String get소개글() {
        return 소개글;
    }

    public void set소개글(String 소개글) {
        this.소개글 = 소개글;
    }

    public int get카테고리() {
        return 카테고리;
    }

    public void set카테고리(int 카테고리) {
        this.카테고리 = 카테고리;
    }

    public int get공개여부() {
        return 공개여부;
    }

    public void set공개여부(int 공개여부) {
        this.공개여부 = 공개여부;
    }

    public int get나이제한_시작() {
        return 나이제한_시작;
    }

    public void set나이제한_시작(int 나이제한_시작) {
        this.나이제한_시작 = 나이제한_시작;
    }

    public int get나이제한_끝() {
        return 나이제한_끝;
    }

    public void set나이제한_끝(int 나이제한_끝) {this.나이제한_끝 = 나이제한_끝;}

    public int get성별제한() {
        return 성별제한;
    }

    public void set성별제한(int 성별제한) {
        this.성별제한 = 성별제한;
    }

    public String get생성일자() {
        return 생성일자;
    }
    public void set생성일자(String 생성일자) {
        this.생성일자 = 생성일자;
    }

    public String get멤버수() {
        return 멤버수;
    }

    public void set멤버수(String 멤버수) {
        this.멤버수 = 멤버수;
    }
}

