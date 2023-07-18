package com.example.neighborfriend;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

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
    private static final String SERVER_URL = "http://43.200.4.212:4000";
    //    private static final String SERVER_URL = "https://43.200.4.212:4000";

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

    private List<PeerConnection.IceServer> iceServers;
    private PeerConnectionFactory peerConnectionFactory;
    private PeerConnection peerConnection;
    private EglBase eglBase;
    private ScrollView scrollview;
    private TextView textView;
    private SurfaceViewRenderer renderer;
    private EditText editMsg;
    private ImageView btnX, btnSwitchCamera, btnSend;

    private String current_user_id, current_user_name;

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
        /**  SharedPreferences **/
        userData = getSharedPreferences("user", MODE_PRIVATE); // sharedpreference
        // 로그인 한 user id,name
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
//        iceServers.add(stun);
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

//                System.out.println(description);
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
                        socket.emit(EVENT_ANSWER, id, sessionDescription.description);
//                        System.out.println("Description : "+ String.valueOf(sessionDescription.description));
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
        socket.on(EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                socket.emit(EVENT_WATCHER);
            }
        });
        // broadcaster
        socket.on(EVENT_BROADCASTER, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                socket.emit(EVENT_WATCHER);
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
                socket.emit("candidate", id, candidate.sdpMid, candidate.sdpMLineIndex, candidate.sdp);
            }

            @Override
            public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
                super.onAddTrack(rtpReceiver, mediaStreams);
                System.out.println("onAddTrack");
                if (mediaStreams != null && mediaStreams.length > 0) {
                    // Stream 에서 track 추출
                    MediaStream stream = mediaStreams[0];

//                    System.out.println(stream.videoTracks.size());
//                    System.out.println(stream.audioTracks.size());
                    // videoTrack 추가
                    if (stream.videoTracks.size() > 0 && stream.audioTracks.size() > 0) {
                        VideoTrack remoteVideoTrack = stream.videoTracks.get(0);
                        AudioTrack remoteAudioTrack = stream.audioTracks.get(0);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 초기화
                                renderer.init(eglBase.getEglBaseContext(), null);

                                // 비디오 track 추가
                                remoteVideoTrack.addSink(renderer);
                                renderer.setMirror(true); // 화면 좌우 대칭

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // 오디오 track 추가
                                        remoteAudioTrack.setEnabled(true);
                                    }
                                }).start();

                            }
                        });
                    }
                }
            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {
                dataChannel.registerObserver(dataChannelObserver);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // watcher 에서 채팅 전송
                        btnSend.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String message = current_user_name+" : "+editMsg.getText().toString();
                                if (!message.equals("") && message != null) {
                                    ByteBuffer buffer = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));
                                    DataChannel.Buffer dataBuffer = new DataChannel.Buffer(buffer, false);
                                    // DataChannel.buffer 전송
                                    dataChannel.send(dataBuffer);
                                    // clear
                                    editMsg.getText().clear();
                                    textView.append(message + "\n");
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
                    textView.append(message + "\n");
                    // 가장 아래로 내리기
                    scrollview.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollview.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    });
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