package com.example.neighborfriend.Adapter;

import static com.example.neighborfriend.Activity_band.시간포멧to몇분전;

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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** 밴드의 게시물 list Recyclerview **/
/** 기능 : 클릭 시 해당 게시물 Activity 로 이동 **/
public class Adapter_band_postList extends RecyclerView.Adapter<Adapter_band_postList.CustomViewHolder> {

    private List 이미지_list;
    private String 썸네일,이미지1,이미지2,이미지3;
    private String 닉네임, 지난시간, 내용;
    private List<bands_post> post_list;
//    private String imguri;

    public Adapter_band_postList(ArrayList<bands_post> list) {
        post_list = list;
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
        View 뷰 = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_band_post, parent,false);
        return new CustomViewHolder(뷰);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        /** 변수 초기화 **/
        이미지_list = Arrays.asList(post_list.get(position).getImage_uri().replace("[", "").replace("]", "").split(","));
        if (이미지_list.size() > 2){
            이미지1 = String.valueOf(이미지_list.get(0)).trim();
            이미지2 = String.valueOf(이미지_list.get(1)).trim();
            이미지3 = String.valueOf(이미지_list.get(2)).trim();

            Log.i("이미지1",이미지1);
            Log.i("이미지1",이미지2);
            Log.i("이미지1",이미지3);
            StorageReference imagesRef1 = FirebaseCloudStorage.Storage_img(이미지1);
            imagesRef1.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(holder.itemView).load(uri).into(holder.image_1);
                }
            });
            StorageReference imagesRef2 = FirebaseCloudStorage.Storage_img(이미지2);
            imagesRef2.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(holder.itemView).load(uri).into(holder.image_2);
                }
            });
            StorageReference imagesRef3 = FirebaseCloudStorage.Storage_img(이미지3);
            imagesRef3.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(holder.itemView).load(uri).into(holder.image_3);
                }
            });
        }else if(이미지_list.size() == 2){
            이미지1 = String.valueOf(이미지_list.get(0)).trim();
            이미지2 = String.valueOf(이미지_list.get(1)).trim();

            Log.i("이미지1",이미지1);
            Log.i("이미지1",이미지2);
            StorageReference imagesRef1 = FirebaseCloudStorage.Storage_img(이미지1);
            imagesRef1.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(holder.itemView).load(uri).into(holder.image_1);
                }
            });
            StorageReference imagesRef2 = FirebaseCloudStorage.Storage_img(이미지2);
            imagesRef2.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(holder.itemView).load(uri).into(holder.image_2);
                }
            });
            holder.image_3.setVisibility(View.INVISIBLE);
        }else if (이미지_list.get(0).toString().trim().equals("")){
            holder.linear.setVisibility(View.GONE);
        }else {
            이미지1 = String.valueOf(이미지_list.get(0)).trim();

            Log.i("이미지1",이미지1);
            StorageReference imagesRef = FirebaseCloudStorage.Storage_img(이미지1);
            imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(holder.itemView).load(uri).into(holder.image_1);
                }
            });
            holder.image_2.setVisibility(View.INVISIBLE);
            holder.image_3.setVisibility(View.INVISIBLE);
        }


        닉네임 = post_list.get(position).getNickname();
        지난시간 = post_list.get(position).getUpdated_at();
        내용 = post_list.get(position).get게시글();

        Log.i("A", String.valueOf(이미지_list.size()));
        Log.i("A", String.valueOf(이미지_list));
        Log.i("A", String.valueOf(이미지1));
        Log.i("A", String.valueOf(이미지2));
//        Log.i("A", String.valueOf(이미지1));
        /** layout 초기화 **/
        썸네일 = post_list.get(position).getThumnail_url();

        if(썸네일==null);
        else if (썸네일.split("/")[0].equals("https:")) {
            Glide.with(holder.itemView).load(썸네일).into(holder.user_thumnail);
        } else {
            StorageReference imagesRef = FirebaseCloudStorage.Storage_img(썸네일);
            imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(holder.itemView).load(uri).into(holder.user_thumnail);
                }
            });
        }

        // 닉네임, 지난시간, 내용
        holder.nickname.setText(닉네임);
//        Log.i("asdf", 지난시간);
        try {
            holder.pasttime.setText(시간포멧to몇분전(지난시간));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        holder.contents.setText(내용);

    }

    // 작성여부의 true 갯수 만큼 view 만듦
    @Override
    public int getItemCount() {
        return post_list.size();
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }
    //    ----------------뷰홀더-------------------
    public class CustomViewHolder extends RecyclerView.ViewHolder{
        ImageView user_thumnail, image_1,image_2,image_3;
        TextView nickname, pasttime, contents;
        LinearLayout linear;
        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            user_thumnail =(ImageView)itemView.findViewById(R.id.img_user_thumnail);
            image_1 =(ImageView)itemView.findViewById(R.id.img_post1);
            image_2 =(ImageView)itemView.findViewById(R.id.img_post2);
            image_3 =(ImageView)itemView.findViewById(R.id.img_post3);
            nickname =(TextView)itemView.findViewById(R.id.text_nickname);
            pasttime =(TextView)itemView.findViewById(R.id.text_pasttime);
            contents =(TextView)itemView.findViewById(R.id.text_contents);

            linear = (LinearLayout) itemView.findViewById(R.id.linear_image);

            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    int posi = getAdapterPosition();
                    /** 게시물 식별번호 **/
                    int 밴드번호 = post_list.get(posi).getBand_seq();
                    int seq = post_list.get(posi).getSeq();
                    if (posi != RecyclerView.NO_POSITION)
                    {
                        if (mListener != null) {
                            mListener.onItemClick(v, 밴드번호, seq) ;
                        }
                    }
                }
            });
        }
    }

}

