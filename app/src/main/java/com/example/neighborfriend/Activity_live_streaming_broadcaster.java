package com.example.neighborfriend;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.neighborfriend.databinding.ActivityLiveStreamingBroadcasterBinding;
import com.kakao.sdk.user.UserApiClient;

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.socket.client.IO;
import io.socket.client.Socket;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

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
    public static final String SERVER_URL = "http://43.200.4.212:4000";

    // webrtc
    private EglBase eglBase;
    private PeerConnectionFactory peerConnectionFactory;
    private Socket socket;
    private List<PeerConnection.IceServer> iceServers;
    private HashMap<String, PeerConnection> peerConnections;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    private ArrayList<DataChannel> dataChannels;

    private MediaStream mediaStream;
    // 비디오
    private VideoTrack localVideoTrack; private VideoCapturer videoCapturer; private VideoSource videoSource;
    // 오디오
    private AudioSource audioSource; private AudioTrack localAudioTrack;
    private SurfaceTextureHelper surfaceTextureHelper;

    // view
    private ScrollView scrollview; private TextView textView; private SurfaceViewRenderer renderer;
    private EditText editMsg; private ImageView btnX, btnMic, btnSwitchCamera, btnSend;

    private String current_user_id, current_user_name;
    private boolean camera_front_back= false;

    private int 밴드번호; private String 방송방_id; private String 보내는message;

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
            public void onClick(View v) {
                // 방송 종료 버튼
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("방송종료"); //AlertDialog의 제목 부분
                builder.setMessage("정말로 방송을 종료 하시겠습니까?"); //AlertDialog의 내용 부분
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Socket.io
                        if (socket != null && socket.connected()) {
                            socket.disconnect();
                        }
                        // Camera
                        releaseCamera();
                        finish();
                        // 다른 Peer 에게 '방송 종료' 알림
                        for(DataChannel dataChannel1 : dataChannels){
                            보내는message = "STREAMING_FINISH";
                            // utf-8 포맷 및 buffer 생성
                            ByteBuffer buffer = ByteBuffer.wrap(보내는message.getBytes(StandardCharsets.UTF_8));
                            DataChannel.Buffer dataBuffer = new DataChannel.Buffer(buffer, false);

                            // buffer 보내고 나면 초기화 됨
                            // for 문 돌때마다 계속 생성해줘야 함
                            dataChannel1.send(dataBuffer);
                        }
                    }
                });
                builder.setNegativeButton("아니오", null);
                builder.create().show(); //보이기
            }
        });
        // 마이크 설정
        btnMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(localAudioTrack.enabled()){
                    // 오디오 track 설정 및 리소스 변경
                    localAudioTrack.setEnabled(false);
                    btnMic.setImageResource(R.drawable.mic_off);
                }else{
                    localAudioTrack.setEnabled(true);
                    btnMic.setImageResource(R.drawable.mic_on);
                }
            }
        });
        // 카메라 전환
        btnSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCammera(camera_front_back);
                camera_front_back = !camera_front_back;
            }
        });
        // datachannel 채팅
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                보내는message = current_user_name+" : "+editMsg.getText().toString();
                // 비었는지 확인
                if (!editMsg.getText().toString().equals("") && editMsg.getText().toString() != null) {
                    // 모든 Peer 에게 전송
                    for(DataChannel dataChannel1 : dataChannels){
                        // utf-8 포맷 및 buffer 생성
                        // buffer 보내고 나면 초기화 됨 -> for 문 내부
                        ByteBuffer buffer = ByteBuffer.wrap(보내는message.getBytes(StandardCharsets.UTF_8));
                        DataChannel.Buffer dataBuffer = new DataChannel.Buffer(buffer, false);

                        // for 문 돌때마다 계속 생성해줘야 함
                        dataChannel1.send(dataBuffer);
                    }
                    // clear
                    editMsg.getText().clear();
                    textView.append(보내는message + "\n");
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


        // 화면 꺼졌을 때
        IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(screenStateReceiver, screenStateFilter);

        // onbackpress
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                AlertDialog.Builder builder = new AlertDialog.Builder(Activity_live_streaming_broadcaster.this);
                builder.setTitle("방송종료"); //AlertDialog의 제목 부분
                builder.setMessage("정말로 방송을 종료 하시겠습니까?"); //AlertDialog의 내용 부분
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Socket.io
                        if (socket != null && socket.connected()) {
                            socket.disconnect();
                        }
                        // Camera
                        releaseCamera();
                        finish();
                    }
                });
                builder.setNegativeButton("아니오", null);
                builder.create().show(); //보이기
            }
        });
    }
    /**
     * initial
     **/
    private void initializeView() {
        /** binding **/
        btnX = binding.btnXLiveStreaming;
        btnMic = binding.btnMicLiveStreaming;
        btnSwitchCamera = binding.btnSwitchCameraLiveStreaming;

        renderer = binding.surfaceViewBroadcaster;

        scrollview = binding.scrollLiveStreaming;
        textView = binding.textviewLiveStreaming;

        btnSend = binding.btnSendLiveStreaming;
        editMsg = binding.editMessageLiveStreaming;
    }
    private void initializeProperty() {
        /** Intent **/
        Intent intent = getIntent();
        밴드번호 = intent.getIntExtra("밴드번호", 0);
        /**  SharedPreferences **/
        userData = getSharedPreferences("user", MODE_PRIVATE); // sharedpreference
        // 로그인 한 user id, name
        current_user_id = userData.getString("id", "noneId");
        current_user_name = userData.getString("nickname", "noneNickname");

        // 방송방 식별자
        방송방_id = String.format("%d_%s", 밴드번호, current_user_id);

        /** webrtc **/
        peerConnections = new HashMap<String, PeerConnection>();
        dataChannels = new ArrayList<DataChannel>();

        // stun server / turn server
        PeerConnection.IceServer stun = PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer();
        PeerConnection.IceServer turn = PeerConnection.IceServer.builder("turn:43.200.4.212").setUsername("kyh").setPassword("kyh123").createIceServer();

        iceServers = new ArrayList<>();
        iceServers.add(stun);
        iceServers.add(turn);

        // 권한
        if (checkPermissions()) {
            socketIO();
        } else {
            requestPermissions();
        }
    }
    private void socketIO(){
        // PeerConnectionFactory 설정 (video track..)
        initializePeerConnection();
        initialize_camera();
        // socket.io 연결
        try {
            socket = IO.socket(SERVER_URL);
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        socket.emit("broadcaster", 방송방_id);
        /** watcher **/
        socket.on("watcher", args -> {
            // watcher 의 id
            String watcher_id = (String) args[0];
            // PeerConnection 생성 및 addTrack
            PeerConnection peerConnection = createPeerConnection(socket, watcher_id);

            // offer 생성 및 전송 및 local description 설정
            createAndSetLocalDescription(watcher_id, peerConnection);
            // peerConnection 추가
            peerConnections.put(watcher_id, peerConnection);
            
        });
        /** Candidate **/
        socket.on("candidate", args -> {
            // broadcaster id
            String id = (String) args[0];
            // candidate
            String sdpMid = (String) args[1];
            Integer sdpMLineIndex = (Integer) args[2];
            String sdp = (String) args[3];
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
            // 모든 peerconnection 종료 및 제거
            for(PeerConnection peerConnection : peerConnections.values()){
                peerConnection.close();
            }
            peerConnections.clear();
        });
        /** finish **/
        socket.on("finish", args -> {
            // watcher id
            String id = (String) args[0];
            // 모든 peerconnection 종료 및 제거
            for(DataChannel dataChannel : dataChannels){
                if(dataChannel.label().equals(id)){
                    dataChannels.remove(dataChannel);
                    break;
                }
            }
        });
    }
    private void initializePeerConnection() {
        /** PeerConnectionFactory 초기화 **/
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
    private void initialize_camera(){
        /** 비디오 설정 **/
        // EGL(OpenGL ES) : 렌더링 api 와 window 시스템을 연결해주는 인터페이스
        eglBase = EglBase.create();
        // surfaceTextureHelper : WebRTC VideoFrames 를 생성을 위한 클래스
        surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.getEglBaseContext());
        // videoCapturer
        // 내 디바이스의 카메라에서 비디오 프레임 캡쳐
        // true : 전면,  false : 후면
        videoCapturer = createCameraCapturer(true);
         // VideoSource (VideoCapturer 로 부터 생성)
        videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast());
        // videoCapturer 초기화
        videoCapturer.initialize(surfaceTextureHelper, this, videoSource.getCapturerObserver());
        // 카메라로부터 비디오 캡쳐를 시작한다.
        videoCapturer.startCapture(240, 320, 30);  // width,height and fps
        // 비디오 트랙 생성
        localVideoTrack = peerConnectionFactory.createVideoTrack("100", videoSource);
        /****/

        /** 오디오 설정 **/
        // AudioSource 및 AudioTrack 생성
        audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        localAudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource);
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
        renderer.init(eglBase.getEglBaseContext(),null);
        // 비디오 트랙에 추가
        localVideoTrack.addSink(renderer);
        renderer.setMirror(true);
        /****/
    }
    private void switchCammera(boolean isfront) {
        // 카메라 종료 및 재생성
        releaseCamera();
        createCameraStream(isfront);
    }

    // 화면 꺼졌을 때 event
    private final BroadcastReceiver screenStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    releaseCamera();
                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                    createCameraStream(!camera_front_back);
                }
            }
        }
    };
    // 메서드
    private void createCameraStream(boolean isfront){
        /** 비디오 설정 **/
        videoCapturer = createCameraCapturer(isfront);
        videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast());
        videoCapturer.initialize(surfaceTextureHelper, this, videoSource.getCapturerObserver());
        videoCapturer.startCapture(240, 320, 30);
        VideoTrack newVideoTrack = peerConnectionFactory.createVideoTrack("100", videoSource);

        /** 오디오 설정 **/
        // AudioSource 및 AudioTrack 생성
        audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        localAudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource);
