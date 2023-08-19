package top.wdcc.netcam.client.net;


import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;

import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import top.wdcc.netcam.client.MainActivity;
import top.wdcc.netcam.client.utils.Log;
import top.wdcc.netcam.client.var.Protocal;

@ChannelHandler.Sharable
public class SignalHandler extends SimpleChannelInboundHandler<String> {

    private Handler mUIHandler = new Handler(Looper.getMainLooper());

    private static final String TAG = SignalHandler.class.getSimpleName();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        Log.i(TAG, "channel active...");
        mUIHandler.post(MainActivity.START_ACTION);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        Log.i(TAG, "received message: %s", msg);

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Log.i(TAG, "channel inactive...");
        mUIHandler.post(MainActivity.STOP_ACTION);
    }
}
