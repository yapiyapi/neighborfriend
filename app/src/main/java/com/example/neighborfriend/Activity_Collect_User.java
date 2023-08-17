package com.example.neighborfriend;

import static android.content.ContentValues.TAG;
import static com.example.neighborfriend.Class.KakaoAPI.getAgeRange;
import static com.example.neighborfriend.Class.KakaoAPI.getGender;
import static com.example.neighborfriend.MainActivity.HOST_URL;
import static com.example.neighborfriend.MainActivity.requestQueue;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.example.neighborfriend.Class.FirebaseCloudStorage;
import com.example.neighborfriend.databinding.ActivityCollectUserBinding;
import com.example.neighborfriend.databinding.ActivityFindIdBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kakao.sdk.user.model.AgeRange;
import com.kakao.sdk.user.model.Gender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
/** 카카오 api 회원가입 (추가정보 기입) **/
public class Activity_Collect_User extends AppCompatActivity {
    private ActivityCollectUserBinding binding;
    ArrayList<String> arrayList1,arrayList2;
    ArrayAdapter<String> arrayAdapter1,arrayAdapter2;
    private Button BtnCert, BtnSignup;
    private TextInputEditText EditNick;
    private Spinner SpinOld,SpinSex;
    private ImageView Check;
    private String nickname; private String thumnail_url = ""; private String user_id;
    private String PhoneNum= null; private String ageRange = "선택"; private String gender = "선택";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCollectUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeView();
        initializeProperty();


