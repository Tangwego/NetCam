package top.wdcc.netcam.rtc;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoProcessor;
import org.webrtc.VideoSink;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.Map;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import top.wdcc.netcam.MainActivity;
import top.wdcc.netcam.R;
import top.wdcc.netcam.net.SignalHandler;
import top.wdcc.netcam.net.SignalServer;
import top.wdcc.netcam.utils.RtcUtils;
import top.wdcc.netcam.var.Protocal;

public class RtcConnection {

    private static final String TAG = RtcConnection.class.getSimpleName();

    private Handler mUIHandler = new Handler(Looper.getMainLooper());

    public static final String RTC_CLIENT_NO = "1001";
    public static final String RTC_SERVER_NO = "1002";

    private MainActivity context;

    private VideoCapturer videoCapturer;

    private VideoSink videoSink;

    private boolean isStarted;

    private DataChannel.Observer observer;

    private VideoProcessor videoProcessor;

    private SignalServer signalServer;

    private PeerConnection peerConnection;

    private RtcConnectionObserver rtcConnectionObserver;

    public RtcConnection(MainActivity context, VideoSink videoSink) {
        this.context = context;
        this.videoSink = videoSink;
        this.isStarted = false;
        this.signalServer = new SignalServer(2333);
        this.rtcConnectionObserver = new RtcConnectionObserver(this);
        this.peerConnection = RtcUtils.createPeerConnection(this.rtcConnectionObserver);
        this.signalServer.setPeer(this);
    }

    public void setVideoProcessor(VideoProcessor processor) {
        this.videoProcessor = processor;
    }

    public void setObserver(DataChannel.Observer observer) {
        this.observer = observer;
    }

    public PeerConnection getPeer() {
        return this.peerConnection;
    }

    public RtcConnectionObserver getObserver() {
        return this.rtcConnectionObserver;
    }

    public void handleUnregister(Channel channel) {
        this.stopVideoCapturer();
    }

    public void handleInvite(Channel channel, SessionDescription sdp) {
        if (!this.isStarted) {
            this.startVideoCapturer(context.getWindowManager().getDefaultDisplay().getWidth(),
                    context.getWindowManager().getDefaultDisplay().getHeight(), 30);
        }
        this.peerConnection.setRemoteDescription(this.rtcConnectionObserver, sdp);
        this.rtcConnectionObserver.createAnswer();
    }

    public void handleCandidate(Channel channel, IceCandidate iceCandidate) {
        if (!this.isStarted) {
            this.startVideoCapturer(context.getWindowManager().getDefaultDisplay().getWidth(),
                    context.getWindowManager().getDefaultDisplay().getHeight(), 30);
        }
        this.peerConnection.addIceCandidate(iceCandidate);
    }

    public void handleHangup(Channel channel) {
        this.stopVideoCapturer();
    }

    public void send(String number, Map<String, Object> data) {
        this.signalServer.send(number, data);
    }

    public void startVideoCapturer(int w, int h, int fps) {
        try {
            if (this.isStarted) return;

            if (this.peerConnection == null) {
                this.peerConnection = RtcUtils.createPeerConnection(this.rtcConnectionObserver);
            }

            VideoSource videoSource = RtcUtils.createCameraSource();
            videoSource.setVideoProcessor(this.videoProcessor);
            VideoTrack videoTrack = RtcUtils.getFactory().createVideoTrack(RtcUtils.VIDEO_CAMERA_TRACK_ID, videoSource);
//        videoTrack.addSink(this.videoSink);
            videoTrack.setEnabled(true);
            this.peerConnection.addTrack(videoTrack);

            AudioSource audioSource = RtcUtils.createAudioSource();
            AudioTrack audioTrack = RtcUtils.getFactory().createAudioTrack(RtcUtils.AUDIO_TRACK_ID, audioSource);
            audioTrack.setEnabled(true);
            this.peerConnection.addTrack(audioTrack);

            SurfaceTextureHelper textureHelper =
                    SurfaceTextureHelper.create("capturerThread", RtcUtils.getEglBaseContext());
            this.videoCapturer = RtcUtils.getCapturer();
            this.videoCapturer.initialize(textureHelper, this.context, videoSource.getCapturerObserver());
            this.videoCapturer.startCapture(w, h, fps);
        } finally {
            this.isStarted = true;
        }
    }

    public void stopVideoCapturer() {
        if (!this.isStarted) return;
        try {
            if (this.videoCapturer != null) {
                this.videoCapturer.stopCapture();
                this.videoCapturer = null;
            }
            if (this.peerConnection != null) {
                this.peerConnection.close();
                this.peerConnection = null;
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "stop video capturer failed: ", e);
        } finally {
            this.isStarted = false;
        }
    }

    public void startSignalServer() {
        try {
            this.signalServer.start();
        } finally {
            mUIHandler.post(MainActivity.START_ACTION);
        }
    }

    public void stopSignalServer() {
        try {
            this.signalServer.stop();
        } finally {
            mUIHandler.post(MainActivity.STOP_ACTION);
        }
    }

    public boolean isStarted() {
        return this.signalServer.isRunning();
    }
}
