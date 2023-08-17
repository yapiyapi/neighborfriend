package com.example.neighborfriend;

import static com.example.neighborfriend.Activity_Find_pw_change.check_pw_vali;
import static com.example.neighborfriend.Activity_Find_pw_change.check_validation;
import static com.example.neighborfriend.MainActivity.HOST_URL;
import static com.example.neighborfriend.MainActivity.requestQueue;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.neighborfriend.databinding.ActivityChangePwBinding;
import com.example.neighborfriend.databinding.ActivityCollectUserBinding;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

public class Activity_ChangePw extends AppCompatActivity {
    private ActivityChangePwBinding binding;
    /**  SharedPreferences **/ SharedPreferences userData;
    private ImageView btnBack; private TextView btnChange,newPwErr,newPwChErr;
    private TextInputEditText 현재비밀번호,새비밀번호,새비밀번호확인;
    private boolean CheckVa=false, CheckPwVa = false;
    private String currentId, password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePwBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeView();
        initializeProperty();

        // 입력시 확인 이벤트
        새비밀번호.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if(check_validation(새비밀번호.getText().toString(),CheckVa)){
                    CheckVa = true;
                    newPwErr.setVisibility(View.GONE);
                }else{
                    CheckVa = false;
                    newPwErr.setVisibility(View.VISIBLE);
                }

            }
        });
        새비밀번호확인.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if(check_pw_vali(새비밀번호.getText().toString(), 새비밀번호확인.getText().toString(),CheckPwVa)){
                    CheckPwVa = true;
                    newPwChErr.setVisibility(View.GONE);
                }else{
                    CheckPwVa = false;
                    newPwChErr.setVisibility(View.VISIBLE);
                }
            }
        });

        // 뒤로가기
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {finish();}
        });
        // 변경하기
        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // EditText값 예외처리
                if(현재비밀번호.getText().toString().trim().length() > 0 &&
                        새비밀번호.getText().toString().trim().length() > 0 &&
                        새비밀번호확인.getText().toString().trim().length() > 0) {
                    // 비밀번호 정규식 및 비밀번호 확인
                    if (CheckVa== true && CheckPwVa== true){
                        // Volley (http 통신)
                        if(requestQueue == null) requestQueue = Volley.newRequestQueue(getApplicationContext());
                        Volley(currentId,현재비밀번호.getText().toString() ,새비밀번호.getText().toString());
                    } else Toast.makeText(getApplicationContext() , "정규식을 확인해주세요.", Toast.LENGTH_SHORT).show();
                }else Toast.makeText(getApplicationContext(), "모든 입력란에 해당값을 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void initializeView() {
        /** binding **/
        // Edittext
        현재비밀번호 = binding.textCurrentPw;
        새비밀번호 = binding.textNewPw;
        새비밀번호확인 = binding.textNewPwCh;
        //btn
        btnBack = binding.backBtnChangePw;
        btnChange = binding.TochangeChangePw;
        // error
        newPwErr = binding.newPwError;
        newPwChErr = binding.newPwChError;
    }
    private void initializeProperty() {
        /**  SharedPreferences **/
        // user
        userData = getSharedPreferences("user", MODE_PRIVATE); // sharedpreference
        // user id
        currentId = userData.getString("id","noneId");
    }

    // 비밀번호 변경
    public void Volley(String user_id, String currentPw, String changePw) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                HOST_URL+"user/changepw.php",
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.equals("2")) {
                            // 성공
                            finish();
                            Toast.makeText(Activity_ChangePw.this , "변경 성공", Toast.LENGTH_SHORT).show();
                        }else if(response.equals("1")) {
                            // 성공
                            Toast.makeText(Activity_ChangePw.this , "현재 비밀번호가 맞지 않습니다.", Toast.LENGTH_SHORT).show();
                        }else {
                            // 실패
                            finish();
                            Toast.makeText(Activity_ChangePw.this, "변경 실패", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Activity_ChangePw",error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_id", user_id);
                params.put("currentPw", currentPw);
                params.put("changePw", changePw);

                return params;
            }
        };

        // 이전 결과가 있더라도 새로 요청
        request.setShouldCache(false);
        requestQueue.add(request);
    }


}