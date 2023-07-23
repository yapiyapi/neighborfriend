package com.example.neighborfriend;

import static com.example.neighborfriend.Activity_band_create_update.카테고리한글;
import static com.example.neighborfriend.Activity_search_box.나이포맷;
import static com.example.neighborfriend.Activity_search_box.성별포맷;
import static com.example.neighborfriend.MainActivity.HOST_URL;
import static com.example.neighborfriend.MainActivity.requestQueue;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.neighborfriend.Class.FirebaseCloudStorage;
import com.example.neighborfriend.Class.RetrofitClass;
import com.example.neighborfriend.Interface.RetrofitAPI;
import com.example.neighborfriend.databinding.ActivityBandIntroduceBinding;
import com.example.neighborfriend.databinding.ActivityBandJoinSettingBinding;
import com.example.neighborfriend.object.band;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Activity_band_introduce extends AppCompatActivity {
    private ActivityBandIntroduceBinding binding;
    /**  SharedPreferences **/ SharedPreferences userData;
    /**  RetrofitAPI **/ RetrofitAPI retrofitAPI;
    private ImageView imgIntr, backBtn;
    private TextView title, txtTitl, txtMemb, txtPubl, txtCatg, txtGend, txtAge, txtCont;
    private Button joinBand;
    private String user_id,thumnail_url,제목,소개글,멤버수;private int 밴드번호;
    private String current_id, current_sex, current_old;
    private int 카테고리,공개여부,나이제한_시작,나이제한_끝,성별제한;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBandIntroduceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeView();
        initializeProperty();

        // 뒤로가기
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {finish();}
        });

    }



    private void initializeView() {
        // 제목
        title = binding.titleBandIntro;
        // 이미지
        imgIntr = binding.imageViewBandIntro;
        // text
        txtTitl = binding.textTitle;
        txtMemb = binding.textMember;
        txtPubl = binding.textPublic;
        txtCatg = binding.textCategory;
        txtGend = binding.textGender;
        txtAge = binding.textAge;
        txtCont = binding.textContents;
        // btn
        joinBand = binding.btnJoinband;
        backBtn = binding.backBtnBandIntro;
    }

    private void initializeProperty() {
        /**  SharedPreferences **/
        // user
        userData = getSharedPreferences("user", MODE_PRIVATE); // sharedpreference
        // user id
        current_id = userData.getString("id","noneId");
        current_sex = userData.getString("sex","noneSex");
        current_old = userData.getString("old","noneOld");

        /**  검색창에서 넘어왔을 때 **/
        Intent intent = getIntent();
        밴드번호 = intent.getIntExtra("밴드번호",-1);
        if(밴드번호!=-1){ // 검색창에서 넘어왔을 때
            /** Http 통신 **/
            // 밴드 데이터 가져오기
            Retrofit(밴드번호);
            // band 멤버수 가져오기
            if(requestQueue == null) requestQueue = Volley.newRequestQueue(getApplicationContext());
            Volley(밴드번호);
        }else { // 초대링크 타고 넘어왔을 때
            getDeeplink();
        }
    }
    /** 초대 링크  **/
    private void getDeeplink() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        //app으로 실행 했을 경우 (deeplink 없는 경우)
                        if (pendingDynamicLinkData == null) {
                            Log.d("TAG", "No have dynamic link");
                            return;
                        }
                        //deeplink로 app 넘어 왔을 경우
                        Uri deepLink = pendingDynamicLinkData.getLink();
                        Log.d("TAG", "deepLink: " + deepLink);

                        String 초대uri = deepLink.toString();
//                        Log.i("aaaaa",초대uri);
                        밴드번호 = Integer.parseInt(초대uri.substring(초대uri.lastIndexOf("=")+1));

                        /** Http 통신 **/
                        // 밴드 데이터 가져오기
                        Retrofit(밴드번호);
                        // band 멤버수 가져오기
                        if(requestQueue == null) requestQueue = Volley.newRequestQueue(getApplicationContext());
                        Volley(밴드번호);
