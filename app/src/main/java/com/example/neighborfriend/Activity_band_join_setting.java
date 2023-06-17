package com.example.neighborfriend;

import static com.example.neighborfriend.Activity_band_create_update.나이제한;
import static com.example.neighborfriend.Activity_band_create_update.성별제한;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.neighborfriend.databinding.ActivityBandJoinSettingBinding;

import org.w3c.dom.Text;

import java.util.ArrayList;
/**  밴드 가입 조건 설정 **/
public class Activity_band_join_setting extends AppCompatActivity {
    private ActivityBandJoinSettingBinding binding;
    Spinner spinnerOld1,spinnerOld2,spinnerSex;ArrayList<String> 나이제한_list,성별제한_list;
    ArrayAdapter<String> arrayAdapter1,arrayAdapter2,arrayAdapter3;
    String 나이제한_from="제한없음", 나이제한_to="제한없음", 성별제한="선택안함";
    private ImageView backBtn;
    private TextView title,complete,titleOld,titleSex;
    private String Activity_from;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_band_join_setting);
        binding = ActivityBandJoinSettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeView();
        initializeProperty();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {finish();}
        });
        complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // intent에 데이터 저장
                Intent intent = new Intent();
                intent.putExtra("나이제한_from", 나이제한_from);
                intent.putExtra("나이제한_to", 나이제한_to);
                intent.putExtra("성별제한", 성별제한);
                if(Activity_from.equals("bandCreate")) setResult(1, intent);
                else if (Activity_from.equals("bandSet")) setResult(2, intent);
                else if (Activity_from.equals("bandUpdate")) setResult(1, intent);
                // 종료
                finish();
            }
        });
    }



    private void initializeView() {
//        상단
        backBtn = binding.backBtnBandJoinSetting;
        title = binding.titleBandJoinSetting;
        complete = binding.completeBandJoinSetting;
//        나이제한
        titleOld = binding.textOldCutLine;
        spinnerOld1 = binding.spinnerOld1;
        spinnerOld2 = binding.spinnerOld2;
//        성별제한
        titleSex = binding.textSexCutLine;
        spinnerSex = binding.spinnerSex;
    }
    private void initializeProperty() {
        /** Intent **/
        Intent intent = getIntent();
        /** 초기화 **/
        Activity_from = intent.getStringExtra("Activity");
        if(Activity_from.equals("bandCreate")){
            /** spinner **/
            Spinner_old_1(나이제한_from);
            Spinner_old_2(나이제한_to);
            Spinner_sex(성별제한);
        }else if(Activity_from.equals("bandSet")){
            나이제한_from = intent.getStringExtra("나이제한_from");
            나이제한_to = intent.getStringExtra("나이제한_to");
            성별제한 = intent.getStringExtra("성별제한");

            /** spinner **/
            Spinner_old_1(나이제한_from);
            Spinner_old_2(나이제한_to);
            Spinner_sex(성별제한);
        }else if(Activity_from.equals("bandUpdate")){
            나이제한_from = intent.getStringExtra("나이제한_from");
            나이제한_to = intent.getStringExtra("나이제한_to");
            성별제한 = intent.getStringExtra("성별제한");

            /** spinner **/
            Spinner_old_1(나이제한_from);
            Spinner_old_2(나이제한_to);
            Spinner_sex(성별제한);
        }


    }

    private void Spinner_old_1(String limit_f) {
        나이제한_list = new ArrayList<>(); // 배열 생성

        나이제한_list.add("제한없음");
        for (int i = 1; i <= 100; i++) {
            나이제한_list.add(""+i);
        }

        arrayAdapter1 = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, 나이제한_list);

        spinnerOld1.setAdapter(arrayAdapter1); // 어댑터 적용
        if(limit_f.equals("제한없음"))spinnerOld1.setSelection(0);
        else spinnerOld1.setSelection(Integer.parseInt(limit_f)); // 초기 스피너 메뉴 항목 지정

        spinnerOld1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //TODO 하위 버전 텍스트 색상 지원하기 위해 선언
                ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);
                ((TextView) adapterView.getChildAt(0)).setTextSize(13);

                나이제한_from = 나이제한(String.valueOf(나이제한_list.get(i)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }
    private void Spinner_old_2(String limit_t) {
        나이제한_list = new ArrayList<>(); // 배열 생성

        나이제한_list.add("제한없음");
        for (int i = 1; i <= 100; i++) {
            나이제한_list.add(""+i);
        }

        arrayAdapter2 = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, 나이제한_list);

        spinnerOld2.setAdapter(arrayAdapter2); // 어댑터 적용
        if(limit_t.equals("제한없음"))spinnerOld2.setSelection(0);
        else spinnerOld2.setSelection(Integer.parseInt(limit_t)); // 초기 스피너 메뉴 항목 지정

        spinnerOld2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //TODO 하위 버전 텍스트 색상 지원하기 위해 선언
                ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);
                ((TextView) adapterView.getChildAt(0)).setTextSize(13);

                나이제한_to = 나이제한(String.valueOf(나이제한_list.get(i)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }
    private void Spinner_sex(String limit_s) {
        성별제한_list = new ArrayList<>(); // 배열 생성

        성별제한_list.add("선택안함");
        성별제한_list.add("남자");
        성별제한_list.add("여자");

        arrayAdapter3 = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, 성별제한_list);

        spinnerSex.setAdapter(arrayAdapter3); // 어댑터 적용
        if(limit_s.equals("선택안함"))spinnerSex.setSelection(0);
        else spinnerSex.setSelection(Integer.parseInt(limit_s)); // 초기 스피너 메뉴 항목 지정

        spinnerSex.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //TODO 하위 버전 텍스트 색상 지원하기 위해 선언
                ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);
                ((TextView) adapterView.getChildAt(0)).setTextSize(13);

                성별제한 = 성별제한(String.valueOf(성별제한_list.get(i)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }



}