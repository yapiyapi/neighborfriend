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
    /**  SharedPreferences **/ SharedPreferences userData;
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


//    /** 추가 **/
//    private ServiceChattingConnection serviceConnection;
//    private Service_chatting service;
//    private boolean isServiceBound = false;
//    /** 추가 **/

    private RecyclerView recy; private ImageView backBtn,plusBtn; private TextView title_view;
    private String currentId;
    ArrayList<chattingRoom> chatRoom_list;
    private String user_id,thumnail_uri,title,introduction,updated_at,created_at;
    private int seq,band_seq,room_type;

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
        채팅방종류 =-1;
        recy.setItemAnimator(null);

        Retrofit(밴드번호,currentId);
    }

    /** Http 통신 **/
    private void Retrofit(int band_seq_get, String user_id) {
        /***********************  채팅방 정보 가져오기  ***************************/
        retrofitAPI = RetrofitClass.getApiClient().create(RetrofitAPI.class);
        Call<ArrayList<chattingRoom>> call1 = retrofitAPI.readChatRoom(band_seq_get,user_id);
        call1.enqueue(new Callback<ArrayList<chattingRoom>>() {
            @Override
            public void onResponse(@NotNull Call<ArrayList<chattingRoom>> call, @NotNull Response<ArrayList<chattingRoom>> response) {
                // 서버에서 응답을 받아옴
                if (response.isSuccessful() && response.body() != null) {
                    List<chattingRoom> chattingRoomList = response.body();

                    for (int i = 0; i < chattingRoomList.size(); i++) {

                        // 종류 바뀔 때마다 카테고리 추가
                        if (채팅방종류 != chattingRoomList.get(i).getIsMine()){
                            채팅방종류 = chattingRoomList.get(i).getIsMine();
                            chattingRoom chattingRoom_1 = new chattingRoom();
                            chattingRoom_1.setViewType(0);
                            if (채팅방종류 == 1) {
                                chattingRoom_1.setTxt_contents("내 채팅");
                            }else{
                                chattingRoom_1.setTxt_contents("참여할 수 있는 공개채팅방");
                            }
                            chatRoom_list.add(chattingRoom_1);
                        }

                        // home_cell 초기화 -------------------------------
                        chattingRoom chattingRoom_2 = new chattingRoom();

                        chattingRoom_2.setSeq(chattingRoomList.get(i).getSeq());
                        chattingRoom_2.setBand_seq(chattingRoomList.get(i).getBand_seq());
                        chattingRoom_2.setUser_id(chattingRoomList.get(i).getUser_id());
                        chattingRoom_2.setThumnail(chattingRoomList.get(i).getThumnail());
                        chattingRoom_2.setTitle(chattingRoomList.get(i).getTitle());
                        chattingRoom_2.setIntroduction(chattingRoomList.get(i).getIntroduction());
                        chattingRoom_2.setRoom_type(chattingRoomList.get(i).getRoom_type());
                        chattingRoom_2.setCreated_at(chattingRoomList.get(i).getCreated_at());
                        chattingRoom_2.setMember(chattingRoomList.get(i).getMember());

                        // 마지막 메세지 정보 (null 가능)
                        chattingRoom_2.setTxt_contents(chattingRoomList.get(i).getTxt_contents());
                        chattingRoom_2.setMsg_type(chattingRoomList.get(i).getMsg_type());
                        if (chattingRoomList.get(i).getMsg_created_at()==null) chattingRoom_2.setMsg_created_at(null);
                        else chattingRoom_2.setMsg_created_at( 시간포맷(chattingRoomList.get(i).getMsg_created_at()));
                        chattingRoom_2.setIsMine(chattingRoomList.get(i).getIsMine());

                        chattingRoom_2.setViewType(1);
                        // --------------------------------------------------------------



                        chatRoom_list.add(chattingRoom_2);

                    }

                    /** 어댑터 연결 **/
                    레이아웃매니저 = new LinearLayoutManager(Activity_band_chatting_room_list.this);
                    어댑터 = new Adapter_chattingList(chatRoom_list,0);
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
                            intent.putExtra("isMine",isMine);

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
                Log.i("Activity_band_chatting_room_err", t.getLocalizedMessage());
            }
        });
    }

    private void Retrofit채팅방멤버추가(String user_id, int chatRoom_seq) {
        /***********************  채팅방 정보 가져오기  ***************************/
        retrofitAPI = RetrofitClass.getApiClient().create(RetrofitAPI.class);
        Call<String> call1 = retrofitAPI.createMember(user_id,chatRoom_seq);
        call1.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                // 서버에서 응답을 받아옴
                if (response.isSuccessful() && response.body() != null) {
                    if(response.body().equals("2")){
                        Toast.makeText(Activity_band_chatting_room_list.this, "멤버 추가 성공", Toast.LENGTH_SHORT).show();
                    }else if (response.body().equals("0"))
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

    /** 채팅방 추가 **/
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

    private BroadcastReceiver chatReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // 메세지를 받을 때마다 service 에서 msg 전송
            String chatMessage = intent.getStringExtra("message");

            JSONObject jsonMessage = null;
            try {
                jsonMessage = new JSONObject(chatMessage);

                int chatRoom_seq = Integer.parseInt(jsonMessage.optString("chatRoom_seq"));

//                        String seq = jsonMessage.optString("seq");
                // 혹시나 storage 저장할 때 필요할지도

                String user_id = jsonMessage.optString("user_id");
                String nickname = jsonMessage.optString("nickname");
                String txt_contents = jsonMessage.optString("txt_contents");
                String msg_uri = jsonMessage.optString("msg_uri");
                int msg_type = Integer.parseInt(jsonMessage.optString("msg_type"));
                String msg_created_at = jsonMessage.optString("msg_created_at");


                if(msg_type==3 && (txt_contents.equals("채팅방입장") ||
                        txt_contents.equals("채팅방나가기") ||
                        txt_contents.equals("채팅방삭제")));
                else{
                    // 해당 채팅방 찾아서
                    // 채팅메세지, 채팅메세지 시간, 읽음표시 수정
                    for (int i = 0; i < chatRoom_list.size(); i++) {
                        if(chatRoom_list.get(i).getSeq()==chatRoom_seq){
                            chattingRoom chattingRoom = new chattingRoom();

                            chattingRoom = chatRoom_list.get(i);

                            if(msg_type==3 && txt_contents.equals("채팅방최초입장")){
                                어댑터.updateItemSpecial(i,1);
                                break;
                            }else if (msg_type==3 && txt_contents.equals("채팅방탈퇴")) {
                                어댑터.updateItemSpecial(i, -1);
                                break;
                            }else {

                                if (msg_type == 1) chattingRoom.setTxt_contents("사진을 보냈습니다.");
                                else if (msg_type == 2) chattingRoom.setTxt_contents("동영상을 보냈습니다.");
                                else  chattingRoom.setTxt_contents(txt_contents);


                                // 읽음 표시
//                        chattingRoom.set(txt_contents);
                                chattingRoom.setMsg_created_at(시간포맷(msg_created_at));

                                어댑터.updateItem(i,chattingRoom);
                                break;
                            }
                        }
                    }
                }

//                System.out.println(chatRoom_seq);
//                System.out.println(user_id);
//                System.out.println(nickname);
//                System.out.println(txt_contents);
//                System.out.println(msg_uri);
//                System.out.println(msg_type);
//                System.out.println(msg_created_at);


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