package com.example.neighborfriend;

import static android.content.ContentValues.TAG;

import static com.example.neighborfriend.Class.KakaoAPI.getAgeRange;
import static com.example.neighborfriend.Class.KakaoAPI.getGender;
import static com.example.neighborfriend.MainActivity.HOST_URL;
import static com.example.neighborfriend.MainActivity.requestQueue;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.blob.BlobStoreManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInstaller;
import android.media.MediaCas;
import android.media.tv.TvInputService;
import android.media.tv.interactive.TvInteractiveAppService;
import android.os.Bundle;
import android.os.PerformanceHintManager;
import android.se.omapi.Session;
import android.service.textservice.SpellCheckerService;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.neighborfriend.databinding.ActivityLoginBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.kakao.sdk.auth.AuthApiClient;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.AccessTokenInfo;
import com.kakao.sdk.user.model.AgeRange;
import com.kakao.sdk.user.model.Gender;
import com.kakao.sdk.user.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

public class Activity_login extends AppCompatActivity {
    private ActivityLoginBinding binding;
    /**  SharedPreferences **/ SharedPreferences userData;
    private TextView ToFindId, ToFindPw,ToSignUp;
    private TextInputEditText TextLoginId, TextLoginPw;
    private Button BtnLogin;
    private ImageView BtnKakao;
    private String current_id, current_sex, current_old;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeView();
        initializeProperty();

