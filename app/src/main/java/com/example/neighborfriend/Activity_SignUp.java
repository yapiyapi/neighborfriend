package com.example.neighborfriend;

import static com.example.neighborfriend.MainActivity.HOST_URL;
import static com.example.neighborfriend.MainActivity.requestQueue;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.trusted.TokenStore;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.neighborfriend.databinding.ActivitySignUpBinding;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Activity_SignUp extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    Spinner spinner1,spinner2;ArrayList<String> arrayList1,arrayList2;
    ArrayAdapter<String> arrayAdapter1,arrayAdapter2;
    private String PhoneNum= null; private String old = "선택"; private String sex = "선택";
    private TextInputEditText id,pw,pwcheck,nick;
    private ImageView Check1,Check2;
    private TextView BtnDup,BtnCert,BtnSignup,TextPwErr,TextPwChErr;
    private boolean CheckId = false, CheckVa=false, CheckPwVa = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeView();
        initializeProperty();



        // 입력시 확인 이벤트 (비밀번호, 비밀번호 확인)
        pw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {check_validation(pw.getText().toString());}
        });
        pwcheck.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {check_pw_vali(pw.getText().toString(), pwcheck.getText().toString());}
        });

        // 아이디 중복체크
        BtnDup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(id.getText().toString().length()==0){
                    Toast.makeText(Activity_SignUp.this, "아이디를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }else{
                    // requestQueue 없으면 새로 생성
                    if(requestQueue == null) requestQueue = Volley.newRequestQueue(getApplicationContext());
                    // Volley (http 통신)
                    Volley(id.getText().toString());
                }
            }
        });
        // 휴대폰 번호 인증
        BtnCert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( v.getContext() , Activity_Phone_cert.class);
                intent.putExtra("Activity","signup");
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivityResult.launch(intent); // [인텐트 이동 실시]
            }
        });
        // 회원가입
        BtnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // EditText값 예외처리
                if(id.getText().toString().trim().length() > 0 &&
                        pw.getText().toString().trim().length() > 0 &&
                        pwcheck.getText().toString().trim().length() > 0 &&
                        nick.getText().toString().trim().length() > 0 ) {
                    // 아이디 중복 체크
                    if (CheckId== true){
                        // 비밀번호 정규식 및 비밀번호 확인
                        if (CheckVa== true && CheckPwVa== true) {
                            // 나이 입력했는지 확인
                            if (old != "선택") {
                                // 성별 입력했는지 확인
                                if (sex != "선택") {
                                    // 휴대폰번호 인증을 했는지 확인
                                    if (PhoneNum != null) {
                                        // requestQueue 없으면 새로 생성
                                        if (requestQueue == null)
                                            requestQueue = Volley.newRequestQueue(getApplicationContext());
                                        // Volley (http 통신)
                                        Volley(id.getText().toString(), pw.getText().toString(), nick.getText().toString(), old, sex, PhoneNum);

                                    } else
                                        Toast.makeText(getApplicationContext(), "휴대폰번호 인증을 해주세요.", Toast.LENGTH_SHORT).show();
                                } else
                                    Toast.makeText(Activity_SignUp.this, "성별을 선택해주세요.", Toast.LENGTH_SHORT).show();
                            }else
                                Toast.makeText(Activity_SignUp.this, "나이를 선택해주세요.", Toast.LENGTH_SHORT).show();
                            
                        } else Toast.makeText(Activity_SignUp.this, "비밀번호 혹은 비밀번호 확인란을 확인해주세요.", Toast.LENGTH_SHORT).show();
                    }else Toast.makeText(Activity_SignUp.this, "아이디 중복체크 해주세요.", Toast.LENGTH_SHORT).show();
                }else Toast.makeText(getApplicationContext(), "모든 입력란에 해당값을 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }



    // 휴대폰번호 인증 Activity 가 사라지면서 데이터를 보내는데
    // 해당 데이터를 수신
    ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    // result 에는 resultCode 가 있다.
                    // resultCode 의 값으로, 여러가지 구분해서 사용이 가능.
                    if (result.getResultCode() == 1000){
                        Intent intent = result.getData();
                        PhoneNum = intent.getStringExtra("PhoneNum");
                        // 체크 표시 visible
                        Check2.setVisibility(View.VISIBLE);
                    }
                }
            });

    /** initial **/
    private void initializeView() {
        /** binding **/
        id = binding.idSignupEdit;
        pw = binding.pwSignupEdit;
        pwcheck = binding.pwCheckSignupEdit;
        nick = binding.nicknameSignupEdit;

        spinner1 = binding.SpinnerOld;
        spinner2 = binding.SpinnerSex;

        Check1 = binding.imageViewCheck1;
        Check2 = binding.imageViewCheck2;

        BtnDup = binding.buttonDuplicationCheck;
        BtnCert = binding.buttonCerti;
        BtnSignup = binding.btnSignup;

        TextPwErr = binding.TextPwError;
        TextPwChErr = binding.TextCheckPwError;
    }
    private void initializeProperty() {
        Spinner_1();
        Spinner_2();
    }

    // 아이디 중복 체크
    public void Volley(String id) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                HOST_URL+"signup/signup.php",
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.equals("1")) {
                            // 체크 표시 visible
                            Check1.setVisibility(View.VISIBLE);
                            CheckId= true;
                        }else {
                            // 체크 표시 GONE
                            Check1.setVisibility(View.INVISIBLE);
                            CheckId= false;
                            Toast.makeText(getApplicationContext(), "중복되는 아이디 입니다.", Toast.LENGTH_SHORT).show();
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

                return params;
            }
        };

        // 이전 결과가 있더라도 새로 요청
        request.setShouldCache(false);
        requestQueue.add(request);
    }
    // 회원가입 (정보 저장)
    public void Volley(String id, String pw, String nickname, String old, String sex, String PhoneNum) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                HOST_URL+"signup/signup.php",
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.equals("1")) {
                            Toast.makeText(getApplicationContext() , "회원가입에 성공 했습니다.", Toast.LENGTH_SHORT).show();
                            // 화면 이동
                            Intent intent = new Intent( Activity_SignUp.this , Activity_login.class);
                            startActivity(intent);
                        }else {
                            Toast.makeText(getApplicationContext(), "회원가입에 실패 했습니다.", Toast.LENGTH_SHORT).show();
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
                params.put("id", id);
                params.put("password", pw);
                params.put("nickname", nickname);
                params.put("old", old);
                params.put("sex", sex);
                params.put("PhoneNum", PhoneNum);

                return params;
            }
        };

        // 이전 결과가 있더라도 새로 요청
        request.setShouldCache(false);
        requestQueue.add(request);
    }

    // 비밀번호 정규식
    public void check_validation(String password) {
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
            CheckVa=true;
            TextPwErr.setVisibility(View.INVISIBLE);}
        // 정규식 미통과
        else {
            CheckVa=false;
            TextPwErr.setVisibility(View.VISIBLE);
        }

    }
    // 비밀번호 확인
    public void check_pw_vali(String pw, String pwCheck){
        if (pw.equals(pwCheck)){
            CheckPwVa=true;
            TextPwChErr.setVisibility(View.INVISIBLE);}
        else {
            CheckPwVa=false;
            TextPwChErr.setVisibility(View.VISIBLE);
        }
    }

    // Spinner
    private void Spinner_1() {
        arrayList1 = new ArrayList<>(); // 배열 생성

        arrayList1.add("선택");
        for (int i = 1; i <= 100; i++) {
            arrayList1.add(""+i);
        }

        arrayAdapter1 = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, arrayList1);

        spinner1.setAdapter(arrayAdapter1); // 어댑터 적용
        spinner1.setSelection(0); // 초기 스피너 메뉴 항목 지정

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //TODO 하위 버전 텍스트 색상 지원하기 위해 선언
                ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);
                ((TextView) adapterView.getChildAt(0)).setTextSize(13);

                old = String.valueOf(arrayList1.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }
    private void Spinner_2() {
        arrayList2 = new ArrayList<>(); // 배열 생성

        arrayList2.add("선택");
        arrayList2.add("남자");
        arrayList2.add("여자");

        arrayAdapter2 = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, arrayList2);

        spinner2.setAdapter(arrayAdapter2); // 어댑터 적용
        spinner2.setSelection(0); // 초기 스피너 메뉴 항목 지정

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //TODO 하위 버전 텍스트 색상 지원하기 위해 선언
                ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);
                ((TextView) adapterView.getChildAt(0)).setTextSize(13);

                sex = String.valueOf(arrayList2.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }
}