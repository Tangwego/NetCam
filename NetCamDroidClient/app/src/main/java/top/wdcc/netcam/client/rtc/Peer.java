package top.wdcc.netcam.client.rtc;

import static top.wdcc.netcam.client.rtc.Peer.SignalState.ON_ANSWER;
import static top.wdcc.netcam.client.rtc.Peer.SignalState.ON_CANDIDATE;
import static top.wdcc.netcam.client.rtc.Peer.SignalState.ON_INVITE;
import static top.wdcc.netcam.client.rtc.Peer.SignalState.ON_OPENED;
import static top.wdcc.netcam.client.rtc.Peer.SignalState.ON_OPENNING;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSink;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.HashMap;
import java.util.Map;

import top.wdcc.netcam.client.MainActivity;
import top.wdcc.netcam.client.NetCamApp;
import top.wdcc.netcam.client.net.IClient;
import top.wdcc.netcam.client.net.WebSocketListener;
import top.wdcc.netcam.client.utils.Log;
import top.wdcc.netcam.client.utils.RtcUtils;
import top.wdcc.netcam.client.var.Protocal;

public class Peer extends SimpleRtcObserver implements WebSocketListener {

    private static final String TAG = Peer.class.getSimpleName();

    private Handler mUIHandler = new Handler(Looper.getMainLooper());

    public static final String RTC_CLIENT_NO = "1001";
    public static final String RTC_SERVER_NO = "1002";

    private Activity activity;
    private IClient client;
    private PeerConnection connection;

    private VideoSink videoSink;

    private VideoCapturer videoCapturer;

    private SignalState state;

    private SessionDescription.Type sdpType;

    public Peer(Activity activity) {
        this.activity = activity;
        this.connection = RtcUtils.createPeerConnection(this);
    }