//        localAudioTrack.setVolume(5); // audio volume
        localAudioTrack.setEnabled(true); // 오디오 트랙 활성화

        /** mediaStream **/
        // 초기화
        mediaStream = peerConnectionFactory.createLocalMediaStream("mediaStream");
        mediaStream.addTrack(newVideoTrack);
        mediaStream.addTrack(localAudioTrack);

        /** stream **/
        for (PeerConnection peerConnection : peerConnections.values()){
            peerConnection.removeStream(mediaStream);
            peerConnection.addStream(mediaStream);
        }

        /** 비디오 띄우기 **/
        // 비디오 트랙에 추가
        localVideoTrack.removeSink(renderer);
        newVideoTrack.addSink(renderer);
        renderer.setMirror(true);
        /****/
        // 초기화
        localVideoTrack = newVideoTrack;
    }
    private void releaseCamera(){
        videoCapturer.dispose();
        videoSource.dispose();
        mediaStream.dispose();
    }

    /** PeerConnection 생성 **/
    private PeerConnection createPeerConnection(Socket socket, String id) {
        // PeerConnection 생성
        PeerConnection peerConnection = peerConnectionFactory.createPeerConnection(iceServers, new PeerConnectionAdapter() {
            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                // icecandidate 수집시 서버에 전송
                socket.emit("candidate", 방송방_id, id, iceCandidate.sdpMid, iceCandidate.sdpMLineIndex, iceCandidate.sdp);
            }
            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
//                System.out.println(iceConnectionState);
            }
        });

        DataChannel.Init dcInit = new DataChannel.Init();
        dcInit.ordered = true;  // 순서대로 전송
