package com.example.neighborfriend;

import static com.example.neighborfriend.MainActivity.HOST_URL;
import static com.example.neighborfriend.MainActivity.requestQueue;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.neighborfriend.Adapter.Adapter_band_post;
import com.example.neighborfriend.Adapter.Adapter_band_postList;
import com.example.neighborfriend.Class.FirebaseCloudStorage;
import com.example.neighborfriend.Class.RetrofitClass;
import com.example.neighborfriend.Interface.RetrofitAPI;
import com.example.neighborfriend.databinding.ActivityBandPostBinding;
import com.example.neighborfriend.object.bands_post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Query;

public class Activity_band_post extends AppCompatActivity {
    private ActivityBandPostBinding binding;
    /**SharedPreferences**/
    SharedPreferences userData;
    /** RetrofitAPI**/
    RetrofitAPI retrofitAPI;
    /**Gson**/
    Gson gson;
    /**RecyclerView**/
    Adapter_band_post 어댑터_band_post;
    LinearLayoutManager 레이아웃매니저;
    private RecyclerView recy_img;
    private ImageView btnBack, imgDot, userThum;
    private TextView title, userNick, txtTime, txtCont;
    private String current_login_id;
    int 밴드번호, seq;
    private String user_id, thumnail, nickname, updated_at, created_at, contents, image_list;
    private List image_list1;
    private String 게시물jsonString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBandPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeView();
        initializeProperty();
        // 뒤로가기
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Afasdasdf", String.valueOf(밴드번호));
        Log.i("Afasdasdf", String.valueOf(seq));
//        if (밴드번호 != 0 && seq != 0) Retrofit(밴드번호, seq);
    }

    /**
     * initialize
     **/
    private void initializeView() {
        // 상단
        btnBack = binding.btnBack;
        title = binding.titleBandPost;
        imgDot = binding.imgThreeDot;
        // 사용자 정보
        userThum = binding.imgUserThumnail;
        userNick = binding.textNickname;
        txtTime = binding.textPasttime;
        // recyclerView
        recy_img = binding.recyImg;
        // contents
        txtCont = binding.textContents;
    }

    private void initializeProperty() {
        /**  SharedPreferences **/
        userData = getSharedPreferences("user", MODE_PRIVATE); // sharedpreference
        // 로그인 한 user id
        current_login_id = userData.getString("id", "noneId");
        /**  밴드번호 **/
        Intent intent = getIntent();
        밴드번호 = intent.getIntExtra("밴드번호", 0);
        seq = intent.getIntExtra("seq", 0);
        /** 초기화 **/
        Log.i("밴드번호 seq", String.valueOf(밴드번호));
        Log.i("밴드번호 seq", String.valueOf(seq));

        if (밴드번호 != 0 && seq != 0) {

            if(requestQueue == null) requestQueue = Volley.newRequestQueue(getApplicationContext());
            Volley게시물읽기(밴드번호, seq);
//            Retrofit(밴드번호, seq);
        }
    }

    /** Http 통신 **/
    /***********************  게시물 정보 가져오기  ***************************/
    public void Volley게시물읽기(final int band_seq_get, int seq_get) {
        StringRequest request = new StringRequest(
                Request.Method.GET,
                HOST_URL + String.format("band/post/read_post.php?band_seq=%s&seq=%s", String.valueOf(band_seq_get),String.valueOf(seq_get)),

                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Gson gson = new Gson();
                        bands_post bands_post1 = gson.fromJson(response, bands_post.class);

                        user_id = bands_post1.getUser_id();
                        thumnail = bands_post1.getThumnail_url();
                        nickname = bands_post1.getNickname();
                        updated_at = bands_post1.getUpdated_at();
                        created_at = bands_post1.getCreated_at();
                        contents = bands_post1.get게시글();
                        image_list = bands_post1.getImage_uri();

                        /**  layout 초기화 **/
                        // 이미지 초기화

                        if(thumnail==null);
                        else if (thumnail.split("/")[0].equals("https:")) {
                            Glide.with(Activity_band_post.this).load(thumnail).into(userThum);
                        } else {
                            StorageReference imagesRef = FirebaseCloudStorage.Storage_img(thumnail);
                            imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Glide.with(Activity_band_post.this).load(uri).into(userThum);
                                }
                            });
                        }
