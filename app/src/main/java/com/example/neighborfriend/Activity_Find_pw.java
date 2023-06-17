package com.example.neighborfriend;

import static com.example.neighborfriend.MainActivity.HOST_URL;
import static com.example.neighborfriend.MainActivity.requestQueue;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.example.neighborfriend.databinding.ActivityFindIdGetBinding;
import com.example.neighborfriend.databinding.ActivityFindPwBinding;
import com.google.android.material.textfield.TextInputEditText;

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
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Activity_Find_pw extends AppCompatActivity {
    private ActivityFindPwBinding binding;
    private TextView Next;private Button Cert;
    private ImageView backBtn,Check;
    private TextInputEditText EditId, EditNick;
    private String PhoneNum= null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFindPwBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeView();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {finish();}
        });
//        인증 버튼
        Cert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( v.getContext() , Activity_Phone_cert.class);
                intent.putExtra("Activity","pw");
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivityResult.launch(intent); // [인텐트 이동 실시]
            }
        });
        // 다음 버튼
        Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 휴대폰번호 인증을 했는지 확인
                if (PhoneNum != null) {
                    OkHttp(EditId.getText().toString(), EditNick.getText().toString(), PhoneNum);
//                    if (requestQueue == null) requestQueue = Volley.newRequestQueue(getApplicationContext());
//                    Volley(EditId.getText().toString(), EditNick.getText().toString(), PhoneNum);
                } else Toast.makeText(getApplicationContext(), "휴대폰번호 인증을 해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** initial **/
    private void initializeView() {
        /** binding **/
        backBtn = binding.backBtnFindPw;
        Next = binding.NextFindPw;
        Cert = binding.buttonCerti;

        EditId = binding.textFindId;
        EditNick = binding.textFindNickname;

        Check = binding.imageViewCheck;
    }

    // 휴대폰번호 인증 Activity 가 사라지면서 데이터를 보내는데
    // 해당 데이터를 수신
    ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    // result 에는 resultCode 가 있다.
                    // resultCode 의 값으로, 여러가지 구분해서 사용이 가능.
                    if (result.getResultCode() == 2000){
                        Intent intent = result.getData();
                        PhoneNum = intent.getStringExtra("PhoneNum");
                        // 체크 표시 visible
                        Check.setVisibility(View.VISIBLE);
//                        Toast.makeText(Activity_Find_pw.this, PhoneNum, Toast.LENGTH_SHORT).show();
                    }
                }
            });

    // 4. OkHttp---------------------------
    public void OkHttp(String id, String nickname, String phone_num){
        // OkHttp 객체 생성
        OkHttpClient client = new OkHttpClient();
        // json 객체 생성
        JSONObject jsonInput = new JSONObject();
        try {
            jsonInput.put("id", id);
            jsonInput.put("nickname", nickname);
            jsonInput.put("phone_num", phone_num);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        // RequestBody 객체 생성
        RequestBody reqBody = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                jsonInput.toString()
        );
        // Request 객체 생성
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(HOST_URL+"Find/Pw.php")
                .post(reqBody)
                .build();


        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.d("실패", "실패");
                e.printStackTrace();
            }
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                String str = response.body().string();
                if(str.equals("1")) { // 성공
                    Intent intent = new Intent( Activity_Find_pw.this , Activity_Find_pw_change.class);
                    intent.putExtra("id",id);
                    startActivity(intent);
                }else {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast toast = Toast.makeText(getApplicationContext(), "없는 아이디 또는 닉네임입니다.", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
                }
            }
        });
    }
    // Volley ----------------------
    public void Volley(String id, String nickname, String phone_num) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                HOST_URL+"Find/Pw.php",
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.equals("0")) {
                            Toast.makeText(getApplicationContext(), "없는 아이디 또는 닉네임입니다.", Toast.LENGTH_SHORT).show();
                        }else {
                            Intent intent = new Intent( Activity_Find_pw.this , Activity_Find_pw_change.class);
                            intent.putExtra("id",id);
                            startActivity(intent);
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
                params.put("nickname", nickname);
                params.put("phone_num", phone_num);
                return params;
            }
        };

        // 이전 결과가 있더라도 새로 요청
        request.setShouldCache(false);
        requestQueue.add(request);
    }
}