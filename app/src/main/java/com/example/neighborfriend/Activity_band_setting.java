package com.example.neighborfriend;

import static com.example.neighborfriend.Activity_band_create_update.공개여부;
import static com.example.neighborfriend.Activity_band_create_update.카테고리;
import static com.example.neighborfriend.Activity_band_post.storage삭제하기;
import static com.example.neighborfriend.MainActivity.HOST_URL;
import static com.example.neighborfriend.MainActivity.requestQueue;
import static com.example.neighborfriend.MainActivity.retrofitAPI;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.neighborfriend.Class.FirebaseCloudStorage;
import com.example.neighborfriend.Class.RetrofitClass;
import com.example.neighborfriend.Interface.RetrofitAPI;
import com.example.neighborfriend.databinding.ActivityBandSettingBinding;
import com.example.neighborfriend.object.band;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 밴드 설정 (밴드 개설자 , 멤버 )
 **/
public class Activity_band_setting extends AppCompatActivity {
    private ActivityBandSettingBinding binding;
    /**
     * Spinner
     **/
    Spinner spinnerCat, spinnerPub;
    ArrayList<String> Cat_list, Pub_list;
    ArrayAdapter<String> arrayAdapterCat, arrayAdapterPub;
    /**
     * SharedPreferences
     **/
    SharedPreferences userData;
    /**
     * Gson
     **/
    Gson gson;

