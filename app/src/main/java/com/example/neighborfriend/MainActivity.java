package com.example.neighborfriend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.content.LocusId;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.neighborfriend.Class.RetrofitClass;
import com.example.neighborfriend.Fragment.Fragment_chatting;
import com.example.neighborfriend.Fragment.Fragment_home;
import com.example.neighborfriend.Fragment.Fragment_more;
import com.example.neighborfriend.Fragment.Fragment_search;
import com.example.neighborfriend.Interface.RetrofitAPI;
import com.example.neighborfriend.Service.Service_chatting;
import com.example.neighborfriend.databinding.ActivityMainBinding;
import com.example.neighborfriend.object.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONException;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    public static final String HOST_URL = "http://43.200.4.212/";

    /**  retrofit **/ public static RetrofitAPI retrofitAPI;
    /**  Volley **/ public static RequestQueue requestQueue;
    /**  SharedPreferences **/ SharedPreferences userData; SharedPreferences.Editor userData_e;
    Fragment_home home_f;
    Fragment_search search_f;
    private BottomNavigationView Navigation;
    private String thumnail_url, nickname,password,phone_num;
    private boolean retrofitExecuted = false;
    String old, sex;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        initializeView();
        initializeProperty();


        Navigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment currentFragment = fragmentManager.findFragmentById(R.id.Frame_main);

                switch (item.getItemId()) {
                    case R.id.item_fragment1:
                        if (currentFragment instanceof Fragment_home) {
                            return true; // No need to replace the fragment
                        }
                        fragmentManager.beginTransaction().replace(R.id.Frame_main, new Fragment_home(), "Fragment_home").commit();
                        break;
                    case R.id.item_fragment2:
                        if (currentFragment instanceof Fragment_search) {
                            return true; // No need to replace the fragment
                        }
                        fragmentManager.beginTransaction().replace(R.id.Frame_main, new Fragment_search(), "Fragment_search").commit();
                        break;
                    case R.id.item_fragment3:
                        if (currentFragment instanceof Fragment_chatting) {
                            return true; // No need to replace the fragment
                        }
                        fragmentManager.beginTransaction().replace(R.id.Frame_main, new Fragment_chatting(), "Fragment_chatting").commit();
                        break;
                    case R.id.item_fragment4:
                        if (currentFragment instanceof Fragment_more) {
                            return true; // No need to replace the fragment
                        }
                        fragmentManager.beginTransaction().replace(R.id.Frame_main, new Fragment_more(), "Fragment_more").commit();
                        break;
                }

                return false;
            }
        });


//        joinband.setLayoutManager(레이아웃매니저);
//        joinband.setAdapter(어댑터);




    }

    private void initializeView() {
        Navigation = binding.bottomNavigationView;
    }
    private void initializeProperty() {
        // getIntent
        Intent intent = getIntent();
        String user_id = intent.getStringExtra("user_id");
        /**  SharedPreferences **/ // shared 초기화
        userData = getSharedPreferences("user", MODE_PRIVATE); // sharedpreference
        userData_e = userData.edit(); // editor
        /**  Retrofit **/
        // user data 가져오기
        // shared 에 가져온 값 저장
        if (!retrofitExecuted && user_id != null) {
            Retrofit(user_id);
            retrofitExecuted = true;
        }


    }

    /** Http 통신 **/
    // user data 가져오기
    private void Retrofit(final String id){
        retrofitAPI = RetrofitClass.getApiClient().create(RetrofitAPI.class);
        Call<User> call = retrofitAPI.sendData(id);

        call.enqueue(new Callback<User>() {
             @Override
             public void onResponse(@NotNull Call<User> call, @NotNull Response<User> response) {
                 // 서버에서 응답을 받아옴
                 if (response.isSuccessful() && response.body() != null) {

                     thumnail_url = response.body().getThumnail_url();
                     nickname = response.body().getNickname();
                     password = response.body().getPassword();
                     old = response.body().getOld();
                     sex = response.body().getSex();
                     phone_num = response.body().getPhone_num();


                     /**  SharedPreferences **/
                     // 가져온 데이터 shared 에 저장
                     userData_e.putString("id",id).apply();
                     userData_e.putString("thumnail_url",thumnail_url).apply();
                     userData_e.putString("nickname",nickname).apply();
                     if(password==null) userData_e.putString("password","false").apply();
                     else userData_e.putString("password","true").apply();

                     userData_e.putString("old",old).apply();
                     userData_e.putString("sex",sex).apply();
                     userData_e.putString("phone_num",phone_num).apply();
//                     Toast.makeText(MainActivity.this, password, Toast.LENGTH_SHORT).show();

                     // fragment 로 이동
                     // fragment 로 이동
                     Fragment_home fragmentHome = (Fragment_home) getSupportFragmentManager().findFragmentByTag("Fragment_home");
                     if (fragmentHome == null) {
                         // Fragment is not added, add it to the FrameLayout
                         getSupportFragmentManager().beginTransaction().replace(R.id.Frame_main, new Fragment_home(), "Fragment_home").commit();
                     }

                     /**  서비스 start **/
                     Intent ServiceIntent = new Intent(MainActivity.this, Service_chatting.class);
                     ServiceIntent.putExtra("id", id);
                     ServiceIntent.putExtra("nickname", nickname);
                     startService(ServiceIntent);

                 } else {
                     Toast.makeText(MainActivity.this, "실패", Toast.LENGTH_SHORT).show();
                 }
             }
             // 통신실패시
             @Override
             public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                 Toast.makeText(MainActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                 Log.i("main_error", t.getLocalizedMessage());
             }
         });
    }

    // 2. Volley-----------------------------
    public void Volley(String id) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                HOST_URL+"main.php",
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // post 로 로그인 된 회원 id 를 보내준 상태이고
                        // 해당 회원의 밴드를 가져와야 한다.
                        // 회원의 정보 가져옴 (json)
                        Log.i("a",response);
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


}