        // 휴대폰 인증
        BtnCert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( v.getContext() , Activity_Phone_cert.class);
                intent.putExtra("Activity","collect");
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivityResult.launch(intent); // [인텐트 이동 실시]
            }
        });
        // 회원가입
        BtnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //회원가입
                if(!EditNick.getText().toString().equals("")){
                    if(ageRange!="선택"){ // 나이 확인
                        if(gender!="선택"){ // 성별 확인
                            if(PhoneNum!=null){ // 휴대폰 인증 확인
//                                Toast.makeText(Activity_Collect_User.this, user_id, Toast.LENGTH_SHORT).show();
//                                Toast.makeText(Activity_Collect_User.this, thumnail_url, Toast.LENGTH_SHORT).show();
//                                Toast.makeText(Activity_Collect_User.this, nickname, Toast.LENGTH_SHORT).show();
//                                Toast.makeText(Activity_Collect_User.this, ageRange, Toast.LENGTH_SHORT).show();
//                                Toast.makeText(Activity_Collect_User.this, gender, Toast.LENGTH_SHORT).show();
//                                Toast.makeText(Activity_Collect_User.this,PhoneNum , Toast.LENGTH_SHORT).show();

                                String 경로 = String.format("users/%s/thumnail", user_id);

                                // storage 저장
                                StorageReference imagesRef = FirebaseCloudStorage.Storage_img(경로);
                                UploadTask uploadTask = imagesRef.putFile(Uri.parse(thumnail_url));
                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        if (task.isSuccessful()) {
                                        } else {
                                        }
                                    }
                                });

                                if(requestQueue == null) requestQueue = Volley.newRequestQueue(getApplicationContext());
                                Volley(user_id,thumnail_url,EditNick.getText().toString(),ageRange,gender, PhoneNum);
                            }else Toast.makeText(Activity_Collect_User.this, "휴대폰 인증을 해주세요.", Toast.LENGTH_SHORT).show();
                        }else Toast.makeText(Activity_Collect_User.this, "성별을 선택해주세요.", Toast.LENGTH_SHORT).show();
                    }else Toast.makeText(Activity_Collect_User.this, "나이를 선택해주세요.", Toast.LENGTH_SHORT).show();
                }else Toast.makeText(Activity_Collect_User.this, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });



    }


    private void initializeView() {
        /** binding **/
        // Edittext
        EditNick = binding.nicknameCollectUser;
        // Spinner
        SpinOld = binding.SpinnerOldCollectUser;
        SpinSex = binding.SpinnerSexCollectUser;
        //btn
        BtnCert = binding.buttonCertiCollectUser;
        BtnSignup = binding.btnSignupCollectUser;
        //image
        Check = binding.imageViewCheck;
    }
    private void initializeProperty() {
        getintent();
        // nick, age, gender 초기화
        EditNick.setText(nickname);

        Spinner_1(ageRange);
        Spinner_2(gender);
    }

    private void getintent() {
        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");
        nickname = intent.getStringExtra("nickname");
        if(intent.getStringExtra("thumnail_url")!=null) thumnail_url = intent.getStringExtra("thumnail_url");
        if(intent.getStringExtra("ageRange")!=null) ageRange = intent.getStringExtra("ageRange");
        if(intent.getStringExtra("gender")!=null) gender = intent.getStringExtra("gender");

    }

    // 휴대폰 번호 인증
    ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    // result 에는 resultCode 가 있다.
                    // resultCode 의 값으로, 여러가지 구분해서 사용이 가능.
                    if (result.getResultCode() == 3000){
                        Intent intent = result.getData();
                        PhoneNum = intent.getStringExtra("PhoneNum");
//                        Toast.makeText(Activity_Collect_User.this, PhoneNum, Toast.LENGTH_SHORT).show();
                        // 체크 표시 visible
                        Check.setVisibility(View.VISIBLE);
                    }
                }
            });
    // Spinner
    private void Spinner_1(String age_get) {
        arrayList1 = new ArrayList<>(); // 배열 생성

        arrayList1.add("선택");
        for (int i = 1; i <= 100; i++) {
            arrayList1.add(""+i);
        }

        arrayAdapter1 = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, arrayList1);

        SpinOld.setAdapter(arrayAdapter1); // 어댑터 적용
        if(age_get=="선택") SpinOld.setSelection(0); // 초기 스피너 메뉴 항목 지정
        else SpinOld.setSelection(Integer.parseInt(ageRange));


        SpinOld.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //TODO 하위 버전 텍스트 색상 지원하기 위해 선언
                ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);
                ((TextView) adapterView.getChildAt(0)).setTextSize(13);

                ageRange = String.valueOf(arrayList1.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }
    private void Spinner_2(String gender_get) {
        arrayList2 = new ArrayList<>(); // 배열 생성

        arrayList2.add("선택");
        arrayList2.add("남자");
        arrayList2.add("여자");

        arrayAdapter2 = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, arrayList2);

        SpinSex.setAdapter(arrayAdapter2); // 어댑터 적용
        if(gender_get=="선택") SpinSex.setSelection(0); // 초기 스피너 메뉴 항목 지정
        else SpinSex.setSelection(Integer.parseInt(gender)+1);


        SpinSex.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //TODO 하위 버전 텍스트 색상 지원하기 위해 선언
                ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);
                ((TextView) adapterView.getChildAt(0)).setTextSize(13);

                gender = String.valueOf(arrayList2.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    // kakao 회원정보 저장
    public void Volley(String id, String thumnail_url , String nickname, String ageRange, String gender, String phoneNumber) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                HOST_URL+"kakao/signup.php",
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.equals("1")) {
                            // 홈으로
                            Intent intent = new Intent(Activity_Collect_User.this, MainActivity.class);
                            intent.putExtra("user_id",id);
                            startActivity(intent);
                            Toast.makeText(Activity_Collect_User.this , "회원가입 성공", Toast.LENGTH_SHORT).show();
                        }else {
                            // 존재하는 회원
                            finish();
                            Toast.makeText(Activity_Collect_User.this, "회원가입 실패", Toast.LENGTH_SHORT).show();
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
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", id);
                params.put("thumnail_url", thumnail_url);
                params.put("nickname", nickname);
                params.put("ageRange", ageRange);
                params.put("gender", gender);
                params.put("phoneNumber", phoneNumber);

                return params;
            }
        };

        // 이전 결과가 있더라도 새로 요청
        request.setShouldCache(false);
        requestQueue.add(request);
    }

}