    private ConstraintLayout lay_Creat, lay_memb;
    private LinearLayout bandIntro, joinMang, deltBand, secess;
    private ImageView backBtn;
    private TextView complete;
    private String user_id, 제목, 이미지, 내용;
    private String band_seq, 카테고리, 공개설정, 나이제한_from, 나이제한_to, 성별제한;
    private String 밴드정보, current_login_id;
    private band band;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBandSettingBinding.inflate(getLayoutInflater());
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
        // 밴드 소개 수정
        bandIntro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Activity_band_create_update.class);
                intent.putExtra("Activity", "bandSet");
                intent.putExtra("밴드정보", 밴드정보);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                밴드소개수정.launch(intent); // [인텐트 이동 실시]

            }
        });
        // 가입 조건 설정
        joinMang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Activity_band_join_setting.class);
                intent.putExtra("Activity", "bandSet");
                intent.putExtra("나이제한_from", 나이제한_from);
                intent.putExtra("나이제한_to", 나이제한_to);
                intent.putExtra("성별제한", 성별제한);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                가입조건설정.launch(intent); // [인텐트 이동 실시]
            }
        });
        // 밴드 설정 업데이트 [완료]
        complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 카테고리 추가
                if (!카테고리.equals("-1")) {
                    if (이미지.contains("content")) {
                        // content => 경로로 바꾼다.
                        String 경로 = String.format("bands/%s/thumnail", band_seq);

                        // storage 저장
                        StorageReference imagesRef = FirebaseCloudStorage.Storage_img(경로);
                        UploadTask uploadTask = imagesRef.putFile(Uri.parse(이미지));
                        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    Retrofit(band_seq, 제목, 경로, 내용, 카테고리, 공개설정, 나이제한_from, 나이제한_to, 성별제한);
                                } else {
                                }
                            }
                        });
                    } else Retrofit(band_seq, 제목, 이미지, 내용, 카테고리, 공개설정, 나이제한_from, 나이제한_to, 성별제한);
                } else
                    Toast.makeText(Activity_band_setting.this, "카테고리를 선택해주세요.", Toast.LENGTH_SHORT).show();

            }
        });
        // 밴드 삭제
        deltBand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(Activity_band_setting.this);
                builder.setTitle("삭제하기"); //AlertDialog의 제목 부분
                builder.setMessage("정말로 삭제하시겠습니까?"); //AlertDialog의 내용 부분
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // db 삭제
//                        if (requestQueue == null)
//                            requestQueue = Volley.newRequestQueue(getApplicationContext());
                        Retrofit밴드삭제(band_seq);
                    }
                });
                builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                builder.create().show(); //보이기


            }
        });

        // 밴드 탈퇴
        secess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder builder = new AlertDialog.Builder(Activity_band_setting.this);
                builder.setTitle("탈퇴하기"); //AlertDialog의 제목 부분
                builder.setMessage("정말로 탈퇴하시겠습니까?\n탈퇴 후 작성하신 모든 내용이 사라집니다."); //AlertDialog의 내용 부분
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // db 삭제
                        if (requestQueue == null)
                            requestQueue = Volley.newRequestQueue(getApplicationContext());
                        Volley(current_login_id, band_seq);
                    }
                });
                builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                builder.create().show(); //보이기
            }
        });

    }

    private void initializeView() {
        // layout
        lay_Creat = binding.layoutBandCreator;
        lay_memb = binding.layoutMember;
        // 버튼
        backBtn = binding.backBtnBandSet;
        complete = binding.completeBandSet;
        /** Band Creator **/
        // 밴드기본정보관리
        spinnerCat = binding.spinnerCategory;
        spinnerPub = binding.spinnerPublic;
        bandIntro = binding.linearIntroBand;
        // 가입 조건 설정
        joinMang = binding.linearMemberjoinManage;
        // 삭제하기
        deltBand = binding.linearDeleteBand;
        /** Member **/
        // 탈퇴하기
        secess = binding.linearSecession;

    }

    private void initializeProperty() {
        /**  SharedPreferences **/
        userData = getSharedPreferences("user", MODE_PRIVATE); // sharedpreference
        // 로그인 한 user id
        current_login_id = userData.getString("id", "noneId");
        /** gson **/
        gson = new GsonBuilder().create();
        /** Intent **/
        // from band
        Intent intent = getIntent();
        밴드정보 = intent.getStringExtra("밴드정보");
        // band 정보 가져오기
        band = gson.fromJson(밴드정보, band.class);

        /** 초기화 **/
        band_seq = String.valueOf(band.getSeq());
        user_id = String.valueOf(band.getUser_id());
        제목 = String.valueOf(band.get제목());
        이미지 = String.valueOf(band.getThumnail_url());
        내용 = String.valueOf(band.get소개글());
        카테고리 = String.valueOf(band.get카테고리());
        공개설정 = String.valueOf(band.get공개여부());
        나이제한_from = String.valueOf(band.get나이제한_시작());
        나이제한_to = String.valueOf(band.get나이제한_끝());
        성별제한 = String.valueOf(band.get성별제한());

        /** 초기화 **/
        // 밴드 개설자와 현재 로그인 회원 id 비교 하여 초기화 진행
        if (current_login_id.equals(user_id)) {
            lay_Creat.setVisibility(View.VISIBLE);
            lay_memb.setVisibility(View.GONE);
            // 같다면 개설자 권한
            /** spinner **/
            spinnerCat_func(카테고리);
            spinnerPub_func(공개설정);
        } else {// 다르다면 멤버 권한
            lay_Creat.setVisibility(View.GONE);
            lay_memb.setVisibility(View.VISIBLE);
        }

    }

    /**
     * ActivityResultLauncher
     **/
    // 밴드 소개 수정 데이터 수신
    ActivityResultLauncher<Intent> 밴드소개수정 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == 3) {
                        Intent intent = result.getData();
                        제목 = intent.getStringExtra("제목");
                        이미지 = intent.getStringExtra("이미지");
                        내용 = intent.getStringExtra("내용");

                    }
                }
            });
    // 가입조건 설정 데이터 수신
    ActivityResultLauncher<Intent> 가입조건설정 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    // result 에는 resultCode 가 있다.
                    // resultCode 의 값으로, 여러가지 구분해서 사용이 가능.
                    if (result.getResultCode() == 2) {
                        Intent intent = result.getData();
                        나이제한_from = intent.getStringExtra("나이제한_from");
                        나이제한_to = intent.getStringExtra("나이제한_to");
                        성별제한 = intent.getStringExtra("성별제한");

                    }
                }
            });

    /**
     * Spinner
     **/
    private void spinnerCat_func(String category_get) {
        Cat_list = new ArrayList<>(); // 배열 생성

        Cat_list.add("카테고리");
        Cat_list.add("게임");
        Cat_list.add("음식");
        Cat_list.add("운동");
        Cat_list.add("공부");
        Cat_list.add("친구");
        Cat_list.add("취미");
        Cat_list.add("미션");
        Cat_list.add("그외");

        arrayAdapterCat = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, Cat_list);

        spinnerCat.setAdapter(arrayAdapterCat); // 어댑터 적용
        spinnerCat.setSelection(Integer.parseInt(category_get) + 1);

        spinnerCat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //TODO 하위 버전 텍스트 색상 지원하기 위해 선언
                ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);
                ((TextView) adapterView.getChildAt(0)).setTextSize(13);

                카테고리 = 카테고리(String.valueOf(Cat_list.get(i)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void spinnerPub_func(String public_get) {
        Pub_list = new ArrayList<>(); // 배열 생성

        Pub_list.add("공개");
        Pub_list.add("비공개");

        arrayAdapterPub = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, Pub_list);

        spinnerPub.setAdapter(arrayAdapterPub); // 어댑터 적용
        spinnerPub.setSelection(Integer.parseInt(public_get)); // 초기 스피너 메뉴 항목 지정

        spinnerPub.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //TODO 하위 버전 텍스트 색상 지원하기 위해 선언
                ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);
                ((TextView) adapterView.getChildAt(0)).setTextSize(13);

                공개설정 = 공개여부(String.valueOf(Pub_list.get(i)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    /**
     * Http 통신
     **/
    // 밴드 삭제
    public void Volley(String seq_get) {
        /** db band 정보 삭제 **************************************/
        StringRequest request = new StringRequest(
                Request.Method.POST,
                HOST_URL + "band/delete.php",
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (!response.equals("0")) {
                            Toast.makeText(Activity_band_setting.this, "밴드 삭제 성공", Toast.LENGTH_SHORT).show();
                            /**   storage 삭제  **********************/
                            // 썸네일 삭제
                            String 밴드썸네일경로 = String.format("bands/%s/thumnail", band_seq);
                            StorageReference 밴드썸네일 = FirebaseCloudStorage.Storage_img(밴드썸네일경로);
                            밴드썸네일.delete();
                            // posts/* 삭제
                            String[] postSeqList = response.replace("[", "").replace("]", "").split(",");

                            for (int i = 0; i < postSeqList.length; i++) {

                                String 밴드게시물경로 = String.format("bands/%s/posts/%s", band_seq, postSeqList[i].replace("\"", ""));
                                StorageReference imagesRef = FirebaseCloudStorage.Storage_img(밴드게시물경로);

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
//                                                    Log.i("ite,", String.valueOf(item));
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
                            }

                            Intent intent = new Intent(Activity_band_setting.this, MainActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getApplicationContext(), "밴드 삭제 실패", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("실패", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("seq", seq_get);

                return params;
            }
        };
        // 이전 결과가 있더라도 새로 요청
        request.setShouldCache(false);
        requestQueue.add(request);

    }
    private void Retrofit밴드삭제(String seq) {
        retrofitAPI = RetrofitClass.getApiClient().create(RetrofitAPI.class);
        Call<List> call = retrofitAPI.deleteBand(seq);

        call.enqueue(new Callback<List>() {
            @Override
            public void onResponse(@NotNull Call<List> call, @NotNull Response<List> response) {
                // 서버에서 응답을 받아옴
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().equals("0")) {
                        Toast.makeText(Activity_band_setting.this, "밴드 삭제 실패", Toast.LENGTH_SHORT).show();
                    } else {
                        /**   storage 삭제  **********************/
                        // 썸네일 삭제
                        String 밴드썸네일경로 = String.format("bands/%s/thumnail", seq);
                        StorageReference 밴드썸네일 = FirebaseCloudStorage.Storage_img(밴드썸네일경로);
                        밴드썸네일.delete();

                        // posts/* 삭제
//                        String[] postSeqList = response.body().replace("[", "").replace("]", "").split(",");
                        List<String> postSeqList = response.body();

                        for (int i = 0; i < postSeqList.size(); i++) {

                            String 밴드게시물경로 = String.format("bands/%s/posts/%s", seq, postSeqList.get(i).replace("\"", ""));
                            StorageReference imagesRef = FirebaseCloudStorage.Storage_img(밴드게시물경로);

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
//                                                    Log.i("ite,", String.valueOf(item));
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
                        }

                        Intent intent = new Intent(Activity_band_setting.this, MainActivity.class);
                        startActivity(intent);
                        Toast.makeText(Activity_band_setting.this, "밴드 삭제 성공", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Activity_band_setting.this, "밴드 삭제 실패", Toast.LENGTH_SHORT).show();
                }
            }

            // 통신실패시
            @Override
            public void onFailure(@NonNull Call<List> call, @NonNull Throwable t) {
                Toast.makeText(Activity_band_setting.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Log.i("bandSetting_error", t.getLocalizedMessage());
            }
        });
    }

    // 밴드 탈퇴
    public void Volley(String user_id, String band_seq) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                HOST_URL + "band/secess.php",
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Toast.makeText(Activity_band_setting.this, "밴드 탈퇴 성공", Toast.LENGTH_SHORT).show();

                        /**   storage 삭제  **********************/
                        // posts/* 삭제
                        String[] postSeqList = response.replace("[", "").replace("]", "").split(",");

                        for (int i = 0; i < postSeqList.length; i++) {

                            String 밴드게시물경로 = String.format("bands/%s/posts/%s", band_seq, postSeqList[i].replace("\"", ""));
                            Log.i("밴드게시물경로", 밴드게시물경로);
                            StorageReference imagesRef = FirebaseCloudStorage.Storage_img(밴드게시물경로);

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
//                                                    Log.i("ite,", String.valueOf(item));
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
                        }


                        Intent intent = new Intent(Activity_band_setting.this, MainActivity.class);
                        startActivity(intent);


                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("실패", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_id", user_id);
                params.put("band_seq", band_seq);

                return params;
            }
        };
        // 이전 결과가 있더라도 새로 요청
        request.setShouldCache(false);
        requestQueue.add(request);
    }

    // 밴드 설정 저장
    private void Retrofit(String seq, String 제목, String 이미지, String 내용, String category, String publicSet, String old_limit_from, String old_limit_to, String sex_limit) {
        retrofitAPI = RetrofitClass.getApiClient().create(RetrofitAPI.class);
        Call<String> call = retrofitAPI.updateBand(seq, 제목, 이미지, 내용, category, publicSet, old_limit_from, old_limit_to, sex_limit);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                // 서버에서 응답을 받아옴
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().equals("1")) {
                        Toast.makeText(Activity_band_setting.this, "설정 업데이트 성공", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(Activity_band_setting.this, "설정 업데이트 실패", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Activity_band_setting.this, "설정 업데이트 실패", Toast.LENGTH_SHORT).show();
                }
                finish();
            }

            // 통신실패시
            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(Activity_band_setting.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Log.i("bandSetting_error", t.getLocalizedMessage());
            }
        });
    }

}