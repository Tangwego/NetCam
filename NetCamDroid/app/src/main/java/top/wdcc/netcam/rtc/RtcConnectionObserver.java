package top.wdcc.netcam.rtc;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import java.util.HashMap;
import java.util.Map;

import top.wdcc.netcam.utils.Log;
import top.wdcc.netcam.utils.RtcUtils;
import top.wdcc.netcam.var.Protocal;

public class RtcConnectionObserver extends SimpleRtcObserver {
    private static final String TAG = RtcConnectionObserver.class.getSimpleName();

    private RtcConnection peer;

    private boolean mIsinitiator;

    public RtcConnectionObserver(RtcConnection peer) {
        this.peer = peer;
        this.mIsinitiator = false;
    }

    public void createOffer() {
        this.mIsinitiator = true;
        this.peer.getPeer().createOffer(this, RtcUtils.createConstraints());
    }

    public void createAnswer() {
        this.mIsinitiator = false;
        this.peer.getPeer().createAnswer(this, RtcUtils.createConstraints());
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
        Log.d(TAG, "onIceCandidate: %s", iceCandidate.toString());
        Map<String, Object> map = new HashMap<>();
        map.put(Protocal.ACTION, Protocal.CANDIDATE);
        map.put(Protocal.FROM, RtcConnection.RTC_SERVER_NO);
        map.put(Protocal.TO, RtcConnection.RTC_CLIENT_NO);
        Map<String, Object> candidate = new HashMap<>();
        candidate.put(Protocal.CANDIDATE, iceCandidate.toString().substring(4).replaceAll(" network-cost 10::UNKNOWN", ""));
        candidate.put(Protocal.SDP_MLINE_INDEX, iceCandidate.sdpMLineIndex);
        candidate.put(Protocal.SDP_MID, iceCandidate.sdpMid);
        map.put(Protocal.CANDIDATE, candidate);

        this.peer.send(RtcConnection.RTC_CLIENT_NO, map);
    }

    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        Log.d(TAG, "onCreateSuccess: type: %s sdp: %s", sessionDescription.type.name(), sessionDescription.description);
        if (mIsinitiator) {
            Log.d(TAG, "this is an offer sdp...");
        } else {
            Log.d(TAG, "this is an answer sdp...");
            this.peer.getPeer().setLocalDescription(this, sessionDescription);

            Map<String, Object> map = new HashMap<>();
            map.put(Protocal.ACTION, Protocal.ANSWER_COMMAND);
            map.put(Protocal.SDP, sessionDescription.description);
            map.put(Protocal.TYPE, sessionDescription.type.name().toLowerCase());
            map.put(Protocal.FROM, RtcConnection.RTC_SERVER_NO);
            this.peer.send(RtcConnection.RTC_CLIENT_NO, map);

        }
    }

}
