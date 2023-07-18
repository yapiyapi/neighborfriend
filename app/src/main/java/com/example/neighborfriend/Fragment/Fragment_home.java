package com.example.neighborfriend.Fragment;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neighborfriend.Activity_band;
import com.example.neighborfriend.Activity_band_create_update;
import com.example.neighborfriend.Adapter.Adapter_home_joinband;
import com.example.neighborfriend.Adapter.Adapter_home_myband;
import com.example.neighborfriend.Class.RetrofitClass;
import com.example.neighborfriend.Interface.RetrofitAPI;
import com.example.neighborfriend.databinding.FragmentHomeBinding;
import com.example.neighborfriend.object.band;
import com.example.neighborfriend.object.home_cell;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Fragment_home extends Fragment {
    private FragmentHomeBinding binding;
    /**  SharedPreferences **/ SharedPreferences userData,bandData;
    SharedPreferences.Editor userData_e,bandData_e;
    /**  RetrofitAPI **/ RetrofitAPI retrofitAPI;
    /**  RecyclerView **/
    RecyclerView myband, joinband;
    Adapter_home_myband 어댑터_my; Adapter_home_joinband 어댑터_join;
    LinearLayoutManager 레이아웃매니저_my,레이아웃매니저_join;
    private ImageView plusbtn;
    private Button btn1,btn2; TextView name;


    private String current_user_id, current_user_name;
    private ArrayList<home_cell> list_myband, list_joinband;
    private String thumnail_url_b, 제목, 소개글, 나이제한;
    private int 카테고리, 공개여부, 성별제한, 멤버수;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding= FragmentHomeBinding.inflate(inflater);
        View view = binding.getRoot();

        initializeView();
        initializeProperty();
        뒤로가기막기();

        // 플러스 버튼
        plusbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Activity_band_create_update.class);
                intent.putExtra("Activity","home");
                startActivity(intent);
            }
        });

        return view;
    }

    private void initializeView() {
        // Recyclerview
        myband = binding.recyclerViewMyband;
        joinband= binding.recyclerViewJoinband;
        // plusbtn
        plusbtn = binding.plusBtnMain;
    }

    private void initializeProperty() {
        /**  SharedPreferences **/
        // user
        userData = getActivity().getSharedPreferences("user", MODE_PRIVATE); // sharedpreference
        userData_e = userData.edit(); // editor
        // current_user_id
        current_user_id = userData.getString("id","noneId");
        current_user_name = userData.getString("nickname","noneNick");
        // Adapter 에 들어갈 Arraylist
        list_myband = new ArrayList<home_cell>();
        list_joinband = new ArrayList<home_cell>();
        /**  Http 통신 **/
        // user id 의 밴드 목록 가져옴
        Retrofit(current_user_id);


    }
    // 1. Retrofit-----------------------------------
    private void Retrofit(final String id){
        /***********************  내 밴드  ***************************/
        retrofitAPI = RetrofitClass.getApiClient().create(RetrofitAPI.class);
        Call<ArrayList<band>> call1 = retrofitAPI.getMyBandData(id);

        call1.enqueue(new Callback<ArrayList<band>>() {
            @Override
            public void onResponse(@NotNull Call<ArrayList<band>> call, @NotNull Response<ArrayList<band>> response) {
                // 서버에서 응답을 받아옴
                if (response.isSuccessful() && response.body() != null) {
                    List<band> band_list = response.body();

                    for (int i = 0; i < band_list.size(); i++) {
                        // home_cell 초기화
                        home_cell home_cell_1 = new home_cell();
                        home_cell_1.setSeq(band_list.get(i).getSeq());
                        home_cell_1.setThumnail_url(band_list.get(i).getThumnail_url());
                        home_cell_1.set제목(band_list.get(i).get제목());
                        // list 에 home_cell object 추가
                        list_myband.add(home_cell_1);

                    }

                    /** 어댑터 연결 **/
                    레이아웃매니저_my = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
                    어댑터_my = new Adapter_home_myband(list_myband);
                    myband.setLayoutManager(레이아웃매니저_my);
                    myband.setAdapter(어댑터_my);

                    //리사이클러뷰 화면전환
                    어댑터_my.setOnItemClickListener(new Adapter_home_myband.OnItemClickListener() {
                        @Override
                        public void onItemClick(View v, int 밴드번호) {
                            Intent intent = new Intent(v.getContext(), Activity_band.class);
                            intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
                            intent.putExtra("밴드번호", 밴드번호);
                            startActivity(intent);
                        }
                    }) ;

                    // 성공로직
                } else {
                    Toast.makeText(getActivity(), "실패", Toast.LENGTH_SHORT).show();
                }
            }
            // 통신실패시
            @Override
            public void onFailure(@NonNull Call<ArrayList<band>> call, @NonNull Throwable t) {
                Log.i("error", t.getLocalizedMessage());
            }
        });

        /***********************  가입한 밴드  ***************************/
        retrofitAPI = RetrofitClass.getApiClient().create(RetrofitAPI.class);
        Call<ArrayList<band>> call2 = retrofitAPI.getJoinBandData(id);

        call2.enqueue(new Callback<ArrayList<band>>() {
            @Override
            public void onResponse(@NotNull Call<ArrayList<band>> call, @NotNull Response<ArrayList<band>> response) {
                // 서버에서 응답을 받아옴
                if (response.isSuccessful() && response.body() != null) {
                    List<band> band_list = response.body();

                    for (int i = 0; i < band_list.size(); i++) {
                        // home_cell 초기화
                        home_cell home_cell_2 = new home_cell();
                        home_cell_2.setSeq(band_list.get(i).getSeq());
                        home_cell_2.setThumnail_url(band_list.get(i).getThumnail_url());
                        home_cell_2.set제목(band_list.get(i).get제목());
                        // list 에 home_cell object 추가
                        list_joinband.add(home_cell_2);
                    }

                    /** 어댑터 연결 **/
                    레이아웃매니저_join = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
                    어댑터_join = new Adapter_home_joinband(list_joinband);
                    joinband.setLayoutManager(레이아웃매니저_join);
                    joinband.setAdapter(어댑터_join);

                    //리사이클러뷰 화면전환
                    어댑터_join.setOnItemClickListener(new Adapter_home_joinband.OnItemClickListener() {
                        @Override
                        public void onItemClick(View v, int 밴드번호) {
                            Intent intent = new Intent(v.getContext(), Activity_band.class);
                            intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
                            intent.putExtra("밴드번호", 밴드번호);
                            startActivity(intent);
                        }
                    }) ;

                    // 성공로직
                } else {

                    Toast.makeText(getActivity(), "실패ㅗㅑ", Toast.LENGTH_SHORT).show();
                }
            }
            // 통신실패시
            @Override
            public void onFailure(@NonNull Call<ArrayList<band>> call, @NonNull Throwable t) {
                Log.i("error", t.getLocalizedMessage());
            }
        });
    }
    /**뒤로가기막기**/
    public void 뒤로가기막기(){
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
    }
}