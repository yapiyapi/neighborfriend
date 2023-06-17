package com.example.neighborfriend;

import static com.example.neighborfriend.MainActivity.HOST_URL;
import static com.example.neighborfriend.MainActivity.requestQueue;

import android.Manifest;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.neighborfriend.databinding.ActivityPhoneCertBinding;
import com.example.neighborfriend.databinding.ActivitySignUpBinding;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Activity_Phone_cert extends AppCompatActivity {
    private ActivityPhoneCertBinding binding;
    private TextView send,cert;
    private TextInputEditText EditPhoneNum,EditCert;
    private ImageView backBtn;

    static final int SMS_SEND_PERMISSION = 1;
    private String Activity_from, checkNum;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneCertBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        initializeView();
        initializeProperty();

        // back 버튼
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {finish();}
        });
        // 전송 버튼
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 전화번호 유효성 검사
                String strPhoneNumber = binding.textPhoneCertPhone.getText().toString().replaceAll("[^0-9]", "");
                if(strPhoneNumber.equals("") || strPhoneNumber.length() < 10){
                    Toast.makeText(getApplicationContext(),"전화번호를 확인하세요",Toast.LENGTH_LONG).show();
                    return;
                }else{
                    // requestQueue 없으면 새로 생성
                    if(requestQueue == null) requestQueue = Volley.newRequestQueue(getApplicationContext());
                    // Volley (http 통신)
                    Volley(strPhoneNumber);
                }
            }
        });
        // 인증 버튼
        cert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 인증번호 맞으면
//                if (EditCert.getText().toString().trim().equals(checkNum)){
                    // 1. 휴대폰 번호
                    String PhoneNum= EditPhoneNum.getText().toString().trim();

                    // 2. 데이터를 이전 Activity에 전달
                    Intent intent = new Intent();
                    intent.putExtra("PhoneNum", PhoneNum);
                    // 이전 Activity에 따라 resultcode 달리해둠
                    // resultCode "1"로 설정 시 에러 발생
                    if(Activity_from.equals("pw")) setResult(2000, intent);
                    else if (Activity_from.equals("signup"))setResult(1000, intent);
                    else if (Activity_from.equals("collect"))setResult(3000, intent);
                    else if (Activity_from.equals("myprofile"))setResult(4000, intent);

                    // 3. Activity 종료
                    finish();
//                }else Toast.makeText(Activity_Phone_cert.this, "인증번호를 확인해주세요.", Toast.LENGTH_SHORT).show();

            }
        });

        /// SMS 권한
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        if(permissionCheck != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)){
                Toast.makeText(getApplicationContext(), "SMS 권한이 필요합니다", Toast.LENGTH_SHORT).show();
            }
        }
        ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.SEND_SMS}, SMS_SEND_PERMISSION);

    }

    /** initial **/
    private void initializeView() {
        /** binding **/
        EditPhoneNum = binding.textPhoneCertPhone;
        EditCert = binding.textPhoneCertCertNum;
        cert = binding.TocertPhoneCert;
        send = binding.buttonPhoneCertSend;
        backBtn = binding.backBtnPhoneCert;
    }

    private void initializeProperty() {
        Intent intent = getIntent();
        Activity_from = intent.getStringExtra("Activity");
    }
    public void Volley(String phone_num) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                HOST_URL+"signup/phoneCert.php",
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.equals("1")) { // db 에 phoneNum 존재
                            Toast.makeText(getApplicationContext(), "이미 존재하는 휴대폰 번호입니다.", Toast.LENGTH_SHORT).show();
                        }else { // db 에 phoneNum 없음
                            // sendSMS
                            checkNum = numberGen(4,1);
                            sendSMS(binding.textPhoneCertPhone.getText().toString() , "인증번호 : "+ checkNum);
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
                params.put("phone_num", phone_num);

                return params;
            }
        };


        // 이전 결과가 있더라도 새로 요청
        request.setShouldCache(false);
        requestQueue.add(request);
    }

    private void sendSMS(String phoneNumber, String message){
        PendingIntent pi = PendingIntent.getBroadcast(this,0,
                new Intent("com.example.neighborfriend.SMS_DELIVERD"), PendingIntent.FLAG_IMMUTABLE);
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber,null,message,pi,null);

        Toast.makeText(getBaseContext(), "메세지가 전송되었습니다.", Toast.LENGTH_SHORT).show();
    }

    public static String numberGen (int len, int dupCd ){
        Random rand = new Random();
        String numStr = ""; //난수가 저장될 변수

        for (int i = 0; i < len; i++){
            //0~9까지 난수 생성
            String ran = Integer.toString(rand.nextInt(10));

            if(dupCd==1){
                //중복 허용시 numStr 에 append
                numStr += ran;
            } else if (dupCd==2) {
                //중복 허용하지 않을 시 중복된 값이 있는지 검사
                numStr += ran;
            }else {
                //생성된 난수가 중복되면 루틴을 다시 생성
                i-=1;
            }

        }
        return numStr;
    }
}