package com.example.neighborfriend;

import static android.content.ContentValues.TAG;
import static com.example.neighborfriend.MainActivity.HOST_URL;
import static com.example.neighborfriend.MainActivity.requestQueue;
import static com.example.neighborfriend.MainActivity.retrofitAPI;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.neighborfriend.Class.FirebaseCloudStorage;
import com.example.neighborfriend.Class.RetrofitClass;
import com.example.neighborfriend.Interface.RetrofitAPI;
import com.example.neighborfriend.databinding.ActivityBandCreateUpdateBinding;
import com.example.neighborfriend.databinding.ActivityFindIdBinding;
import com.example.neighborfriend.object.band;
import com.example.neighborfriend.object.home_cell;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** 밴드 생성 / 밴드 수정 **/
public class Activity_band_create_update extends AppCompatActivity {
    private ActivityBandCreateUpdateBinding binding;
    Spinner spinner_category,spinner_open;ArrayList<String> arrayList1,arrayList2;
    ArrayAdapter<String> arrayAdapter1,arrayAdapter2;

    /**  SharedPreferences **/ SharedPreferences userData;
    /**  Gson **/ Gson gson;
    SharedPreferences.Editor userData_e;

    private TextView title, complete, setTitle, setCtgry, setOpen, setJoinTit, setJoinSet;
    private EditText editTitle, editContents;
    private ImageView backBtn, plusImage; private View setboundary3,setboundary4;
    private String 나이제한_from="제한없음", 나이제한_to="제한없음", 성별제한="선택안함";
    private String 제목, 이미지, 내용,카테고리, 공개여부; private String id;
    private String 밴드정보;  private band band; private String Activity_from;
    Uri uri = null; String 경로; int band_seq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBandCreateUpdateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeView();

        /// 생성 및 수정에 따른 화면 초기화
        Intent intent = getIntent();
        Activity_from = intent.getStringExtra("Activity");

        // 홈 화면의 + 버튼을 눌려서 넘어왔을 때 [생성]
        if(Activity_from.equals("home")){
            initializeProperty생성();

            //완료
            complete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 제목/내용 추가
                    if ( editTitle.getText().toString().trim().length() > 0 &&
                            editContents.getText().toString().trim().length() > 0 ){
                        // 썸네일 추가
                        if ( 이미지 != null ) {
                            // 카테고리 추가
                            if (!카테고리.equals("-1")){

//                                if(requestQueue == null) requestQueue = Volley.newRequestQueue(getApplicationContext());
                                Retrofit밴드생성(id, editTitle.getText().toString(),이미지,editContents.getText().toString(),
                                        카테고리, 공개여부,나이제한_from,나이제한_to,성별제한);

                                Toast.makeText(Activity_band_create_update.this, "전송", Toast.LENGTH_SHORT).show();
                            }else
                                Toast.makeText(Activity_band_create_update.this, "카테고리를 추가해주세요.", Toast.LENGTH_SHORT).show();
                        }else
                            Toast.makeText(Activity_band_create_update.this, "이미지를 추가해주세요.", Toast.LENGTH_SHORT).show();
                    }else
                        Toast.makeText(Activity_band_create_update.this, "제목 또는 활동소개를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
            });

