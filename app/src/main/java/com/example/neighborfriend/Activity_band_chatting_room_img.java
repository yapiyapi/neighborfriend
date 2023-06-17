package com.example.neighborfriend;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.MediaController;

import com.bumptech.glide.Glide;
import com.example.neighborfriend.Class.FirebaseCloudStorage;
import com.example.neighborfriend.databinding.ActivityBandChattingRoomImgBinding;
import com.example.neighborfriend.databinding.ActivityBandChattingRoomVideoBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;

public class Activity_band_chatting_room_img extends AppCompatActivity {
    private ActivityBandChattingRoomImgBinding binding;
    private ImageView img;
    private String current_uri_path; private int current_chatRoom_seq, current_seq;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBandChattingRoomImgBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeView();
        initializeProperty();
    }

    /** initialize **/
    private void initializeView() {
        // 상단
        img = binding.imageView;
    }
    private void initializeProperty() {
        /**  밴드번호 **/
        Intent intent = getIntent();
        current_uri_path = intent.getStringExtra("uri_path");
        current_chatRoom_seq = intent.getIntExtra("chatRoom_seq",0);
        current_seq = intent.getIntExtra("seq",0);

        /**  초기화 **/
        StorageReference imagesRef = FirebaseCloudStorage.Storage_img(current_uri_path);
        imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(Activity_band_chatting_room_img.this).load(uri).into(img);
            }
        });
    }

}