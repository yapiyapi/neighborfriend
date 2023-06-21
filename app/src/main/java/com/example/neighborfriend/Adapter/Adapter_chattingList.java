package com.example.neighborfriend.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.example.neighborfriend.Activity_band_post;
import com.example.neighborfriend.Class.FirebaseCloudStorage;
import com.example.neighborfriend.R;
import com.example.neighborfriend.object.chatting;
import com.example.neighborfriend.object.chattingRoom;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Locale;

/**
 * 채팅 리스트 Recyclerview
 **/
public class Adapter_chattingList extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private String imguri;
    private List<chattingRoom> chattingRoom_list;
    private int seq, band_seq, room_type, member, 마지막채팅타입;
    int 채팅방종류, isMine;
    private String user_id, band_title, thumnail_url, title, intro, created_at, 마지막채팅, 마지막채팅시간;
    private int 읽지않은메세지수, from; // 0: 밴드 1: 내 채팅

    public Adapter_chattingList(List list, int from) {
        chattingRoom_list = list;
        this.from = from;
        채팅방종류 = -1;
    }

    //  --------------------------onclick --------------
    private OnItemClickListener mListener = null;

    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, String user_id, String thumnail_uri, String title, String intro, int isMine, int 밴드번호, int seq);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        View view;
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        if (viewType == 0) {
            view = inflater.inflate(R.layout.cell_chattingroom_txt, parent, false);
            return new TextViewHolder(view);
        } else {
            view = inflater.inflate(R.layout.cell_chattingroom, parent, false);
            return new CustomViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        마지막채팅 = chattingRoom_list.get(position).getTxt_contents();
        if (holder instanceof Adapter_chattingList.TextViewHolder) {
            ((TextViewHolder) holder).txtCate.setText(마지막채팅);
        }else if (holder instanceof Adapter_chattingList.CustomViewHolder) {
            // 초기화
            seq = chattingRoom_list.get(position).getSeq();
            band_seq = chattingRoom_list.get(position).getSeq();
            user_id = chattingRoom_list.get(position).getUser_id();
            thumnail_url = chattingRoom_list.get(position).getThumnail();
            title = chattingRoom_list.get(position).getTitle();
            intro = chattingRoom_list.get(position).getIntroduction();
            room_type = chattingRoom_list.get(position).getRoom_type(); // 0: 전체 / 1: 공개
            created_at = chattingRoom_list.get(position).getCreated_at();

            member = chattingRoom_list.get(position).getMember();
            마지막채팅타입 = chattingRoom_list.get(position).getMsg_type();
            마지막채팅시간 = chattingRoom_list.get(position).getMsg_created_at();
            읽지않은메세지수 = chattingRoom_list.get(position).getUnreadNum();
            /** 채팅목록 종류 (밴드 / 내 채팅목록)**/
            // [밴드]의 채팅방이면
            if (from == 0) {
                // 초기화 ------------------------------------------------------
                isMine = chattingRoom_list.get(position).getIsMine(); // 내 채팅방: 1 / 참여할.. : 0

                // 마지막 채팅 or 채팅방 설명 /  마지막 채팅 시간 -----------------------------------
                if (isMine == 1) {
                    if (마지막채팅시간 == null) ((CustomViewHolder) holder).txtLastChat.setText(intro);
                    else if (마지막채팅타입 == 0) ((CustomViewHolder) holder).txtLastChat.setText(마지막채팅);
                    else if (마지막채팅타입 == 1) ((CustomViewHolder) holder).txtLastChat.setText("사진을 보냈습니다.");
                    else ((CustomViewHolder) holder).txtLastChat.setText("동영상을 보냈습니다.");

                    ((CustomViewHolder) holder).txtLastChatTime.setText(마지막채팅시간);
                } else {
                    ((CustomViewHolder) holder).txtLastChat.setText(intro);
                    ((CustomViewHolder) holder).txtLastChatTime.setText(null);
                }

                // 채팅방 종류
                if (room_type == 0) ((CustomViewHolder) holder).txtRoomtype.setText("전체채팅방");
                else if (room_type == 1) ((CustomViewHolder) holder).txtRoomtype.setText("공개채팅방");
                else ((CustomViewHolder) holder).txtRoomtype.setText("비공개채팅방");
            }
            // 나의 채팅 리스트
            else {
                band_title = chattingRoom_list.get(position).getBand_title();

                if (마지막채팅시간 == null) ((CustomViewHolder) holder).txtLastChat.setText(intro);
                else if (마지막채팅타입 == 0) ((CustomViewHolder) holder).txtLastChat.setText(마지막채팅);
                else if (마지막채팅타입 == 1) ((CustomViewHolder) holder).txtLastChat.setText("사진을 보냈습니다.");
                else ((CustomViewHolder) holder).txtLastChat.setText("동영상을 보냈습니다.");

                ((CustomViewHolder) holder).txtLastChatTime.setText(마지막채팅시간);

                // 채팅방 종류
                if (room_type == 0) ((CustomViewHolder) holder).txtRoomtype.setText(band_title + " · 전체채팅방");
                else if (room_type == 1) ((CustomViewHolder) holder).txtRoomtype.setText(band_title + " · 공개채팅방");
                else ((CustomViewHolder) holder).txtRoomtype.setText(band_title + " · 비공개채팅방");
            }

            // 썸네일
            if (thumnail_url == null)
                Glide.with(holder.itemView).load(R.drawable.private_chatting_thum).into(((CustomViewHolder) holder).imgView);
            else {
                StorageReference imagesRef1 = FirebaseCloudStorage.Storage_img(thumnail_url);
                imagesRef1.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(holder.itemView).load(uri).into(((CustomViewHolder) holder).imgView);
                    }
                });
            }
            // 제목, 멤버수
            if (((CustomViewHolder) holder).txtTitle == null) ((CustomViewHolder) holder).txtTitle.setText(title);
            else ((CustomViewHolder) holder).txtTitle.setText(title);
            ((CustomViewHolder) holder).txtMembNum.setText(String.valueOf(member));

            // 읽지 않은 메세지 수
            if(읽지않은메세지수==0) {
                ((CustomViewHolder) holder).txtNonReadNum.setVisibility(View.INVISIBLE);
            }
            else{
                ((CustomViewHolder) holder).txtNonReadNum.setVisibility(View.VISIBLE);
                ((CustomViewHolder) holder).txtNonReadNum.setText(String.valueOf(읽지않은메세지수));
            }
        }

    }

    // 작성여부의 true 갯수 만큼 view 만듦
    @Override
    public int getItemCount() {
        return chattingRoom_list.size();
    }

    @Override
    public int getItemViewType(int position) {
        return chattingRoom_list.get(position).getViewType();
    }

    //    ----------------뷰홀더-------------------
    public class CustomViewHolder extends RecyclerView.ViewHolder {

        ImageView imgView;
        TextView txtTitle, txtMembNum, txtLastChatTime, txtLastChat, txtRoomtype, txtNonReadNum;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            imgView = (ImageView) itemView.findViewById(R.id.img_chatting_room_thumnail);
            txtTitle = (TextView) itemView.findViewById(R.id.text_chatting_room_title);
            txtMembNum = (TextView) itemView.findViewById(R.id.text_chatting_room_member_num);
            txtLastChatTime = (TextView) itemView.findViewById(R.id.text_chatting_room_last_chat_time);
            txtLastChat = (TextView) itemView.findViewById(R.id.text_chatting_room_last_chat);
            txtRoomtype = (TextView) itemView.findViewById(R.id.text_chatting_room_type);
            txtNonReadNum = (TextView) itemView.findViewById(R.id.text_chatting_room_none_read_num);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int posi = getAdapterPosition();
                    /** 게시물 식별번호 **/
                    String user_id = chattingRoom_list.get(posi).getUser_id();
                    String thumnail_uri = chattingRoom_list.get(posi).getThumnail();
                    String title = chattingRoom_list.get(posi).getTitle();
                    String intro = chattingRoom_list.get(posi).getIntroduction();

                    int 밴드번호 = chattingRoom_list.get(posi).getBand_seq();
                    int 채팅방_seq = chattingRoom_list.get(posi).getSeq();
                    if (posi != RecyclerView.NO_POSITION) {
                        if (mListener != null) {
                            // [밴드]의 채팅방이면
                            if (from == 0) {
                                int isMine = chattingRoom_list.get(posi).getIsMine();
                                mListener.onItemClick(v, user_id, thumnail_uri, title, intro, isMine, 밴드번호, 채팅방_seq);
                            } else {
                                mListener.onItemClick(v, user_id, thumnail_uri, title, intro, -1, 밴드번호, 채팅방_seq);
                            }
                        }
                    }
                }
            });
        }
    }


    public class TextViewHolder extends RecyclerView.ViewHolder {
        TextView txtCate;

        public TextViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCate = (TextView) itemView.findViewById(R.id.txt_category_chatRoomList);
        }
    }


    /**
     * 수정
     **/
    public void updateItem(int position, chattingRoom Item) {
        chattingRoom_list.set(position, Item);
        notifyItemChanged(position);
    }

    public void updateItemSpecial(int position, int PlusMinus) {
        chattingRoom_list.get(position).setMember(chattingRoom_list.get(position).getMember() + PlusMinus);
        notifyItemChanged(position);
    }

    public void deleteItem(int position) {
        notifyItemRangeChanged(position-1, position+1);
    }
}

