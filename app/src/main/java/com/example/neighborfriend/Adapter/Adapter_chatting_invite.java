package com.example.neighborfriend.Adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.neighborfriend.Class.FirebaseCloudStorage;
import com.example.neighborfriend.Fragment.Fragment_more;
import com.example.neighborfriend.R;
import com.example.neighborfriend.object.bands_post;
import com.example.neighborfriend.object.memberToInvite;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

/**
 * 밴드의 게시물 list Recyclerview
 **/

/** 기능 : 클릭 시 해당 게시물 Activity 로 이동 **/
public class Adapter_chatting_invite extends RecyclerView.Adapter<Adapter_chatting_invite.CustomViewHolder> {
    private String user_id,created_at,thum_uri,nick;
    private List<memberToInvite> member_list;
    public Adapter_chatting_invite(List<memberToInvite> list) {
        member_list = list;
    }

    //  --------------------------onclick --------------
    private OnItemClickListener mListener = null ;
    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener ;
    }
    public interface OnItemClickListener {
        void onItemClick(View v, String user_id, boolean ischecked) ;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View 뷰 = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_chatting_invite_member, parent, false);
        return new CustomViewHolder(뷰);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        /** 변수 초기화 **/
        user_id = member_list.get(position).getUser_id();
        created_at = member_list.get(position).getCreated_at();
        thum_uri = member_list.get(position).getThumnail_url();
        nick = member_list.get(position).getNickname();

        // 썸네일
        if (thum_uri.split("/")[0].equals("https:")) {
            Glide.with(holder.itemView).load(thum_uri).into(holder.imgThum);
        } else {
            StorageReference imagesRef1 = FirebaseCloudStorage.Storage_img(thum_uri);
            imagesRef1.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(holder.itemView).load(uri).into(holder.imgThum);
                }
            });
        }

        // 닉네임
        holder.txtNick.setText(nick);

    }

    // 작성여부의 true 갯수 만큼 view 만듦
    @Override
    public int getItemCount() {
        return member_list.size();
    }

    //    ----------------뷰홀더-------------------
    public class CustomViewHolder extends RecyclerView.ViewHolder {
        RadioButton btnRadio; ImageView imgThum; TextView txtNick;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            btnRadio = (RadioButton) itemView.findViewById(R.id.radioButton_inviteMember);
            imgThum = (ImageView) itemView.findViewById(R.id.img_inviteMember);
            txtNick = (TextView) itemView.findViewById(R.id.text_nickname_inviteMember);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(btnRadio.isChecked()) btnRadio.setChecked(false);
                    else btnRadio.setChecked(true);

                    int posi = getAdapterPosition();
                    String user_id = member_list.get(posi).getUser_id();

                    if (posi != RecyclerView.NO_POSITION)
                    {
                        if (mListener != null) {
                            mListener.onItemClick(v, user_id, btnRadio.isChecked()) ;
                        }
                    }
                }
            });
        }
    }

}