            // 가입 조건 설정
            setJoinSet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent( v.getContext(), Activity_band_join_setting.class);
                    intent.putExtra("Activity","bandCreate");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    가입조건설정.launch(intent); // [인텐트 이동 실시]
                }
            });
        }
        // 밴드 설정에서 넘어왔을 때 [수정]
        else if(Activity_from.equals("bandSet")){
            initializeProperty수정();

            //완료 버튼
            complete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 제목/내용 추가
                    if ( editTitle.getText().toString().trim().length() > 0 &&
                            editContents.getText().toString().trim().length() > 0 ){
                        // 썸네일 추가
                        if ( 이미지 != null ) {
                            // intent에 데이터 저장

                            Log.i("ㅑㅑㅑ",editTitle.getText().toString());
                            Log.i("ㅑㅑㅑ",이미지);
                            Log.i("ㅑㅑㅑ",editContents.getText().toString());
                            Intent intent_setting = new Intent();
                            intent_setting.putExtra("제목", editTitle.getText().toString());
                            intent_setting.putExtra("이미지", 이미지);
                            intent_setting.putExtra("내용", editContents.getText().toString());
                            setResult(3, intent_setting);
                            // 종료
                            finish();
                        }else
                            Toast.makeText(Activity_band_create_update.this, "이미지를 추가해주세요.", Toast.LENGTH_SHORT).show();
                    }else
                        Toast.makeText(Activity_band_create_update.this, "제목 또는 활동소개를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
            });
        }


        //뒤로가기
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {finish();}
        });
        //이미지 선택
        plusImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("image/*");
                imgLauncher.launch(intent);
            }
        });


        /** 권한 **/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                return; // Return or handle the permission request result before proceeding
            }
        }
    }

    /** initialize **/
    private void initializeView() {
        // 상단
        backBtn = binding.btnBack;
        title = binding.titleBandCreateUpdate;
        complete = binding.textComplete;
        // 제목, 썸네일, 내용
        editTitle = binding.EditTitleBandCreateUpdate;
        plusImage = binding.btnPlusimg;
        editContents = binding.editContents;
        // 필수 설정
        setboundary3 = binding.bandCreateUpdateBoundary3;
        setTitle = binding.textNecessarySetting;
        setCtgry = binding.textCategory;
        setOpen = binding.textOpenOrNotopen;
        spinner_category = binding.spinner1;
        spinner_open = binding.spinner2;
        // 가입 조건 설정
        setboundary4 = binding.bandCreateUpdateBoundary4;
        setJoinTit = binding.textJoiningConditionSetting;
        setJoinSet = binding.textSetting;
    }
    private void initializeProperty생성() {
        /********************* 생성 ************************/
        /**  Spinner **/
        Spinner_1("생성");
        Spinner_2("생성");

        /**  SharedPreferences **/
        // user
        userData = getSharedPreferences("user", MODE_PRIVATE); // sharedpreference
        userData_e = userData.edit(); // editor
        // user id
        id = userData.getString("id","noneId");
    }
    private void initializeProperty수정() {
        /********************* 수정 ************************/
        /** gson **/
        gson = new GsonBuilder().create();
        /** Intent **/
        Intent intent = getIntent();
        밴드정보 = intent.getStringExtra("밴드정보");
        // band 정보 가져오기
        band = gson.fromJson(밴드정보, band.class);

        /** 변수 초기화 **/
        제목 =band.get제목();
        이미지 =band.getThumnail_url();
        내용 =band.get소개글();
        band_seq =band.getSeq();
        경로 = String.format("bands/%s/thumnail", band_seq);

        /** layout 초기화 **/
        editTitle.setText(제목);

        Log.i("ASdfa",경로);
        // storage 읽기
        StorageReference imagesRef = FirebaseCloudStorage.Storage_img(경로);
        imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(Activity_band_create_update.this).load(uri).into(plusImage);
            }
        });
        editContents.setText(내용);
        /** Gone **/
        // 필수 설정
        setboundary3.setVisibility(View.GONE);
        setTitle.setVisibility(View.GONE);
        setCtgry.setVisibility(View.GONE);
        setOpen.setVisibility(View.GONE);
        spinner_category.setVisibility(View.GONE);
        spinner_open.setVisibility(View.GONE);
        // 가입 조건 설정
        setboundary4.setVisibility(View.GONE);
        setJoinTit.setVisibility(View.GONE);
        setJoinSet.setVisibility(View.GONE);
    }

    /** ActivityResultLauncher **/
    // 이미지 (썸네일) 받아오기
    ActivityResultLauncher<Intent> imgLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>()
            {
                @Override
                public void onActivityResult(ActivityResult result)
                {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent intent = result.getData();
                        uri = intent.getData();
                        /** 초기화 **/
                        이미지 = uri.toString();
                        // 권한
                        getContentResolver().takePersistableUriPermission(Uri.parse(이미지), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                        Glide.with(Activity_band_create_update.this)
                                .load(이미지)
                                .into(plusImage);
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
                    if (result.getResultCode() == 1){
                        Intent intent = result.getData();
                        나이제한_from = intent.getStringExtra("나이제한_from");
                        나이제한_to = intent.getStringExtra("나이제한_to");
                        성별제한 = intent.getStringExtra("성별제한");
                    }
                }
            });
    /** Spinner **/
    private void Spinner_1(String category_get) {
        arrayList1 = new ArrayList<>(); // 배열 생성

        arrayList1.add("카테고리");
        arrayList1.add("게임");
        arrayList1.add("음식");
        arrayList1.add("운동");
        arrayList1.add("공부");
        arrayList1.add("친구");
        arrayList1.add("취미");
        arrayList1.add("미션");
        arrayList1.add("그외");



        arrayAdapter1 = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, arrayList1);

        spinner_category.setAdapter(arrayAdapter1); // 어댑터 적용
        if (category_get.equals("생성")) {spinner_category.setSelection(0);} // 초기 스피너 메뉴 항목 지정
        else spinner_category.setSelection(Integer.parseInt(category_get)+1); // 초기 스피너 메뉴 항목 지정

        spinner_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //TODO 하위 버전 텍스트 색상 지원하기 위해 선언
                ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);
                ((TextView) adapterView.getChildAt(0)).setTextSize(13);

                카테고리 = 카테고리(String.valueOf(arrayList1.get(i)));

//                Toast.makeText(Activity_band_create_update.this, i, Toast.LENGTH_SHORT).show();
                Log.i("A", String.valueOf(i));
