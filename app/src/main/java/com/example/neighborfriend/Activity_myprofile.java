package com.example.neighborfriend;

import static android.content.ContentValues.TAG;
import static com.example.neighborfriend.MainActivity.HOST_URL;
import static com.example.neighborfriend.MainActivity.requestQueue;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import com.example.neighborfriend.Class.FirebaseCloudStorage;
import com.example.neighborfriend.Fragment.Fragment_more;
import com.example.neighborfriend.databinding.ActivityLoginBinding;
import com.example.neighborfriend.databinding.ActivityMyprofileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

public class Activity_myprofile extends AppCompatActivity {
    private ActivityMyprofileBinding binding;
    /**  SharedPreferences **/ SharedPreferences userData;
    private String currentId,currentThum,currentNick,currentPhNum;
    private TextInputEditText editNick; private Button btnCh; private ImageView imgThum;
    private TextView title, btnComp,textPhNum; private ImageView btnBack;
    private String 이미지,폰번호;
    String 경로;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyprofileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeView();
        initializeProperty();

        // 뒤로가기
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {finish();}
        });
        // 썸네일 가져오기
        imgThum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("image/*");
                유저썸네일.launch(intent);
            }
        });
        // 폰번호 수정하기 [수정]
        btnCh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( v.getContext() , Activity_Phone_cert.class);
                intent.putExtra("Activity","myprofile");
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                휴대폰번호.launch(intent) ; // [인텐트 이동 실시]
            }
        });
        // 프로필 정보 최종변경 [확인]
        btnComp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // storage 저장
                System.out.println(이미지);
                if(!경로.contains("https")){ // 카카오톡 경로가 아닐 때는 저장 안함

                    // 썸네일 바꿨을 때
                    if(!이미지.contains("users/")){

                        StorageReference imagesRef = FirebaseCloudStorage.Storage_img(경로);
                        UploadTask uploadTask = imagesRef.putFile(Uri.parse(이미지));
                        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    // db 저장
                                    if(requestQueue == null) requestQueue = Volley.newRequestQueue(getApplicationContext());
                                    Volley(currentId, 경로,editNick.getText().toString(),폰번호);
                                } else {
                                    Log.e(TAG, "Image upload failed: " + task.getException().getMessage());
                                }
                            }
                        });}
                    // 썸네일 안 바꿧을 때
                    // 이미지 -> users/kyh1/thumnail
                    else{
                        if(requestQueue == null) requestQueue = Volley.newRequestQueue(getApplicationContext());
                        Volley(currentId, 경로,editNick.getText().toString(),폰번호);
                    }

                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        /**  변수 초기화 **/
        /**  layout 초기화 **/
        // 썸네일 초기화
