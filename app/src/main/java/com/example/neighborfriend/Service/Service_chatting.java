package com.example.neighborfriend.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.gson.JsonObject;

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
import java.util.List;

public class Service_chatting extends Service {
    private static final String SERVER_HOST = "192.168.1.48";
    private static final int SERVER_PORT = 8888;

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


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(this, "서비스 onbind", Toast.LENGTH_SHORT).show();

        return binder;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getExtras() != null) {
            String current_user_id = intent.getStringExtra("id");
            String current_user_name = intent.getStringExtra("nickname");

            receiveThread = new ReceiveThread(current_user_id, current_user_name);
            receiveThread.start();
        }

        Toast.makeText(this, "서비스 startCommand", Toast.LENGTH_SHORT).show();
        System.out.println("서비스 startCommand");
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "서비스 destroy", Toast.LENGTH_SHORT).show();
        System.out.println("서비스 destroy");
        closeConnection();
    }


    public void sendMessage(int 채팅방_seq, String current_user_id, String current_user_name, int msg_type, String content, String path) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonMessage = new JSONObject();
                    jsonMessage.put("chatRoom_seq", 채팅방_seq);
                    jsonMessage.put("user_id", current_user_id);
                    jsonMessage.put("nickname", current_user_name);
                    if(content!=null) jsonMessage.put("txt_contents", content.trim());
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

    class ReceiveThread extends Thread {

        String current_user_id, current_user_name;

        public ReceiveThread(String current_user_id, String current_user_name) {
            this.current_user_id = current_user_id;
            this.current_user_name = current_user_name;
        }

        @Override
        public void run() {
            try {
                socket = new Socket(SERVER_HOST, SERVER_PORT);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // 아이디, 이름 전송
                out.println(current_user_id);
                out.println(current_user_name);


                String inputMsg;
                while ((inputMsg = in.readLine()) != null && !inputMsg.isEmpty()) {

                    // 여기 두번 발생함 이유를 모르겠다.
//                    System.out.println(inputMsg);
//                    System.out.println("여기는 service 브로드");
                    // 브로드캐스트 리시버 전송
                    Intent intent = new Intent("채팅");
                    intent.putExtra("message", inputMsg);
                    sendBroadcast(intent);

                }


            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

}