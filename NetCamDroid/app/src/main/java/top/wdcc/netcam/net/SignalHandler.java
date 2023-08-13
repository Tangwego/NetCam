package top.wdcc.netcam.net;


import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.SessionDescription;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import top.wdcc.netcam.rtc.RtcConnection;
import top.wdcc.netcam.utils.Log;
import top.wdcc.netcam.var.Protocal;

@ChannelHandler.Sharable
public class SignalHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static final String TAG = SignalHandler.class.getSimpleName();

    private RtcConnection peer;

    private static volatile Map<String, Channel> clients = new HashMap<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        Log.i(TAG, "channel active...");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        Log.i(TAG, "received message: %s", msg.text());

        Channel client = ctx.channel();
        Map<String, Object> response = new HashMap<>();

        JSONObject jsonObject = new JSONObject(msg.text());
        String command = jsonObject.optString(Protocal.ACTION);

        switch (command) {
            case Protocal.REGISTER_COMMAND: {
                String number = jsonObject.optString(Protocal.NUMBER);
                if (clients.containsKey(number)) {
                    response.put(Protocal.ACTION, Protocal.RESPONSE_COMMAND);
                    response.put(Protocal.RESULT, Protocal.FAILURE);
                    send(client, response);
                    break;
                }
                clients.put(number, client);
                response.put(Protocal.ACTION, Protocal.RESPONSE_COMMAND);
                response.put(Protocal.RESULT, Protocal.SUCCESS);
                send(client, response);
            }
                break;

            case Protocal.INVITE_COMMAND: {
                String sdp = jsonObject.optString(Protocal.SDP);
                SessionDescription sessionDescription = new SessionDescription(SessionDescription.Type.OFFER, sdp);
                this.peer.handleInvite(client, sessionDescription);
                response.put(Protocal.ACTION, Protocal.RESPONSE_COMMAND);
                response.put(Protocal.RESULT, Protocal.SUCCESS);
                send(client, response);
            }
                break;

            case Protocal.CANDIDATE_COMMAND:{
                JSONObject candidate = jsonObject.optJSONObject(Protocal.CANDIDATE);
                String sdpMid = candidate.optString(Protocal.SDP_MID);
                int sdpMlineIndex = candidate.optInt(Protocal.SDP_MLINE_INDEX);
                IceCandidate iceCandidate = new IceCandidate(sdpMid, sdpMlineIndex, this.peer.getPeer().getRemoteDescription().description);
                this.peer.handleCandidate(client, iceCandidate);
                response.put(Protocal.ACTION, Protocal.RESPONSE_COMMAND);
                response.put(Protocal.RESULT, Protocal.SUCCESS);
                send(client, response);
            }
                break;

            case Protocal.HANGUP_COMMAND:
                this.peer.handleHangup(client);
                response.put(Protocal.ACTION, Protocal.RESPONSE_COMMAND);
                response.put(Protocal.RESULT, Protocal.SUCCESS);
                send(client, response);
                break;

            case Protocal.UNREGISTER_COMMAND:
                String number = jsonObject.optString(Protocal.NUMBER);
                if (!clients.containsKey(number)) {
                    return;
                }
                this.peer.handleUnregister(client);
                clients.remove(number);
                break;

            default:
                break;
        }

    }

    public void send(String number, Map<String, Object> data) {
        if (!clients.containsKey(number)) {
            return;
        }
        Gson gson = new Gson();
        Objects.requireNonNull(clients.get(number)).writeAndFlush(new TextWebSocketFrame(gson.toJson(data)));
    }

    public void send(String number, String message) {
        if (!clients.containsKey(number)) {
            return;
        }
        Gson gson = new Gson();
        Map<String, Object> res = new HashMap<>();
        res.put(Protocal.ACTION, Protocal.RESPONSE_COMMAND);
        res.put(Protocal.RESULT, Protocal.SUCCESS);
        res.put(Protocal.MESSAGE, message);
        Objects.requireNonNull(clients.get(number)).writeAndFlush(new TextWebSocketFrame(gson.toJson(res)));
    }

    public void send(Channel client, Map<String, Object> data) {
        Gson gson = new Gson();
        client.writeAndFlush(new TextWebSocketFrame(gson.toJson(data)));
    }

    public void setPeer(RtcConnection peer) {
        this.peer = peer;
    }



}
