package com.example.neighborfriend.Fragment;

import static android.content.Context.MODE_PRIVATE;

import static com.example.neighborfriend.Activity_band_chatting_room.시간포맷;
import static com.example.neighborfriend.MainActivity.retrofitAPI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.example.neighborfriend.databinding.FragmentChattingBinding;
import com.example.neighborfriend.object.band;
import com.example.neighborfriend.object.chattingRoom;
import com.example.neighborfriend.object.home_cell;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Fragment_chatting extends Fragment {
    private FragmentChattingBinding binding;
    /**  SharedPreferences **/ SharedPreferences userData;
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
                        // home_cell 초기화
                        chattingRoom chattingRoom_1 = new chattingRoom();

                        // 채팅방 정보
                        chattingRoom_1.setSeq(chattingRoomList.get(i).getSeq());
                        chattingRoom_1.setBand_seq(chattingRoomList.get(i).getBand_seq());
                        chattingRoom_1.setBand_title(chattingRoomList.get(i).getBand_title());
                        chattingRoom_1.setUser_id(chattingRoomList.get(i).getUser_id());
                        chattingRoom_1.setThumnail(chattingRoomList.get(i).getThumnail());
                        chattingRoom_1.setTitle(chattingRoomList.get(i).getTitle());
                        chattingRoom_1.setIntroduction(chattingRoomList.get(i).getIntroduction());
                        chattingRoom_1.setRoom_type(chattingRoomList.get(i).getRoom_type());
                        chattingRoom_1.setCreated_at(chattingRoomList.get(i).getCreated_at());
                        chattingRoom_1.setMember(chattingRoomList.get(i).getMember());

                        // 마지막 메세지 정보 (null 가능)
                        chattingRoom_1.setTxt_contents(chattingRoomList.get(i).getTxt_contents());
                        chattingRoom_1.setMsg_type(chattingRoomList.get(i).getMsg_type());
                        if (chattingRoomList.get(i).getMsg_created_at()==null) chattingRoom_1.setMsg_created_at(null);
                        else chattingRoom_1.setMsg_created_at( 시간포맷(chattingRoomList.get(i).getMsg_created_at()));
                        chattingRoom_1.setIsMine(chattingRoomList.get(i).getIsMine());

                        // list 에 bands_post object 추가
//                       Log.i("ASdf",chattingRoomList.get(i).getBand_title());
                        list_chattingRoom.add(chattingRoom_1);

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
}