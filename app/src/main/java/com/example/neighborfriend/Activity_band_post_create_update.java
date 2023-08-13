package com.example.neighborfriend;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.neighborfriend.Adapter.Adapter_band_post_create_img;
import com.example.neighborfriend.Class.FirebaseCloudStorage;
import com.example.neighborfriend.Class.RetrofitClass;
import com.example.neighborfriend.Fragment.Fragment_home;
import com.example.neighborfriend.Interface.RetrofitAPI;
import com.example.neighborfriend.databinding.ActivityBandPostCreateBinding;
import com.example.neighborfriend.object.User;
import com.example.neighborfriend.object.bands_post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import static com.example.neighborfriend.MainActivity.HOST_URL;
import static com.example.neighborfriend.MainActivity.requestQueue;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Field;

public class Activity_band_post_create_update extends AppCompatActivity {
    private ActivityBandPostCreateBinding binding;
    /**
     * SharedPreferences
     **/
    SharedPreferences userData;
    /**
     * retrofit
     **/
    RetrofitAPI retrofitAPI;
    Adapter_band_post_create_img 어댑터;
    LinearLayoutManager 레이아웃매니저;
    private RecyclerView recyclerImg;
    private EditText editContents;
    private ImageView btnBack, btnPlusimg;
    private TextView complete;
    ArrayList<String> imgList, imgList_path, new_img_list;
    String current_login_id;
    int 밴드번호, seq;
    String image_list, 게시물; String 경로;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBandPostCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeView();
        initializeProperty();
        // 뒤로가기
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // 이미지 추가 버튼
        btnPlusimg.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("IntentReset")
            @Override
            public void onClick(View v) {
                // 다중 이미지 선택
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                이미지다중선택.launch(intent);
            }
        });

        // 데이터 저장 [완료]
        if (seq != 0) {// 수정
            complete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (imgList.isEmpty() && editContents.getText().toString().equals("")) {// 이미지, 내용 둘 중하나는 입력
                        Toast.makeText(Activity_band_post_create_update.this, "이미지를 추가하거나 내용을 작성해주세요.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Storage 에 추가된 데이터 업로드
//                        Retrofit(밴드번호, seq, imgList,new_img_list, editContents.getText().toString());
                        if(requestQueue == null) requestQueue = Volley.newRequestQueue(getApplicationContext());
                        Volley게시물수정(밴드번호, seq, imgList,new_img_list, editContents.getText().toString());
                    }
                }
            });
        } else {       // 생성
            complete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (imgList_path.isEmpty() && editContents.getText().toString().equals("")) {// 이미지, 내용 둘 중하나는 입력
                        Toast.makeText(Activity_band_post_create_update.this, "이미지를 추가하거나 내용을 작성해주세요.", Toast.LENGTH_SHORT).show();
                    } else {
                        if (requestQueue == null) {
                            requestQueue = Volley.newRequestQueue(getApplicationContext());
                        }
                        Volley(current_login_id, 밴드번호, imgList, imgList_path, editContents.getText().toString());
                    }
                }
            });
        }

        /** 권한 **/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
        }
    }

    /**
     * initialize
     **/
    private void initializeView() {
        // 버튼 (백, 완료)
        btnBack = binding.btnBack;
        complete = binding.textComplete;
        // 이미지추가/리사이클러/글
        btnPlusimg = binding.btnPlusimg;
        recyclerImg = binding.recyclerImg;
        editContents = binding.editContents;
    }
    private void initializeProperty() {
        /**  SharedPreferences **/
        userData = getSharedPreferences("user", MODE_PRIVATE); // sharedpreference
        // 로그인 한 user id
        current_login_id = userData.getString("id", "noneId");
        /**  밴드번호 **/
        Intent intent = getIntent();
        밴드번호 = intent.getIntExtra("밴드번호", 0);
        // 만약 seq가 0이면 추가, else 수정
        seq = intent.getIntExtra("seq", 0);
        image_list = intent.getStringExtra("image_list");
        게시물 = intent.getStringExtra("contents");
        /**  List **/
        imgList = new ArrayList<>();
        imgList_path = new ArrayList<>();
        new_img_list = new ArrayList<>();

//        Log.i("aaaaa", String.valueOf(밴드번호));
//        Log.i("aaaaa", String.valueOf(seq));
//        Log.i("aaaaa",image_list);
//        Log.i("aaaaa",게시물);
        /** post 수정 시 초기화 **/
        if (seq != 0) {// 수정
            Log.i("aaaaa", String.valueOf(imgList));
            Log.i("aaaaa", String.valueOf(imgList_path));
            Log.i("aaaaa", image_list);
            for (String i : Arrays.asList(image_list.replace("[", "").replace("]", "").split(","))) {
                imgList.add(i.trim());
            }
            editContents.setText(게시물);

            // list 가 분명히 비었는데도 size() 가 1 / 내용은 없음..
            Log.i("asdf", String.valueOf(imgList.get(0).equals("")));
            if(imgList.get(0).equals("")) imgList.clear();
            Log.i("asdf", String.valueOf(imgList.size()));
            /** 어댑터 연결 **/
            레이아웃매니저 = new LinearLayoutManager(Activity_band_post_create_update.this, LinearLayoutManager.HORIZONTAL, false);
            어댑터 = new Adapter_band_post_create_img(imgList,String.valueOf(밴드번호),String.valueOf(seq) );
            recyclerImg.setLayoutManager(레이아웃매니저);
            recyclerImg.setAdapter(어댑터);


            /** 경로 초기화 **/
            경로 = String.format("bands/%s/posts/%s/", 밴드번호, seq);
        }

    }

    /**
     * 이미지다중선택
     **/
    ActivityResultLauncher<Intent> 이미지다중선택 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    경로 = String.format("bands/%s/posts/%s/", 밴드번호, seq);

                    if (result.getResultCode() == RESULT_OK) {
                        Intent intent = result.getData();
                        ClipData clipData = intent.getClipData();

                        if (clipData.getItemCount() > 10) {   // 선택한 이미지가 11장 이상인 경우
                            Toast.makeText(getApplicationContext(), "10장까지 선택 가능합니다.", Toast.LENGTH_LONG).show();
                        } else {   // 선택한 이미지가 1장 이상 10장 이하인 경우
                            Log.i("Aaaaa", String.valueOf(clipData.getItemCount()));
                            Log.i("bbbbb", String.valueOf(imgList.size()));
                            if (clipData.getItemCount() + imgList.size() > 10) { // 선택한 이미지 개수 + list 내의 개수 > 10
                                Toast.makeText(Activity_band_post_create_update.this, "10장까지 선택 가능합니다.", Toast.LENGTH_SHORT).show();
                            } else {
                                for (int i = 0; i < clipData.getItemCount(); i++) {
                                    Uri imageUri = clipData.getItemAt(i).getUri();  // 선택한 이미지들의 uri를 가져온다.
                                    try {
                                        int imglist_size = imgList.size();
                                        //uri를 list에 추가
                                        imgList.add(String.valueOf(imageUri));
                                        imgList_path.add(경로+ String.valueOf(imglist_size));
                                        //new_img_list [수정시]
                                        new_img_list.add(String.valueOf(imageUri));

                                    } catch (Exception e) {
                                        Log.e(TAG, "File select error", e);
                                    }
                                }

                                Log.i("ASdf", String.valueOf(imgList));

                                /** 어댑터 연결 **/
                                레이아웃매니저 = new LinearLayoutManager(Activity_band_post_create_update.this, LinearLayoutManager.HORIZONTAL, false);
                                어댑터 = new Adapter_band_post_create_img(imgList, null,null);
                                recyclerImg.setLayoutManager(레이아웃매니저);
                                recyclerImg.setAdapter(어댑터);


                                Log.i("inten", String.valueOf(imgList));
                            }
                            Log.i("항요", String.valueOf(imgList_path));


                        }
                    }

                }
            });

    /** http 통신 **/
    /**************************** 생성 ****************************/
    public void Volley(String user_id, int band_seq, ArrayList image_uri, ArrayList imgList_path, String 게시글) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                HOST_URL + "band/post/create.php",
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (image_uri.size()==0){ // text 만 게시할 때
                            finish();
                        }else{
                            ArrayList 업데이트 = new ArrayList<>();
                            for(int i=0 ; i<imgList_path.size(); i++){
                                String cloud경로 = imgList_path.get(i).toString();
                                Log.i("abssc",cloud경로);
                                String cloud경로_new =
                                        cloud경로.split("/")[0] + "/"
                                                + cloud경로.split("/")[1] + "/"
                                                + cloud경로.split("/")[2] + "/"
                                                + response.trim() + "/"
                                                + cloud경로.split("/")[4];

                                Log.i("abssc",cloud경로_new);
                                String 휴대폰경로 = image_uri.get(i).toString();

                                업데이트.add(cloud경로_new);

                                StorageReference imagesRef = FirebaseCloudStorage.Storage_img(cloud경로_new);
                                UploadTask uploadTask = imagesRef.putFile(Uri.parse(휴대폰경로));
                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            Volley(String.valueOf(band_seq) ,response,String.valueOf(업데이트) );
                                            finish();
                                        } else {
                                            Log.e(TAG, "Image upload failed: " + task.getException().getMessage());
                                        }
                                    }
                                });
                            }
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Activity_band_post_create", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_id", user_id);
                params.put("band_seq", String.valueOf(band_seq));
                params.put("imgList_path", String.valueOf(imgList_path));
                params.put("게시글", 게시글);

                return params;
            }
        };
        // 이전 결과가 있더라도 새로 요청
        request.setShouldCache(false);
        requestQueue.add(request);
    }
    public void Volley(String band_seq, String seq, String image_uri) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                HOST_URL + "band/post/create_update.php",
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Activity_band_post_create", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("band_seq", band_seq);
                params.put("seq", seq);
                params.put("image_uri", image_uri);

                return params;
            }
        };
        // 이전 결과가 있더라도 새로 요청
        request.setShouldCache(false);
        requestQueue.add(request);
    }

    /**************************** 수정 ****************************/
    // 게시물 수정 로직
    public void Volley게시물수정(final int band_seq_get, int seq_get, ArrayList<String> imgList,ArrayList new_img_list, String 게시글) {
        ArrayList new_Array = new ArrayList();
        for (int i = 0; i < imgList.size(); i++) {
            if(imgList.get(i).contains("bands/")){
                new_Array.add(imgList.get(i));
            }
        }

        int 마지막seq;
        if(new_Array.size()==0){
            마지막seq = -1;
        }else{
            마지막seq = Integer.parseInt(new_Array.get(new_Array.size() - 1).toString().split("/")[4]);
        }

        if(new_img_list.size()!=0){
//                        ArrayList 업데이트 = new ArrayList<>();
            for(int i=0 ; i< new_img_list.size(); i++){

                String cloud경로 = String.format("bands/%s/posts/%s/%s",
                        String.valueOf(band_seq_get),
                        String.valueOf(seq_get),
                        String.valueOf(마지막seq+i+1) );
                String 휴대폰경로 = new_img_list.get(i).toString();

                Log.i("ASdf",cloud경로);
                Log.i("ASdf",휴대폰경로);
                StorageReference imagesRef = FirebaseCloudStorage.Storage_img(cloud경로);
                UploadTask uploadTask = imagesRef.putFile(Uri.parse(휴대폰경로));

                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            // db 에 저장할 리스트 [ new_Array ]
                            new_Array.add(cloud경로);
                        } else {
                            Log.e(TAG, "Image upload failed: " + task.getException().getMessage());
                        }
                    }
                });


            }
        }

        StringRequest request = new StringRequest(
                Request.Method.PUT,
                HOST_URL+"band/post/update.php",
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        finish();
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
                params.put("band_seq", String.valueOf(band_seq_get));
                params.put("seq", String.valueOf(seq_get));
                params.put("image_uri", String.valueOf(new_Array));
                params.put("게시글", 게시글);

                return params;
            }
        };

        // 이전 결과가 있더라도 새로 요청
        request.setShouldCache(false);
        requestQueue.add(request);
    }

}