//        Log.i("asdfads", String.valueOf(이미지.equals(경로)));
        if(!이미지.equals(경로)) {
            Glide.with(Activity_myprofile.this).load(이미지).into(imgThum);
        }else{
            StorageReference imagesRef = FirebaseCloudStorage.Storage_img(경로);
            imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(Activity_myprofile.this).load(uri).into(imgThum);
                }
            });
        }
        // 전화번호 초기화
        textPhNum.setText(전화번호포맷(폰번호));
    }

    /** initial **/
    private void initializeView() {
        /** binding **/
        // 상단
        btnBack = binding.backBtnChangePw;
        title = binding.titleChangePw;
        btnComp = binding.TochangeChangePw;
        // 이미지
        imgThum = binding.imgThumnailUrl;
        // 닉네임, 휴대폰번호
        editNick = binding.editNickname;
        textPhNum = binding.textNumPhoneNum;
        btnCh = binding.btnChangeNum;
    }

    private void initializeProperty() {
        /**  SharedPreferences **/
        // user
        userData = getSharedPreferences("user", MODE_PRIVATE); // sharedpreference
        // user data
        currentId = userData.getString("id","noneId");
        currentThum = userData.getString("thumnail_url","noneThum");
        currentNick = userData.getString("nickname","noneNick");
        currentPhNum = userData.getString("phone_num","nonePhNum");

        /**  변수 초기화 **/
        if(!currentThum.contains("https")) 경로 = String.format("users/%s/thumnail", currentId);
        else 경로 = currentThum;
        /**  layout 초기화 **/
        // 썸네일 초기화

        if(currentThum==null);
        else if(currentThum.split("/")[0].equals("https:")){
            Glide.with(Activity_myprofile.this).load(currentThum).into(imgThum);
        }else{
            StorageReference imagesRef = FirebaseCloudStorage.Storage_img(경로);
            imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(Activity_myprofile.this).load(uri).into(imgThum);
                }
            });
        }
        // 닉네임 초기화
        editNick.setText(currentNick);
        // 전화번호 초기화
        textPhNum.setText(전화번호포맷(currentPhNum));

        /**  변수 초기화 **/
        // 최종 변경시 사용할 변수 초기화
        이미지 =경로;
        폰번호 =currentPhNum;
    }

    /** ActivityResultLauncher **/
    // 유저 썸네일 받아오기
    ActivityResultLauncher<Intent> 유저썸네일 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>()
            {
                @Override
                public void onActivityResult(ActivityResult result)
                {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent intent = result.getData();

                        /** 초기화 **/
                        이미지 = intent.getData().toString();
                        Glide.with(Activity_myprofile.this)
                                .load(이미지)
                                .into(imgThum);
                    }
                }
            });
    // 휴대폰번호 받아오기
    ActivityResultLauncher<Intent> 휴대폰번호 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    // result 에는 resultCode 가 있다.
                    // resultCode 의 값으로, 여러가지 구분해서 사용이 가능.
                    if (result.getResultCode() == 4000){
                        Intent intent = result.getData();
                        폰번호 = intent.getStringExtra("PhoneNum");
                    }
                }
            });

    /** http 통신 **/
    // 회원정보 변경
    public void Volley(String user_id, String 이미지, String 닉네임, String 폰번호) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                HOST_URL+"user/changeprofile.php",
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        Log.i("a",response);
                        if(response.equals("1")) {
                            // fragment_more 로 데이터 전송
                            Intent intent_profile = new Intent();
                            intent_profile.putExtra("이미지", 이미지);
                            intent_profile.putExtra("닉네임", 닉네임);
                            intent_profile.putExtra("폰번호", 폰번호);

                            setResult(1, intent_profile);
                            // 성공
                            finish();
                            Toast.makeText(Activity_myprofile.this , "변경 성공", Toast.LENGTH_SHORT).show();
                        }else {
                            // 실패
                            finish();
                            Toast.makeText(Activity_myprofile.this, "변경 실패", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Activity_myprofile",error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_id", user_id);
                params.put("이미지", 이미지);
                params.put("닉네임", 닉네임);
                params.put("폰번호", 폰번호);

                return params;
            }
        };

        // 이전 결과가 있더라도 새로 요청
        request.setShouldCache(false);
        requestQueue.add(request);
    }

    // 전화번호 포맷
    private String 전화번호포맷(String number) {
        // 전화번호 정규표현식으로 제한
        String regEx = "(\\d{2,3})(\\d{3,4})(\\d{4})";

        if(!Pattern.matches(regEx, number)){
            return null;
        }

        // 지역번호가 02이면서 9자리 수일 때 == not error
        if(number.substring(0,2).contains("02") && number.length() == 9){
            return number.replaceAll(regEx, "$1-$2-$3"); // 출력 xxx-xxx-xxxx
        }

        // 지역번호 02를 제외한 번호 (070,031,064 ...) 가 9자리 일 때 == > 에러
        else if(number.length() == 9){
            return null;
        }
        return number.replaceAll(regEx, "$1-$2-$3"); // 출력 xxx-xxxx-xxxx
    }
}