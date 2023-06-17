package com.example.neighborfriend;

import static com.example.neighborfriend.MainActivity.HOST_URL;
import static com.example.neighborfriend.MainActivity.requestQueue;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.neighborfriend.Class.HttpUrlConnetionClass;
import com.example.neighborfriend.databinding.ActivityFindIdBinding;
import com.example.neighborfriend.databinding.ActivitySignUpBinding;
import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Activity_Find_id extends AppCompatActivity {
    private ActivityFindIdBinding binding;
    private TextView toFind;
    private ImageView backBtn;
    private TextInputEditText TextNick,TextPhone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFindIdBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeView();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {finish();}
        });
        toFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Volley -------------------------------
//                if(requestQueue == null) requestQueue = Volley.newRequestQueue(getApplicationContext());
//                Volley(TextNick.getText().toString(),TextPhone.getText().toString());

                // httpurlconnection ----------------------------
//                new Thread(() -> {
//                    String url = HOST_URL+"Find/Id.php";
//                    String method = "POST";
//                    String result = "";
//                    HttpURLConnection conn = null;
//
//                    //HttpURLConnection 객체 생성
//                    conn = getHttpURLConnection(url, method);
//                    //URL 연결에서 데이터를 읽을지에 대한 설정 ( defualt true )
//                    // conn.setDoInput(true);
//                    //API에서 받은 데이터를 StringBuilder 형태로 리턴하여 줍니다.
//                    result = getHttpRespons(conn);
//                    //해당 정보를 확인합니다.
//                    System.out.println("결과 : " + result);
//                }).start();

                new Thread(()->{
                    testPostRequest(TextNick.getText().toString(),TextPhone.getText().toString());
                }).start();

            }
        });
    }

    /** initial **/
    private void initializeView() {
        /** binding **/
        TextNick = binding.textFindIdNickname;
        TextPhone = binding.textFindIdPhone;

        backBtn = binding.backBtnFindId;
        toFind = binding.tofindFindId;
    }

    // HttpURLConnection ----------------------------------------
    public void testPostRequest(String nickname, String phone_num ) {

        String url = HOST_URL+"Find/Id.php";

        Map<String, String> map = new HashMap<String, String>();
        map.put("nickname", nickname);
        map.put("phone_num", phone_num);

        String response = HttpUrlConnetionClass.postRequest(url, map);
        System.out.println("postRequest:" + response);

        if(!response.equals("0")){
            Intent intent = new Intent( Activity_Find_id.this , Activity_Find_id_get.class);
            intent.putExtra("id",response);
            startActivity(intent);
        }else Toast.makeText(this, "닉네임 또는 휴대폰번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
    }

    public void Volley(String TextNick, String TextPhone) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                HOST_URL+"Find/Id.php",
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.equals("0")) {
                            Toast.makeText(getApplicationContext(), "닉네임 또는 휴대폰번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
                        }else {
                            Intent intent = new Intent( Activity_Find_id.this , Activity_Find_id_get.class);
                            intent.putExtra("id",response);
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
                params.put("nickname", TextNick);
                params.put("phone_num", TextPhone);
                return params;
            }
        };

        // 이전 결과가 있더라도 새로 요청
        request.setShouldCache(false);
        requestQueue.add(request);
    }
}