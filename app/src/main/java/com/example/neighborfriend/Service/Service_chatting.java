package com.example.neighborfriend.Service;

import static com.example.neighborfriend.MainActivity.retrofitAPI;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.neighborfriend.Activity_band_chatting_room;
import com.example.neighborfriend.Class.RetrofitClass;
import com.example.neighborfriend.Interface.RetrofitAPI;
import com.example.neighborfriend.R;
import com.example.neighborfriend.object.chattingRoom;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Service_chatting extends Service {
    private static final String SERVER_HOST = "192.168.1.48";
//    private static final String SERVER_HOST = "192.168.0.3";
    private static final int SERVER_PORT = 8888;
    /**
     * 알림
     **/
    private static final String CHANNEL_ID = "CHANNEL_ID";
    private NotificationManager mNotificationManager;
    private static final int NOTIFICATION_ID = 0;
    /**
     * 쓰레드
     **/
    ReceiveThread receiveThread;
    private final IBinder binder = new ChattingBinder();
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public class ChattingBinder extends Binder {
        public Service_chatting getService() {
            return Service_chatting.this;
        }
    }

    /**
     * 생명주기
     **/
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(this, "서비스 onbind", Toast.LENGTH_SHORT).show();

        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 서비스 start 될 때 (한번) 활성화
        if (intent != null && intent.getExtras() != null) {
            // 받은 아이디, 이름
            String current_user_id = intent.getStringExtra("id");
            String current_user_name = intent.getStringExtra("nickname");
            // 소켓 쓰레드 생성 및 실행
            receiveThread = new ReceiveThread(current_user_id, current_user_name);
            receiveThread.start();
        }

        Toast.makeText(this, "서비스 startCommand", Toast.LENGTH_SHORT).show();
        System.out.println("서비스 startCommand");
        // 강제로 종료 시 : Service가 재시작 하지 않는다.
        return START_NOT_STICKY;
        // 강제로 종료 시 : Service가 재시작 / intent 값을 null로 초기화 -> START_STICKY
        // 강제로 종료 시 : Service가 재시작 / intent 값 유지 -> START_REDELIVER_INTENT

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "서비스 destroy", Toast.LENGTH_SHORT).show();
        System.out.println("서비스 destroy");
        closeConnection();
    }

    /**
     * 메서드
     **/
    public void sendMessage_Service(int 밴드_seq, int 채팅방_seq, String current_user_id, String current_user_name, int msg_type, String content, String path) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonMessage = new JSONObject();
                    jsonMessage.put("band_seq", 밴드_seq);
                    jsonMessage.put("chatRoom_seq", 채팅방_seq);
                    jsonMessage.put("user_id", current_user_id);
                    jsonMessage.put("nickname", current_user_name);
                    if (content != null) jsonMessage.put("txt_contents", content.trim());
                    jsonMessage.put("msg_uri", path);
                    jsonMessage.put("msg_type", msg_type);

                    // 현재 날짜 구하기
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        String formatedNow = now.format(formatter);

                        jsonMessage.put("msg_created_at", formatedNow);
                    }

                    out.println(jsonMessage.toString());

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 쓰레드
     **/
    class ReceiveThread extends Thread {

        String current_user_id, current_user_name;

        public ReceiveThread(String current_user_id, String current_user_name) {
            this.current_user_id = current_user_id;
            this.current_user_name = current_user_name;
        }

        @Override
        public void run() {
            try {
                // 서버의 ip, port 로 소켓 연결 시도
                socket = new Socket(SERVER_HOST, SERVER_PORT);
                // 쉽게 datastream 을 받고, 전송 할 수 있는 class
                // 자동으로 flush 처리 (flush : buffer 에 남아있는 부분을 전부 보내는 역할)
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // 아이디, 이름 전송
                out.println(current_user_id);
                out.println(current_user_name);


                String inputMsg;
                while ((inputMsg = in.readLine()) != null && !inputMsg.isEmpty()) {

                    /** 알림 **/
                    JSONObject jsonMessage = null;
                    try {
                        jsonMessage = new JSONObject(inputMsg);
                        int band_seq = Integer.parseInt(jsonMessage.optString("band_seq"));
                        String chatRoom_seq = jsonMessage.optString("chatRoom_seq");
                        String user_id = jsonMessage.optString("user_id");
                        String nickname = jsonMessage.optString("nickname");
                        String txt_contents = jsonMessage.optString("txt_contents");
                        String msg_uri = jsonMessage.optString("msg_uri");
                        int msg_type = Integer.parseInt(jsonMessage.optString("msg_type"));
                        String msg_created_at = jsonMessage.optString("msg_created_at");
                        String 채팅방에있는멤버리스트 = jsonMessage.optString("채팅방에있는멤버리스트");
                        String 채팅방에없는멤버리스트 = jsonMessage.optString("채팅방에없는멤버리스트");  // ["kyh2","kyh1"]


                        // 상대방에게만 알림
                        if (!current_user_id.equals(user_id)) {
                            // text, img, video 만
                            if (msg_type == 0 || msg_type == 1 || msg_type == 2) {
                                // 채팅방에없는멤버에게만 전송
                                if (채팅방에없는멤버리스트.contains(current_user_id)) {
                                    Retrofit채팅방정보가져오기(Integer.valueOf(chatRoom_seq), current_user_id, txt_contents);
                                }
                            }
                        }


                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }


                    /** 브로드캐스트 리시버 전송 **/
                    // 채팅방, 채팅목록, 나의 채팅목록
                    Intent intent = new Intent("채팅");
                    intent.putExtra("message", inputMsg);
                    sendBroadcast(intent);

                }


            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }


    /**
     * Http 통신
     **/
    private void Retrofit채팅방정보가져오기(int chatRoom_seq_get, String user_id, String msg) {
        /***********************  채팅방 정보 가져오기  ***************************/
        retrofitAPI = RetrofitClass.getApiClient().create(RetrofitAPI.class);
        Call<chattingRoom> call1 = retrofitAPI.getChatRoom(chatRoom_seq_get,user_id);
        call1.enqueue(new Callback<chattingRoom>() {
            @Override
            public void onResponse(@NotNull Call<chattingRoom> call, @NotNull Response<chattingRoom> response) {
                // 서버에서 응답을 받아옴
                if (response.isSuccessful() && response.body() != null) {
                    chattingRoom chattingRoom = response.body();

                    메세지알림(chattingRoom, msg);
                } else {
                    System.out.println("Service 실패");
                }
            }

            // 통신실패시
            @Override
            public void onFailure(@NonNull Call<chattingRoom> call, @NonNull Throwable t) {
                Log.i("Service_chatting_err", t.getLocalizedMessage());
            }
        });
    }

    /**
     * 알림
     **/
    private void 메세지알림(chattingRoom chattingRoom, String message) {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // 기기(device)의 SDK 버전 확인 ( SDK 26 버전 이상인지 - VERSION_CODES.O = 26)
        if (android.os.Build.VERSION.SDK_INT
                >= android.os.Build.VERSION_CODES.O) {
            //Channel 정의 생성자( construct 이용 )
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Test Notification", mNotificationManager.IMPORTANCE_HIGH);
            //Channel에 대한 기본 설정
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("Notification from Mascot");
            // Manager을 이용하여 Channel 생성
            mNotificationManager.createNotificationChannel(notificationChannel);
        }

        Intent notificationIntent = new Intent(this, Activity_band_chatting_room.class);

        notificationIntent.putExtra("방개설자_id", chattingRoom.getUser_id());
        notificationIntent.putExtra("밴드번호", chattingRoom.getBand_seq());
        notificationIntent.putExtra("채팅방_seq", chattingRoom.getSeq());
        notificationIntent.putExtra("thumnail_uri", chattingRoom.getThumnail());
        notificationIntent.putExtra("title", chattingRoom.getTitle());
        notificationIntent.putExtra("intro", chattingRoom.getIntroduction());
        notificationIntent.putExtra("isMine", chattingRoom.getIsMine());

        System.out.println(chattingRoom.getUser_id());
        System.out.println(chattingRoom.getBand_seq());
        System.out.println(chattingRoom.getSeq());
        System.out.println(chattingRoom.getThumnail());
        System.out.println(chattingRoom.getTitle());
        System.out.println(chattingRoom.getIntroduction());
        System.out.println(chattingRoom.getIsMine());

        PendingIntent notificationPendingIntent = PendingIntent.getActivity(this, NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(chattingRoom.getTitle())
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true); //notification을 탭 했을경우 notification을 없앤다.

        mNotificationManager.notify(NOTIFICATION_ID, notifyBuilder.build());
    }
}