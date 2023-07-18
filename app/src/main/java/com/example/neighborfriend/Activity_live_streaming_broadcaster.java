package com.example.neighborfriend;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.neighborfriend.databinding.ActivityLiveStreamingBroadcasterBinding;

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;

import org.webrtc.AudioDecoderFactoryFactory;
import org.webrtc.AudioEncoderFactoryFactory;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SoftwareVideoDecoderFactory;
import org.webrtc.SoftwareVideoEncoderFactory;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.voiceengine.WebRtcAudioRecord;


public class Activity_live_streaming_broadcaster extends AppCompatActivity {
    private ActivityLiveStreamingBroadcasterBinding binding;
    /**
     * SharedPreferences
     **/
    SharedPreferences userData;
    private static final String SERVER_URL = "http://43.200.4.212:4000";
    private ScrollView scrollview;
    private TextView textView;
    private SurfaceViewRenderer renderer;
    private EditText editMsg;
    private ImageView btnX, btnSwitchCamera, btnSend;

    private EglBase eglBase;

    private PeerConnectionFactory peerConnectionFactory;
    private Socket socket;
    private List<PeerConnection.IceServer> iceServers;
    private HashMap<String, PeerConnection> peerConnections;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};
    private DataChannel dataChannel;
    private MediaStream mediaStream;


    private String current_user_id, current_user_name;
    private boolean camera_front_back= true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLiveStreamingBroadcasterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeView();
        initializeProperty();

        // 나가기
        btnX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {finish();}
        });
        // 카메라 전환
        btnSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera_front_back = !camera_front_back;
                for (PeerConnection peerConnection : peerConnections.values()) {
                    // stream 제거
                    peerConnection.removeStream(mediaStream);
                    // mediastream
                    mediaStream=null;
                    // renderer (출력 view) -> 제거
                    renderer.release();
                    // 카메라 stream 재생성
                    // renderer 초기화 및 addSink
                    initialize_camera(camera_front_back);
                    peerConnection.addStream(mediaStream);
                }
            }
        });
        // datachannel 채팅
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

    /**
     * initial
     **/
    private void initializeView() {
        /** binding **/
        btnX = binding.btnXLiveStreaming;
        btnSwitchCamera = binding.btnSwitchCameraLiveStreaming;

        renderer = binding.surfaceViewBroadcaster;

        scrollview = binding.scrollLiveStreaming;
        textView = binding.textviewLiveStreaming;

        btnSend = binding.btnSendLiveStreaming;
        editMsg = binding.editMessageLiveStreaming;
    }

    private void initializeProperty() {
        /**  SharedPreferences **/
        userData = getSharedPreferences("user", MODE_PRIVATE); // sharedpreference
        // 로그인 한 user id,name
        current_user_id = userData.getString("id", "noneId");
        current_user_name = userData.getString("nickname", "noneNickname");

        /** webrtc **/
        peerConnections = new HashMap<>();

        // stun server / turn server
        PeerConnection.IceServer stun = PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer();
        PeerConnection.IceServer turn = PeerConnection.IceServer.builder("turn:43.200.4.212").setUsername("kyh").setPassword("kyh123").createIceServer();

        iceServers = new ArrayList<>();
//        iceServers.add(stun);
        iceServers.add(turn);

        // 권한
        if (checkPermissions()) {
            // PeerConnectionFactory 설정 (video track..)
            initialize();
            initialize_camera(true);
            // socket.io 연결
            try {
                socket = IO.socket(SERVER_URL);
                socket.connect();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            socket.emit("broadcaster");

            /** watcher **/
            socket.on("watcher", args -> {
                // watcher 의 id
                String id = (String) args[0];

                // PeerConnection 생성 및 addTrack
                PeerConnection peerConnection = createPeerConnection(socket, id);

                // offer 생성 및 전송 및 local description 설정
                createAndSetLocalDescription(id, peerConnection);
                // peerConnection 추가
                peerConnections.put(id, peerConnection);

            });


            /** Candidate **/
            socket.on("candidate", args -> {
                // broadcaster id
                String id = (String) args[0];
                // candidate
                String sdpMid = (String) args[1];
                Integer sdpMLineIndex = (Integer) args[2];
                String sdp = (String) args[3];

//                        System.out.println(sdpMid);
//                        System.out.println(sdpMLineIndex);
//                        System.out.println(sdp);
                // peerconnection 객체 가져오기
                PeerConnection peerConnection = peerConnections.get(id);

                // candidate 생성
                IceCandidate candidate = new IceCandidate(sdpMid, sdpMLineIndex, sdp);
                // candidate 추가
                peerConnection.addIceCandidate(candidate);
            });

            /** Answer **/
            socket.on("answer", args -> {
                // watcher id
                String id = (String) args[0];
                // answer description from watcher
                String description = (String) args[1];

//                        System.out.println("answer description" + description);

                // peerconnection 가져오기
                PeerConnection peerConnection = peerConnections.get(id);

                if (peerConnection != null) {
                    // description 으로 SDP answer 생성
                    SessionDescription remoteSdp = new SessionDescription(SessionDescription.Type.ANSWER, description);
                    // 1. setRemoteDescription
                    peerConnection.setRemoteDescription(new SimpleSdpObserver(), remoteSdp);
                }
            });


            /** disconnectPeer **/
            socket.on("disconnectPeer", args -> {
                String id = (String) args[0];

                // Find the corresponding PeerConnection in the HashMap and close it
                PeerConnection peerConnection = peerConnections.get(id);

                if (peerConnection != null) {
                    peerConnection.close();
                    peerConnections.remove(id);
                }
            });


//                    getVideoTrack();
        } else {
            requestPermissions();
        }
    }

    private void initialize() {
        /** PeerConnectionFactory **/
        // 로컬 미디어 스트림 생성
        // 스트림에 트랙 추가
        // remote peer 에 연결
        // peer 연결 상태, 미디어 스트림 이벤트 처리 기능
        PeerConnectionFactory.initialize(PeerConnectionFactory
                .InitializationOptions
                .builder(this)
                .createInitializationOptions());
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();

        // 인코더, 디코더 생성
        VideoEncoderFactory encoderFactory;
        VideoDecoderFactory decoderFactory;
        encoderFactory = new SoftwareVideoEncoderFactory();
        decoderFactory = new SoftwareVideoDecoderFactory();

        // peerConnectionFactory 초기화 및 생성
        peerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();
    }

    private void initialize_camera(boolean camera_front_back){
        /** 비디오 설정 **/
        // videoCapturer
        // 내 디바이스의 카메라에서 비디오 프레임 캡쳐
        // true : 전면,  false : 후면
        VideoCapturer videoCapturer = createCameraCapturer(camera_front_back);

        // EGL(OpenGL ES) : 렌더링 api 와 window 시스템을 연결해주는 인터페이스
        eglBase = EglBase.create();

        // surfaceTextureHelper : WebRTC VideoFrames 를 생성을 위한 클래스
        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.getEglBaseContext());
        // VideoCapturer 로 부터 VideoSource 를 생성
        VideoSource videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast());
        videoCapturer.initialize(surfaceTextureHelper, this, videoSource.getCapturerObserver());
        // 카메라로부터 비디오 캡쳐를 시작한다.
        videoCapturer.startCapture(240, 320, 30);  // width,height and fps

        // VideoSource 로 부터 VideoTrack 을 생성
        VideoTrack localVideoTrack = peerConnectionFactory.createVideoTrack("100", videoSource);;
        /****/

        /** 오디오 설정 **/


        MediaConstraints mediaConstraints = new MediaConstraints();
        mediaConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
        mediaConstraints.optional.add(new MediaConstraints.KeyValuePair("googEchoCancellation", "true"));
        mediaConstraints.optional.add(new MediaConstraints.KeyValuePair("googAutoGainControl", "true"));
        mediaConstraints.optional.add(new MediaConstraints.KeyValuePair("googNoiseSuppression", "true"));
        mediaConstraints.optional.add(new MediaConstraints.KeyValuePair("googHighpassFilter", "true"));

        // AudioSource 및 AudioTrack 생성
        AudioSource audioSource = peerConnectionFactory.createAudioSource(mediaConstraints);
        AudioTrack localAudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource);
