package com.example.neighborfriend.Fragment;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;
import static android.content.Context.BIND_AUTO_CREATE;
import static android.content.Context.MODE_PRIVATE;
import static com.example.neighborfriend.MainActivity.HOST_URL;
import static com.example.neighborfriend.MainActivity.requestQueue;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.neighborfriend.Activity_ChangePw;
import com.example.neighborfriend.Activity_band_create_update;
import com.example.neighborfriend.Activity_band_setting;
import com.example.neighborfriend.Activity_login;
import com.example.neighborfriend.Activity_myprofile;
import com.example.neighborfriend.Class.FirebaseCloudStorage;
import com.example.neighborfriend.R;
import com.example.neighborfriend.Service.Service_chatting;
import com.example.neighborfriend.databinding.FragmentMoreBinding;
import com.example.neighborfriend.databinding.FragmentSearchBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ktx.Firebase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kakao.sdk.user.UserApiClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class Fragment_more extends Fragment {
    private FragmentMoreBinding binding;
    private final int SPECIAL = 3;
    /**
     * Service
     **/
    Service_chatting ServiceChat;
    boolean isService = false; // 서비스 중인 확인용
    /**
     * SharedPreferences
     **/
    SharedPreferences userData;
    SharedPreferences.Editor userData_e;
    private FrameLayout userFrm, pwChgFrm, logoutFrm, unlinkFrm;
    private ImageView userImg, pwChgImg, logoutImg, unlinkImg;
    private TextView usernick, securityTitle, pwChgTxt, logoutTxt, unlinkTxt;
    private String current_user_id, currentThum, current_user_name, currentPw, 폰번호;

    String 경로;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMoreBinding.inflate(inflater);
        View view = binding.getRoot();

        initializeView();
        initializeProperty();
        뒤로가기막기();

        // 프로필 수정
        userFrm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Activity_myprofile.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                밴드소개수정.launch(intent); // [인텐트 이동 실시]
            }
        });
        // 비밀번호 변경
        pwChgFrm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Activity_ChangePw.class);
                startActivity(intent);
            }
        });
        // 로그아웃
        logoutFrm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("로그아웃"); //AlertDialog의 제목 부분
                builder.setMessage("정말로 로그아웃 하시겠습니까?"); //AlertDialog의 내용 부분
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // 로그아웃
                        UserApiClient.getInstance().logout(new Function1<Throwable, Unit>() {
                            @Override
                            public Unit invoke(Throwable throwable) {
                                // 소켓 서버에 전달
                                sendMessage(-1,-1, current_user_id, current_user_name,
                                        SPECIAL, "로그아웃", null);
                                shared삭제();
//                                stopService();
                                // 로그인 창으로 이동
                                Intent intent = new Intent(getActivity(), Activity_login.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                return null;
                            }
                        });
                    }
                });
                builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                builder.create().show(); //보이기
            }
        });
        // 언링크
        unlinkFrm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("탈퇴하기"); //AlertDialog의 제목 부분
                builder.setMessage("정말로 탈퇴하시겠습니까?"); //AlertDialog의 내용 부분
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // 탈퇴하기
                        UserApiClient.getInstance().unlink(new Function1<Throwable, Unit>() {
                            @Override
                            public Unit invoke(Throwable throwable) {
                                // shared Delete
                                shared삭제();
                                // storage Delete
                                String 경로 = String.format("users/%s/thumnail", current_user_id);
                                StorageReference imagesRef = FirebaseCloudStorage.Storage_img(경로);
                                imagesRef.delete();
                                // User Delete
                                if (requestQueue == null)
                                    requestQueue = Volley.newRequestQueue(getContext());
                                Volley(current_user_id);
                                return null;
                            }
                        });
                    }
                });
                builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                builder.create().show(); //보이기
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        /**  변수 초기화 **/
        // 닉네임 초기화
        usernick.setText(current_user_name);
        /**  서비스에 바인딩 **/
        Intent serviceIntent = new Intent(getActivity(), Service_chatting.class);
        getContext().bindService(serviceIntent, conn, BIND_AUTO_CREATE);

    }


    /**
     * ActivityResultLauncher
     **/
    // 밴드 소개 수정 데이터 수신
    ActivityResultLauncher<Intent> 밴드소개수정 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == 1) {
                        Intent intent = result.getData();
                        currentThum = intent.getStringExtra("이미지");
                        current_user_name = intent.getStringExtra("닉네임");
                        폰번호 = intent.getStringExtra("폰번호");

                        /**  SharedPreferences **/
                        // 가져온 데이터 shared 에 저장
                        userData_e.putString("thumnail_url", currentThum).apply();
                        userData_e.putString("nickname", current_user_name).apply();
                        userData_e.putString("phone_num", 폰번호).apply();

//                        /**  썸네일 초기화 **/
//                        비동기썸네일초기화(currentThum, 경로);

                        if (currentThum.split("/")[0].equals("https:")) {
                            Glide.with(Fragment_more.this).load(currentThum).into(userImg);
                        } else {
                            StorageReference imagesRef = FirebaseCloudStorage.Storage_img(경로);
                            imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Glide.with(Fragment_more.this).load(uri).into(userImg);
                                }
                            });
                        }
                    }
                }

            });

    /**
     * initialize
     **/
    private void initializeView() {
        // 유저
        userFrm = binding.frameUser;
        userImg = binding.imageUserThumnail;
        usernick = binding.textUsername;
        // 비밀번호 변경
        securityTitle = binding.textTitleSecurity;
        pwChgFrm = binding.framePwChange;
        pwChgImg = binding.imageSecurity;
        pwChgTxt = binding.textPwChange;
        // 로그아웃
        logoutFrm = binding.frameLogout;
        logoutImg = binding.imageLogout;
        logoutTxt = binding.textLogout;
        // 탈퇴하기
        unlinkFrm = binding.frameX;
        unlinkImg = binding.imageX;
        unlinkTxt = binding.textX;
    }

    private void initializeProperty() {
        /**  SharedPreferences **/
        userData = getActivity().getSharedPreferences("user", MODE_PRIVATE); // sharedpreference
        userData_e = userData.edit(); // editor
        // user data
        current_user_id = userData.getString("id", "noneId");
        currentThum = userData.getString("thumnail_url", "noneThum");
        current_user_name = userData.getString("nickname", "noneNick");
        currentPw = userData.getString("password", "nonePw");

        /**  변수 초기화 **/
        경로 = String.format("users/%s/thumnail", current_user_id);
        /**  layout 초기화 **/
        // 썸네일 이미지 초기화
        if(currentThum==null);
        else if (currentThum.split("/")[0].equals("https:")) {
            Glide.with(Fragment_more.this).load(currentThum).into(userImg);
        } else {
            StorageReference imagesRef = FirebaseCloudStorage.Storage_img(경로);
            imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(Fragment_more.this).load(uri).into(userImg);
                }
            });
        }
        // 비밀번호 변경 (카카오 : 비밀번호 변경 필요 X)
        if (currentPw.equals("false")) {
            pwChgFrm.setVisibility(View.GONE);
            securityTitle.setVisibility(View.GONE);
        } else {
            pwChgFrm.setVisibility(View.VISIBLE);
            securityTitle.setVisibility(View.VISIBLE);
        }
        // 닉네임 초기화
        usernick.setText(current_user_name);
    }

    public void Volley(String id) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                HOST_URL + "kakao/unlink.php",
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("1")) {
                            Toast.makeText(getActivity(), "탈퇴하였습니다.", Toast.LENGTH_SHORT).show();
                            // 화면 이동
                            Intent intent = new Intent(getActivity(), Activity_login.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getActivity(), "탈퇴 실패", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Fragment_more", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", id);

                return params;
            }
        };

        // 이전 결과가 있더라도 새로 요청
        request.setShouldCache(false);
        requestQueue.add(request);
    }

    /** 비동기 썸네일 초기화 **/
    public void 비동기썸네일초기화(String 썸네일주소, String 경로) {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (썸네일주소.split("/")[0].equals("https:")) {
                    Glide.with(Fragment_more.this).load(썸네일주소).into(userImg);
                } else {
                    StorageReference imagesRef = FirebaseCloudStorage.Storage_img(경로);
                    imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(Fragment_more.this).load(uri).into(userImg);
                        }
                    });
                }
            }
        });

        thread.start();
    }

    /**뒤로가기막기**/
    public void 뒤로가기막기(){
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
    }

    private void shared삭제(){
        userData_e.clear().commit();
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
        }

        public void onServiceDisconnected(ComponentName name) {
            // 서비스와 연결이 끊겼을 때 호출되는 메서드
            isService = false;
        }
    };
    // 서비스 send 메서드
    public void sendMessage(int 밴드번호, int 채팅방_seq, String current_user_id, String current_user_name,
                            int msg_type, String content, String path) {
        if (isService && ServiceChat != null) {
            ServiceChat.sendMessage(밴드번호, 채팅방_seq, current_user_id, current_user_name,
                    msg_type, content, path);
        }
    }
}