    @Override
    public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
        Log.d(TAG, "on add track");
        MediaStreamTrack track = rtpReceiver.track();
        if (track instanceof VideoTrack) {
            VideoTrack remoteVideoTrack = (VideoTrack) track;
            remoteVideoTrack.setEnabled(true);
            if (this.videoSink != null) {
                remoteVideoTrack.addSink(this.videoSink);
            }
        }
    }

    public void setSignalClient(IClient signalClient) {
        this.client = signalClient;
    }

    public void setVideoSink(VideoSink videoSink) {
        this.videoSink = videoSink;
    }

    private void startCapturer() {
        VideoSource videoSource = RtcUtils.createCameraSource();
        VideoTrack videoTrack = RtcUtils.getFactory().createVideoTrack(RtcUtils.VIDEO_CAMERA_TRACK_ID, videoSource);
//        videoTrack.addSink(this.videoSink);
        videoTrack.setEnabled(true);
        this.connection.addTrack(videoTrack);

        AudioSource audioSource = RtcUtils.createAudioSource();
        AudioTrack audioTrack = RtcUtils.getFactory().createAudioTrack(RtcUtils.AUDIO_TRACK_ID, audioSource);
        audioTrack.setEnabled(true);
        this.connection.addTrack(audioTrack);

        SurfaceTextureHelper textureHelper =
                SurfaceTextureHelper.create("capturerThread", RtcUtils.getEglBaseContext());
        this.videoCapturer = RtcUtils.getCapturer();
        this.videoCapturer.initialize(textureHelper, NetCamApp.getContext(), videoSource.getCapturerObserver());
        this.videoCapturer.startCapture(this.activity.getWindowManager().getDefaultDisplay().getWidth(),
                this.activity.getWindowManager().getDefaultDisplay().getHeight(), 30);
    }

    private void stopCapturer() {
        try {
            if (this.videoCapturer != null) {
                this.videoCapturer.stopCapture();
                this.videoCapturer = null;
            }
        } catch (InterruptedException e) {
            Log.e(TAG, e, "error: %s", e.getMessage());
        }
    }

    public void connect(String ip, int port) {
        if (this.client == null) {
            throw new RuntimeException("client cannot be null");
        }
        if (this.connection == null) {
            this.connection = RtcUtils.createPeerConnection(this);
        }
        this.state = ON_OPENNING;
        this.client.setListener(this);
        this.client.connect(ip, port);
        startCapturer();
    }

    public boolean isConnected() {
        if (this.client == null) {
            return false;
        }
        return this.client.isConnected();
    }

    public void close() {
        if (this.client != null) {
            Map<String, Object> data = new HashMap<>();
            data.put(Protocal.ACTION, Protocal.UNREGISTER_COMMAND);
            data.put(Protocal.NUMBER, Peer.RTC_CLIENT_NO);
            this.client.send(data);
            this.client.close();
        }

        if (this.connection != null) {
            this.connection.close();
            this.connection = null;
        }
        stopCapturer();
    }

    public void createOffer() {
        this.sdpType = SessionDescription.Type.OFFER;
        this.connection.createOffer(this, RtcUtils.createConstraints());
    }

    public void createAnswer() {
        this.sdpType = SessionDescription.Type.ANSWER;
        this.connection.createAnswer(this, RtcUtils.createConstraints());
    }

    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        Log.d(TAG, "on create success, sdp: %s", sessionDescription.description);
        if (SessionDescription.Type.OFFER.equals(this.sdpType)) {
            // offer sdp
            this.connection.setLocalDescription(this, sessionDescription);
            Map<String, Object> data = new HashMap<>();
            data.put(Protocal.ACTION, Protocal.INVITE_COMMAND);
            data.put(Protocal.SDP, sessionDescription.description);
            data.put(Protocal.TYPE, sessionDescription.type.name().toLowerCase());
            data.put(Protocal.CALLER, RTC_CLIENT_NO);
            data.put(Protocal.CALLEE, RTC_SERVER_NO);
            this.client.send(data);
            this.state = ON_INVITE;
        } else {
            // answer sdp
        }
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
        /* candidate:2089015932 1 udp 2122260223 192.168.10.161 38390 typ host generation 0 ufrag Lb9+ network-id 3 */
        /* candidate:2089015932 1 udp 2122260223 192.168.10.161 45840 typ host generation 0 ufrag hhys network-id 3 */
        /* audio:0:candidate:2089015932 1 udp 2122260223 192.168.10.161 41994 typ host generation 0 ufrag JQVQ network-id 3 network-cost 10::UNKNOWN */
        Log.d(TAG, "on ice candidate, candidate: %s", iceCandidate.toString());
        Map<String, Object> data = new HashMap<>();
        data.put(Protocal.ACTION, Protocal.CANDIDATE_COMMAND);
        data.put(Protocal.FROM, Peer.RTC_CLIENT_NO);
        data.put(Protocal.TO, Peer.RTC_SERVER_NO);
        Map<String, Object> candidate = new HashMap<>();
        candidate.put(Protocal.CANDIDATE, iceCandidate.toString().substring(8).replaceAll(" network-cost 10::UNKNOWN", ""));
        candidate.put(Protocal.SDP_MLINE_INDEX, iceCandidate.sdpMLineIndex);
        candidate.put(Protocal.SDP_MID, iceCandidate.sdpMid);
        data.put(Protocal.CANDIDATE, candidate);
        this.client.send(data);
    }

    @Override
    public void onOpen() {
        Log.d(TAG, "on websocket open!");

        Map<String, Object> data = new HashMap<>();
        data.put(Protocal.ACTION, Protocal.REGISTER_COMMAND);
        data.put(Protocal.NUMBER, Peer.RTC_CLIENT_NO);
        this.client.send(data);
        this.state = ON_OPENED;

        mUIHandler.post(MainActivity.START_ACTION);
    }

    @Override
    public void onMessage(String message) {
        Log.d(TAG, "received websocket message: %s", message);
        try {
            JSONObject object = new JSONObject(message);
            switch (this.state) {
                case ON_OPENED:
                    // register成功
                    this.createOffer();
                    break;
                case ON_INVITE:
                    if (Protocal.ANSWER_COMMAND.equals(object.optString(Protocal.ACTION))) {
                        // answer command
                        SessionDescription sessionDescription = new SessionDescription(SessionDescription.Type.ANSWER, object.optString(Protocal.SDP));
                        this.connection.setRemoteDescription(this, sessionDescription);
                        this.state = ON_ANSWER;
                    }
                    break;
            }
            Toast.makeText(NetCamApp.getContext(), object.optString(Protocal.RESULT), Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.d(TAG, "on websocket close! why? %s", reason);
        mUIHandler.post(MainActivity.STOP_ACTION);
    }

    @Override
    public void onError(Exception e) {

    }

    enum SignalState {
        ON_OPENNING,
        ON_OPENED,
        ON_REGISTERED,
        ON_INVITE,
        ON_ANSWER,
        ON_CANDIDATE,
        ON_HANGUP,
        ON_UNREGISTERED
    };
}