//        dcInit.negotiated = true; // 데이터 채널이 협상된 채널인지 설정
        dcInit.maxRetransmits = -1; // 최대 재전송 횟수 (-1 : 무제한)

        // DataChannel 초기화
        DataChannel dataChannel = peerConnection.createDataChannel(id, dcInit);
        dataChannel.registerObserver(new datachannelobserver(){
            @Override
            public void onMessage(DataChannel.Buffer buffer) {
                super.onMessage(buffer);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 받은 buffer data ( binary )
                        ByteBuffer data = buffer.data;
                        // ByteBuffer 에 남은 데이터
                        byte[] messageBytes = new byte[data.remaining()];
                        // messageBytes 에 데이터 복사
                        data.get(messageBytes);

                        // 각 Peer 로 부터 받은 메세지 ( UTF-8 포맷 )
                        String 받은message = new String(messageBytes, StandardCharsets.UTF_8);

                        // 정규식
                        // ^:시작, \s:공백, \n:개행, $:끝 14, \p{Punct}: 14개의 punctuation marks
                        // 한글 : ㄱ-ㅎ, ㅏ-ㅣ, 가-힣
                        String validCharsetPattern = "^[a-zA-Z0-9\\u3131-\\u314e|\\u314f-\\u3163|\\uac00-\\ud7a3\\p{Punct}\\s\\n]+$";
                        Pattern pattern = Pattern.compile(validCharsetPattern);
                        Matcher matcher = pattern.matcher(받은message);

                        if (!matcher.matches()) {
                            // 재전송 요청
                            보내는message = "AGAIN";
                            byte[] msg = 보내는message.getBytes(StandardCharsets.UTF_8);
                            ByteBuffer buffer = ByteBuffer.wrap(msg);
                            DataChannel.Buffer dataBuffer = new DataChannel.Buffer(buffer, false);

                            dataChannel.send(dataBuffer);
                        }else if(받은message.equals("AGAIN")) {
                            // 재전송
                            byte[] msg = 보내는message.getBytes(StandardCharsets.UTF_8);
                            ByteBuffer buffer = ByteBuffer.wrap(msg);
                            DataChannel.Buffer dataBuffer = new DataChannel.Buffer(buffer, false);

                            dataChannel.send(dataBuffer);
                        }else{
                            //각 watcher 에게 받은 message 다시 전송
                            보내는message = 받은message;
                            //Broadcaster : 모든 watcher 의 datachannel 을 관리
                            for(DataChannel dataChannel1 : dataChannels){
                                // buffer 보내고 나면 초기화 됨
                                // for 문 돌때마다 계속 생성해줘야 함
                                byte[] msg = 보내는message.getBytes(StandardCharsets.UTF_8);
                                ByteBuffer buffer = ByteBuffer.wrap(msg);
                                DataChannel.Buffer dataBuffer = new DataChannel.Buffer(buffer, false);

                                dataChannel1.send(dataBuffer);
                            }
                            // Broadcaster 부분에도 추가
                            textView.append(받은message + "\n");
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
        // Arraylist 에 추가
        dataChannels.add(dataChannel);


        // Stream 추가
        peerConnection.addStream(mediaStream);

        return peerConnection;
    }

    // Offer 생성 및 서버에 전송
    private void createAndSetLocalDescription(String id, PeerConnection peerConnection) {
        peerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                // LocalDescription 설정
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                // 서버에 만들어진 sdp 전송
                socket.emit("offer", 방송방_id, id, sessionDescription.description);
            }
        }, new MediaConstraints());
    }

    /******************************************************************/
    // 간단한 SdpObserver 구현 클래스
    private static class SimpleSdpObserver implements SdpObserver {
        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {}
        @Override
        public void onSetSuccess() {}
        @Override
        public void onCreateFailure(String s) {}
        @Override
        public void onSetFailure(String s) {}
    }
    private static class PeerConnectionAdapter implements PeerConnection.Observer {
        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {}
        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {}
        @Override
        public void onIceConnectionReceivingChange(boolean b) {}
        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {}
        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {}
        @Override
        public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {}
        @Override
        public void onAddStream(MediaStream mediaStream) {}
        @Override
        public void onRemoveStream(MediaStream mediaStream) {}
        @Override
        public void onDataChannel(DataChannel dataChannel) {}
        @Override
        public void onRenegotiationNeeded() {}
        @Override
        public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {}
    }
    private static class datachannelobserver implements DataChannel.Observer {
        @Override
        public void onBufferedAmountChange(long l) {
//            System.out.println("onBufferedAmountChange_datachannel");
        }
        @Override
        public void onStateChange() {
//            System.out.println("onStateChange_datachannel");
        }
        @Override
        public void onMessage(DataChannel.Buffer buffer) {
        }
    };
    /******************************************************************/

    /**
     * 비디오
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

    /** 카메라 **/

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
                socketIO();
            }
        }
    }

}