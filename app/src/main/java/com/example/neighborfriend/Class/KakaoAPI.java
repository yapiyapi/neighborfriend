package com.example.neighborfriend.Class;

import android.app.Application;

import com.kakao.sdk.common.KakaoSdk;
import com.kakao.sdk.user.model.AgeRange;
import com.kakao.sdk.user.model.Gender;

public class KakaoAPI extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        KakaoSdk.init(this, "957f138933c654658821b0e999c128fe");
    }
    public static String getGender(Gender gender){
        if (gender== Gender.MALE)return "0";
        else if (gender == Gender.FEMALE) return "1";
        else return null;
    }
    public static String getAgeRange(AgeRange ageRange){
        if (ageRange== AgeRange.AGE_0_9)return "5";
        else if (ageRange == AgeRange.AGE_10_14) return "10";
        else if (ageRange == AgeRange.AGE_15_19) return "15";
        else if (ageRange == AgeRange.AGE_20_29) return "20";
        else if (ageRange == AgeRange.AGE_30_39) return "30";
        else if (ageRange == AgeRange.AGE_40_49) return "40";
        else if (ageRange == AgeRange.AGE_50_59) return "50";
        else if (ageRange == AgeRange.AGE_60_69) return "60";
        else if (ageRange == AgeRange.AGE_70_79) return "70";
        else if (ageRange == AgeRange.AGE_80_89) return "80";
        else if (ageRange == AgeRange.AGE_90_ABOVE) return "90";
        else return null;
    }
}
