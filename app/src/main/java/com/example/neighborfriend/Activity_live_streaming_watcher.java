package com.example.neighborfriend;

import static com.example.neighborfriend.Activity_live_streaming_broadcaster.SERVER_URL;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.neighborfriend.databinding.ActivityLiveStreamingBroadcasterBinding;
import com.example.neighborfriend.databinding.ActivityLiveStreamingWatcherBinding;

import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpTransceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SoftwareVideoDecoderFactory;
import org.webrtc.SoftwareVideoEncoderFactory;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoTrack;

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class Activity_live_streaming_watcher extends AppCompatActivity {
    private ActivityLiveStreamingWatcherBinding binding;

    /**
     * SharedPreferences
     **/
    SharedPreferences userData;

    public static final String EVENT_OFFER = "offer";
    public static final String EVENT_ANSWER = "answer";
    public static final String EVENT_CANDIDATE = "candidate";
    public static final String EVENT_BROADCASTER = "broadcaster";
    public static final String EVENT_WATCHER = "watcher";
    public static final String EVENT_FINISH = "finish";
    public static final String EVENT_disconnect = "disconnectPeer";

    private Socket socket;

    // webrtc
    private List<PeerConnection.IceServer> iceServers;
    private PeerConnectionFactory peerConnectionFactory;
    private PeerConnection peerConnection;
    private EglBase eglBase;
    private MediaStream stream;
    private DataChannel.Buffer dataBuffer;


    // view
    private ScrollView scrollview;private TextView textView;private SurfaceViewRenderer renderer;
    private EditText editMsg;private ImageView btnX, btnSend;

    private String current_user_id, current_user_name;
    private int 밴드번호; private String 방송방_id; private String user_id;

    private boolean isRendererInitialized = false; private String 받는message, 보내는message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLiveStreamingWatcherBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeView();
        initializeProperty();
    }

    /**
     * initialize
     **/
    private void initializeView() {
        /** binding **/
        btnX = binding.btnXLiveStreamingWatcher;

        renderer = binding.surfaceViewWatcher;

        scrollview = binding.scrollLiveStreamingWatcher;
        textView = binding.textviewLiveStreamingWatcher;

        btnSend = binding.btnSendLiveStreamingWatcher;
        editMsg = binding.editMessageLiveStreamingWatcher;
    }

    private void initializeProperty() {
        /** Intent from band **/
        Intent intent = getIntent();
        // 방송하는 밴드번호, 방송자 id
        밴드번호 = intent.getIntExtra("밴드번호", 0);
        user_id = intent.getStringExtra("id");
        // 방송방 식별자
        방송방_id = String.format("%d_%s", 밴드번호, user_id);
        /**  SharedPreferences **/
        userData = getSharedPreferences("user", MODE_PRIVATE); // sharedpreference
        // 로그인 한 user id, name
        current_user_id = userData.getString("id", "noneId");
        current_user_name = userData.getString("nickname", "noneNickname");


        /** webrtc **/
        try {
            socket = IO.socket(SERVER_URL); // 소켓 생성 및 연결
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        PeerConnection.IceServer stun = PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer();
        PeerConnection.IceServer turn = PeerConnection.IceServer.builder("turn:43.200.4.212").setUsername("kyh").setPassword("kyh123").createIceServer();

        iceServers = new ArrayList<>();
        iceServers.add(stun);
        iceServers.add(turn);

        initialize();
        // offer
        socket.on(EVENT_OFFER, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                // broadcaster 의 id
                String id = (String) args[0];
                // offer description
                String description = (String) args[1];
                // PeerConnection 생성
                createPeerConnection(id);

                // description 으로 SDP offer 생성
                SessionDescription offer = new SessionDescription(SessionDescription.Type.OFFER, String.valueOf(description));
                // 1. RemoteDescription 설정
                peerConnection.setRemoteDescription(new SimpleSdpObserver(), offer);

                // 2. answer 생성
                peerConnection.createAnswer(new SimpleSdpObserver() {
                    @Override
                    public void onCreateSuccess(SessionDescription sessionDescription) {
                        super.onCreateSuccess(sessionDescription);
                        // 3. LocalDescription 설정
                        peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                        // 4. 전송
                        socket.emit(EVENT_ANSWER, 방송방_id, id, sessionDescription.description);
                    }
                }, new MediaConstraints());

            }
        });
        // candidate
        socket.on(EVENT_CANDIDATE, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                // broadcaster id
                String id = (String) args[0];
                // candidate
                String sdpMid = (String) args[1];
                Integer sdpMLineIndex = (Integer) args[2];
                String sdp = (String) args[3];

                IceCandidate candidate = new IceCandidate(sdpMid, sdpMLineIndex, sdp);
                // candidate 추가
                peerConnection.addIceCandidate(candidate);
            }
        });
        socket.on(EVENT_disconnect, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                socket.disconnect();
            }
        });

        socket.emit(EVENT_WATCHER, 방송방_id);

        btnX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // watcher 종료한다고 알림
                socket.emit(EVENT_FINISH, 방송방_id);
                finish();
            }
        });

    }

    private void initialize() {
        // peerConnectionFactory 생성
        PeerConnectionFactory.initialize(PeerConnectionFactory
                .InitializationOptions
                .builder(this)
                .createInitializationOptions());
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();

        VideoEncoderFactory encoderFactory;
        VideoDecoderFactory decoderFactory;
        encoderFactory = new SoftwareVideoEncoderFactory();
        decoderFactory = new SoftwareVideoDecoderFactory();

        peerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();

        eglBase = EglBase.create();
    }


    /** wather 에서 peerconnection 생성 **/
    private void createPeerConnection(String id) {
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, new YourRtcObserver() {
            @Override
            public void onIceCandidate(IceCandidate candidate) {
                // icecandidate 수집시 서버에 전송
                socket.emit(EVENT_CANDIDATE, 방송방_id, id, candidate.sdpMid, candidate.sdpMLineIndex, candidate.sdp);
            }
            @Override
            public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
                super.onAddTrack(rtpReceiver, mediaStreams);
                System.out.println("onAddTrack");

                if(rtpReceiver.track().id().equals("video")){ // Video
                    VideoTrack remoteVideoTrack_f = (VideoTrack) rtpReceiver.track();

                    // 비디오 출력
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // 초기화
                            if(!isRendererInitialized){
                                renderer.init(eglBase.getEglBaseContext(), null);
                                isRendererInitialized = true;
                            }
                            // 비디오 track 추가
                            remoteVideoTrack_f.addSink(renderer);
                            renderer.setMirror(true); // 화면 좌우 대칭

                        }
                    });
                }else{ // Audio
                    // 오디오 enable
                    rtpReceiver.track().setEnabled(true);
                }
            }
            @Override
            public void onDataChannel(DataChannel dataChannel) {
                super.onDataChannel(dataChannel);

                /** 메세지 수신 **/
                dataChannel.registerObserver(new dataChannelObserver(){
                    @Override
                    public void onMessage(DataChannel.Buffer buffer) {
                        super.onMessage(buffer);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 받은 buffer data ( binary )
                                ByteBuffer data = buffer.data;
                                // ByteBuffer 에 남은 데이터 크기로 byte array 생성
                                byte[] messageBytes = new byte[data.remaining()];
                                // messageBytes 에 데이터 복사
                                data.get(messageBytes);
                                받는message = new String(messageBytes, StandardCharsets.UTF_8);

                                if(받는message.equals("STREAMING_FINISH")) {
                                    // 방송 종료!!
                                    Toast.makeText(Activity_live_streaming_watcher.this, "방송이 종료되었습니다.", Toast.LENGTH_SHORT).show();
                                    finish();
                                }else{

                                    System.out.println("받는message : "+ 받는message);
                                    // 나머지 message 는 채팅
                                    textView.append(받는message + "\n");
                                    // 가장 아래로 내리기
                                    scrollview.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            scrollview.fullScroll(ScrollView.FOCUS_DOWN);
                                        }
                                    });
                                }
                            }
                        });
                    }
                });

                /** 메세지 송신 **/
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnSend.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // 전송할 채팅 메세지
                                보내는message = current_user_name+" : "+editMsg.getText().toString();
                                if (!editMsg.getText().toString().equals("") && editMsg.getText().toString() != null) {
                                    // byte 로 변환
                                    byte[] msg = 보내는message.getBytes(StandardCharsets.UTF_8);
                                    // msg 크기 만큼의 buffer 생성
                                    ByteBuffer buffer = ByteBuffer.wrap(msg);
                                    // DataChannel.buffer 생성 (false : text 형식)
                                    dataBuffer = new DataChannel.Buffer(buffer, false);

                                    // DataChannel.buffer 전송
                                    boolean success = dataChannel.send(dataBuffer);
                                    // clear
                                    if(success) editMsg.getText().clear();
                                }
                            }
                        });
                    }
                });
            }
        });
    }


    /**
     *
     **/

    public class YourRtcObserver implements PeerConnection.Observer {
        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        }

        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {
        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
        }

        @Override
        public void onAddStream(MediaStream mediaStream) {
        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {
        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {
        }

        @Override
        public void onRenegotiationNeeded() {
        }

        @Override
        public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
        }

        @Override
        public void onTrack(RtpTransceiver transceiver) {
        }
    }


    private static class SimpleSdpObserver implements SdpObserver {
        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
        }

        @Override
        public void onSetSuccess() {
        }

        @Override
        public void onCreateFailure(String s) {
        }

        @Override
        public void onSetFailure(String s) {
        }
    }

    private static class dataChannelObserver implements DataChannel.Observer{
        @Override
        public void onBufferedAmountChange(long l) {        }
        @Override
        public void onStateChange() {        }
        @Override
        public void onMessage(DataChannel.Buffer buffer) {       }
    };

    /**
     * 생명주기
     **/
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (stream != null) stream.dispose();
        if (renderer != null) renderer.release();

        // Close the socket connection
        socket.disconnect();
        socket.close();

        // Close the PeerConnection
        if (peerConnection != null) {
            peerConnection.close();
        }
        if (eglBase != null) {
            eglBase.release();
        }
//        if (peerConnectionFactory != null) {
//            peerConnectionFactory.dispose();
//        }
    }
}