        // kakao social
        Function2<OAuthToken, Throwable, Unit> callback = new Function2<OAuthToken, Throwable, Unit>() {
            @Override
            public Unit invoke(OAuthToken oAuthToken, Throwable throwable) {
                // OAuthToken이 있으면
                if(oAuthToken!= null){
//                    Toast.makeText(Activity_login.this, "토큰 있음", Toast.LENGTH_SHORT).show();
                }
                updateKakaoLoginUi();
                return null;
            }
        };
        BtnKakao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //카카오톡이 핸드폰에 설치 되어 있는지 확인함
                if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(Activity_login.this)){
                    // 카카오톡이 설치 돼 있으면
                    UserApiClient.getInstance().loginWithKakaoTalk(Activity_login.this,callback);
                }else {// 카카오톡이 설치 돼 있지 않으면
                    UserApiClient.getInstance().loginWithKakaoAccount(Activity_login.this, callback);
                }
            }
        });

        // 로그인
        BtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(requestQueue == null) requestQueue = Volley.newRequestQueue(getApplicationContext());
                Volley(TextLoginId.getText().toString(),TextLoginPw.getText().toString());
            }
        });

        // 아이디 찾기/비밀번호 찾기/회원가입
        ToFindId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( v.getContext() , Activity_Find_id.class);
                startActivity(intent);
            }
        });
        ToFindPw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( v.getContext() , Activity_Find_pw.class);
                startActivity(intent);
            }
        });
        ToSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( v.getContext() , Activity_SignUp.class);
                startActivity(intent);
            }
        });
    }

    /** initial **/
    private void initializeView() {
        /** binding **/
        // id/pw
        TextLoginId = binding.textLoginId;
        TextLoginPw = binding.textLoginPassword;
        // btn
        BtnLogin = binding.btnLogin;
        BtnKakao = binding.kakaoLogin;
        // 아이디/비번 찾기/회원가입
        ToFindId = binding.textviewFindId;
        ToFindPw = binding.textviewFindPw;
        ToSignUp = binding.textviewSignup;
    }
    private void initializeProperty() {
        /**  SharedPreferences **/
        // user
        userData = getSharedPreferences("user", MODE_PRIVATE); // sharedpreference
        // user id
        current_id = userData.getString("id","noneId");
        /**  자동 로그인 **/
        AutoLoginWithhasToken();
        AutoLogin();
    }

    /** http 통신 **/
    // 로그인 로직 (아이디 및 비밀번호 중복 검사)
    public void Volley(String id, String pw){
        StringRequest request = new StringRequest(
                Request.Method.POST,
                HOST_URL+"login.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.equals("1")) {
                            Intent intent = new Intent(Activity_login.this, MainActivity.class);
                            intent.putExtra("user_id",id);
                            startActivity(intent);
                            finish();
                        }else {
                            Toast.makeText(getApplicationContext(), "로그인 실패 했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Activity_login",error.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", id);
                params.put("password", pw);

                return params;
            }
        };
        // 이전 결과가 있더라도 새로 요청
        request.setShouldCache(false);
        requestQueue.add(request);
    }
    // 서버에 data 있는지 확인 후 없으면 저장
    public void Volley(String id,String thumnail_url,String nickname,AgeRange ageRange,Gender gender){
                StringRequest request = new StringRequest(
                Request.Method.POST,
                HOST_URL+"login.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.equals("1")) { // id 존재
                            Intent intent = new Intent(Activity_login.this, MainActivity.class);
                            intent.putExtra("user_id",id);
                            startActivity(intent);
                        }else {  // id 존재 X
                            Intent intent = new Intent(Activity_login.this, Activity_Collect_User.class);
                            intent.putExtra("user_id",id);
                            intent.putExtra("thumnail_url",thumnail_url);
                            intent.putExtra("nickname",nickname);
                            intent.putExtra("ageRange",getAgeRange(ageRange));
                            intent.putExtra("gender",getGender(gender));
                            startActivity(intent);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Activity_login",error.getMessage());
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

    /** kakao 나의 정보 가져오기 **/
    public void updateKakaoLoginUi(){
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                // 로그인이 되어 있다면
                if (user != null){
                    String id = String.valueOf((user.getId()));
                    String thumnail_url = user.getKakaoAccount().getProfile().getProfileImageUrl();
                    String nickname = user.getKakaoAccount().getProfile().getNickname();
                    AgeRange ageRange = user.getKakaoAccount().getAgeRange();
                    Gender gender = user.getKakaoAccount().getGender();

//                    Toast.makeText(Activity_login.this, id, Toast.LENGTH_SHORT).show();
//                    Toast.makeText(Activity_login.this, thumnail_url, Toast.LENGTH_SHORT).show();
//                    Toast.makeText(Activity_login.this, nickname, Toast.LENGTH_SHORT).show();
//                    Toast.makeText(Activity_login.this, ageRange.toString(), Toast.LENGTH_SHORT).show();
//                    Toast.makeText(Activity_login.this, gender.toString(), Toast.LENGTH_SHORT).show();
                    // 서버에 data 있는지 확인 후 없으면 저장
                    if(requestQueue == null) requestQueue = Volley.newRequestQueue(getApplicationContext());
                    Volley(id,thumnail_url,nickname,ageRange,gender);
                // 로그인이 안되어 있다면
                }else Log.d(TAG, "로그인이 안되어 있습니다.");
                return null;
            }
        });
    }

    public void AutoLoginWithhasToken(){
        if(AuthApiClient.getInstance().hasToken()){ // 토큰이 있을 때 자동 로그인 처리
            UserApiClient.getInstance().accessTokenInfo(new Function2<AccessTokenInfo, Throwable, Unit>() {
                @Override
                public Unit invoke(AccessTokenInfo accessTokenInfo, Throwable throwable) {
                    if(throwable!=null){
                        Log.i(TAG,throwable.getMessage() );
                    }else{
                        // 홈화면으로
                        String id = String.valueOf(accessTokenInfo.getId());
                        Intent intent = new Intent(Activity_login.this, MainActivity.class);
                        intent.putExtra("user_id",id);
                        startActivity(intent);
                        finish();
                    }
                    return null;
                }
            });
        }
    }

    public void AutoLogin() {

        Log.i("뭐",current_id);
        if(!current_id.equals("noneId")){
            Intent intent = new Intent(Activity_login.this, MainActivity.class);
            intent.putExtra("user_id",current_id);
            startActivity(intent);
            finish();
        }
    }

}

