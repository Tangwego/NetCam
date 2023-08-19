package top.wdcc.netcam.client.utils;

import android.content.Context;

import org.webrtc.AudioSource;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;

import java.util.ArrayList;
import java.util.List;

import top.wdcc.netcam.client.NetCamApp;
import top.wdcc.netcam.client.rtc.SimpleRtcObserver;

public class RtcUtils {

    public static final String VIDEO_CAMERA_TRACK_ID = "video_camera_track";

    public static final String VIDEO_SCREEN_TRACK_ID = "video_screen_track";

    public static final String AUDIO_TRACK_ID = "audio_track";

    public static final String DATA_CHANNEL_LABEL = "net_cam";

    public static final String MEDIA_STREAM_LABEL = "local_media";

    private static EglBase eglBase;

    private static PeerConnectionFactory factory;

    private static VideoCapturer capturer;

    static {
        eglBase = EglBase.create();
        factory = createPeerConnectionFactory(NetCamApp.getContext());
        capturer = createVideoCapturer(NetCamApp.getContext());
    }

    private static PeerConnectionFactory createPeerConnectionFactory(Context context) {
        PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(context)
                        .setEnableInternalTracer(true).createInitializationOptions());

        DefaultVideoDecoderFactory decoderFactory =
                new DefaultVideoDecoderFactory(eglBase.getEglBaseContext());
        DefaultVideoEncoderFactory encoderFactory =
                new DefaultVideoEncoderFactory(eglBase.getEglBaseContext()
                        ,false, true);

        return PeerConnectionFactory.builder()
                .setVideoDecoderFactory(decoderFactory)
                .setVideoEncoderFactory(encoderFactory)
                .setOptions(null)
                .createPeerConnectionFactory();
    }

    private static VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        String[] deviceNames = enumerator.getDeviceNames();
        // 后置摄像头
        for (String deviceName : deviceNames) {
            if (enumerator.isBackFacing(deviceName)) {
                return enumerator.createCapturer(deviceName, null);
            }
        }
        // 前置摄像头
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                return enumerator.createCapturer(deviceName, null);
            }
        }

        return null;
    }

    private static VideoCapturer createVideoCapturer(Context context) {
        if (Camera2Enumerator.isSupported(context)) {
            return createCameraCapturer(new Camera2Enumerator(context));
        } else {
            return createCameraCapturer(new Camera1Enumerator(true));
        }
    }

    public static VideoSource createCameraSource() {
        return factory.createVideoSource(false);
    }

    public static VideoSource createScreenSource() {
        return factory.createVideoSource(true);
    }

    public static AudioSource createAudioSource() {
        return factory.createAudioSource(new MediaConstraints());
    }

    public static MediaStream createLocalStream() {
        return factory.createLocalMediaStream(MEDIA_STREAM_LABEL);
    }

    public static PeerConnectionFactory getFactory() {
        return factory;
    }

    public static EglBase.Context getEglBaseContext() {
        return eglBase.getEglBaseContext();
    }

    public static VideoCapturer getCapturer() {
        return capturer;
    }

    public static PeerConnection createPeerConnection(SimpleRtcObserver observer) {
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
//        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.PLAN_B;
        return factory.createPeerConnection(rtcConfig, observer);
    }

    public static DataChannel createDataChannel(PeerConnection connection, String label) {
        if (connection == null) throw new RuntimeException("connection cannot be null!");
        DataChannel.Init init = new DataChannel.Init();
        init.ordered = true;
        init.negotiated = true;
        init.id = 0;
        return connection.createDataChannel(label, init);
    }

    public static MediaConstraints createVideoConstraints() {
        // 创建发起方的媒体条件
        MediaConstraints mOfferConstraints = new MediaConstraints();
        // 是否接受音频流
        mOfferConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        // 是否接受视频流
        mOfferConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        return mOfferConstraints;
    }

    public static MediaConstraints createAudioConstraints() {
        // 创建音频流的媒体条件
        MediaConstraints mAudioConstraints = new MediaConstraints();
        // 是否消除回声
        mAudioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googEchoCancellation", "true"));
        // 是否自动增益
        mAudioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googAutoGainControl", "true"));
        // 是否过滤高音
        mAudioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googHighpassFilter", "true"));
        // 是否抑制噪音
        mAudioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googNoiseSuppression", "true"));
        return mAudioConstraints;
    }

    public static MediaConstraints createConstraints() {
        // 创建发起方的媒体条件
        MediaConstraints mOfferConstraints = new MediaConstraints();
        // 是否接受音频流
        mOfferConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        // 是否接受视频流
        mOfferConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        // 是否消除回声
        mOfferConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googEchoCancellation", "true"));
        // 是否自动增益
        mOfferConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googAutoGainControl", "true"));
        // 是否过滤高音
        mOfferConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googHighpassFilter", "true"));
        // 是否抑制噪音
        mOfferConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googNoiseSuppression", "true"));
        return mOfferConstraints;
    }
}
