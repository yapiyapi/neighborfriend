package com.example.neighborfriend.Adapter;

import static com.example.neighborfriend.Activity_band.시간포멧to몇분전;

import android.content.Context;
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
public class Adapter_band_postList extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List 이미지_list;
    private String 썸네일,이미지1,이미지2,이미지3; String 동영상썸네일;
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
        void onItemClick(View v, int band_seq, String id) ;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (viewType == 0) {
            view = inflater.inflate(R.layout.cell_band_post, parent, false);
            return new CustomViewHolder(view);
        } else {
            view = inflater.inflate(R.layout.cell_band_post_live_streaming, parent, false);
            return new LiveStreamViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CustomViewHolder) {
            /** 변수 초기화 **/
            이미지_list = Arrays.asList(post_list.get(position).getImage_uri().replace("[", "").replace("]", "").split(","));
            if (이미지_list.size() > 2){
                이미지1 = String.valueOf(이미지_list.get(0)).trim();
                이미지2 = String.valueOf(이미지_list.get(1)).trim();
                이미지3 = String.valueOf(이미지_list.get(2)).trim();

                StorageReference imagesRef1 = FirebaseCloudStorage.Storage_img(이미지1);
                imagesRef1.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(holder.itemView).load(uri).into(((CustomViewHolder) holder).image_1);
                    }
                });
                StorageReference imagesRef2 = FirebaseCloudStorage.Storage_img(이미지2);
                imagesRef2.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(holder.itemView).load(uri).into(((CustomViewHolder) holder).image_2);
                    }
                });
                StorageReference imagesRef3 = FirebaseCloudStorage.Storage_img(이미지3);
                imagesRef3.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(holder.itemView).load(uri).into(((CustomViewHolder) holder).image_3);
                    }
                });
            }else if(이미지_list.size() == 2){
                이미지1 = String.valueOf(이미지_list.get(0)).trim();
                이미지2 = String.valueOf(이미지_list.get(1)).trim();

                StorageReference imagesRef1 = FirebaseCloudStorage.Storage_img(이미지1);
                imagesRef1.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(holder.itemView).load(uri).into(((CustomViewHolder) holder).image_1);
                    }
                });
                StorageReference imagesRef2 = FirebaseCloudStorage.Storage_img(이미지2);
                imagesRef2.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(holder.itemView).load(uri).into(((CustomViewHolder) holder).image_2);
                    }
                });
                ((CustomViewHolder) holder).image_3.setVisibility(View.INVISIBLE);
            }else if (이미지_list.get(0).toString().trim().equals("")){
                ((CustomViewHolder) holder).linear.setVisibility(View.GONE);
            }else {
                이미지1 = String.valueOf(이미지_list.get(0)).trim();

                StorageReference imagesRef = FirebaseCloudStorage.Storage_img(이미지1);
                imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(holder.itemView).load(uri).into(((CustomViewHolder) holder).image_1);
                    }
                });
                ((CustomViewHolder) holder).image_2.setVisibility(View.INVISIBLE);
                ((CustomViewHolder) holder).image_3.setVisibility(View.INVISIBLE);
            }


            닉네임 = post_list.get(position).getNickname();
            지난시간 = post_list.get(position).getUpdated_at();
            내용 = post_list.get(position).get게시글();

            /** layout 초기화 **/
            썸네일 = post_list.get(position).getThumnail_url();

            if(썸네일==null);
            else if (썸네일.split("/")[0].equals("https:")) {
                Glide.with(holder.itemView).load(썸네일).into(((CustomViewHolder) holder).user_thumnail);
            } else {
                StorageReference imagesRef = FirebaseCloudStorage.Storage_img(썸네일);
                imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(holder.itemView).load(uri).into(((CustomViewHolder) holder).user_thumnail);
                    }
                });
            }

            // 닉네임, 지난시간, 내용
            ((CustomViewHolder) holder).nickname.setText(닉네임);
//        Log.i("asdf", 지난시간);
            try {
                ((CustomViewHolder) holder).pasttime.setText(시간포멧to몇분전(지난시간));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            ((CustomViewHolder) holder).contents.setText(내용);
        } else {
            /** LIVE Streaming **/
            닉네임 = post_list.get(position).getNickname();
            지난시간 = post_list.get(position).getUpdated_at();
            // 닉네임
            ((LiveStreamViewHolder) holder).nickname.setText(닉네임);
            try { // 지난시간
                ((LiveStreamViewHolder) holder).pasttime.setText(시간포멧to몇분전(지난시간));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            // 방송자 썸네일
            썸네일 = post_list.get(position).getThumnail_url();

            if(썸네일==null);
            else if (썸네일.split("/")[0].equals("https:")) {
                Glide.with(holder.itemView).load(썸네일).into(((LiveStreamViewHolder) holder).user_thumnail);
            } else {
                StorageReference imagesRef = FirebaseCloudStorage.Storage_img(썸네일);
                imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(holder.itemView).load(uri).into(((LiveStreamViewHolder) holder).user_thumnail);
                    }
                });
            }

            // 동영상 썸네일
            동영상썸네일 = post_list.get(position).getThumnail_url();
            Glide.with(holder.itemView).load(동영상썸네일).into(((LiveStreamViewHolder) holder).image);
        }


    }

    // 작성여부의 true 갯수 만큼 view 만듦
    @Override
    public int getItemCount() {
        return post_list.size();
    }
    @Override
    public int getItemViewType(int position) {
        return post_list.get(position).getViewType();
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

    public class LiveStreamViewHolder extends RecyclerView.ViewHolder{
        ImageView user_thumnail, image;
        TextView nickname, pasttime;
        public LiveStreamViewHolder(@NonNull View itemView) {
            super(itemView);
            user_thumnail =(ImageView)itemView.findViewById(R.id.img_user_thumnail_liveCell);
            nickname =(TextView)itemView.findViewById(R.id.text_nickname_liveCell);
            pasttime =(TextView)itemView.findViewById(R.id.text_pasttime_liveCell);
            image =(ImageView)itemView.findViewById(R.id.imageview_liveCell);
            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    int posi = getAdapterPosition();
                    /** 게시물 식별번호 **/
                    int 밴드번호 = post_list.get(posi).getBand_seq();
                    String user_id = post_list.get(posi).getUser_id();
                    if (posi != RecyclerView.NO_POSITION)
                    {
                        if (mListener != null) {
                            mListener.onItemClick(v, 밴드번호, user_id) ;
                        }
                    }
                }
            });
        }
    }

}

