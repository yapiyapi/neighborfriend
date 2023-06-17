package com.example.neighborfriend;

import static android.content.ContentValues.TAG;
import static com.example.neighborfriend.MainActivity.HOST_URL;
import static com.example.neighborfriend.MainActivity.retrofitAPI;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.neighborfriend.Class.FirebaseCloudStorage;
import com.example.neighborfriend.Class.RetrofitClass;
import com.example.neighborfriend.Interface.RetrofitAPI;
import com.example.neighborfriend.Service.Service_chatting;
import com.example.neighborfriend.databinding.ActivityBandChattingRoomCreateUpdateBinding;
import com.example.neighborfriend.object.band;
import com.example.neighborfriend.object.chattingRoom;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Activity_band_chatting_room_create_update extends AppCompatActivity {
    private ActivityBandChattingRoomCreateUpdateBinding binding;
    /**
     * Service
     **/
    Service_chatting ServiceChat;
    boolean isService = false; // 서비스 중인 확인용
    /**SharedPreferences**/
    SharedPreferences userData;


    private EditText editTit, editCont;
    private ImageView img;
    private TextView txtCancel, txtCompl;
    Uri uri = null; String 이미지;
    private int 밴드번호, 채팅방_seq; private String chatRoom_thum, chatRoom_title, chatRoom_intro;
    private String current_user_id,current_user_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBandChattingRoomCreateUpdateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeView();
        initializeProperty();

        // 뒤로 가기
        txtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 완료
        if(채팅방_seq==0) {
            // 생성
            txtCompl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 공개채팅방 개설
                    if (!이미지.equals("")) {
                        if (!editTit.getText().toString().equals("") && !editCont.getText().toString().equals("")) {
                            Retrofit생성(밴드번호, current_user_id, current_user_name , 이미지,
                                    editTit.getText().toString(), editCont.getText().toString(), 1);
                        } else
                            Toast.makeText(Activity_band_chatting_room_create_update.this, "모든 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();

                    } else
                        Toast.makeText(Activity_band_chatting_room_create_update.this, "이미지를 추가해주세요.", Toast.LENGTH_SHORT).show();


                }
            });
        }else{
            // 수정
            txtCompl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 공개채팅방 수정
                    if (!editTit.getText().toString().equals("") && !editCont.getText().toString().equals("")) {

                        Retrofit수정(밴드번호, 채팅방_seq, 이미지,
                                editTit.getText().toString(), editCont.getText().toString());

                    } else Toast.makeText(Activity_band_chatting_room_create_update.this, "모든 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();


                }
            });
        }

        // 썸네일 추가
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("image/*");
                imgLauncher.launch(intent);
            }
        });

    }

    private void initializeView() {
        /** 상단 **/
        txtCancel = binding.textCancelBandChatCreateUpdate;
        txtCompl = binding.textCompleteBandChatCreateUpdate;
        /** 설정 **/
        img = binding.imgBandChatCreateUpdate;
        editTit = binding.editTitleBandChatCreateUpdate;
        editCont = binding.editContentsBandChatCreateUpdate;

    }

    private void initializeProperty() {
        /**  SharedPreferences **/
        userData = getSharedPreferences("user", MODE_PRIVATE); // sharedpreference
        // 로그인 한 user id
        current_user_id = userData.getString("id", "noneId");
        current_user_name = userData.getString("nickname", "noneNickname");
        /**  밴드번호 **/
        Intent intent = getIntent();
        밴드번호 = intent.getIntExtra("밴드번호", 0);
        // 수정 시
        채팅방_seq = intent.getIntExtra("채팅방_seq", 0);
        chatRoom_thum = intent.getStringExtra("thumnail_uri");
        chatRoom_title = intent.getStringExtra("title");
        chatRoom_intro = intent.getStringExtra("intro");

        이미지 = "";

        /** 수정 **/
        if(채팅방_seq!=0){
            // band 썸네일 초기화
            StorageReference imagesRef = FirebaseCloudStorage.Storage_img(chatRoom_thum);
            imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(Activity_band_chatting_room_create_update.this).load(uri).into(img);
                }
            });
            editTit.setText(chatRoom_title);
            editCont.setText(chatRoom_intro);
            // 수정일 때 이미지가 "" 이면
            // DB : 수정안함
            // storage : 수정안함
        }
    }


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

                        Glide.with(Activity_band_chatting_room_create_update.this)
                                .load(이미지)
                                .into(img);
                    }
                }
            });

    /** Http 통신 **/
    // 채팅방 생성
    private void Retrofit생성(int band_seq, String user_id, String user_name, String 썸네일, String 제목,String 소개글 , int room_type) {
        /***********************  공개채팅방 저장  ***************************/
        retrofitAPI = RetrofitClass.getApiClient().create(RetrofitAPI.class);
        Call<String> call1 = retrofitAPI.createChatRoom(band_seq,user_id, 썸네일, 제목, 소개글, room_type);
        call1.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                // 서버에서 응답을 받아옴
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().equals("0")) {
                        Toast.makeText(Activity_band_chatting_room_create_update.this, "채팅방 개설 실패", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(Activity_band_chatting_room_create_update.this, "채팅방 개설 성공", Toast.LENGTH_SHORT).show();


                        String 경로 = String.format("chatRooms/%s/thumnail", response.body());

                        // storage 저장
                        StorageReference imagesRef = FirebaseCloudStorage.Storage_img(경로);
                        Log.i("마지막 ",썸네일);
                        UploadTask uploadTask = imagesRef.putFile(Uri.parse(썸네일));
                        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    finish();
                                } else {
                                    Log.e(TAG, "Image upload failed: " + task.getException().getMessage());
                                }
                            }
                        });

                        // 서버에 메세지 전송
                        // 공개채팅방 만들었으니 방 생성자 ReceiveThread 초기화 해달라
                        if (isService && ServiceChat != null) {
                            ServiceChat.sendMessage(Integer.parseInt(response.body()), user_id, user_name,
                                    4, "공개채팅방", null);
                        }
                    }
                } else {
                    Toast.makeText(Activity_band_chatting_room_create_update.this, "채팅방 개설 실패", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            // 통신실패시
            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(Activity_band_chatting_room_create_update.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Log.i("Activity_band_chatting_room_create_update_err", t.getLocalizedMessage());
            }
        });
    }
    // 채팅방 수정
    private void Retrofit수정(int band_seq, int 채팅방_seq, String 썸네일, String 제목,String 소개글 ) {
        /***********************  공개채팅방 저장  ***************************/
        retrofitAPI = RetrofitClass.getApiClient().create(RetrofitAPI.class);
        Call<String> call1 = retrofitAPI.updateChatRoom(band_seq,채팅방_seq, 제목, 소개글);
        call1.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                // 서버에서 응답을 받아옴
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().equals("0")) {
                        Toast.makeText(Activity_band_chatting_room_create_update.this, "채팅방 수정 실패", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(Activity_band_chatting_room_create_update.this, "채팅방 수정 성공", Toast.LENGTH_SHORT).show();

                        if(!썸네일.equals("")){ // 썸네일 바꿧을 때, storage 수정
                            String 경로 = String.format("chatRooms/%s/thumnail", response.body());

                            // storage 저장
                            StorageReference imagesRef = FirebaseCloudStorage.Storage_img(경로);
//                            Log.i("마지막 ",썸네일);
                            UploadTask uploadTask = imagesRef.putFile(Uri.parse(썸네일));
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent( Activity_band_chatting_room_create_update.this , Activity_band_chatting_room_list.class);
                                        intent.putExtra("밴드번호", 밴드번호);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    } else {
                                        Log.e(TAG, "Image upload failed: " + task.getException().getMessage());
                                    }
                                }
                            });
                        } else{
                            Intent intent = new Intent( Activity_band_chatting_room_create_update.this , Activity_band_chatting_room_list.class);
                            intent.putExtra("밴드번호", 밴드번호);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }

                    }
                } else {
                    Toast.makeText(Activity_band_chatting_room_create_update.this, "채팅방 개설 실패", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            // 통신실패시
            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(Activity_band_chatting_room_create_update.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Log.i("Activity_band_chatting_room_create_update_err", t.getLocalizedMessage());
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