//                    if (!thumnail.equals("") || thumnail!=null) Glide.with(Activity_band_post.this).load(thumnail).into(userThum);
                        // 닉네임, 지난시간, 내용
                        userNick.setText(nickname);
                        txtTime.setText(updated_at);
                        txtCont.setText(contents);

                        // 로그인 유저 = post 작성자
                        if (user_id.equals(current_login_id)) {
                            // 더보기 버튼 보여준다.
                            imgDot.setVisibility(View.VISIBLE);
                            imgDot.setClickable(true);
                        } else {
                            // 더보기 버튼 사라진다.
                            imgDot.setVisibility(View.INVISIBLE);
                            imgDot.setClickable(false);
                        }

                        /**  더보기 버튼 클릭 이벤트 **/
                        // 더보기 (수정/삭제)
                        imgDot.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showCustomMenu(v);
                            }
                        });

                        /**  이미지 recyclerview **/
                        List 이미지_list = Arrays.asList(image_list.replace("[", "").replace("]", "").split(","));


                        /** 어댑터 연결 **/
                        레이아웃매니저 = new LinearLayoutManager(Activity_band_post.this, LinearLayoutManager.HORIZONTAL, false);
                        어댑터_band_post = new Adapter_band_post(이미지_list);
                        /** PagerSnapHelper : 이미지 하나씩 포커스 **/
                        PagerSnapHelper snapHelper = new PagerSnapHelper();
                        snapHelper.attachToRecyclerView(recy_img);

                        recy_img.setLayoutManager(레이아웃매니저);
                        recy_img.setAdapter(어댑터_band_post);

                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("band_post", error.toString());
                    }
                }
        );

        // 이전 결과가 있더라도 새로 요청
        request.setShouldCache(false);
        requestQueue.add(request);
    }
    private void Retrofit(final int band_seq_get, int seq_get) {
        retrofitAPI = RetrofitClass.getApiClient().create(RetrofitAPI.class);
        Call<bands_post> call1 = retrofitAPI.getBandPost(band_seq_get, seq_get);

        call1.enqueue(new Callback<bands_post>() {
            @Override
            public void onResponse(@NotNull Call<bands_post> call, @NotNull Response<bands_post> response) {
                // 서버에서 응답을 받아옴
                if (response.isSuccessful() && response.body() != null) {
                    bands_post bands_post1 = response.body();

                    user_id = bands_post1.getUser_id();
                    thumnail = bands_post1.getThumnail_url();
                    nickname = bands_post1.getNickname();
                    updated_at = bands_post1.getUpdated_at();
                    created_at = bands_post1.getCreated_at();
                    contents = bands_post1.get게시글();
                    image_list = bands_post1.getImage_uri();

                    /**  layout 초기화 **/
                    // 이미지 초기화

                    if(thumnail==null);
                    else if (thumnail.split("/")[0].equals("https:")) {
                        Glide.with(Activity_band_post.this).load(thumnail).into(userThum);
                    } else {
                        StorageReference imagesRef = FirebaseCloudStorage.Storage_img(thumnail);
                        imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Glide.with(Activity_band_post.this).load(uri).into(userThum);
                            }
                        });
                    }
//                    if (!thumnail.equals("") || thumnail!=null) Glide.with(Activity_band_post.this).load(thumnail).into(userThum);
                    // 닉네임, 지난시간, 내용
                    userNick.setText(nickname);
                    txtTime.setText(updated_at);
                    txtCont.setText(contents);

                    // 로그인 유저 = post 작성자
                    if (user_id.equals(current_login_id)) {
                        // 더보기 버튼 보여준다.
                        imgDot.setVisibility(View.VISIBLE);
                        imgDot.setClickable(true);
                    } else {
                        // 더보기 버튼 사라진다.
                        imgDot.setVisibility(View.INVISIBLE);
                        imgDot.setClickable(false);
                    }

                    /**  더보기 버튼 클릭 이벤트 **/
                    // 더보기 (수정/삭제)
                    imgDot.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showCustomMenu(v);
                        }
                    });

                    /**  이미지 recyclerview **/
                    List 이미지_list = Arrays.asList(image_list.replace("[", "").replace("]", "").split(","));


                    /** 어댑터 연결 **/
                    레이아웃매니저 = new LinearLayoutManager(Activity_band_post.this, LinearLayoutManager.HORIZONTAL, false);
                    어댑터_band_post = new Adapter_band_post(이미지_list);
                    /** PagerSnapHelper : 이미지 하나씩 포커스 **/
                    PagerSnapHelper snapHelper = new PagerSnapHelper();
                    snapHelper.attachToRecyclerView(recy_img);

                    recy_img.setLayoutManager(레이아웃매니저);
                    recy_img.setAdapter(어댑터_band_post);


                } else {
                    Toast.makeText(Activity_band_post.this, "실패", Toast.LENGTH_SHORT).show();
                }
            }

            // 통신실패시
            @Override
            public void onFailure(@NonNull Call<bands_post> call, @NonNull Throwable t) {
                Toast.makeText(Activity_band_post.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Log.i("Activity_band_post", t.getLocalizedMessage());
            }
        });
    }

    /***********************  게시물 삭제 로직  ***************************/
    public void Volley(int band_seq, int seq) {
        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                HOST_URL + String.format("band/post/delete.php?band_seq=%s&seq=%s", String.valueOf(band_seq),String.valueOf(seq)),

        new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.equals("1")){
                            Toast.makeText(Activity_band_post.this, "삭제 성공", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(Activity_band_post.this, "삭제 실패", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("band_post", error.toString());
                    }
                }
        );

        // 이전 결과가 있더라도 새로 요청
        request.setShouldCache(false);
        requestQueue.add(request);
    }

    private void Retrofit게시물삭제(int band_seq_get, int seq_get) {
        retrofitAPI = RetrofitClass.getApiClient().create(RetrofitAPI.class);
        Call<Integer> call1 = retrofitAPI.deleteBandPost(band_seq_get, seq_get);

        call1.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(@NotNull Call<Integer> call, @NotNull Response<Integer> response) {
                // 서버에서 응답을 받아옴
                if (response.isSuccessful() && response.body() != null) {
                    Log.i("ASdf",response.body().toString());
                    if(response.body()==1)
                        Toast.makeText(Activity_band_post.this, "삭제 완료", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(Activity_band_post.this, "삭제 실패", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Activity_band_post.this, "실패", Toast.LENGTH_SHORT).show();
                }
            }

            // 통신실패시
            @Override
            public void onFailure(@NonNull Call<Integer> call, @NonNull Throwable t) {
                Toast.makeText(Activity_band_post.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Log.i("Activity_band_post", t.getLocalizedMessage());
            }
        });
    }

    /**
     * 더보기 [수정/삭제]
     **/
    private void showCustomMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_update:
                        // 수정
                        Intent intent = new Intent(Activity_band_post.this, Activity_band_post_create_update.class);
                        intent.putExtra("밴드번호", 밴드번호);
                        intent.putExtra("seq", seq);
                        intent.putExtra("image_list", image_list);
                        intent.putExtra("contents", contents);
                        startActivity(intent);
                        Toast.makeText(Activity_band_post.this, "hihi", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.menu_delete:
                        // db 삭제
                        if(requestQueue == null) requestQueue = Volley.newRequestQueue(getApplicationContext());
                        Volley(밴드번호, seq);
//                        Retrofit게시물삭제(밴드번호, seq);
                        // storage 삭제
                        String 경로 = String.format("bands/%s/posts/%s", 밴드번호, seq);
                        StorageReference imagesRef = FirebaseCloudStorage.Storage_img(경로);
                        imagesRef.listAll()
                                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                                    @Override
                                    public void onSuccess(ListResult listResult) {
                                        for (StorageReference item : listResult.getItems()) {
                                            String a = String.format("%s/%s/%s/%s/%s",
                                                    item.toString().split("/")[3],
                                                    item.toString().split("/")[4],
                                                    item.toString().split("/")[5],
                                                    item.toString().split("/")[6],
                                                    item.toString().split("/")[7]);
                                            Log.i("ite,", String.valueOf(a));

                                            storage삭제하기(a);
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Uh-oh, an error occurred!
                                    }
                                });
                        /************************************/

                        finish();
                        return true;
                    default:
                        return false;
                }
            }
        });
        popup.inflate(R.menu.upd_del_btn);
        popup.show();
    }

    public static void storage삭제하기(String 경로){
        StorageReference imagesRef = FirebaseCloudStorage.Storage_img(경로);
        imagesRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
            }
        });
    }

}