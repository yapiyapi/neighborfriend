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

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class Activity_live_streaming_watcher extends AppCompatActivity {
    private ActivityLiveStreamingWatcherBinding binding;

    /**
     * SharedPreferences
     **/
    SharedPreferences userData;

    private static final String EVENT_OFFER = "offer";
    private static final String EVENT_ANSWER = "answer";
    private static final String EVENT_CANDIDATE = "candidate";
    private static final String EVENT_CONNECT = "connect";
    private static final String EVENT_BROADCASTER = "broadcaster";
    private static final String EVENT_WATCHER = "watcher";

    private Socket socket;

    // webrtc
    private List<PeerConnection.IceServer> iceServers;
    private PeerConnectionFactory peerConnectionFactory;
    private PeerConnection peerConnection;
    private EglBase eglBase;
    private MediaStream stream;

    // view
    private ScrollView scrollview;private TextView textView;private SurfaceViewRenderer renderer;
    private EditText editMsg;private ImageView btnX, btnSend;

    private String current_user_id, current_user_name;
    private int 밴드번호; private String 방송방_id; private String user_id;

    private boolean isRendererInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLiveStreamingWatcherBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeView();
        initializeProperty();

        btnX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {finish();}
        });
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
            socket = IO.socket(SERVER_URL);
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
                // 1. setRemoteDescription
                peerConnection.setRemoteDescription(new SimpleSdpObserver(), offer);

                // 2. Create an answer
                peerConnection.createAnswer(new SimpleSdpObserver() {
                    @Override
                    public void onCreateSuccess(SessionDescription sessionDescription) {
                        super.onCreateSuccess(sessionDescription);
                        // 3. setLocalDescription
                        peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                        // 4. emit
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
        // connect
//        socket.on(EVENT_CONNECT, new Emitter.Listener() {
//            @Override
//            public void call(Object... args) {
//                socket.emit(EVENT_WATCHER, 방송방_id);
//            }
//        });
//        // broadcaster
//        socket.on(EVENT_BROADCASTER, new Emitter.Listener() {
//            @Override
//            public void call(Object... args) {
//                socket.emit(EVENT_WATCHER, 방송방_id);
//            }
//        });
        socket.emit(EVENT_WATCHER, 방송방_id);


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


    private void createPeerConnection(String id) {
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, new YourRtcObserver() {
            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                System.out.println(iceConnectionState);
            }

            @Override
            public void onIceCandidate(IceCandidate candidate) {
                // icecandidate 수집시 서버에 전송
                socket.emit("candidate", 방송방_id, id, candidate.sdpMid, candidate.sdpMLineIndex, candidate.sdp);
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                super.onAddStream(mediaStream);
                System.out.println("onAddStream");
            }

            @Override
            public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
                super.onAddTrack(rtpReceiver, mediaStreams);
                System.out.println("onAddTrack");

                if (mediaStreams != null && mediaStreams.length > 0) {
                    // Stream 에서 track 추출
                    stream = mediaStreams[0];
                    // videoTrack 추가
                    if (stream.videoTracks.size() == 1 && stream.audioTracks.size() == 1) {
                        VideoTrack remoteVideoTrack_f = stream.videoTracks.get(0);

                        // 오디오 enable
                        stream.audioTracks.get(0).setEnabled(true);
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
                    }
                }
            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {
                super.onDataChannel(dataChannel);
                dataChannel.registerObserver(dataChannelObserver);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnSend.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String message = current_user_name+" : "+editMsg.getText().toString();
                                if (!editMsg.getText().toString().equals("") && editMsg.getText().toString() != null) {
                                    ByteBuffer buffer = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));
                                    DataChannel.Buffer dataBuffer = new DataChannel.Buffer(buffer, false);
                                    // DataChannel.buffer 전송
                                    dataChannel.send(dataBuffer);
                                    // clear
                                    editMsg.getText().clear();
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

    DataChannel.Observer dataChannelObserver = new DataChannel.Observer() {
        @Override
        public void onBufferedAmountChange(long l) {
            System.out.println("onBufferedAmountChange_datachannel");
        }

        @Override
        public void onStateChange() {
            System.out.println("onStateChange_datachannel");
        }

        @Override
        public void onMessage(DataChannel.Buffer buffer) {
            // 상대방에게 메세지가 왔을 때
            System.out.println("onMessage_datachannel");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ByteBuffer data = buffer.data;
                    byte[] messageBytes = new byte[data.remaining()];
                    data.get(messageBytes);
                    String message = new String(messageBytes, StandardCharsets.UTF_8);
                    // 방송 종료!!
                    if(message.equals("STREAMING_FINISH")){
                        stream.dispose();
                        renderer.release();

                        finish();
                        Toast.makeText(Activity_live_streaming_watcher.this, "방송이 종료되었습니다.", Toast.LENGTH_SHORT).show();
                    }else{
                        // 나머지 message 는 채팅
                        textView.append(message + "\n");
                        System.out.println("hi");
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
    };

    /**
     * 생명주기
     **/
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Close the socket connection
        socket.disconnect();
        socket.close();

        // Close the PeerConnection
        if (peerConnection != null) {
            peerConnection.close();
        }
        if (peerConnectionFactory != null) {
            peerConnectionFactory.dispose();
        }
        if (eglBase != null) {
            eglBase.release();
        }
    }
}