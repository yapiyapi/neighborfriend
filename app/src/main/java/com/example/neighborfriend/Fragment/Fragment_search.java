package com.example.neighborfriend.Fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.neighborfriend.Activity_search_box;
import com.example.neighborfriend.R;
import com.example.neighborfriend.databinding.FragmentHomeBinding;
import com.example.neighborfriend.databinding.FragmentMoreBinding;
import com.example.neighborfriend.databinding.FragmentSearchBinding;

public class Fragment_search extends Fragment {
    private FragmentSearchBinding binding;
    private TextView 게임,음식,운동,공부,친구,취미,미션,etc;
    private LinearLayout linearBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding= FragmentSearchBinding.inflate(inflater);
        View view = binding.getRoot();

        initializeView();
        initializeProperty();
        뒤로가기막기();

        // 검색창 클릭
        linearBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Activity_search_box.class);
                startActivity(intent);
            }
        });
        게임.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Activity_search_box.class);
                intent.putExtra("category",0);
                startActivity(intent);
            }
        });
        음식.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Activity_search_box.class);
                intent.putExtra("category",1);
                startActivity(intent);
            }
        });
        운동.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Activity_search_box.class);
                intent.putExtra("category",2);
                startActivity(intent);
            }
        });
        공부.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Activity_search_box.class);
                intent.putExtra("category",3);
                startActivity(intent);
            }
        });
        친구.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Activity_search_box.class);
                intent.putExtra("category",4);
                startActivity(intent);
            }
        });
        취미.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Activity_search_box.class);
                intent.putExtra("category",5);
                startActivity(intent);
            }
        });
        미션.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Activity_search_box.class);
                intent.putExtra("category",6);
                startActivity(intent);
            }
        });
        etc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), Activity_search_box.class);
                intent.putExtra("category",7);
                startActivity(intent);
            }
        });


        return view;
    }

    /** initialize **/
    private void initializeView() {
        linearBar = binding.linearSearchBar;
        // category
        게임 = binding.categoryGame;
        음식 = binding.categoryFoods;
        운동 = binding.categorySports;
        공부 = binding.categoryStudy;
        친구 = binding.categoryFriends;
        취미 = binding.categoryHobbies;
        미션 = binding.categoryMissions;
        etc = binding.categoryEtc;
    }
    private void initializeProperty() {

    }


    /**뒤로가기막기**/
    public void 뒤로가기막기(){
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
    }
}