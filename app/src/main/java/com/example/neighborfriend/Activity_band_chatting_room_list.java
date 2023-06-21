package com.example.neighborfriend;

import static com.example.neighborfriend.Activity_band_chatting_room.시간포맷;
import static com.example.neighborfriend.MainActivity.retrofitAPI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.neighborfriend.Adapter.Adapter_chattingList;
import com.example.neighborfriend.Class.RetrofitClass;
import com.example.neighborfriend.Interface.RetrofitAPI;
import com.example.neighborfriend.Service.Service_chatting;
import com.example.neighborfriend.databinding.ActivityBandChattingRoomListBinding;
import com.example.neighborfriend.object.chatting;
import com.example.neighborfriend.object.chattingRoom;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Activity_band_chatting_room_list extends AppCompatActivity {
    private ActivityBandChattingRoomListBinding binding;
    /**
     * SharedPreferences
     **/
    SharedPreferences userData;
    /**
     * Service
     **/
    Service_chatting ServiceChat;
    boolean isService = false; // 서비스 중인 확인용
    /**
     * RecyclerView
     **/
    Adapter_chattingList 어댑터;
    LinearLayoutManager 레이아웃매니저;

    private RecyclerView recy;
    private ImageView backBtn, plusBtn;
    private TextView title_view;
    private String currentId;
    ArrayList<chattingRoom> chatRoom_list;

    private int 채팅방종류;
    private int 밴드번호;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBandChattingRoomListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding = ActivityBandChattingRoomListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeView();
        initializeProperty();

        // 뒤로 가기
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 플러스 버튼
        plusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomMenu(v);
            }
        });
    }

    private void initializeView() {
        /** 상단 **/
        backBtn = binding.backBtnBandChatList;
        title_view = binding.titleBandChatList;
        plusBtn = binding.imgPlusBandChatList;
        /** 채팅 recy **/
        recy = binding.recyclerViewBandChatList;

    }

    private void initializeProperty() {
        /**  SharedPreferences **/
        userData = getSharedPreferences("user", MODE_PRIVATE); // sharedpreference
        // user data
        currentId = userData.getString("id", "noneId");
        /**  밴드번호 **/
        Intent intent = getIntent();
        밴드번호 = intent.getIntExtra("밴드번호", 0);

        /** 초기화 **/
        chatRoom_list = new ArrayList<>();
        채팅방종류 = -1;
        recy.setItemAnimator(null);

        /** recyclerView 생성 **/
        Retrofit채팅방정보가져오기(밴드번호, currentId);
    }

    /**
     * Http 통신
     **/
    private void Retrofit채팅방정보가져오기(int band_seq_get, String user_id) {
        /***********************  채팅방 정보 가져오기  ***************************/
        retrofitAPI = RetrofitClass.getApiClient().create(RetrofitAPI.class);
        Call<ArrayList<chattingRoom>> call1 = retrofitAPI.readChatRoom(band_seq_get, user_id);
        call1.enqueue(new Callback<ArrayList<chattingRoom>>() {
            @Override
            public void onResponse(@NotNull Call<ArrayList<chattingRoom>> call, @NotNull Response<ArrayList<chattingRoom>> response) {
                // 서버에서 응답을 받아옴
                if (response.isSuccessful() && response.body() != null) {
                    List<chattingRoom> chattingRoomList = response.body();


                    for (int i = 0; i < chattingRoomList.size(); i++) {

                        /** 카테고리 **/
                        if (채팅방종류 != chattingRoomList.get(i).getIsMine()) {
                            채팅방종류 = chattingRoomList.get(i).getIsMine();
                            chattingRoom chattingRoom_1 = new chattingRoom();
                            chattingRoom_1.setViewType(0);
                            if (채팅방종류 == 1) {
                                chattingRoom_1.setTxt_contents("내 채팅");
                            } else {
                                chattingRoom_1.setTxt_contents("참여할 수 있는 공개채팅방");
                            }
                            chatRoom_list.add(chattingRoom_1);
                        }

                        /** 채팅방 **/
                        // 마지막 채팅 시간
                        if (chattingRoomList.get(i).getMsg_created_at() == null)
                            chattingRoomList.get(i).setMsg_created_at(null);
                        else
                            chattingRoomList.get(i).setMsg_created_at(시간포맷(chattingRoomList.get(i).getMsg_created_at()));
                        // 0: 카테고리/ 1: 채팅방정보
                        chattingRoomList.get(i).setViewType(1);
                        // --------------------------------------------------------------

                        chatRoom_list.add(chattingRoomList.get(i));

                    }

                    /** 어댑터 연결 **/
                    레이아웃매니저 = new LinearLayoutManager(Activity_band_chatting_room_list.this);
                    어댑터 = new Adapter_chattingList(chatRoom_list, 0);
                    recy.setLayoutManager(레이아웃매니저);
                    recy.setAdapter(어댑터);

                    //리사이클러뷰 화면전환
                    어댑터.setOnItemClickListener(new Adapter_chattingList.OnItemClickListener() {
                        @Override
                        public void onItemClick(View v, String user_id, String thumnail_uri, String title, String intro, int isMine, int 밴드번호, int 채팅방_seq) {

                            Intent intent = new Intent(v.getContext(), Activity_band_chatting_room.class);
                            intent.putExtra("방개설자_id", user_id);
                            intent.putExtra("밴드번호", 밴드번호);
                            intent.putExtra("채팅방_seq", 채팅방_seq);
                            intent.putExtra("thumnail_uri", thumnail_uri);
                            intent.putExtra("title", title);
                            intent.putExtra("intro", intro);
                            intent.putExtra("isMine", isMine);

                            Retrofit채팅방멤버추가(currentId, 채팅방_seq);

                            startActivity(intent);


//                            /** 추가 **/
//                            serviceConnection = new ServiceChattingConnection(채팅방_seq);
//                            Intent servIntent = new Intent(Activity_band_chatting_room_list.this, Service_chatting.class);
//                            bindService(servIntent, serviceConnection, Context.BIND_AUTO_CREATE);
                        }
                    });

                } else {
                    Toast.makeText(Activity_band_chatting_room_list.this, "채팅방 가져오기 실패", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            // 통신실패시
            @Override
            public void onFailure(@NonNull Call<ArrayList<chattingRoom>> call, @NonNull Throwable t) {
                Toast.makeText(Activity_band_chatting_room_list.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Log.i("Activity_band_chatting_room_list_err", t.getLocalizedMessage());
            }
        });
    }

    private void Retrofit채팅방멤버추가(String user_id, int chatRoom_seq) {
        /***********************  채팅방 멤버 추가  ***************************/
        retrofitAPI = RetrofitClass.getApiClient().create(RetrofitAPI.class);
        Call<String> call1 = retrofitAPI.createMember(user_id, chatRoom_seq);
        call1.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                // 서버에서 응답을 받아옴
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().equals("2")) {
                        Toast.makeText(Activity_band_chatting_room_list.this, "멤버 추가 성공", Toast.LENGTH_SHORT).show();
                    } else if (response.body().equals("0"))
                        Toast.makeText(Activity_band_chatting_room_list.this, "멤버 추가 실패", Toast.LENGTH_SHORT).show();
                    else ;
                } else {
                    Toast.makeText(Activity_band_chatting_room_list.this, "멤버 추가 실패", Toast.LENGTH_SHORT).show();
                }
            }

            // 통신실패시
            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(Activity_band_chatting_room_list.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Log.i("Activity_band_chatting_room_list_err", t.getLocalizedMessage());
            }
        });


    }

    /**
     * 채팅방 추가 버튼 클릭 이벤트
     **/
    private void showCustomMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.chatting_room_public:
                        // 공개채팅방
                        Intent intent1 = new Intent(Activity_band_chatting_room_list.this, Activity_band_chatting_room_create_update.class);
                        intent1.putExtra("밴드번호", 밴드번호);
                        startActivity(intent1);
                        return true;
                    case R.id.chatting_room_private:
                        // 비공개채팅방
                        Intent intent2 = new Intent(Activity_band_chatting_room_list.this, Activity_band_chatting_room_invite.class);
                        intent2.putExtra("밴드번호", 밴드번호);
                        startActivity(intent2);
                        return true;
                    //보이기
                    default:
                        return false;
                }
            }
        });
        popup.inflate(R.menu.chatting_room_plus);
        popup.show();
    }


    /**
     * ServiceConnection
     **/
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

    private BroadcastReceiver chatReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // 메세지를 받을 때마다 service 에서 msg 전송
            String chatMessage = intent.getStringExtra("message");

            JSONObject jsonMessage = null;
            try {
                jsonMessage = new JSONObject(chatMessage);


                int band_seq = Integer.parseInt(jsonMessage.optString("band_seq"));
                int chatRoom_seq = Integer.parseInt(jsonMessage.optString("chatRoom_seq"));
                String user_id = jsonMessage.optString("user_id");
                String nickname = jsonMessage.optString("nickname");
                String txt_contents = jsonMessage.optString("txt_contents");
                String msg_uri = jsonMessage.optString("msg_uri");
                int msg_type = Integer.parseInt(jsonMessage.optString("msg_type"));
                String msg_created_at = jsonMessage.optString("msg_created_at");

                if (msg_type == 3 && (txt_contents.equals("채팅방입장") ||
                        txt_contents.equals("채팅방나가기") ||
                        txt_contents.equals("로그아웃"))) ;
                else if (msg_type == 4) {
                    // 채팅방 (공개/비공개) 생성 시
                    chatRoom_list.clear();
                    /** recyclerView 생성 **/
                    Retrofit채팅방정보가져오기(밴드번호, currentId);
                } else {
                    // 해당 채팅방 찾아서
                    // 채팅메세지, 채팅메세지 시간, 읽음표시 수정
                    for (int i = 0; i < chatRoom_list.size(); i++) {
                        if (chatRoom_list.get(i).getSeq() == chatRoom_seq) {
                            chattingRoom chattingRoom = new chattingRoom();

                            chattingRoom = chatRoom_list.get(i);

                            if (msg_type == 3) {
                                if (txt_contents.equals("채팅방최초입장")) {
                                    어댑터.updateItemSpecial(i, 1);
                                    break;
                                } else if (txt_contents.equals("채팅방퇴장")) {
                                    어댑터.updateItemSpecial(i, -1);
                                    break;
                                } else if (txt_contents.equals("채팅방삭제")){
                                    chatRoom_list.remove(i);
                                    어댑터.deleteItem(i);
                                }

                            } else {

                                // 마지막 채팅
                                if (msg_type == 1) chattingRoom.setTxt_contents("사진을 보냈습니다.");
                                else if (msg_type == 2) chattingRoom.setTxt_contents("동영상을 보냈습니다.");
                                else chattingRoom.setTxt_contents(txt_contents);

                                // 읽음 표시 +1
                                if (chatRoom_list.get(i).getIsMine() == 1) {// 내 채팅
                                    chattingRoom.setUnreadNum(chattingRoom.getUnreadNum() + 1);
                                }
                                // 마지막 채팅 시간
                                chattingRoom.setMsg_created_at(시간포맷(msg_created_at));

                                어댑터.updateItem(i, chattingRoom);
                                break;
                            }
                        }
                    }
                }


            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
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


        // 브로드캐스트 받을 준비
        IntentFilter intentFilter = new IntentFilter("채팅");
        registerReceiver(chatReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // 서비스에 언바인딩
        if (isService) {
            unbindService(conn);
            isService = false;
        }

        // 브로드캐스트 받을 준비 해제
        unregisterReceiver(chatReceiver);
    }


}