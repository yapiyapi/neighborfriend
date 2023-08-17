package com.example.neighborfriend;

import static android.content.ContentValues.TAG;
import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static com.example.neighborfriend.Activity_band_create_update.카테고리한글;
import static com.example.neighborfriend.Activity_live_streaming_broadcaster.SERVER_URL;
import static com.example.neighborfriend.Activity_search_box.나이포맷;
import static com.example.neighborfriend.Activity_search_box.성별포맷;
import static com.example.neighborfriend.MainActivity.HOST_URL;
import static com.example.neighborfriend.MainActivity.requestQueue;
import static com.example.neighborfriend.MainActivity.retrofitAPI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.neighborfriend.Adapter.Adapter_band_postList;
import com.example.neighborfriend.Class.FirebaseCloudStorage;
import com.example.neighborfriend.Class.RetrofitClass;
import com.example.neighborfriend.Interface.RetrofitAPI;
import com.example.neighborfriend.databinding.ActivityBandBinding;
import com.example.neighborfriend.object.User;
import com.example.neighborfriend.object.band;
import com.example.neighborfriend.object.bands_post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;
import org.webrtc.PeerConnection;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Activity_band extends AppCompatActivity {
    private ActivityBandBinding binding;
    /**
     * Gson
     **/
    Gson gson;
    /**
     * RecyclerView
     **/
    Adapter_band_postList 어댑터_band_post;
    LinearLayoutManager 레이아웃매니저;
    ArrayList<bands_post> post_list;
    /** Socket **/
    private Socket socket;
    // view
    private ImageView imgband, btnChat, btnSet;
    private RecyclerView recyBandPost; private SwipeRefreshLayout swipeRefreshLayout;
    private TextView txtTitl, txtMemb, txtPubl, txtInvt;
    private Button btnwrt, btnStr;
    private String user_id, thumnail_url, 제목, 소개글, 멤버수;
    private String user_nickname, user_thumnail_url;
    private int 카테고리, 공개여부, 나이제한_시작, 나이제한_끝, 성별제한;
    private int 밴드번호; private String 방송자_id, 방송자_시작시간;
    private String 밴드jsonString, Activity_from;
    private Uri 초대링크;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBandBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

    }

    @Override
    protected void onResume() {
        super.onResume();
        binding = ActivityBandBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initializeView();
        initializeProperty();

        // 초대
        txtInvt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDynamicLink(String.valueOf(밴드번호));
            }
        });
        // 글쓰기
        btnwrt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 글쓰기
                Intent intent = new Intent(Activity_band.this, Activity_band_post_create_update.class);
                intent.putExtra("밴드번호", 밴드번호);
                startActivity(intent);
            }
        });
        // 채팅
        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Activity_band.this, Activity_band_chatting_room_list.class);
                intent.putExtra("밴드번호", 밴드번호);
                startActivity(intent);
            }
        });
        // 라이브 스트리밍
        btnStr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Activity_band.this, Activity_live_streaming_broadcaster.class);
                intent.putExtra("밴드번호", 밴드번호);
                startActivity(intent);
            }
        });

    }

    private void initializeView() {
        // 이미지
        imgband = binding.imageViewBand;
        // text
        txtTitl = binding.textTitle;
        txtMemb = binding.textMember;
        txtPubl = binding.textPublic;
        txtInvt = binding.textInvite;
        // btn
        btnwrt = binding.btnWrite;
        btnStr = binding.btnStream;
        btnChat = binding.btnChat;
        btnSet = binding.btnSetting;
        // recy
        swipeRefreshLayout = binding.swipeRefreshLayout;
        recyBandPost = binding.recyBandPost;
    }

    private void initializeProperty() {
        Intent intent = getIntent();
        밴드번호 = intent.getIntExtra("밴드번호", 0);
        /** gson **/
        gson = new GsonBuilder().create();
        // 밴드 정보 가져오기
        // 및 초기화
        if (밴드번호 != 0) {
            // 밴드 정보 가져오기 (bands)
            // 밴드 posts 가져오기 (recyclerview)
            Retrofit(밴드번호);
            // 밴드 멤버수 가져오기 (users_bands)
            if (requestQueue == null)
                requestQueue = Volley.newRequestQueue(getApplicationContext());
            Volley(밴드번호);
        }
        /**  RecyclerView **/
        post_list = new ArrayList<>();

    }

    /**
     * 초대
     **/
    // 긴 동적 주소
    private void createDynamicLink(String 밴드번호_get) {
        DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(getCheckDeepLink(밴드번호_get))    //정보를 담는 json 사이트를 넣자!!
                .setDynamicLinkDomain("neighborfriend1.page.link")
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder(getPackageName()).build())
                .buildDynamicLink();
        Uri dylinkuri = dynamicLink.getUri();   //긴 URI
        //짧은 URI사용
        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLongLink(dylinkuri)
                .buildShortDynamicLink()

                .addOnCompleteListener(this, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            Uri shortLink = task.getResult().getShortLink();
                            Uri flowchartLink = task.getResult().getPreviewLink();

                            /** 초대 링크 보내기 **/
                            // 짧은 동적 링크 코드 보내기 (메신저 앱)
                            Intent sendIntent = new Intent();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_TEXT, shortLink.toString());
                            sendIntent.setType("text/plain");

                            Intent shareIntent = Intent.createChooser(sendIntent, null);
                            startActivity(shareIntent);
                        } else {
                            Log.w(TAG, task.toString());
                        }
                    }
                });

    }

    //deeplink uri만들기
    private Uri getCheckDeepLink(String 밴드번호_get) {
        return Uri.parse("https://www.example.com/?band=" + 밴드번호_get); //example
    }

    /**
     * Http 통신
     **/
    // band seq 로 band data 가져오기
    private void Retrofit(final int band_seq) {
        /***********************  밴드 정보 가져오기  ***************************/
        retrofitAPI = RetrofitClass.getApiClient().create(RetrofitAPI.class);
        Call<band> call1 = retrofitAPI.getBand(band_seq);

        call1.enqueue(new Callback<band>() {
            @Override
            public void onResponse(@NotNull Call<band> call, @NotNull Response<band> response) {
                // 서버에서 응답을 받아옴
                if (response.isSuccessful() && response.body() != null) {

                    user_id = response.body().getUser_id();
                    thumnail_url = response.body().getThumnail_url();
                    제목 = response.body().get제목();
                    소개글 = response.body().get소개글();
                    카테고리 = response.body().get카테고리();
                    공개여부 = response.body().get공개여부();
                    성별제한 = response.body().get성별제한();
                    나이제한_시작 = response.body().get나이제한_시작();
                    나이제한_끝 = response.body().get나이제한_끝();

                    /**  초기화 **/
                    // band 객체 초기화
                    밴드jsonString = gson.toJson(response.body(), band.class);
                    // band 썸네일 초기화
                    String 경로 = String.format("bands/%s/thumnail", band_seq);

                    StorageReference imagesRef = FirebaseCloudStorage.Storage_img(경로);
                    imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(Activity_band.this).load(uri).into(imgband);
                        }
                    });
                    // 제목
                    txtTitl.setText(제목);
                    // 공개 여부
                    if (공개여부 == 0) txtPubl.setText("공개");
                    else txtPubl.setText("비공개");



                    // 설정으로
                    btnSet.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Activity_band.this, Activity_band_setting.class);
                            intent.putExtra("밴드정보", 밴드jsonString);
                            startActivity(intent);
                        }
                    });

                } else {
                    Toast.makeText(Activity_band.this, "실패", Toast.LENGTH_SHORT).show();
                }
            }

            // 통신실패시
            @Override
            public void onFailure(@NonNull Call<band> call, @NonNull Throwable t) {
                Toast.makeText(Activity_band.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Log.i("band_error", t.getLocalizedMessage());
            }
        });
        /***********************  밴드 게시물 Arraylist 가져오기  ***************************/
        retrofitAPI = RetrofitClass.getApiClient().create(RetrofitAPI.class);
        Call<ArrayList<bands_post>> call2 = retrofitAPI.getBandPostList(band_seq);

        call2.enqueue(new Callback<ArrayList<bands_post>>() {
            @Override
            public void onResponse(@NotNull Call<ArrayList<bands_post>> call, @NotNull Response<ArrayList<bands_post>> response) {
                // 서버에서 응답을 받아옴
                if (response.isSuccessful() && response.body() != null) {
                    List<bands_post> band_list = response.body();

                    for (int i = 0; i < band_list.size(); i++) {
                        // home_cell 초기화
                        bands_post bands_post_1 = new bands_post();
                        bands_post_1.setBand_seq(band_list.get(i).getBand_seq());
                        bands_post_1.setSeq(band_list.get(i).getSeq());
                        bands_post_1.setUser_id(band_list.get(i).getUser_id());
                        bands_post_1.setImage_uri(band_list.get(i).getImage_uri());
                        bands_post_1.set게시글(band_list.get(i).get게시글());
                        bands_post_1.setUpdated_at(band_list.get(i).getUpdated_at());
                        bands_post_1.setCreated_at(band_list.get(i).getCreated_at());
                        bands_post_1.setNickname(band_list.get(i).getNickname());
                        bands_post_1.setThumnail_url(band_list.get(i).getThumnail_url());
                        bands_post_1.setViewType(0);
                        // list 에 bands_post object 추가
                        post_list.add(bands_post_1);

                    }

                    /** socket 통신 **/
                    // 현재 방송중인 방송방 확인
                    try {
                        socket = IO.socket(SERVER_URL);
                        socket.connect();
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    socket.emit("check_start_streaming", String.valueOf(밴드번호));
                    /** 방송중 **/
                    socket.on("streaming", args -> {
                        // watcher id
                        방송자_id = (String) args[0];
                        방송자_시작시간 = (String) args[1];
                        Retrofit_user(방송자_id, 방송자_시작시간);
                    });

                    /** 어댑터 연결 **/
                    레이아웃매니저 = new LinearLayoutManager(Activity_band.this);
                    어댑터_band_post = new Adapter_band_postList(post_list);
                    recyBandPost.setLayoutManager(레이아웃매니저);
                    recyBandPost.setAdapter(어댑터_band_post);

                    // 리사이클러뷰 새로고침
                    // 1. 기존 list 정보 / 2. 방송방 정보
                    swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            swipeRefreshLayout.setRefreshing(false);
                            post_list.clear();

                            // 밴드 정보 및 list 정보 가져오기
                            // 방송방 가져오기
                            Retrofit(밴드번호);
                            // 새로고침
                            어댑터_band_post.notifyDataSetChanged();
                        }
                    });

                    //리사이클러뷰 화면전환
                    어댑터_band_post.setOnItemClickListener(new Adapter_band_postList.OnItemClickListener() {
                        @Override
                        public void onItemClick(View v, int 밴드번호, int seq) {
                            Intent intent = new Intent(v.getContext(), Activity_band_post.class);
                            intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
                            intent.putExtra("밴드번호", 밴드번호);
                            intent.putExtra("seq", seq);
                            startActivity(intent);
                        }

                        @Override
                        public void onItemClick(View v, int 밴드번호, String id) {
                            Intent intent = new Intent(Activity_band.this, Activity_live_streaming_watcher.class);
                            intent.putExtra("밴드번호", 밴드번호);
                            intent.putExtra("id", id);
                            startActivity(intent);
                        }
                    });

                } else {
                    Toast.makeText(Activity_band.this, "실패", Toast.LENGTH_SHORT).show();
                }
            }

            // 통신실패시
            @Override
            public void onFailure(@NonNull Call<ArrayList<bands_post>> call, @NonNull Throwable t) {
                Toast.makeText(Activity_band.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Log.i("band_error", t.getLocalizedMessage());
            }
        });
    }

    // users_bands 에서 band 멤버수 가져오기
    public void Volley(int 밴드번호_get) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                HOST_URL + "band/read_member.php",
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("0")) { // 실패
                            Toast.makeText(getApplicationContext(), "멤버수 가져오기 실패", Toast.LENGTH_SHORT).show();
                        } else { // 멤버수 가져오기
                            멤버수 = response;
                            /**  초기화 **/
                            txtMemb.setText("멤버 " + 멤버수);
//                            Toast.makeText(Activity_band.this, response.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("band_volley", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("seq", String.valueOf(밴드번호_get));

                return params;
            }
        };


        // 이전 결과가 있더라도 새로 요청
        request.setShouldCache(false);
        requestQueue.add(request);
    }

    // 방송자 정보 가져오기
    private void Retrofit_user(final String user_id, String 방송_시작시간){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        retrofitAPI = RetrofitClass.getApiClient().create(RetrofitAPI.class);
        Call<User> call = retrofitAPI.getUser(user_id);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NotNull Call<User> call, @NotNull Response<User> response) {
                // 서버에서 응답을 받아옴
                if (response.isSuccessful() && response.body() != null) {

                    user_thumnail_url = response.body().getThumnail_url();
                    user_nickname = response.body().getNickname();

                    bands_post bands_post_1 = new bands_post();
                    bands_post_1.setBand_seq(밴드번호);
                    bands_post_1.setUser_id(user_id);
                    bands_post_1.setNickname(user_nickname);
                    bands_post_1.setThumnail_url(user_thumnail_url);
                    bands_post_1.setUpdated_at(방송_시작시간);
//            bands_post_1.setImage_uri();
                    bands_post_1.setViewType(1);


                    try {
                        // 방송 시작 시간 순서
                        int insertIndex = -1;
                        Date newStartTime = sdf.parse(방송_시작시간);

                        for (int i = 0; i < post_list.size(); i++) {
                            bands_post post = post_list.get(i);
                            Date postStartTime = sdf.parse(post.getUpdated_at());

                            // 방송 시작 시간 비교
                            if (newStartTime.compareTo(postStartTime) >= 0) {
                                insertIndex = i;
                                break;
                            }
                        }
                        // 비교 후 시간 순으로 추가
                        if (insertIndex == -1) {
                            post_list.add(0, bands_post_1);
                        } else {
                            post_list.add(insertIndex, bands_post_1);
                        }



                        /** 어댑터 연결 **/
                        레이아웃매니저 = new LinearLayoutManager(Activity_band.this);
                        어댑터_band_post = new Adapter_band_postList(post_list);
                        recyBandPost.setLayoutManager(레이아웃매니저);
                        recyBandPost.setAdapter(어댑터_band_post);


                        // 리사이클러뷰 새로고침
                        // 1. 기존 list 정보 / 2. 방송방 정보
                        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                            @Override
                            public void onRefresh() {
                                swipeRefreshLayout.setRefreshing(false);
                                post_list.clear();

                                // 밴드 정보 및 list 정보 가져오기
                                // 방송방 가져오기
                                Retrofit(밴드번호);
                                // 새로고침
                                어댑터_band_post.notifyDataSetChanged();
                            }
                        });

                        //리사이클러뷰 화면전환
                        어댑터_band_post.setOnItemClickListener(new Adapter_band_postList.OnItemClickListener() {
                            @Override
                            public void onItemClick(View v, int 밴드번호, int seq) {
                                Intent intent = new Intent(v.getContext(), Activity_band_post.class);
                                intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
                                intent.putExtra("밴드번호", 밴드번호);
                                intent.putExtra("seq", seq);
                                startActivity(intent);
                            }

                            @Override
                            public void onItemClick(View v, int 밴드번호, String id) {
                                Intent intent = new Intent(Activity_band.this, Activity_live_streaming_watcher.class);
                                intent.putExtra("밴드번호", 밴드번호);
                                intent.putExtra("id", id);
                                startActivity(intent);
                            }
                        });

                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }


                } else {
                    Toast.makeText(Activity_band.this, "실패_ㅁㄴㅇ리ㅓㅁㄴㅇ라", Toast.LENGTH_SHORT).show();
                }
            }
            // 통신실패시
            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Toast.makeText(Activity_band.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Log.i("band_error", t.getLocalizedMessage());
            }
        });
    }

    public static String 시간포멧to몇분전(String db_time) throws ParseException {
         // 현재 시간 가져오기
        Date clientTime = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dbDate = sdf.parse(db_time);

        // 두 시간의 차이를 분 단위로 계산
        long differenceInMinutes = (clientTime.getTime() - dbDate.getTime()) / (60 * 1000);

        // 차이 표시
        if (differenceInMinutes < 1) {
            return "1분전";
        } else if (differenceInMinutes < 60) {
            return (int) differenceInMinutes + "분전";
        } else {
            return (int) Math.floor(differenceInMinutes / 60) + "시간전";
        }
    }

}