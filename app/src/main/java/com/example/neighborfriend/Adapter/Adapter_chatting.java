package com.example.neighborfriend.Adapter;

import static com.example.neighborfriend.Activity_band_chatting_room.시간포맷;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.neighborfriend.Class.FirebaseCloudStorage;
import com.example.neighborfriend.R;
import com.example.neighborfriend.object.chatting;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 채팅 Recyclerview
 **/
public class Adapter_chatting extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int TXT_SEND_CONTENT = 0;
    public static final int TXT_RECEIVE_CONTENT = 1;
    public static final int IMG_SEND_CONTENT = 2;
    public static final int IMG_RECEIVE_CONTENT = 3;
    public static final int VDO_SEND_CONTENT = 4;
    public static final int VDO_RECEIVE_CONTENT = 5;
    public static final int VDO_LOADING = 6;
    public static final int SPECIAL = 7;
    public static final int IMG = 0;
    public static final int VDO = 1;
    private List<chatting> chatting_list;

    public Adapter_chatting(List list) {
        chatting_list = list;
    }

    //  --------------------------onclick --------------
    private OnItemClickListener mListener = null;

    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int type, String uri_path, int chatRoom_seq, int seq);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (viewType == TXT_SEND_CONTENT) {
            view = inflater.inflate(R.layout.cell_chatting_txt_sender, parent, false);
            return new sendTxtViewHolder(view);
        } else if (viewType == TXT_SEND_CONTENT + 10) {
            view = inflater.inflate(R.layout.cell_chatting_txt_sender, parent, false);
            return new sendTxtViewHolder(view);
        } else if (viewType == TXT_RECEIVE_CONTENT) {
            view = inflater.inflate(R.layout.cell_chatting_txt_receiver, parent, false);
            return new receiveTxtViewHolder(view);
        } else if (viewType == IMG_SEND_CONTENT) {
            view = inflater.inflate(R.layout.cell_chatting_img_sender, parent, false);
            return new sendImgViewHolder(view);
        } else if (viewType == IMG_SEND_CONTENT + 10) {
            view = inflater.inflate(R.layout.cell_chatting_img_sender, parent, false);
            return new sendImgViewHolder(view);
        } else if (viewType == IMG_RECEIVE_CONTENT) {
            view = inflater.inflate(R.layout.cell_chatting_img_receiver, parent, false);
            return new receiveImgViewHolder(view);
        } else if (viewType == VDO_SEND_CONTENT) {
            view = inflater.inflate(R.layout.cell_chatting_video_sender, parent, false);
            return new sendVdoViewHolder(view);
        } else if (viewType == VDO_SEND_CONTENT + 10) {
            view = inflater.inflate(R.layout.cell_chatting_video_sender, parent, false);
            return new sendVdoViewHolder(view);
        } else if (viewType == VDO_RECEIVE_CONTENT) {
            view = inflater.inflate(R.layout.cell_chatting_video_receiver, parent, false);
            return new receiveVdoViewHolder(view);
        } else if (viewType == VDO_LOADING) {
            view = inflater.inflate(R.layout.cell_chatting_loading, parent, false);
            return new loadingViewHolder(view);
        } else {
            view = inflater.inflate(R.layout.cell_chatting_for_special, parent, false);
            return new specialViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        // txt ---------------------------------------------------------
        if (holder instanceof sendTxtViewHolder) {
            // 전송되지 않았을 때
            if (chatting_list.get(position).getViewType() == 10)
                ((sendTxtViewHolder) holder).not_sent.setVisibility(View.VISIBLE);
            else ((sendTxtViewHolder) holder).not_sent.setVisibility(View.INVISIBLE);
            ((sendTxtViewHolder) holder).sendMsg.setText(chatting_list.get(position).getTxt_contents().trim());

            // 읽음 표시
            String unread_list = chatting_list.get(position).getUnread_list();
            if (unread_list.equals("")) ((sendTxtViewHolder) holder).readNum.setText("");
            else ((sendTxtViewHolder) holder).readNum.setText(String.valueOf(읽지않은사람수(unread_list)));
            // 시간
            ((sendTxtViewHolder) holder).created_at.setText(시간포맷(chatting_list.get(position).getMsg_created_at()));
        } else if (holder instanceof receiveTxtViewHolder) {
            ((receiveTxtViewHolder) holder).txtName.setText(chatting_list.get(position).getNickname());
            ((receiveTxtViewHolder) holder).receiveMsg.setText(chatting_list.get(position).getTxt_contents().trim());
            // 읽음 표시
            String unread_list = chatting_list.get(position).getUnread_list();
            if (unread_list.equals("")) ((receiveTxtViewHolder) holder).readNum.setText("");
            else
                ((receiveTxtViewHolder) holder).readNum.setText(String.valueOf(읽지않은사람수(unread_list)));
            // 시간
            ((receiveTxtViewHolder) holder).created_at.setText(시간포맷(chatting_list.get(position).getMsg_created_at()));
        }
        // img ---------------------------------------------------------
        else if (holder instanceof sendImgViewHolder) {
            // 전송되지 않았을 때
            String 이미지_uri = chatting_list.get(position).getMsg_uri();
            if (chatting_list.get(position).getViewType() == 12) {
                // 전송되지 않음 표시
                ((sendImgViewHolder) holder).not_sent.setVisibility(View.VISIBLE);
                ((sendImgViewHolder) holder).not_sent_x.setVisibility(View.VISIBLE);
                // 썸네일
                Glide.with(holder.itemView).load(이미지_uri).into(((sendImgViewHolder) holder).img);
            } else {
                // 전송되지 않음 표시
                ((sendImgViewHolder) holder).not_sent.setVisibility(View.INVISIBLE);
                ((sendImgViewHolder) holder).not_sent_x.setVisibility(View.INVISIBLE);
                // 썸네일
                StorageReference imagesRef1 = FirebaseCloudStorage.Storage_img(이미지_uri);
                imagesRef1.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(holder.itemView).load(uri).into(((sendImgViewHolder) holder).img);
                    }
                });
            }
            // 읽음 표시
            String unread_list = chatting_list.get(position).getUnread_list();
            if (unread_list.equals("")) ((sendImgViewHolder) holder).readNum.setText("");
            else ((sendImgViewHolder) holder).readNum.setText(String.valueOf(읽지않은사람수(unread_list)));
            // 시간
            ((sendImgViewHolder) holder).created_at.setText(시간포맷(chatting_list.get(position).getMsg_created_at()));
        } else if (holder instanceof receiveImgViewHolder) {
            String 이미지_uri = chatting_list.get(position).getMsg_uri();
            // 썸네일
            StorageReference imagesRef1 = FirebaseCloudStorage.Storage_img(이미지_uri);
            imagesRef1.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(holder.itemView).load(uri).into(((receiveImgViewHolder) holder).img);
                }
            });
            ((receiveImgViewHolder) holder).imgName.setText(chatting_list.get(position).getNickname());
            // 읽음 표시
            String unread_list = chatting_list.get(position).getUnread_list();
            if (unread_list.equals("")) ((receiveImgViewHolder) holder).readNum.setText("");
            else
                ((receiveImgViewHolder) holder).readNum.setText(String.valueOf(읽지않은사람수(unread_list)));
            // 시간
            ((receiveImgViewHolder) holder).created_at.setText(시간포맷(chatting_list.get(position).getMsg_created_at()));
        }
        // vdo ---------------------------------------------------------
        else if (holder instanceof sendVdoViewHolder) {
            String 동영상_uri = chatting_list.get(position).getMsg_uri();
            // 전송되지 않았을 때
            if (chatting_list.get(position).getViewType() == 14) {
                // 전송되지 않음 표시
                ((sendVdoViewHolder) holder).not_sent.setVisibility(View.VISIBLE);
                ((sendVdoViewHolder) holder).not_sent_x.setVisibility(View.VISIBLE);
                ((sendVdoViewHolder) holder).playButton_s.setVisibility(View.INVISIBLE);
                // 썸네일
                Glide.with(holder.itemView).load(동영상_uri).into(((sendVdoViewHolder) holder).vdo);
            } else {
                // 전송되지 않음 표시
                ((sendVdoViewHolder) holder).not_sent.setVisibility(View.INVISIBLE);
                ((sendVdoViewHolder) holder).not_sent_x.setVisibility(View.INVISIBLE);
                ((sendVdoViewHolder) holder).playButton_s.setVisibility(View.VISIBLE);
                // 썸네일
                StorageReference imagesRef = FirebaseCloudStorage.Storage_img(동영상_uri);
                imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // 썸네일
                        Glide.with(holder.itemView).load(uri).into(((sendVdoViewHolder) holder).vdo);
                    }
                });
            }
            // 읽음 표시
            String unread_list = chatting_list.get(position).getUnread_list();
            if (unread_list.equals("")) ((sendVdoViewHolder) holder).readNum.setText("");
            else ((sendVdoViewHolder) holder).readNum.setText(String.valueOf(읽지않은사람수(unread_list)));
            // 시간
            ((sendVdoViewHolder) holder).created_at.setText(시간포맷(chatting_list.get(position).getMsg_created_at()));
        } else if (holder instanceof receiveVdoViewHolder) {
            //vdo 렌더링 로직
            String 동영상_uri = chatting_list.get(position).getMsg_uri();
            StorageReference imagesRef = FirebaseCloudStorage.Storage_img(동영상_uri);
            imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    // 썸네일
                    Glide.with(holder.itemView).load(uri).into(((receiveVdoViewHolder) holder).vdo);
                }
            });
            ((receiveVdoViewHolder) holder).vdoName.setText(chatting_list.get(position).getNickname());
            // 읽음 표시
            String unread_list = chatting_list.get(position).getUnread_list();
            if (unread_list.equals("")) ((receiveVdoViewHolder) holder).readNum.setText("");
            else
                ((receiveVdoViewHolder) holder).readNum.setText(String.valueOf(읽지않은사람수(unread_list)));
            // 시간
            ((receiveVdoViewHolder) holder).created_at.setText(시간포맷(chatting_list.get(position).getMsg_created_at()));
        } else if (holder instanceof loadingViewHolder) { // 이미지, 동영상 로딩

            String uri = chatting_list.get(position).getMsg_uri();
            int msg_type = chatting_list.get(position).getMsg_type();
            if (msg_type == 1) { // img
                Glide.with(holder.itemView).load(uri).into(((loadingViewHolder) holder).imageView);
            } else { // vdo
                ((loadingViewHolder) holder).imageView.setImageBitmap(createThumbnail(holder.itemView.getContext(), uri));
            }
        } else { // 채팅방퇴장, 채팅방최초입장
            String 닉네임 = chatting_list.get(position).getNickname();
            if (chatting_list.get(position).getTxt_contents().equals("채팅방퇴장")) {
                ((specialViewHolder) holder).txtSpec.setText(닉네임 + " 님이 나가셨습니다.");
            } else if (chatting_list.get(position).getTxt_contents().equals("채팅방최초입장")) {
                ((specialViewHolder) holder).txtSpec.setText(닉네임 + " 님이 들어오셨습니다.");
            } else ;
        }
    }

    // 작성여부의 true 갯수 만큼 view 만듦
    @Override
    public int getItemCount() {
        return chatting_list.size();
    }

    @Override
    public int getItemViewType(int position) {
        return chatting_list.get(position).getViewType();
    }


    //    ----------------뷰홀더-------------------

    /**
     * text ---------------------------------------------------------
     **/
    public class sendTxtViewHolder extends RecyclerView.ViewHolder {
        TextView sendMsg, readNum, created_at;

        ImageView not_sent;

        public sendTxtViewHolder(@NonNull View itemView) {
            super(itemView);
            sendMsg = itemView.findViewById(R.id.txt_sendMessage_s);
            readNum = itemView.findViewById(R.id.txt_readNum_s);
            created_at = itemView.findViewById(R.id.txt_created_at_s);
            not_sent = itemView.findViewById(R.id.txt_not_sent);
        }
    }

    public class receiveTxtViewHolder extends RecyclerView.ViewHolder {

        TextView txtName, receiveMsg, readNum, created_at;

        public receiveTxtViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txt_name);
            receiveMsg = itemView.findViewById(R.id.txt_receiveMessage);
            readNum = itemView.findViewById(R.id.txt_readNum);
            created_at = itemView.findViewById(R.id.txt_created_at);
        }
    }

    /**
     * img ---------------------------------------------------------
     **/
    public class sendImgViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView readNum, created_at;
        ImageView not_sent, not_sent_x;

        public sendImgViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgView_s);
            readNum = itemView.findViewById(R.id.img_readNum_s);
            created_at = itemView.findViewById(R.id.img_created_at_s);
            not_sent = itemView.findViewById(R.id.img_not_sent);
            not_sent_x = itemView.findViewById(R.id.img_not_sent_x);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int posi = getAdapterPosition();
                    /** 게시물 식별번호 **/
                    String uri_path = chatting_list.get(posi).getMsg_uri();
                    int 채팅방_seq = chatting_list.get(posi).getChatRoom_seq();
                    int 채팅_seq = chatting_list.get(posi).getSeq();
                    // 전송되지 않은 상태면 클릭 안됨
                    if (chatting_list.get(posi).getViewType() != 12) {
                        if (posi != RecyclerView.NO_POSITION) {
                            if (mListener != null) {
                                mListener.onItemClick(v, IMG, uri_path, 채팅방_seq, 채팅_seq);
                            }
                        }
                    }
                }
            });
        }
    }

    public class receiveImgViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView imgName, readNum, created_at;

        public receiveImgViewHolder(@NonNull View itemView) {
            super(itemView);
            imgName = itemView.findViewById(R.id.img_name);
            img = itemView.findViewById(R.id.imgView);
            readNum = itemView.findViewById(R.id.img_readNum);
            created_at = itemView.findViewById(R.id.img_created_at);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int posi = getAdapterPosition();
                    /** 게시물 식별번호 **/
                    String uri_path = chatting_list.get(posi).getMsg_uri();
                    int 채팅방_seq = chatting_list.get(posi).getChatRoom_seq();
                    int 채팅_seq = chatting_list.get(posi).getSeq();
                    if (posi != RecyclerView.NO_POSITION) {
                        if (mListener != null) {
                            mListener.onItemClick(v, IMG, uri_path, 채팅방_seq, 채팅_seq);
                        }
                    }
                }
            });
        }
    }

    /**
     * Video ---------------------------------------------------------
     **/
    public class sendVdoViewHolder extends RecyclerView.ViewHolder {
        ImageView vdo;
        TextView readNum, created_at;
        ImageView not_sent, not_sent_x, playButton_s;

        public sendVdoViewHolder(@NonNull View itemView) {
            super(itemView);
            vdo = itemView.findViewById(R.id.vdoView_s);
            readNum = itemView.findViewById(R.id.vdo_readNum_s);
            created_at = itemView.findViewById(R.id.vdo_created_at_s);

            playButton_s = itemView.findViewById(R.id.playButton_s);
            not_sent = itemView.findViewById(R.id.vdo_not_sent);
            not_sent_x = itemView.findViewById(R.id.vdo_not_sent_x);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int posi = getAdapterPosition();
                    /** 게시물 식별번호 **/
                    String uri_path = chatting_list.get(posi).getMsg_uri();
                    int 채팅방_seq = chatting_list.get(posi).getChatRoom_seq();
                    int 채팅_seq = chatting_list.get(posi).getSeq();
                    // 전송되지 않은 상태면 클릭 안됨
                    if (chatting_list.get(posi).getViewType() != 14) {
                        if (posi != RecyclerView.NO_POSITION) {
                            if (mListener != null) {
                                mListener.onItemClick(v, VDO, uri_path, 채팅방_seq, 채팅_seq);
                            }
                        }
                    }
                }
            });
        }
    }

    public class receiveVdoViewHolder extends RecyclerView.ViewHolder {

        ImageView vdo;
        TextView vdoName, readNum, created_at;

        public receiveVdoViewHolder(@NonNull View itemView) {
            super(itemView);
            vdoName = itemView.findViewById(R.id.vdo_name);
            vdo = itemView.findViewById(R.id.vdoView);
            readNum = itemView.findViewById(R.id.vdo_readNum);
            created_at = itemView.findViewById(R.id.vdo_created_at);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int posi = getAdapterPosition();
                    /** 게시물 식별번호 **/
                    String uri_path = chatting_list.get(posi).getMsg_uri();
                    int 채팅방_seq = chatting_list.get(posi).getChatRoom_seq();
                    int 채팅_seq = chatting_list.get(posi).getSeq();
                    if (posi != RecyclerView.NO_POSITION) {
                        if (mListener != null) {
                            mListener.onItemClick(v, VDO, uri_path, 채팅방_seq, 채팅_seq);
                        }
                    }
                }
            });
        }
    }

    public class loadingViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ProgressBar progressBar;

        public loadingViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgView_loading);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    /**
     * Special ( 채팅방퇴장, 채팅방최초입장 ) ---------------------------------------------------------
     **/
    public class specialViewHolder extends RecyclerView.ViewHolder {
        TextView txtSpec;

        public specialViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSpec = itemView.findViewById(R.id.text_for_special);
        }
    }

    /**
     * 메서드
     **/
    public void addChat(chatting chatting1) {
        chatting_list.add(0,chatting1);
        notifyItemInserted(0);
    }

    public void removeChat() { // 로딩 (이미지/ 동영상)
        chatting_list.remove(0);
        notifyItemRemoved(0);
    }

    public void updateChat(String 채팅방에있는멤버리스트_string) {

        채팅방에있는멤버리스트_string = 채팅방에있는멤버리스트_string.replace("\"","");
        //채팅방에있는멤버리스트 string to list
        String formattedStr = 채팅방에있는멤버리스트_string.replaceAll("\\[|\\]|\\s", "");
        String[] strArray = formattedStr.split(",");
        List<String> 채팅방에있는멤버리스트 = new ArrayList<>();
        for (String element : strArray) {
            채팅방에있는멤버리스트.add(element.trim());
        }


        // chatting_list 의 unread_list
        for (int i = 0; i < chatting_list.size(); i++) {
            // 이미지, 동영상 로딩이 아닐 때

            if (chatting_list.get(i).getViewType()!=6){
                String unread_list_string = chatting_list.get(i).getUnread_list(); // kyh1, kyh2
                // 리스트로 변환
                String[] unread_array = unread_list_string.split(","); // [kyh1, kyh2]
                List<String> unread_list = new ArrayList<>();
                for (String element : unread_array) {
                    unread_list.add(element.trim().replace("\"",""));
                }

                // unread_list - 채팅방에있는멤버리스트 = new_unread_list
                List<String> new_unread_list = new ArrayList<>(unread_list);
                new_unread_list.removeAll(채팅방에있는멤버리스트);

                chatting_list.get(i).setUnread_list(String.valueOf(new_unread_list).replace("[", "").replace("]", "").replace(" ",""));

            }

        }
        notifyDataSetChanged();

    }

    /**
     * 동영상에서 썸네일 추출
     **/
    public static Bitmap createThumbnail(Context activity, String path) {
        MediaMetadataRetriever mediaMetadataRetriever = null;
        Bitmap bitmap = null;

        mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(activity, Uri.parse((path)));
        bitmap = mediaMetadataRetriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);

        if (mediaMetadataRetriever != null) {
            try {
                mediaMetadataRetriever.release();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return bitmap;
    }


    /**
     * 읽지않은사람수
     **/
    private int 읽지않은사람수(String id_list) {
        if (id_list == null) return 0;
        else if (id_list.equals("[]")) return 0;
        else {
            List<String> list = Arrays.asList(id_list.split(","));
            return list.size();
        }
    }

}