//                        Log.i("aaaaa", String.valueOf(밴드번호));
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TAG", "getDynamicLink:onFailure", e);
                    }
                });

    }
    /** Http 통신 **/
    // band seq 로 data 가져오기
    private void Retrofit(final int seq){
        retrofitAPI = RetrofitClass.getApiClient().create(RetrofitAPI.class);
        Call<band> call = retrofitAPI.getBand(seq);

        call.enqueue(new Callback<band>() {
            @Override
            public void onResponse(@NotNull Call<band> call, @NotNull Response<band> response) {
                // 서버에서 응답을 받아옴
                if (response.isSuccessful() && response.body() != null) {

                    user_id = response.body().getUser_id();
                    thumnail_url = response.body().getThumnail_url();
                    제목 = response.body().get제목();
                    소개글 = response.body().get소개글();
                    카테고리 = response.body().get카테고리();
                    공개여부 = response.body().get공개여부();
                    성별제한 = response.body().get성별제한();
                    나이제한_시작 = response.body().get나이제한_시작();
                    나이제한_끝 = response.body().get나이제한_끝();


                    Log.i("b", String.valueOf(seq));
                    Log.i("b",user_id);
                    Log.i("bandband",thumnail_url);
                    Log.i("b",제목);
                    Log.i("b",소개글);
                    Log.i("b", String.valueOf(카테고리));
                    Log.i("b", String.valueOf(공개여부));
                    Log.i("b", String.valueOf(성별제한));
                    Log.i("b", String.valueOf(나이제한_시작));
                    Log.i("b", String.valueOf(나이제한_끝));

                    /**  초기화 **/
                    // 이미지 초기화
                    StorageReference imagesRef = FirebaseCloudStorage.Storage_img(thumnail_url);
                    imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(Activity_band_introduce.this).load(uri).into(imgIntr);
                        }
                    });
//                    if (!thumnail_url.equals("")) Glide.with(Activity_band_introduce.this).load(thumnail_url).into(imgIntr);
                    // title/제목/공개여부
                    title.setText(제목);
                    txtTitl.setText(제목);
                    if(공개여부==0) txtPubl.setText("공개");
                    else txtPubl.setText("비공개");
                    // 카테고리
                    txtCatg.setText("#"+카테고리한글(카테고리));
                    // 성별, 나이
                    txtGend.setText(성별포맷(성별제한));
                    txtAge.setText(나이포맷(나이제한_시작,나이제한_끝));
                    // 소개글
                    txtCont.setText(소개글);

                    /**  버튼 **/
                    // 가입하기
                    joinBand.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            /** Http 통신 **/
                            // users_bands Create (가입하기)
                            // 성별 제한
                            if(!current_sex.equals(String.valueOf(성별제한-1))){ // 성별제한에 걸리지 않는다면
                                if (나이제한_끝==0) 나이제한_끝=100;
                                /// 나이 제한
                                if ( Integer.parseInt(current_old) >= 나이제한_시작 && Integer.parseInt(current_old) <= 나이제한_끝){
                                    if(requestQueue == null) requestQueue = Volley.newRequestQueue(getApplicationContext());
                                    Volley(current_id,seq);
                                }else
                                    Toast.makeText(Activity_band_introduce.this, "가입하실 수 없습니다.", Toast.LENGTH_SHORT).show();
                            }else
                                Toast.makeText(Activity_band_introduce.this, "가입하실 수 없습니다.", Toast.LENGTH_SHORT).show();

                        }
                    });

                } else {
                    Toast.makeText(Activity_band_introduce.this, "실패", Toast.LENGTH_SHORT).show();
                }
            }
            // 통신실패시
            @Override
            public void onFailure(@NonNull Call<band> call, @NonNull Throwable t) {
                Toast.makeText(Activity_band_introduce.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Log.i("band_introduce_error", t.getLocalizedMessage());
            }
        });
    }

    // users_bands 에서 band 멤버수 가져오기
    public void Volley(int 밴드번호_get) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                HOST_URL+"band/read_member.php",
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        Log.i("a",response);
                        if(response.equals("0")) { // 실패
                            Toast.makeText(getApplicationContext(), "멤버수 가져오기 실패", Toast.LENGTH_SHORT).show();
                        }else { // 멤버수 가져오기
                            멤버수 = response;
                            /**  초기화 **/
                            txtMemb.setText("멤버 "+멤버수);
//                            Toast.makeText(Activity_band.this, response.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("band_introduce_error",error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("seq", String.valueOf(밴드번호_get));

                return params;
            }
        };


        // 이전 결과가 있더라도 새로 요청
        request.setShouldCache(false);
        requestQueue.add(request);
    }

    /*** 밴드 가입하기 **/
    // users_bands 에서 users_bands 생성하기 (밴드가입)
    public void Volley(String 유저아이디, int 밴드번호) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                HOST_URL+"band/join.php",
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        Log.i("a",response);
                        if(response.equals("4")) { // 성공
                            Toast.makeText(getApplicationContext(), "가입 성공", Toast.LENGTH_SHORT).show();
                            // 밴드로 이동
                            Intent intent = new Intent(Activity_band_introduce.this, Activity_band.class);
//                            intent.putExtra("Activity","bandIntro");
                            intent.putExtra("밴드번호",밴드번호);
                            startActivity(intent);
                            finish();
                        }else if(response.equals("2")) {
                            Toast.makeText(getApplicationContext(), "이미 가입한 밴드입니다.", Toast.LENGTH_SHORT).show();
                        }else Toast.makeText(getApplicationContext(), "가입 실패", Toast.LENGTH_SHORT).show();
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("band_intro",error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_id", 유저아이디);
                params.put("band_seq", String.valueOf(밴드번호));

                return params;
            }
        };


        // 이전 결과가 있더라도 새로 요청
        request.setShouldCache(false);
        requestQueue.add(request);
    }

}