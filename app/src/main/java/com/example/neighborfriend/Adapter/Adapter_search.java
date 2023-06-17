package com.example.neighborfriend.Adapter;

import static com.example.neighborfriend.Activity_band_create_update.카테고리한글;
import static com.example.neighborfriend.Activity_search_box.나이포맷;
import static com.example.neighborfriend.Activity_search_box.성별포맷;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.neighborfriend.Class.FirebaseCloudStorage;
import com.example.neighborfriend.R;
import com.example.neighborfriend.object.band;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

/** 검색 Recyclerview **/
/** 기능 : 클릭 시 해당 밴드 Activity 로 이동 **/
public class Adapter_search extends RecyclerView.Adapter<Adapter_search.CustomViewHolder> {


    private List<band> search_list;
    private String user_id, title, thum_url, contents, created_at, memberNum;
    private int seq, category, publicSet, ageStart, ageEnd, gender;

    public Adapter_search(ArrayList<band> list) {
        search_list = list;
    }

    //  --------------------------onclick --------------
    private OnItemClickListener mListener = null ;
    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener ;
    }
    public interface OnItemClickListener {
        void onItemClick(View v, int band_seq) ;
    }

    // Arraylist return callback
    private MyAdapterCallback callback;
    public interface MyAdapterCallback {
        void onValueReceived(ArrayList list);
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View 뷰 = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_search, parent,false);
        return new CustomViewHolder(뷰);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        /** 변수 초기화 **/
        seq = search_list.get(position).getSeq();
        user_id = search_list.get(position).getUser_id();
        title = search_list.get(position).get제목();
        thum_url = search_list.get(position).getThumnail_url();
        contents = search_list.get(position).get소개글();
        category = search_list.get(position).get카테고리();
        publicSet = search_list.get(position).get공개여부();
        ageStart = search_list.get(position).get나이제한_시작();
        ageEnd = search_list.get(position).get나이제한_끝();
        gender = search_list.get(position).get성별제한();
        created_at = search_list.get(position).get생성일자();
        memberNum = search_list.get(position).get멤버수();

        /** layout 초기화 **/
        // 이미지
        StorageReference imagesRef = FirebaseCloudStorage.Storage_img(thum_url);
        imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(holder.itemView).load(uri).into(holder.imgThum);
            }
        });

        // 제목, 카테고리
        holder.txtTitle.setText(title);
        holder.txtCate.setText("#"+카테고리한글(category));
        // 나이
        holder.txtAge.setText(나이포맷(ageStart,ageEnd));
        // 성별
        holder.txtGend.setText(성별포맷(gender));
        // 멤버수
        holder.txtMemb.setText(memberNum);

//        /** 나이필터리스트 초기화 **/
//        if(ageStart==0 && ageEnd==0){
//            나이필터리스트.add(search_list.get(position));
//        }
//        /** 성별필터리스트 초기화 **/
//        else if(gender==0){
//            성별필터리스트.add(search_list.get(position));
//        }
    }

    // 작성여부의 true 갯수 만큼 view 만듦
    @Override
    public int getItemCount() {
        return search_list.size();
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }


    //    ----------------뷰홀더-------------------
    public class CustomViewHolder extends RecyclerView.ViewHolder{
        ImageView imgThum;
        TextView txtTitle, txtCate , txtAge, txtGend, txtMemb;
        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            // 이미지
            imgThum =(ImageView)itemView.findViewById(R.id.img_search_thumnail);
            // textview
            txtTitle =(TextView)itemView.findViewById(R.id.text_search_title);
            txtCate =(TextView)itemView.findViewById(R.id.text_search_category);
            txtAge =(TextView)itemView.findViewById(R.id.text_search_limit_age);
            txtGend =(TextView)itemView.findViewById(R.id.text_search_limit_gender);
            txtMemb =(TextView)itemView.findViewById(R.id.text_search_member_num);

            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    int posi = getAdapterPosition();
                    /** 게시물 식별번호 **/
                    int 밴드번호 = search_list.get(posi).getSeq();
                    if (posi != RecyclerView.NO_POSITION)
                    {
                        if (mListener != null) {
                            mListener.onItemClick(v, 밴드번호) ;
                        }
                    }
                }
            });
        }
    }

}