//                Toast.makeText(Activity_band_create_update.this, 카테고리, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }
    private void Spinner_2(String public_get) {
        arrayList2 = new ArrayList<>(); // 배열 생성

        arrayList2.add("공개");
        arrayList2.add("비공개");

        arrayAdapter2 = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, arrayList2);

        spinner_open.setAdapter(arrayAdapter2); // 어댑터 적용
        if (public_get.equals("생성")) {spinner_category.setSelection(0);} // 초기 스피너 메뉴 항목 지정
        else spinner_category.setSelection(Integer.parseInt(public_get)); // 초기 스피너 메뉴 항목 지정


        spinner_open.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //TODO 하위 버전 텍스트 색상 지원하기 위해 선언
                ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);
                ((TextView) adapterView.getChildAt(0)).setTextSize(13);

                공개여부 = 공개여부(String.valueOf(arrayList2.get(i)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    /** Http 통신 **/
    public void Volley(String id, String 제목, String 썸네일, String 내용, String 카테고리, String 공개여부,String 나이제한_from,String 나이제한_to, String 성별제한) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                HOST_URL+"band/create.php",
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response 가 band_seq 값이 와야한다.
                        if(response.equals("0")) {
                            Toast.makeText(getApplicationContext(), "밴드 추가 실패", Toast.LENGTH_SHORT).show();
                        }else {
                            String 경로 = String.format("bands/%s/thumnail", response);

                            // storage 저장
                            StorageReference imagesRef = FirebaseCloudStorage.Storage_img(경로);
                            Log.i("마지막 ",썸네일);
                            UploadTask uploadTask = imagesRef.putFile(Uri.parse(썸네일));
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(Activity_band_create_update.this, "밴드 추가 성공", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent( Activity_band_create_update.this , MainActivity.class);
                                        startActivity(intent);
                                    } else {
                                        Log.e(TAG, "Image upload failed: " + task.getException().getMessage());
                                    }
                                }
                            });

                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("실패",error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_id", id);
                params.put("제목", 제목);
                params.put("썸네일", 썸네일);
                params.put("내용", 내용);
                params.put("카테고리", 카테고리);
                params.put("공개여부", 공개여부);
                params.put("나이제한_from", 나이제한_from);
                params.put("나이제한_to", 나이제한_to);
                params.put("성별제한", 성별제한);
                return params;
            }
        };
        // 이전 결과가 있더라도 새로 요청
        request.setShouldCache(false);
        requestQueue.add(request);
    }

    private void Retrofit밴드생성(String id, String 제목, String 썸네일, String 내용, String 카테고리, String 공개여부,String 나이제한_from,String 나이제한_to, String 성별제한) {
        retrofitAPI = RetrofitClass.getApiClient().create(RetrofitAPI.class);
        Call<String> call1 = retrofitAPI.createBand(id,제목,썸네일,내용,카테고리,공개여부,나이제한_from,나이제한_to,성별제한);

        call1.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                // 서버에서 응답을 받아옴
                if (response.isSuccessful() && response.body() != null) {
                    Log.i("ASdf",response.body().toString());
                    if(response.body().equals("0")) {
                        Toast.makeText(getApplicationContext(), "밴드 추가 실패", Toast.LENGTH_SHORT).show();
                    }else {
                        String 경로 = String.format("bands/%s/thumnail", response.body());

                        // storage 저장
                        StorageReference imagesRef = FirebaseCloudStorage.Storage_img(경로);
                        Log.i("마지막 ",썸네일);
                        UploadTask uploadTask = imagesRef.putFile(Uri.parse(썸네일));
                        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(Activity_band_create_update.this, "밴드 추가 성공", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent( Activity_band_create_update.this , MainActivity.class);
                                    startActivity(intent);
                                } else {
                                    Log.e(TAG, "Image upload failed: " + task.getException().getMessage());
                                }
                            }
                        });


                    }
                } else {
                    Toast.makeText(Activity_band_create_update.this, "실패", Toast.LENGTH_SHORT).show();
                }
            }

            // 통신실패시
            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(Activity_band_create_update.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Log.i("Activity_band_create_update_err", t.getLocalizedMessage());
            }
        });
    }


    /** 카테고리 변환 **/
    public static String 카테고리(String 카테고리){
        switch(카테고리) {
            case "게임" :
                return "0";
            case "음식" :
                return "1";
            case "운동" :
                return "2";
            case "공부" :
                return "3";
            case "친구" :
                return "4";
            case "취미" :
                return "5";
            case "미션" :
                return "6";
            case "그외" :
                return "7";
        }
        return "-1";
    }
    public static String 카테고리한글(int 카테고리){
        switch(카테고리) {
            case 0 :
                return "게임";
            case 1 :
                return "음식";
            case 2 :
                return "운동";
            case 3 :
                return "공부";
            case 4 :
                return "친구";
            case 5 :
                return "취미";
            case 6 :
                return "미션";
            case 7 :
                return "그외";
        }
        return "-1";
    }
    /** 공개여부 변환 **/
    public static String 공개여부(String 공개여부){
        switch(공개여부) {
            case "공개" :
                return "0";
            case "비공개" :
                return "1";
        }
        return "-1";
    }
    /** 성별제한 변환 **/
    public static String 성별제한(String 성별제한){
        switch(성별제한) {
            case "선택안함" :
                return "0";
            case "남자" :
                return "1";
            case "여자" :
                return "2";
        }
        return "0";
    }
    /** 나이제한 변환 **/
    public static String 나이제한(String 나이제한){
        if(나이제한.equals("제한없음")){
            return "0";
        }else return 나이제한;
    }
}