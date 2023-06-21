package com.example.neighborfriend.Interface;

import com.example.neighborfriend.object.User;
import com.example.neighborfriend.object.band;
import com.example.neighborfriend.object.bands_post;
import com.example.neighborfriend.object.chatting;
import com.example.neighborfriend.object.chattingRoom;
import com.example.neighborfriend.object.memberToInvite;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RetrofitAPI {
    // MainActivity -----------------------------------------------------------
    // User data 가져오기
    @FormUrlEncoded
    @POST("main.php")
    Call<User> sendData(
            @Field("id") String id
    );
    // Fragment home -----------------------------------------------------------
    @FormUrlEncoded
    @POST("frag/homemyband.php")             // 읽기
    Call<ArrayList<band>> getMyBandData(
            @Field("id") String id
    );
    @FormUrlEncoded
    @POST("frag/homejoinband.php")           // 읽기
    Call<ArrayList<band>> getJoinBandData(
            @Field("id") String id
    );
    // Activity band  -----------------------------------------------------------
    @FormUrlEncoded
    @POST("band/create.php")                   // 생성
    Call<String> createBand(
            @Field("user_id") String user_id,
            @Field("제목") String 제목,
            @Field("썸네일") String 썸네일,
            @Field("내용") String 내용,
            @Field("카테고리") String 카테고리,
            @Field("공개여부") String 공개여부,
            @Field("나이제한_from") String 나이제한_from,
            @Field("나이제한_to") String 나이제한_to,
            @Field("성별제한") String 성별제한
    );

    @GET("band/read.php")                   // 읽기
    Call<band> getBand(
            @Query("seq") int seq
    );
    @FormUrlEncoded
    @PUT("band/update.php")           // 수정
    Call<String> updateBand(
            @Field("seq") String seq,
            @Field("title") String 제목,
            @Field("thumnail_url") String 이미지,
            @Field("contents") String 내용,
            @Field("category") String category,
            @Field("publicSet") String publicSet,
            @Field("old_limit_from") String old_limit_from,
            @Field("old_limit_to") String old_limit_to,
            @Field("sex_limit") String sex_limit
    );

    @DELETE("band/delete.php")           // 삭제
    Call<List> deleteBand(
            @Query("seq") String seq
    );


    // Activity band post  -----------------------------------------------------------

    @GET("band/post/read_post.php")                // 읽기 ( band_post )
    Call<bands_post> getBandPost(
            @Query("band_seq") int bandD_seq,
            @Query("seq") int seq
    );
    @FormUrlEncoded
    @POST("band/post/read_post_list.php")          // 읽기 ( band_post list )
    Call<ArrayList<bands_post>> getBandPostList(
            @Field("seq") int seq
    );


    @FormUrlEncoded
    @PUT("band/post/update.php")                // 수정
    Call<String> updateBandPost(
            @Field("band_seq") int band_seq,
            @Field("seq") int seq,
            @Field("image_uri") String image_uri,
            @Field("게시글") String 게시글
    );

    @DELETE("band/post/delete.php")                // 삭제
    Call<Integer> deleteBandPost(
            @Query("band_seq") int band_seq,
            @Query("seq") int seq
    );


    // Activity search  -----------------------------------------------------------
    @FormUrlEncoded
    @POST("search/search_band.php")                // 읽기 [제목]
    Call<ArrayList<band>> searchBand(
            @Field("title") String title
    );
    @FormUrlEncoded
    @POST("search/search_band_category.php")                // 읽기 [카테고리]
    Call<ArrayList<band>> searchBand_category(
            @Field("category") int category
    );

    // 채팅방   -----------------------------------------------------------
    @GET("chatting/read_chatRoom.php")                // 채팅방 리스트 가져오기 [밴드]
    Call<ArrayList<chattingRoom>> readChatRoom(
            @Query("band_seq") int band_seq,
            @Query("user_id") String user_id
    );

    @GET("chatting/read_chatRoom_my.php")                // 채팅방 리스트 가져오기 [나의 채팅]
    Call<ArrayList<chattingRoom>> readChatRoom_my(
            @Query("user_id") String user_id
    );

    @GET("chatting/read_chatRoom_member_list.php")                // 채팅방 멤버 list
    Call<List> read_chatRoom_member_list(
            @Query("chatRoom_seq") int chatRoom_seq
    );

    @FormUrlEncoded
    @POST("chatting/create_chatRoom.php")                // 공개채팅방 생성
    Call<String> createChatRoom(
            @Field("band_seq") int band_seq,
            @Field("user_id") String user_id,
            @Field("썸네일") String 썸네일,
            @Field("제목") String 제목,
            @Field("소개글") String 소개글,
            @Field("room_type") int room_type
    );
    @FormUrlEncoded
    @POST("chatting/create_chatRoom_private.php")                // 비공개채팅방 생성
    Call<Integer> createChatRoom_private(
            @Field("band_seq") int band_seq,
            @Field("user_id") String user_id,
            @Field("memberList[]") ArrayList<String> memberList,
            @Field("room_type") int room_type
    );

    @FormUrlEncoded
    @POST("chatting/create_member.php")                // 채팅방 멤버 추가
    Call<String> createMember(
            @Field("user_id") String user_id,
            @Field("chatRoom_seq") int chatRoom_seq
    );

    @FormUrlEncoded
    @PUT("chatting/update_chatRoom.php")                // 수정
    Call<String> updateChatRoom(
            @Field("band_seq") int band_seq,
            @Field("채팅방_seq") int 채팅방_seq,
            @Field("제목") String 제목,
            @Field("소개글") String 소개글
    );

    @DELETE("chatting/delete_chatRoom.php")                // 삭제
    Call<List<String>> deleteChatRoom(
            @Query("band_seq") int band_seq,
            @Query("채팅방_seq") int 채팅방_seq
    );

    @DELETE("chatting/delete_member.php")                // 채팅방 멤버 삭제
    Call<Integer> deleteMember(
            @Query("user_id") String user_id,
            @Query("chatRoom_seq") int chatRoom_seq
    );


    // 채팅   -----------------------------------------------------------
    @GET("chatting/chat/read.php")                // 읽기
    Call<ArrayList<chatting>> readChat(
            @Query("chatRoom_seq") int chatRoom_seq,
            @Query("user_id") String user_id,
            @Query("page") int page,
            @Query("limit") int limit
    );


    @FormUrlEncoded
    @POST("chatting/chat/create.php")                // 생성
    Call<String> createChat(
            @Field("chatRoom_seq") int chatRoom_seq,
            @Field("user_id") String user_id,
            @Field("txt_contents") String txt_contents,
            @Field("msg_uri") String msg_uri,
            @Field("msg_type") int msg_type
    );

    @FormUrlEncoded
    @PUT("chatting/chat/update_unread_list.php")                // 수정
    Call<String> update_unread_list(
            @Field("user_id") String user_id,
            @Field("current_user_id") String current_user_id,
            @Field("chatRoom_seq") int chatRoom_seq
    );

    // 채팅방 초대 --------------------------------------------------------
    @GET("chatting/get_memberList.php")                // 밴드 멤버 가져오기
    Call<ArrayList<memberToInvite>> getMemberToInvite(
            @Query("id") String id,
            @Query("band_seq") int band_seq
    );

    // 서비스 -------------------------------------------------------------
    @GET("service/get_chatRoom.php")                // 채팅방 정보 가져오기
    Call<chattingRoom> getChatRoom(
            @Query("chatRoom_seq") int chatRoom_seq,
            @Query("user_id") String user_id
    );
}
