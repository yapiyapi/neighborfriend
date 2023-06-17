package com.example.neighborfriend;

import static com.example.neighborfriend.Activity_band_chatting_room.시간포맷;
import static com.example.neighborfriend.MainActivity.retrofitAPI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.neighborfriend.Adapter.Adapter_chattingList;
import com.example.neighborfriend.Adapter.Adapter_chatting_invite;
import com.example.neighborfriend.Class.FirebaseCloudStorage;
import com.example.neighborfriend.Class.RetrofitClass;
import com.example.neighborfriend.Interface.RetrofitAPI;
import com.example.neighborfriend.Service.Service_chatting;
import com.example.neighborfriend.databinding.ActivityBandChattingRoomImgBinding;
import com.example.neighborfriend.databinding.ActivityBandChattingRoomInviteBinding;
import com.example.neighborfriend.object.chattingRoom;
import com.example.neighborfriend.object.memberToInvite;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Activity_band_chatting_room_invite extends AppCompatActivity {
    private ActivityBandChattingRoomInviteBinding binding;
    /**
     * Service
     **/
    Service_chatting ServiceChat;
    boolean isService = false; // 서비스 중인 확인용
    /**  SharedPreferences **/ SharedPreferences userData;
    Adapter_chatting_invite 어댑터;
    LinearLayoutManager 레이아웃매니저;
    private TextView txtCancel, txtInvt; private RecyclerView recy;
    private ArrayList<String> memberToInviteArr;
    private String current_user_id, current_user_name;
    private int 밴드번호;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBandChattingRoomInviteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeView();
        initializeProperty();

        // 취소
        txtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });



    }

    /** initialize **/
    private void initializeView() {
        // 상단
        txtCancel = binding.textCancelBandChatInvite;
        txtInvt = binding.textInviteBandChatInvite;
        recy = binding.recyBandChatInvite;
    }
    private void initializeProperty() {
        /**  SharedPreferences **/
        userData = getSharedPreferences("user", MODE_PRIVATE); // sharedpreference
        // user data
        current_user_id = userData.getString("id", "noneId");

        /**  밴드번호 **/
        Intent intent = getIntent();
        밴드번호 = intent.getIntExtra("밴드번호", 0);

        // 초대할 멤버 id 담는 list
        memberToInviteArr = new ArrayList<>();

        Retrofit(current_user_id, current_user_name, 밴드번호);
    }

    /** Http 통신 **/
    // 멤버 리스트 가져오기
    private void Retrofit(String current_user_id, String current_user_name, int band_seq_get) {
        /***********************  멤버 리스트 가져오기  ***************************/
        retrofitAPI = RetrofitClass.getApiClient().create(RetrofitAPI.class);
        Call<ArrayList<memberToInvite>> call1 = retrofitAPI.getMemberToInvite(current_user_id ,band_seq_get);
        call1.enqueue(new Callback<ArrayList<memberToInvite>>() {
            @Override
            public void onResponse(@NotNull Call<ArrayList<memberToInvite>> call, @NotNull Response<ArrayList<memberToInvite>> response) {
                // 서버에서 응답을 받아옴
                if (response.isSuccessful() && response.body() != null) {
                    List<memberToInvite> memberToInviteList = response.body();

                    /** 어댑터 연결 **/
                    레이아웃매니저 = new LinearLayoutManager(Activity_band_chatting_room_invite.this);
                    어댑터 = new Adapter_chatting_invite(memberToInviteList);
                    recy.setLayoutManager(레이아웃매니저);
                    recy.setAdapter(어댑터);

//
                    //리사이클러뷰 화면전환
                    어댑터.setOnItemClickListener(new Adapter_chatting_invite.OnItemClickListener() {
                        @Override
                        public void onItemClick(View v, String user_id,  boolean ischecked) {
                            if(ischecked) memberToInviteArr.add(user_id);
                            else memberToInviteArr.remove(user_id);

                            // 초대
                            txtInvt.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Retrofit비공개채팅방생성(밴드번호,current_user_id, current_user_name,memberToInviteArr,2);
                                }
                            });
                        }
                    });


                } else {
                    Toast.makeText(Activity_band_chatting_room_invite.this, "멤버 가져오기 실패", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            // 통신실패시
            @Override
            public void onFailure(@NonNull Call<ArrayList<memberToInvite>> call, @NonNull Throwable t) {
                Toast.makeText(Activity_band_chatting_room_invite.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Log.i("Activity_band_chatting_room_invite_err", t.getLocalizedMessage());
            }
        });
    }
    // 초대 클릭 시
    // 비공개 채팅방 생성 및
    // 개설자/초대한 사람 채팅방에 추가
    private void Retrofit비공개채팅방생성(int band_seq, String user_id, String user_name, ArrayList memberList, int room_type) {
        /***********************  채팅방 정보 가져오기  ***************************/

        retrofitAPI = RetrofitClass.getApiClient().create(RetrofitAPI.class);
        Call<Integer> call1 = retrofitAPI.createChatRoom_private(band_seq, user_id,memberList,room_type);
        call1.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(@NotNull Call<Integer> call, @NotNull Response<Integer> response) {
                // 서버에서 응답을 받아옴
                if (response.isSuccessful() && response.body() != null) {
                    System.out.println(response.body());
//
                    /** 서버에 메세지 전송 **/
                    // 비공개채팅방 만들었으니 방 생성자 및 초대 멤버 ReceiveThread 초기화 해달라
                    if (isService && ServiceChat != null) {
                        ServiceChat.sendMessage(response.body(), user_id, user_name,
                                4, "비공개채팅방", String.valueOf(memberList));
                    }

                    /** 채팅방으로 이동 **/
                    Intent intent = new Intent(Activity_band_chatting_room_invite.this, Activity_band_chatting_room.class);
                    intent.putExtra("방개설자_id", current_user_id);
                    intent.putExtra("밴드번호", 밴드번호);
                    intent.putExtra("채팅방_seq", response.body());
                    intent.putExtra("isMine",1);

                    startActivity(intent);
                    finish();

                } else {
                    Toast.makeText(Activity_band_chatting_room_invite.this, "비공개 채팅방 개설 실패", Toast.LENGTH_SHORT).show();
                }
            }

            // 통신실패시
            @Override
            public void onFailure(@NonNull Call<Integer> call, @NonNull Throwable t) {
                Toast.makeText(Activity_band_chatting_room_invite.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Log.i("Activity_band_chatting_room_invite_err", t.getLocalizedMessage());
            }
        });
    }

    /** ServiceConnection **/
    ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            // 서비스와 연결되었을 때 호출되는 메서드
            // 서비스 객체를 전역변수로 저장
            Service_chatting.ChattingBinder mb = (Service_chatting.ChattingBinder) service;
            ServiceChat = mb.getService(); // 서비스가 제공하는 메소드 호출하여
            // 서비스쪽 객체를 전달받을수 있슴
            isService = true;
        }

        public void onServiceDisconnected(ComponentName name) {
            // 서비스와 연결이 끊겼을 때 호출되는 메서드
            isService = false;
        }
    };

    /**
     * 생명주기
     **/
    @Override
    protected void onStart() {
        super.onStart();

        /** 서비스에 바인딩 **/
        Intent serviceIntent = new Intent(this, Service_chatting.class);
        bindService(serviceIntent, conn, BIND_AUTO_CREATE);
    }


    @Override
    protected void onStop() {
        super.onStop();

        /** 서비스에 언바인딩 **/
        if (isService) {
            unbindService(conn);
            isService = false;
        }
    }
}