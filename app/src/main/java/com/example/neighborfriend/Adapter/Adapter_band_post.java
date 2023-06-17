package com.example.neighborfriend.Adapter;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.neighborfriend.Class.FirebaseCloudStorage;
import com.example.neighborfriend.R;
import com.example.neighborfriend.object.bands_post;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** 게시물 Recyclerview **/
/** 게시물에 올린 이미지 **/
public class Adapter_band_post extends RecyclerView.Adapter<Adapter_band_post.CustomViewHolder> {
    private String imguri;
    private List post_img_list;
//    private String imguri;

    public Adapter_band_post(List list) {
        post_img_list = list;
    }

    //  --------------------------onclick --------------
    private OnItemClickListener mListener = null ;
    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener ;
    }
    public interface OnItemClickListener {
        void onItemClick(View v, int band_seq, int seq) ;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View 뷰 = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_band_post_img, parent,false);
        return new CustomViewHolder(뷰);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {

        if(!post_img_list.get(0).equals("")) {
            /** 변수 초기화 **/
            imguri = post_img_list.get(position).toString().trim();

            StorageReference imagesRef = FirebaseCloudStorage.Storage_img(imguri);
            imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(holder.itemView).load(uri).into(holder.imageView);
                }
            });
        }else{
            holder.imageView.setVisibility(View.GONE);
        }

    }

    // 작성여부의 true 갯수 만큼 view 만듦
    @Override
    public int getItemCount() {
        return post_img_list.size();
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }
    //    ----------------뷰홀더-------------------
    public class CustomViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView =(ImageView)itemView.findViewById(R.id.cell_post_image);
        }
    }

}