//        localAudioTrack.setVolume(5); // audio volume
        localAudioTrack.setEnabled(true); // 오디오 트랙 활성화
        /****/

        /** mediaStream **/
        mediaStream = peerConnectionFactory.createLocalMediaStream("mediaStream");
        mediaStream.addTrack(localAudioTrack);
        mediaStream.addTrack(localVideoTrack);
        /****/

        /** 비디오 띄우기 **/
        // renderer 이니셜라이즈
        renderer.init(eglBase.getEglBaseContext(), null);

        // 비디오 트랙에 추가
        localVideoTrack.addSink(renderer);
        renderer.setMirror(true);
        /****/
    }

    /** PeerConnection 생성 **/
    private PeerConnection createPeerConnection(Socket socket, String id) {
        // PeerConnection 생성
        PeerConnection peerConnection = peerConnectionFactory.createPeerConnection(iceServers, new PeerConnectionAdapter() {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                // icecandidate 수집시 서버에 전송
                socket.emit("candidate", id, iceCandidate.sdpMid, iceCandidate.sdpMLineIndex, iceCandidate.sdp);
            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
//                System.out.println(iceConnectionState);
            }

        });

        // DataChannel 초기화
        dataChannel = peerConnection.createDataChannel("chat", new DataChannel.Init());
        dataChannel.registerObserver(dataChannelObserver);

        peerConnection.addStream(mediaStream);

        return peerConnection;
    }

    // Offer 생성 및 서버에 전송
    private void createAndSetLocalDescription(String id, PeerConnection peerConnection) {
        peerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                // setLocalDescription
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                // 서버에 만들어진 sdp 전송
                socket.emit("offer", id, sessionDescription.description);
//                System.out.println("description : "+sessionDescription.description);
            }
        }, new MediaConstraints());
    }


    /**
     * 생명주기
     **/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socket != null && socket.connected()) {
            socket.disconnect();
        }
    }

    /******************************************************************/
    // 간단한 SdpObserver 구현 클래스
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

    private static class PeerConnectionAdapter implements PeerConnection.Observer {
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

    /******************************************************************/

    /**
     * 비디오 트랙
     **/
    // video capturer 생성하기
    private VideoCapturer createCameraCapturer(boolean isFront) {

        Camera2Enumerator enumerator = new Camera2Enumerator(this);
        final String[] deviceNames = enumerator.getDeviceNames();

        for (String deviceName : deviceNames) {
            if (isFront ? enumerator.isFrontFacing(deviceName) : enumerator.isBackFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, new CameraVideoCapturer.CameraEventsHandler() {
                    @Override
                    public void onCameraError(String s) {
                        Log.w("onCameraError", s);
                    }

                    @Override
                    public void onCameraDisconnected() {
                        Log.w("onCameraDisconnected", "");
                    }

                    @Override
                    public void onCameraFreezed(String s) {
                        Log.w("onCameraFreezed", s);
                    }

                    @Override
                    public void onCameraOpening(String s) {
                        Log.w("onCameraOpening", s);
                    }

                    @Override
                    public void onFirstFrameAvailable() {
                        Log.w("onFirstFrameAvailable", "");
                    }

                    @Override
                    public void onCameraClosed() {
                        Log.w("onCameraClosed", "");
                    }
                });

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        return null;
    }

    /**
     * 권한
     **/
    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : REQUIRED_PERMISSIONS) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, CAMERA_PERMISSION_REQUEST_CODE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                initialize();
                initialize_camera(true);
            }
        }
    }
}