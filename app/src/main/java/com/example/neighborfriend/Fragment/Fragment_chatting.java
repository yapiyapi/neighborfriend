package com.example.neighborfriend.Fragment;

import static android.content.Context.BIND_AUTO_CREATE;
import static android.content.Context.MODE_PRIVATE;

import static com.example.neighborfriend.Activity_band_chatting_room.시간포맷;
import static com.example.neighborfriend.MainActivity.retrofitAPI;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.neighborfriend.Activity_band_chatting_room;
import com.example.neighborfriend.Activity_band_chatting_room_list;
import com.example.neighborfriend.Adapter.Adapter_chattingList;
import com.example.neighborfriend.Class.RetrofitClass;
import com.example.neighborfriend.Interface.RetrofitAPI;
import com.example.neighborfriend.Service.Service_chatting;
import com.example.neighborfriend.databinding.FragmentChattingBinding;
import com.example.neighborfriend.object.band;
import com.example.neighborfriend.object.chattingRoom;
import com.example.neighborfriend.object.home_cell;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Fragment_chatting extends Fragment {
    private FragmentChattingBinding binding;
    /**  SharedPreferences **/ SharedPreferences userData;
    /**
     * Service
     **/
    Service_chatting ServiceChat;
    boolean isService = false; // 서비스 중인 확인용
    /**  RetrofitAPI **/ RetrofitAPI retrofitAPI;
    /**  RecyclerView **/
    Adapter_chattingList 어댑터;
    LinearLayoutManager 레이아웃매니저;

    private ArrayList<chattingRoom> list_chattingRoom;
    private RecyclerView recy; private TextView title;
    private String currentId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding= FragmentChattingBinding.inflate(inflater);
        View view = binding.getRoot();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        initializeView();
        initializeProperty();
        뒤로가기막기();
    }

    /** initialize **/
    private void initializeView() {
        title = binding.ChatListTitle;
        recy = binding.recyclerViewChatList;
    }
    private void initializeProperty() {
        /**  SharedPreferences **/
        userData = getContext().getSharedPreferences("user", MODE_PRIVATE); // sharedpreference
        // user data
        currentId = userData.getString("id", "noneId");

        /**  초기화 **/
        list_chattingRoom = new ArrayList<chattingRoom>();
        recy.setItemAnimator(null);

        /**  Http 통신 **/
        // user id 의 밴드 목록 가져옴
        Retrofit(currentId);

    }


    /** Http 통신 **/
    private void Retrofit(String user_id) {
        /***********************  채팅방 정보 가져오기  ***************************/
        retrofitAPI = RetrofitClass.getApiClient().create(RetrofitAPI.class);
        Call<ArrayList<chattingRoom>> call1 = retrofitAPI.readChatRoom_my(user_id);
        call1.enqueue(new Callback<ArrayList<chattingRoom>>() {
            @Override
            public void onResponse(@NotNull Call<ArrayList<chattingRoom>> call, @NotNull Response<ArrayList<chattingRoom>> response) {
                // 서버에서 응답을 받아옴
                if (response.isSuccessful() && response.body() != null) {
                    List<chattingRoom> chattingRoomList = response.body();

                    for (int i = 0; i < chattingRoomList.size(); i++) {
                        /** 채팅방 **/
                        // 마지막 채팅 시간
                        if (chattingRoomList.get(i).getMsg_created_at() == null)
                            chattingRoomList.get(i).setMsg_created_at(null);
                        else
                            chattingRoomList.get(i).setMsg_created_at(시간포맷(chattingRoomList.get(i).getMsg_created_at()));
                        // 0: 카테고리/ 1: 채팅방정보
                        chattingRoomList.get(i).setViewType(1);
                        // --------------------------------------------------------------
                        list_chattingRoom.add(chattingRoomList.get(i));

                    }

                    /** 어댑터 연결 **/
                    레이아웃매니저 = new LinearLayoutManager(getContext());
                    어댑터 = new Adapter_chattingList(list_chattingRoom,1);
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
//                            intent.putExtra("isMine",isMine);

                            startActivity(intent);
                        }
                    });

                } else {
                    Toast.makeText(getContext(), "채팅방 가져오기 실패", Toast.LENGTH_SHORT).show();
                }
            }

            // 통신실패시
            @Override
            public void onFailure(@NonNull Call<ArrayList<chattingRoom>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Log.i("Fragment_chatting_err", t.getLocalizedMessage());
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
    /** BroadcastReceiver **/
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
                else if (msg_type == 4) { // 채팅방 생성
                } else {
                    // 해당 채팅방 찾아서
                    // 채팅메세지, 채팅메세지 시간, 읽음표시 수정
                    for (int i = 0; i < list_chattingRoom.size(); i++) {
                        if (list_chattingRoom.get(i).getSeq() == chatRoom_seq) {
                            chattingRoom chattingRoom = new chattingRoom();

                            chattingRoom = list_chattingRoom.get(i);

                            if (msg_type == 3) {
                                if (txt_contents.equals("채팅방최초입장")) {
                                    어댑터.updateItemSpecial(i, 1);
                                    break;
                                } else if (txt_contents.equals("채팅방퇴장")) {
                                    어댑터.updateItemSpecial(i, -1);
                                    break;
                                } else if (txt_contents.equals("채팅방삭제")){
                                    list_chattingRoom.remove(i);
                                    어댑터.deleteItem(i);
                                }

                            } else {

                                // 마지막 채팅
                                if (msg_type == 1) chattingRoom.setTxt_contents("사진을 보냈습니다.");
                                else if (msg_type == 2) chattingRoom.setTxt_contents("동영상을 보냈습니다.");
                                else chattingRoom.setTxt_contents(txt_contents);

                                // 읽음 표시 +1
                                chattingRoom.setUnreadNum(chattingRoom.getUnreadNum() + 1);

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
    public void onStart() {
        super.onStart();

        /** 서비스에 바인딩 **/
        Intent serviceIntent = new Intent(getContext(), Service_chatting.class);
        getContext().bindService(serviceIntent, conn, BIND_AUTO_CREATE);


        // 브로드캐스트 받을 준비
        IntentFilter intentFilter = new IntentFilter("채팅");
        getContext().registerReceiver(chatReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();

        // 서비스에 언바인딩
        if (isService) {
            getContext().unbindService(conn);
            isService = false;
        }

        // 브로드캐스트 받을 준비 해제
        getContext().unregisterReceiver(chatReceiver);
    }

}