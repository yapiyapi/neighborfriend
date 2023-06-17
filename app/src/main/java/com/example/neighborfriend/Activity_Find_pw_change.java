package com.example.neighborfriend;

import static com.example.neighborfriend.MainActivity.HOST_URL;
import static com.example.neighborfriend.MainActivity.requestQueue;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.trusted.TokenStore;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.neighborfriend.databinding.ActivityFindPwBinding;
import com.example.neighborfriend.databinding.ActivityFindPwChangeBinding;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Activity_Find_pw_change extends AppCompatActivity {
    private ActivityFindPwChangeBinding binding;
    private TextView toChange,TextPwErr,TextPwChErr;
    private TextInputEditText TextPw,TextPwCheck;
    private ImageView backBtn;
    private boolean CheckId = false, CheckVa=false, CheckPwVa = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFindPwChangeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeView();

        // 입력시 확인 이벤트
        TextPw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if(check_validation(TextPw.getText().toString(),CheckVa)){
                    CheckVa = true;
                    TextPwErr.setVisibility(View.GONE);
                }else{
                    CheckVa = false;
                    TextPwErr.setVisibility(View.VISIBLE);
                }

            }
        });
        TextPwCheck.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if(check_pw_vali(TextPw.getText().toString(), TextPwCheck.getText().toString(),CheckPwVa)){
                    CheckPwVa = true;
                    TextPwChErr.setVisibility(View.GONE);
                }else{
                    CheckPwVa = false;
                    TextPwChErr.setVisibility(View.VISIBLE);
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {finish();}
        });
        toChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // EditText값 예외처리
                if(TextPw.getText().toString().trim().length() > 0 &&
                        TextPwCheck.getText().toString().trim().length() > 0) {
                    // 비밀번호 정규식 및 비밀번호 확인
                    if (CheckVa== true && CheckPwVa== true){
                        // Volley (http 통신)
                        if(requestQueue == null) requestQueue = Volley.newRequestQueue(getApplicationContext());
                        Volley(TextPw.getText().toString());
                    } else Toast.makeText(Activity_Find_pw_change.this, "입력란을 확인해주세요.", Toast.LENGTH_SHORT).show();
                }else Toast.makeText(getApplicationContext(), "모든 입력란에 해당값을 입력해주세요.", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private String GetId() {
        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        return id;
    }

    /** initial **/
    private void initializeView() {
        /** binding **/
        backBtn = binding.backBtnChangePw;
        toChange = binding.TochangeChangePw;

        TextPw = binding.textNewPw;
        TextPwCheck = binding.textNewPwCh;

        TextPwErr = binding.newPwError;
        TextPwChErr = binding.newPwChError;
    }

    public void Volley(String Pw) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                HOST_URL+"Find/PwChange.php",
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.equals("0")) {
                            Toast.makeText(getApplicationContext(), "password 변경에 실패 했습니다.", Toast.LENGTH_SHORT).show();
                        }else {
                            Intent intent = new Intent( Activity_Find_pw_change.this , Activity_login.class);
                            startActivity(intent);
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("실패","실패");
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", GetId());
                params.put("password", Pw);
                return params;
            }
        };

        // 이전 결과가 있더라도 새로 요청
        request.setShouldCache(false);
        requestQueue.add(request);
    }
    // 비밀번호 정규식
    public static boolean check_validation(String password, boolean CheckValue) {
        // 비밀번호 유효성 검사식1 : 숫자, 특수문자가 포함되어야 한다.
        String val_symbol = "([0-9].*[!,@,#,^,&,*,(,)])|([!,@,#,^,&,*,(,)].*[0-9])";
        // 비밀번호 유효성 검사식2 : 영문자 대소문자가 적어도 하나씩은 포함되어야 한다.
        String val_alpha = "([a-z])";
        // 정규표현식 컴파일
        Pattern pattern_symbol = Pattern.compile(val_symbol);
        Pattern pattern_alpha = Pattern.compile(val_alpha);

        Matcher matcher_symbol = pattern_symbol.matcher(password);
        Matcher matcher_alpha = pattern_alpha.matcher(password);
        // 정규식 통과
        if (matcher_symbol.find() && matcher_alpha.find()) {
            CheckValue=true;}
        // 정규식 미통과
        else {
            CheckValue=false;}
        return CheckValue;

    }
    // 비밀번호 확인
    public static boolean check_pw_vali(String pw, String pwCheck, boolean CheckVa){
        if (pw.equals(pwCheck)){
            CheckVa=true;}
        else {
            CheckVa=false;}
        return CheckVa;
    }
}