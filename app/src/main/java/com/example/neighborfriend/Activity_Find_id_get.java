package com.example.neighborfriend;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.IInterface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.neighborfriend.databinding.ActivityFindIdBinding;
import com.example.neighborfriend.databinding.ActivityFindIdGetBinding;

public class Activity_Find_id_get extends AppCompatActivity {

    private ActivityFindIdGetBinding binding;
    private TextView toLoginAct, TextId;
    private ImageView backBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFindIdGetBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeView();

        GetId();

        //뒤로가기
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {finish();}
        });
        // 로그인 Activity 로 이동
        toLoginAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( v.getContext() , Activity_login.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    private void GetId() {
        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        TextId.setText("아이디는\n\n"+id+"\n\n입니다.");
    }

    /** initial **/
    private void initializeView() {
        /** binding **/
        backBtn = binding.backBtnFindIdGet;
        toLoginAct = binding.textviewToLogin;
        TextId = binding.textViewFindIdGet;
    }
}