package com.example.neighborfriend.Adapter;

import android.net.Uri;
import android.util.Log;
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
import com.example.neighborfriend.object.home_cell;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class Adapter_home_joinband extends RecyclerView.Adapter<Adapter_home_joinband.CustomViewHolder> {

    private String 밴드;

    private List<home_cell> joinband_list;
    private int seq; private String thumnail_url, title;

    public Adapter_home_joinband(ArrayList<home_cell> list) {
        joinband_list = list;
    }

    //  --------------------------onclick --------------
    private OnItemClickListener mListener = null ;
    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener ;
    }
    public interface OnItemClickListener {
        void onItemClick(View v, int key) ;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View 뷰 = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_home_band, parent,false);
        return new CustomViewHolder(뷰);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        seq = joinband_list.get(position).getSeq();
        thumnail_url = joinband_list.get(position).getThumnail_url();
        title = joinband_list.get(position).get제목();

         // 이미지
        String 경로 = String.format("bands/%s/thumnail", seq);
//        Log.i("ASdfa",경로);
        // storage 읽기
        StorageReference imagesRef = FirebaseCloudStorage.Storage_img(경로);
        imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(holder.itemView).load(uri).into(holder.이미지);
            }
        });
        // 제목
        holder.제목.setText(title);
    }

    // 작성여부의 true 갯수 만큼 view 만듦
    @Override
    public int getItemCount() {
        return joinband_list.size();
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }
    //    ----------------뷰홀더-------------------
    public class CustomViewHolder extends RecyclerView.ViewHolder{
        ImageView 이미지;
        TextView 제목;
        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            이미지 =(ImageView)itemView.findViewById(R.id.cell_myband_image);
            제목 =(TextView)itemView.findViewById(R.id.cell_myband_title);

            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    int posi = getAdapterPosition();
                    /** 게시물 식별번호 **/
                    int 밴드번호 = joinband_list.get(posi).getSeq();
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
