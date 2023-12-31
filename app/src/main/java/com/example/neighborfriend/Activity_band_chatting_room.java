package com.example.neighborfriend;

import static android.content.ContentValues.TAG;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.neighborfriend.Adapter.Adapter_chatting;
import com.example.neighborfriend.Class.FirebaseCloudStorage;
import com.example.neighborfriend.Class.RetrofitClass;
import com.example.neighborfriend.Interface.RetrofitAPI;
import com.example.neighborfriend.Service.Service_chatting;
import com.example.neighborfriend.databinding.ActivityBandChattingRoomBinding;
import com.example.neighborfriend.object.chatting;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Activity_band_chatting_room extends AppCompatActivity {
    private ActivityBandChattingRoomBinding binding;
    private final int TEXT = 0;
    private final int IMAGE = 1;
    private final int VIDEO = 2;
    private final int SPECIAL = 3;
    private final int CREATE_ROOM = 4;

    /**
     * Network
     **/
    int networkStatus;
    /**
     * Gson
     **/
    Gson gson;
    /**
     * SharedPreferences
     **/
    SharedPreferences userData, 전송되지않은메세지;
    SharedPreferences.Editor 전송되지않은메세지_e;
    /**
     * RetrofitAPI
     **/
    RetrofitAPI retrofitAPI;
    /**
     * Service
     **/
    Service_chatting ServiceChat;
    boolean isService = false; // 서비스 중인 확인용

    /**
     * Recyclerview
     **/
    Adapter_chatting 어댑터;
    LinearLayoutManager 레이아웃매니저;
    /**
     * Socket
     **/
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    /**
     * view
     **/
    private EditText editMess;
    private ImageView btnBack, imgMore, btnPlus, btnSend;
    private TextView txtTitle;
    private RecyclerView recy;
    private int 밴드번호, 채팅방_seq, isMine;
    private String 방개설자_id, chatRoom_thum, chatRoom_title, chatRoom_intro;
    private String current_user_id, current_user_name;
    ArrayList<chatting> chatting_list;
    Uri uri = null;
    String 이미지, 비디오;

    // 페이징
    int page = 1, limit = 20;
    boolean isScrollEndReached = false;
    Instant beforeTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBandChattingRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        initializeView();
        initializeProperty();


        // 뒤로 버튼
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 소켓에 전달
                sendMessage(밴드번호, 채팅방_seq, current_user_id, current_user_name,
                        SPECIAL, "채팅방나가기", null);

                finish();
            }
        });
        // onbackpress
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 소켓에 전달
                sendMessage(밴드번호, 채팅방_seq, current_user_id, current_user_name,
                        SPECIAL, "채팅방나가기", null);

                finish();
            }
        });
        // 이미지/비디오
        btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomMenu(v, 0);
            }
        });
        // 전송 버튼
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editMess.getText().toString();
                if (!message.isEmpty()) {
                    sendMessage(밴드번호, 채팅방_seq, current_user_id, current_user_name,
                            TEXT, message, null);

                    editMess.getText().clear();
                }
            }
        });
        // 더보기
        imgMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (current_user_id.equals(방개설자_id)) showCustomMenu(v, 1);
                else showCustomMenu(v, 2);
            }
        });

        // 페이징 처리
        recy.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!recyclerView.canScrollVertically(-1)) {
                    // Check if scroll end is already reached
                    if (!isScrollEndReached) {
                        isScrollEndReached = true; // Set the flag to true

                        // Increment the page variable
                        page++;
                        System.out.println(page);
                        Retrofit_getMsg(채팅방_seq, current_user_id, page, limit, false);
                    }
                } else {
                    // Reset the flag if scrolled away from the bottom
                    isScrollEndReached = false;
                }
            }
        });


        /** 권한 **/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
        }
    }

    private void initializeView() {
        /** 상단 **/
        btnBack = binding.backBtnBandChat;
        txtTitle = binding.titleBandChat;
        imgMore = binding.imgThreedotBandChat;
        /** **/
        recy = binding.recyclerViewBandChat;
        btnPlus = binding.imgPlusImgVideo;
        editMess = binding.editMessage;
        btnSend = binding.imgSend;
    }

    private void initializeProperty() {
        /**  SharedPreferences **/
        userData = getSharedPreferences("user", MODE_PRIVATE);
        전송되지않은메세지 = getSharedPreferences("전송되지않은메세지", MODE_PRIVATE);
        //editor
        전송되지않은메세지_e = 전송되지않은메세지.edit(); // editor
        // 로그인 한 user id,name
        current_user_id = userData.getString("id", "noneId");
        current_user_name = userData.getString("nickname", "noneNickname");
        /** gson **/
        gson = new GsonBuilder().create();
        /**  Intent **/
        Intent intent = getIntent();
        방개설자_id = intent.getStringExtra("방개설자_id"); // 방 개설자
        밴드번호 = intent.getIntExtra("밴드번호", 0);
        채팅방_seq = intent.getIntExtra("채팅방_seq", 0);
        chatRoom_thum = intent.getStringExtra("thumnail_uri");
        chatRoom_title = intent.getStringExtra("title");
        chatRoom_intro = intent.getStringExtra("intro");
        isMine = intent.getIntExtra("isMine", -1);


        /** 초기화 **/
        chatting_list = new ArrayList<>();
        recy.setItemAnimator(null); // 애니메이션 효과 제거

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            beforeTime = Instant.now();
        }
        // 채팅 내용 가져오기
        Retrofit_getMsg(채팅방_seq, current_user_id, page, limit, true);


    }


    /**
     * 이미지 / 동영상
     **/
    ActivityResultLauncher<Intent> imgLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent intent = result.getData();
                        uri = intent.getData();
                        /** 초기화 **/
                        이미지 = uri.toString();
                        // 권한
                        getContentResolver().takePersistableUriPermission(Uri.parse(이미지), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                        sendMessage(밴드번호, 채팅방_seq, current_user_id, current_user_name,
                                IMAGE, null, 이미지);


                    }
                }
            });
    ActivityResultLauncher<Intent> videoLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent intent = result.getData();
                        uri = intent.getData();
                        /** 초기화 **/
                        비디오 = uri.toString();
                        // 권한
                        getContentResolver().takePersistableUriPermission(Uri.parse(비디오), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                        sendMessage(밴드번호, 채팅방_seq, current_user_id, current_user_name,
                                VIDEO, null, 비디오);

                    }
                }
            });


    /**
     * Http 통신
     **/
    // message 저장
    private void Retrofit_createMsg(int band_seq, int chatRoom_seq, String current_user_id, int msg_type,
                                    String txt_contents, String msg_uri) {
        if (msg_type != 3 || !(txt_contents.equals("채팅방입장") || txt_contents.equals("채팅방나가기") || txt_contents.equals("채팅방삭제"))) { // Special msg

            retrofitAPI = RetrofitClass.getApiClient().create(RetrofitAPI.class);
            Call<String> call = retrofitAPI.createChat(chatRoom_seq, current_user_id,
                    txt_contents, msg_uri, msg_type);


            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                    // 서버에서 응답을 받아옴
                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body().equals("0")) {
                            Toast.makeText(Activity_band_chatting_room.this, "채팅 저장 실패", Toast.LENGTH_SHORT).show();
                        } else {
                            // message 를 db에 저장이 되었을 때 message 의 seq 값 return
                            // text 가 아니면 firebase 에 저장
                            if (msg_type == IMAGE || msg_type == VIDEO) {
                                // 이미지 , 동영상 firebase 업로드 로직
                                String 경로 = String.format("chatRooms/%s/chatting/%s", chatRoom_seq, response.body());

                                // storage 저장
                                StorageReference imagesRef = FirebaseCloudStorage.Storage_img(경로);
                                UploadTask uploadTask = imagesRef.putFile(Uri.parse(msg_uri));
                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        if (task.isSuccessful()) {

                                            // 서비스에서 sendMessage
                                            if (isService && ServiceChat != null) {
                                                ServiceChat.sendMessage_Service(band_seq, chatRoom_seq, current_user_id, current_user_name,
                                                        msg_type, txt_contents, 경로);
                                            }

                                        } else {
                                            Log.e(TAG, "Image upload failed: " + task.getException().getMessage());
                                        }
                                    }
                                });
                            } else {
                                // 서비스에서 sendMessage
                                if (isService && ServiceChat != null) {
                                    ServiceChat.sendMessage_Service(band_seq, chatRoom_seq, current_user_id, current_user_name,
                                            msg_type, txt_contents, msg_uri);
                                }
                            }

                        }
                    } else {
                        Toast.makeText(Activity_band_chatting_room.this, "message 저장 실패", Toast.LENGTH_SHORT).show();
                    }
                }

                // 통신실패시
                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    Toast.makeText(Activity_band_chatting_room.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    Log.i("Activity_band_chatting_room_메세지저장_err", t.getLocalizedMessage());
                }
            });
        }
    }

    // message 가져오기
    private void Retrofit_getMsg(final int chatRoom_seq, String current_user_id, int page, int limit, boolean first) {
        /***********************  message 정보 가져오기  ***************************/
        ArrayList 페이징추가메세지 = new ArrayList();

        retrofitAPI = RetrofitClass.getApiClient().create(RetrofitAPI.class);
        Call<ArrayList<chatting>> call1 = retrofitAPI.readChat(chatRoom_seq, current_user_id, page, limit);

        call1.enqueue(new Callback<ArrayList<chatting>>() {
            @Override
            public void onResponse(@NotNull Call<ArrayList<chatting>> call, @NotNull Response<ArrayList<chatting>> response) {
                // 서버에서 응답을 받아옴
                if (response.isSuccessful() && response.body() != null) {
                    List<chatting> chatting = response.body();

                    // 실험 ( 페이징 O  vs  페이징 x )
                    Instant afterTime = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        afterTime = Instant.now();
                        long diffTime = Duration.between(beforeTime, afterTime).toMillis(); // 두 개의 실행 시간
                        System.out.println("실행 시간(ms): " + diffTime);
                    }

                    for (int i = 0; i < chatting.size(); i++) {
                        // home_cell 초기화
                        chatting chatting_1 = new chatting();
                        chatting_1.setChatRoom_seq(chatting.get(i).getChatRoom_seq());
                        chatting_1.setUser_id(chatting.get(i).getUser_id());
                        chatting_1.setNickname(chatting.get(i).getNickname());
                        chatting_1.setTxt_contents(chatting.get(i).getTxt_contents());
                        chatting_1.setMsg_uri(chatting.get(i).getMsg_uri());
                        chatting_1.setMsg_type(chatting.get(i).getMsg_type());
                        chatting_1.setUnread_list(chatting.get(i).getUnread_list());
                        chatting_1.setMsg_created_at(chatting.get(i).getMsg_created_at());
                        // list 에 bands_post object 추가


                        // 채팅을 작성한 사람 == 현재 유저
                        if (chatting.get(i).getNickname().equals(current_user_name)) {
                            if (chatting.get(i).getMsg_type() == 0) {//txt
                                chatting_1.setViewType(0); // 0,1,2,3,4,5
                            } else if (chatting.get(i).getMsg_type() == 1) {//img
                                chatting_1.setViewType(2);
                            } else if (chatting.get(i).getMsg_type() == 2) {//vdo
                                chatting_1.setViewType(4);
                            } else chatting_1.setViewType(7);
                        } else {
                            if (chatting.get(i).getMsg_type() == 0) {//txt
                                chatting_1.setViewType(1); // 0,1,2,3,4,5
                            } else if (chatting.get(i).getMsg_type() == 1) {//img
                                chatting_1.setViewType(3);
                            } else if (chatting.get(i).getMsg_type() == 2) {//vdo
                                chatting_1.setViewType(5);
                            } else chatting_1.setViewType(7);
                        }

                        페이징추가메세지.add(chatting_1);

                    }


                    // 보여지는 부분 (하단/상단)
                    if (first) {
                        chatting_list.addAll(페이징추가메세지);
                        /** 어댑터 연결 **/
                        레이아웃매니저 = new LinearLayoutManager(Activity_band_chatting_room.this, LinearLayoutManager.VERTICAL, true);
                        어댑터 = new Adapter_chatting(chatting_list);
                        recy.setLayoutManager(레이아웃매니저);
                        recy.setAdapter(어댑터);

                        recy.scrollToPosition(0);
                    } else {
                        ((Adapter_chatting) 어댑터).paging(페이징추가메세지);
                    }


                    //리사이클러뷰 화면전환
                    어댑터.setOnItemClickListener(new Adapter_chatting.OnItemClickListener() {
                        @Override
                        public void onItemClick(View v, int type, String uri_path, int chatRoom_seq, int seq) {
                            if (type == 0) { // 이미지
                                Intent intent = new Intent(v.getContext(), Activity_band_chatting_room_img.class);
                                intent.putExtra("uri_path", uri_path);
                                intent.putExtra("chatRoom_seq", chatRoom_seq);
                                intent.putExtra("seq", seq);
                                startActivity(intent);
                            } else { // 동영상
                                Intent intent = new Intent(v.getContext(), Activity_band_chatting_room_video.class);
                                intent.putExtra("uri_path", uri_path);
                                intent.putExtra("chatRoom_seq", chatRoom_seq);
                                intent.putExtra("seq", seq);
                                startActivity(intent);
                            }
                        }
                    });

                } else {
                    Toast.makeText(Activity_band_chatting_room.this, "메세지 가져오기 실패", Toast.LENGTH_SHORT).show();
                }
            }

            // 통신실패시
            @Override
            public void onFailure(@NonNull Call<ArrayList<chatting>> call, @NonNull Throwable t) {
                Toast.makeText(Activity_band_chatting_room.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Log.i("Activity_band_chatting_room_메세지가져오기_err", t.getLocalizedMessage());
            }
        });
    }

    // 채팅방 삭제
    private void Retrofit삭제(int band_seq, int 채팅방_seq) {
        /***********************  공개채팅방 저장  ***************************/
        retrofitAPI = RetrofitClass.getApiClient().create(RetrofitAPI.class);
        Call<List<String>> call1 = retrofitAPI.deleteChatRoom(band_seq, 채팅방_seq);
        call1.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(@NotNull Call<List<String>> call, @NotNull Response<List<String>> response) {
                // 서버에서 응답을 받아옴
                if (response.isSuccessful() && response.body() != null) {

                    List<String> 삭제_uri = response.body();

                    // 썸네일 삭제
                    String 채팅방썸네일경로 = String.format("chatRooms/%s/thumnail", 채팅방_seq);
                    StorageReference 채팅방썸네일 = FirebaseCloudStorage.Storage_img(채팅방썸네일경로);
                    채팅방썸네일.delete();
                    // storage 삭제로직

                    for (int i = 0; i < 삭제_uri.size(); i++) {

                        String 채팅방uri경로 = 삭제_uri.get(i).replace("\"", "");
                        StorageReference imagesRef = FirebaseCloudStorage.Storage_img(채팅방uri경로);
                        imagesRef.delete();
                    }
                    Toast.makeText(Activity_band_chatting_room.this, "채팅방 삭제 성공", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Activity_band_chatting_room.this, "채팅방 삭제 실패", Toast.LENGTH_SHORT).show();
                }
            }

            // 통신실패시
            @Override
            public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable t) {
                Toast.makeText(Activity_band_chatting_room.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Log.i("Activity_band_chatting_room_채팅방삭제_err", t.getLocalizedMessage());
            }
        });

    }

    // 채팅방 퇴장
    private void Retrofit채팅방멤버삭제(String user_id, int 채팅방_seq) {
        /***********************  공개채팅방 저장  ***************************/
        retrofitAPI = RetrofitClass.getApiClient().create(RetrofitAPI.class);
        Call<Integer> call1 = retrofitAPI.deleteMember(user_id, 채팅방_seq);
        call1.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(@NotNull Call<Integer> call, @NotNull Response<Integer> response) {
                // 서버에서 응답을 받아옴
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body() == 1) {
                        Toast.makeText(Activity_band_chatting_room.this, "채팅방 멤버 삭제 성공", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(Activity_band_chatting_room.this, "채팅방 멤버 삭제 실패", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(Activity_band_chatting_room.this, "채팅방 멤버 삭제 실패", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            // 통신실패시
            @Override
            public void onFailure(@NonNull Call<Integer> call, @NonNull Throwable t) {
                Toast.makeText(Activity_band_chatting_room.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Log.i("Activity_band_chatting_room_err", t.getLocalizedMessage());
            }
        });

    }


    /**
     * 더보기 [수정/삭제]
     **/
    private void showCustomMenu(View anchor, int 구별) { //0: 이미지/동영상  1:수정/삭제  2:나가기
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_image:
                        // 이미지
                        Intent intent1 = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent1.setType("image/*");
                        imgLauncher.launch(intent1);
                        return true;
                    case R.id.menu_video:
                        // 동영상
                        Intent intent2 = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent2.setType("video/*");
                        videoLauncher.launch(intent2);
                        return true;
                    case R.id.menu_update_chatting:
                        // 수정
                        Intent intent = new Intent(Activity_band_chatting_room.this, Activity_band_chatting_room_create_update.class);
                        intent.putExtra("밴드번호", 밴드번호);
                        intent.putExtra("채팅방_seq", 채팅방_seq);
                        intent.putExtra("thumnail_uri", chatRoom_thum);
                        intent.putExtra("title", chatRoom_title);
                        intent.putExtra("intro", chatRoom_intro);
                        startActivity(intent);
                        return true;
                    case R.id.menu_delete_chatting:
                        // 삭제
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(Activity_band_chatting_room.this);
                        builder1.setTitle("삭제하기"); //AlertDialog의 제목 부분
                        builder1.setMessage("정말로 삭제하시겠습니까?"); //AlertDialog의 내용 부분
                        builder1.setPositiveButton("예", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // 채팅방 삭제
                                sendMessage(밴드번호, 채팅방_seq, current_user_id, current_user_name,
                                        SPECIAL, "채팅방삭제", null);
                                Retrofit삭제(밴드번호, 채팅방_seq);
                            }
                        });
                        builder1.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                        builder1.create().show(); //보이기
                        return true;
                    case R.id.menu_getout_chatting:
                    case R.id.menu_getout:
                        // 채팅방퇴장
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(Activity_band_chatting_room.this);
                        builder2.setTitle("나가기"); //AlertDialog의 제목 부분
                        builder2.setMessage("정말로 채팅방을 나가시겠습니까?"); //AlertDialog의 내용 부분
                        builder2.setPositiveButton("예", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                // 채팅방에 퇴장했다고 서버에 전송
                                sendMessage(밴드번호, 채팅방_seq, current_user_id, current_user_name,
                                        SPECIAL, "채팅방퇴장", null);
                                Retrofit채팅방멤버삭제(current_user_id, 채팅방_seq);
                            }
                        });
                        builder2.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                        builder2.create().show();
                        return true;
                    //보이기
                    default:
                        return false;
                }
            }
        });
        if (구별 == 0) popup.inflate(R.menu.chatting_img_video);
        else if (구별 == 1) popup.inflate(R.menu.chatting_upd_del_getout);
        else popup.inflate(R.menu.chatting_getout);
        popup.show();
    }


    /**
     * 메서드
     **/
    public static String 시간포맷(String db시간) {
        // 시간 포맷 ex. 오전 11:11
        String 시 = db시간.split(" ")[1].split(":")[0];
        String 분 = db시간.split(" ")[1].split(":")[1];

        if (Integer.parseInt(시) >= 12) {
            return "오후 " + 시 + ":" + 분;
        } else return "오전 " + 시 + ":" + 분;
    }

    /**
     * 서비스
     **/
    // 서비스 커넥션
    ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            // 서비스와 연결되었을 때 호출되는 메서드
            // 서비스 객체를 전역변수로 저장
            Service_chatting.ChattingBinder mb = (Service_chatting.ChattingBinder) service;
            ServiceChat = mb.getService(); // 서비스가 제공하는 메소드 호출하여
            // 서비스쪽 객체를 전달받을수 있슴
            isService = true;

            /** 서비스 바인딩 후 전송 **/
            // 내 채팅방이 아니면 입장했다고 전송
            if (isMine == 0) {
                // 채팅방에 입장했다고 서버에 전송
                sendMessage(밴드번호, 채팅방_seq, current_user_id, current_user_name, SPECIAL, "채팅방최초입장", null);
                isMine = 1;
            } else {
                // 채팅방에 입장했다고 서버에 전송
                sendMessage(밴드번호, 채팅방_seq, current_user_id, current_user_name, SPECIAL, "채팅방입장", null);
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            // 서비스와 연결이 끊겼을 때 호출되는 메서드
            isService = false;
            Toast.makeText(getApplicationContext(),
                    "서비스 연결 해제 - serviceconnection",
                    Toast.LENGTH_LONG).show();
        }
    };

    // 서비스 send 메서드
    public void sendMessage(int 밴드번호, int 채팅방_seq, String current_user_id, String current_user_name,
                            int msg_type, String txt_contents, String path) {





        // 채팅방입장 : db 저장 X , 서버에 전송 O
        if (msg_type == SPECIAL && (txt_contents.equals("채팅방입장") ||
                txt_contents.equals("채팅방나가기") ||
                txt_contents.equals("채팅방삭제"))) {
            // 서비스에서 sendMessage
            if (isService && ServiceChat != null) {
                ServiceChat.sendMessage_Service(밴드번호, 채팅방_seq, current_user_id, current_user_name,
                        msg_type, txt_contents, path);
            }
        }else{
            // 이미지/ 동영상 : 보낼 때 로딩 표시
            if (msg_type == IMAGE || msg_type == VIDEO) {
                chatting chatting1 = new chatting();
                chatting1.setMsg_type(msg_type);
                chatting1.setMsg_uri(path);
                chatting1.setViewType(6);

                // 추가되는 message 어댑터에 반영
                ((Adapter_chatting) 어댑터).addChat(chatting1);
                // 가장 하단 보여주기
                recy.scrollToPosition(0);
                ////////////////////////////////////////////////////
            }

            // db 저장 및
            // 서비스에 전달 (sendMessage)
            Retrofit_createMsg(밴드번호, 채팅방_seq, current_user_id, msg_type, txt_contents, path);

        }


    }


    /**
     * 브로드캐스트 리시버
     **/
    private BroadcastReceiver chatReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // 메세지를 받을 때마다 service 에서 msg 전송
            String chatMessage = intent.getStringExtra("message");

            JSONObject jsonMessage = null;
            try {
                jsonMessage = new JSONObject(chatMessage);

                String chatRoom_seq = jsonMessage.optString("chatRoom_seq");
                String user_id = jsonMessage.optString("user_id");
                String nickname = jsonMessage.optString("nickname");
                String txt_contents = jsonMessage.optString("txt_contents");
                String msg_uri = jsonMessage.optString("msg_uri");
                int msg_type = Integer.parseInt(jsonMessage.optString("msg_type"));
                String msg_created_at = jsonMessage.optString("msg_created_at");
                String 채팅방에있는멤버리스트 = jsonMessage.optString("채팅방에있는멤버리스트");
                String 채팅방에없는멤버리스트 = jsonMessage.optString("채팅방에없는멤버리스트");


                // 읽음표시 업데이트
                if (msg_type == CREATE_ROOM) ;
                else ((Adapter_chatting) 어댑터).updateChat(채팅방에있는멤버리스트);


                //---------------chatting setting (추가할 데이터) ----------------------
                chatting chatting1 = new chatting();
                chatting1.setChatRoom_seq(Integer.parseInt(chatRoom_seq));
                chatting1.setUser_id(user_id);
                chatting1.setNickname(nickname);
                if (txt_contents != null) chatting1.setTxt_contents(txt_contents.trim());
                else chatting1.setTxt_contents(txt_contents);
                chatting1.setMsg_uri(msg_uri);
                chatting1.setMsg_type(msg_type);
                chatting1.setUnread_list(String.valueOf(채팅방에없는멤버리스트).replace("[", "").replace("]", "").replace(" ", ""));
                chatting1.setMsg_created_at(msg_created_at);

                if (user_id.equals(current_user_id)) { // 내가 보낸 메세지
                    if (msg_type == TEXT) chatting1.setViewType(0);
                    else if (msg_type == IMAGE) chatting1.setViewType(2);
                    else if (msg_type == VIDEO) chatting1.setViewType(4);
                    else chatting1.setViewType(7);
                } else {                               // 받은 메세지
                    if (msg_type == TEXT) chatting1.setViewType(1);
                    else if (msg_type == IMAGE) chatting1.setViewType(3);
                    else if (msg_type == VIDEO) chatting1.setViewType(5);
                    else chatting1.setViewType(7);
                }
                //------------------------------------------------------------

                //---------------- 어댑터 추가 (메세지 올 때마다 추가)---------------------------------
                if (msg_type == TEXT || msg_type == IMAGE || msg_type == VIDEO) {
                    // 내가 보낸 메세지일 때 이미지, 비디오는 로딩화면 제거
                    if (user_id.equals(current_user_id)) {
                        if (msg_type == IMAGE || msg_type == VIDEO) {
                            ((Adapter_chatting) 어댑터).removeChat();
                        }
                    }
                    // 추가되는 message 어댑터에 반영
                    ((Adapter_chatting) 어댑터).addChat(chatting1);
                    // 가장 하단 보여주기            if(!unread_list.equals("[]"))
                    recy.scrollToPosition(0);
                    ////////////////////////////////////////////////////
                } else { // Special : 채팅방최초입장 / 채팅방입장 / 채팅방나가기 / 채팅방퇴장 / 채팅방삭제 / 로그아웃
                    if (txt_contents.equals("채팅방삭제")) {
                        // 채팅방에 있으면 Activity finish
                        if (채팅방에있는멤버리스트.contains(current_user_id)) {
                            finish();
                        }
                    } else if (txt_contents.equals("채팅방최초입장")) {
                        ((Adapter_chatting) 어댑터).addChat(chatting1);
                        // 가장 하단 보여주기
                        recy.scrollToPosition(0);
                        ////////////////////////////////////////////////////
                    } else if (txt_contents.equals("채팅방퇴장")) {
                        ((Adapter_chatting) 어댑터).addChat(chatting1);
                        // 가장 하단 보여주기
                        recy.scrollToPosition(0);
                        ////////////////////////////////////////////////////
                    } else ;
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

        // 브로드캐스트 받을 준비
        IntentFilter intentFilter = new IntentFilter("채팅");
        registerReceiver(chatReceiver, intentFilter);

        /** 서비스에 바인딩 **/
        Intent serviceIntent = new Intent(this, Service_chatting.class);
        bindService(serviceIntent, conn, BIND_AUTO_CREATE);

    }

    @Override
    protected void onStop() {
        super.onStop();

        // 브로드캐스트 받을 준비 해제
        unregisterReceiver(chatReceiver);

        // 서비스에 언바인딩
        if (isService) {
            unbindService(conn);
            isService = false;
        }
    }

}