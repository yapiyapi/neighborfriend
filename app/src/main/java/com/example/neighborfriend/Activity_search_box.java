package com.example.neighborfriend;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.neighborfriend.Adapter.Adapter_band_postList;
import com.example.neighborfriend.Adapter.Adapter_search;
import com.example.neighborfriend.Class.RetrofitClass;
import com.example.neighborfriend.Fragment.Fragment_home;
import com.example.neighborfriend.Interface.RetrofitAPI;
import com.example.neighborfriend.databinding.ActivitySearchBoxBinding;
import com.example.neighborfriend.databinding.ActivitySignUpBinding;
import com.example.neighborfriend.object.User;
import com.example.neighborfriend.object.band;
import com.example.neighborfriend.object.bands_post;
import com.google.android.material.textfield.TextInputEditText;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class Activity_search_box extends AppCompatActivity {
    private ActivitySearchBoxBinding binding;
    /**  retrofit **/ RetrofitAPI retrofitAPI;
    /**RecyclerView**/
    Adapter_search 어댑터_search;
    LinearLayoutManager 레이아웃매니저;
    ArrayList<band> 검색리스트, 나이필터리스트, 성별필터리스트;
    private RecyclerView recySch; private EditText editSch; private LinearLayout linSch;
    private ImageView imgSch; private ToggleButton filAge,filGen;
    private int category;boolean age_boolean, gender_boolean;
    private String filter_글저장소;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBoxBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeView();
        initializeProperty();
        // 검색 버튼 눌리거나
        imgSch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Retrofit(editSch.getText().toString().trim());
                filter_글저장소 = editSch.getText().toString();
                editSch.setText("");
            }
        });
        //엔터키 로직

        editSch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (keyCode){
                    case KeyEvent.KEYCODE_ENTER:
                        Retrofit(editSch.getText().toString().trim());
                        filter_글저장소 = editSch.getText().toString();
                        editSch.setText("");                }
                return false;
            }
        });



        /** 필터 **/
        filAge.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){ // on 일 때
                    age_boolean = true;
                }else {
                    age_boolean = false;
                }
                Retrofit(filter_글저장소);
            }
        });
        filGen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){ // on 일 때
                    gender_boolean = true;
                }else {
                    gender_boolean = false;
                }
                Retrofit(filter_글저장소);
            }
        });

    }
    /** initial **/
    private void initializeView() {
        /** binding **/
        // 검색창 (상단바)
        editSch = binding.editSearch;
        imgSch = binding.imgSearch;
        // filter
        filAge = binding.textFilterLimitAge;
        filGen = binding.textFilterLimitGender;
        // recyclerview
        recySch = binding.recySearch;
    }
    private void initializeProperty() {
        검색리스트 = new ArrayList<>();
        나이필터리스트 = new ArrayList<>();
        성별필터리스트 = new ArrayList<>();

        /** Intent **/
        Intent intent = getIntent();
        category = intent.getIntExtra("category",-1);
        if(category!=-1){
            Retrofit(category);
        }else{
            // 검색창에 focus
            editSch.requestFocus();
        }
    }

    /** Http 통신 **/
    // 검색어로 band data 가져오기
    private void Retrofit(String band_제목){
        검색리스트.clear();

        retrofitAPI = RetrofitClass.getApiClient().create(RetrofitAPI.class);
        Call<ArrayList<band>> call = retrofitAPI.searchBand(band_제목);

        call.enqueue(new Callback<ArrayList<band>>() {
            @Override
            public void onResponse(@NotNull Call<ArrayList<band>> call, @NotNull Response<ArrayList<band>> response) {
                // 서버에서 응답을 받아옴
                if (response.isSuccessful() && response.body() != null) {
                    List<band> search_list = response.body();

                    if(age_boolean && gender_boolean){
                        for (int i = 0; i < search_list.size(); i++) {
                            if((search_list.get(i).get나이제한_시작()!=0 || search_list.get(i).get나이제한_끝()!=0) && search_list.get(i).get성별제한()!=0 ){
                                // home_cell 초기화
                                band band1 = new band();
                                band1.setSeq(search_list.get(i).getSeq());
                                band1.setUser_id(search_list.get(i).getUser_id());
                                band1.set제목(search_list.get(i).get제목());
                                band1.setThumnail_url(search_list.get(i).getThumnail_url());
                                band1.set소개글(search_list.get(i).get소개글());
                                band1.set카테고리(search_list.get(i).get카테고리());
                                band1.set공개여부(search_list.get(i).get공개여부());
                                band1.set나이제한_시작(search_list.get(i).get나이제한_시작());
                                band1.set나이제한_끝(search_list.get(i).get나이제한_끝());
                                band1.set성별제한(search_list.get(i).get성별제한());
                                band1.set생성일자(search_list.get(i).get생성일자());
                                // list 에 bands_post object 추가
                                검색리스트.add(band1);
                            }
                        }
                    }
                    else if(age_boolean){ // 나이제한이 켜져 있을 때
                        for (int i = 0; i < search_list.size(); i++) {
                            if(search_list.get(i).get나이제한_시작()!=0 || search_list.get(i).get나이제한_끝()!=0){
                                // home_cell 초기화
                                band band1 = new band();
                                band1.setSeq(search_list.get(i).getSeq());
                                band1.setUser_id(search_list.get(i).getUser_id());
                                band1.set제목(search_list.get(i).get제목());
                                band1.setThumnail_url(search_list.get(i).getThumnail_url());
                                band1.set소개글(search_list.get(i).get소개글());
                                band1.set카테고리(search_list.get(i).get카테고리());
                                band1.set공개여부(search_list.get(i).get공개여부());
                                band1.set나이제한_시작(search_list.get(i).get나이제한_시작());
                                band1.set나이제한_끝(search_list.get(i).get나이제한_끝());
                                band1.set성별제한(search_list.get(i).get성별제한());
                                band1.set생성일자(search_list.get(i).get생성일자());
                                // list 에 bands_post object 추가
                                검색리스트.add(band1);
                            }
                        }
                    }else if(gender_boolean){  // 성별제한이 켜져 있을 때
                        for (int i = 0; i < search_list.size(); i++) {
                            if(search_list.get(i).get성별제한()!=0){
                                // home_cell 초기화
                                band band1 = new band();
                                band1.setSeq(search_list.get(i).getSeq());
                                band1.setUser_id(search_list.get(i).getUser_id());
                                band1.set제목(search_list.get(i).get제목());
                                band1.setThumnail_url(search_list.get(i).getThumnail_url());
                                band1.set소개글(search_list.get(i).get소개글());
                                band1.set카테고리(search_list.get(i).get카테고리());
                                band1.set공개여부(search_list.get(i).get공개여부());
                                band1.set나이제한_시작(search_list.get(i).get나이제한_시작());
                                band1.set나이제한_끝(search_list.get(i).get나이제한_끝());
                                band1.set성별제한(search_list.get(i).get성별제한());
                                band1.set생성일자(search_list.get(i).get생성일자());
                                // list 에 bands_post object 추가
                                검색리스트.add(band1);
                            }
                        }
                    }else{
                        for (int i = 0; i < search_list.size(); i++) {
                            // home_cell 초기화
                            band band1 = new band();
                            band1.setSeq(search_list.get(i).getSeq());
                            band1.setUser_id(search_list.get(i).getUser_id());
                            band1.set제목(search_list.get(i).get제목());
                            band1.setThumnail_url(search_list.get(i).getThumnail_url());
                            band1.set소개글(search_list.get(i).get소개글());
                            band1.set카테고리(search_list.get(i).get카테고리());
                            band1.set공개여부(search_list.get(i).get공개여부());
                            band1.set나이제한_시작(search_list.get(i).get나이제한_시작());
                            band1.set나이제한_끝(search_list.get(i).get나이제한_끝());
                            band1.set성별제한(search_list.get(i).get성별제한());
                            band1.set생성일자(search_list.get(i).get생성일자());
                            // list 에 bands_post object 추가
                            검색리스트.add(band1);
                        }
                        Log.i("Asdfasdf", search_list.toString());
                    }


                    /** 어댑터 연결 **/
                    레이아웃매니저 = new LinearLayoutManager(Activity_search_box.this);
                    어댑터_search = new Adapter_search(검색리스트);
                    recySch.setLayoutManager(레이아웃매니저);
                    recySch.setAdapter(어댑터_search);

                    //리사이클러뷰 화면전환
                    어댑터_search.setOnItemClickListener(new Adapter_search.OnItemClickListener() {
                        @Override
                        public void onItemClick(View v, int 밴드번호) {
                            Intent intent = new Intent(v.getContext(), Activity_band_introduce.class);
                            intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
                            intent.putExtra("밴드번호", 밴드번호);
                            startActivity(intent);
                        }
                    });

                } else {
                    Toast.makeText(Activity_search_box.this, "실패", Toast.LENGTH_SHORT).show();
                }
            }
            // 통신실패시
            @Override
            public void onFailure(@NonNull Call<ArrayList<band>> call, @NonNull Throwable t) {
                Toast.makeText(Activity_search_box.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Log.i("Activity_search_box", t.getLocalizedMessage());
            }
        });
    }
    // 카테고리로 band data 가져오기
    private void Retrofit(int category){
        retrofitAPI = RetrofitClass.getApiClient().create(RetrofitAPI.class);
        Call<ArrayList<band>> call = retrofitAPI.searchBand_category(category);

        call.enqueue(new Callback<ArrayList<band>>() {
            @Override
            public void onResponse(@NotNull Call<ArrayList<band>> call, @NotNull Response<ArrayList<band>> response) {
                // 서버에서 응답을 받아옴
                if (response.isSuccessful() && response.body() != null) {
                    List<band> search_list = response.body();

                    for (int i = 0; i < search_list.size(); i++) {
                        // home_cell 초기화
                        band band1 = new band();
                        band1.setSeq(search_list.get(i).getSeq());
                        band1.setUser_id(search_list.get(i).getUser_id());
                        band1.set제목(search_list.get(i).get제목());
                        band1.setThumnail_url(search_list.get(i).getThumnail_url());
                        band1.set소개글(search_list.get(i).get소개글());
                        band1.set카테고리(search_list.get(i).get카테고리());
                        band1.set공개여부(search_list.get(i).get공개여부());
                        band1.set나이제한_시작(search_list.get(i).get나이제한_시작());
                        band1.set나이제한_끝(search_list.get(i).get나이제한_끝());
                        band1.set성별제한(search_list.get(i).get성별제한());
                        band1.set생성일자(search_list.get(i).get생성일자());
                        // list 에 bands_post object 추가
                        검색리스트.add(band1);
                    }

                    /** 어댑터 연결 **/
                    레이아웃매니저 = new LinearLayoutManager(Activity_search_box.this);
                    어댑터_search = new Adapter_search(검색리스트);
                    recySch.setLayoutManager(레이아웃매니저);
                    recySch.setAdapter(어댑터_search);

                    //리사이클러뷰 화면전환
                    어댑터_search.setOnItemClickListener(new Adapter_search.OnItemClickListener() {
                        @Override
                        public void onItemClick(View v, int 밴드번호) {
                            Intent intent = new Intent(v.getContext(), Activity_band_introduce.class);
                            intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
                            intent.putExtra("밴드번호", 밴드번호);
                            startActivity(intent);
                        }
                    });

                } else {
                    Toast.makeText(Activity_search_box.this, "실패", Toast.LENGTH_SHORT).show();
                }
            }
            // 통신실패시
            @Override
            public void onFailure(@NonNull Call<ArrayList<band>> call, @NonNull Throwable t) {
                Toast.makeText(Activity_search_box.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Log.i("Activity_search_box", t.getLocalizedMessage());
            }
        });
    }

    /** 포맷 **/
    public static String 나이포맷(int from_age , int to_age ){
        if(from_age==0 && to_age==0){
            return "";
        }else if(from_age==0){
            return to_age + "세까지 가능";
        }else if(to_age==0){
            return from_age+ "부터 가능";
        }else{
            return from_age +"세 ~ "+to_age+"세";
        }
    }
    public static String 성별포맷(int gender ){ // 상관없음/남자/여자
        if(gender==0){
            return "";
        }else if(gender==1){
            return "남성만 가능";
        }else{ // 2
            return "여성만 가능";
        }
    }

}