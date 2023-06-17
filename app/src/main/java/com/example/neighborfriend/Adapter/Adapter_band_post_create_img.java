package com.example.neighborfriend.Adapter;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.neighborfriend.Class.FirebaseCloudStorage;
import com.example.neighborfriend.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

/**
 * 밴드에 게시물 올릴 때 다중이미지 Recyclerview  기능 : 클릭 시 삭제
 **/
/** 기능 : 클릭 시 삭제 **/

/**     : Activity_band_post_create -> imgList 에 추가/삭제 시 실시간 반영 **/
public class Adapter_band_post_create_img extends RecyclerView.Adapter<Adapter_band_post_create_img.CustomViewHolder> {

    private String 밴드;

    private List<String> imgList;
    private String imguri, band_seq, seq;

    public Adapter_band_post_create_img(ArrayList<String> list, String band_seq, String seq) {
        imgList = list;
        this.band_seq = band_seq;
        this.seq = seq;
    }

    //  --------------------------onclick --------------
    private OnItemClickListener mListener = null;

    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int key);
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View 뷰 = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_band_post_create_img, parent, false);
        return new CustomViewHolder(뷰);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, @SuppressLint("RecyclerView") int position) {

        imguri = imgList.get(position);
        // 이미지

        if (imguri.split("/")[0].equals("content:")) {
            Glide.with(holder.itemView).load(imguri).into(holder.이미지);

        } else {
            StorageReference imagesRef = FirebaseCloudStorage.Storage_img(imguri);
            imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Log.i("ASdfasdfasdfadsfadfs", String.valueOf(uri));
                    Glide.with(holder.itemView).load(uri).into(holder.이미지);
                }
            });

        }


        Log.i("Asdfa", imguri);

    }

    // 작성여부의 true 갯수 만큼 view 만듦
    @Override
    public int getItemCount() {
        return imgList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    //    ----------------뷰홀더-------------------
    public class CustomViewHolder extends RecyclerView.ViewHolder {
        ImageView 이미지;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            이미지 = (ImageView) itemView.findViewById(R.id.cell_post_create_image);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 클릭 시 이미지 삭제
                    int posi = getAdapterPosition();
                    imgList.remove(posi);
                    notifyItemRemoved(posi);
                    notifyItemRangeChanged(posi, imgList.size());

                    Log.i("이미지리스트", imgList.toString());

                }
            });
        }
    }

}

