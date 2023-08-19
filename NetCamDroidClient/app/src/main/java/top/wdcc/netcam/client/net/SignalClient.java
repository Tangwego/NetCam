package top.wdcc.netcam.client.net;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.google.gson.Gson;

import org.java_websocket.enums.ReadyState;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import top.wdcc.netcam.client.MainActivity;
import top.wdcc.netcam.client.NetCamApp;
import top.wdcc.netcam.client.rtc.Peer;
import top.wdcc.netcam.client.utils.Log;
import top.wdcc.netcam.client.var.Protocal;

public class SignalClient implements IClient {

    private static final String TAG = SignalClient.class.getSimpleName();

    private Handler mUIHandler = new Handler(Looper.getMainLooper());

    private SimpleWebSocketClient webSocketClient;

    private String serverAddr;

    private int serverPort;

    private WebSocketListener listener;


    public SignalClient() {

    }

    @Override
    public boolean isConnected() {
        if (this.webSocketClient != null) {
            return this.webSocketClient.isOpen();
        }
        return false;
    }

    @Override
    public void connect(String uri) {
        URI serverUri = null;
        try {
            serverUri = new URI(uri);
            if (this.webSocketClient == null) {
                this.webSocketClient = new SimpleWebSocketClient(serverUri);
                this.webSocketClient.setListener(this.listener);
            } else {
                if (webSocketClient.getReadyState() == ReadyState.CLOSED) {
                    webSocketClient.reconnectBlocking();
                    return;
                }
            }
            webSocketClient.connectBlocking();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } finally {
            this.serverAddr = serverUri.getHost();
            this.serverPort = serverUri.getPort();
        }
    }

    @Override
    public void connect(String ip, int port) {
        this.connect(String.format("ws://%s:%d", ip, port));
    }

    @Override
    public void close() {
        if (this.webSocketClient != null) {
            this.webSocketClient.close();
        }
    }

    public void send(Object data) {
        if (data instanceof Map) {
            this.webSocketClient.send(new Gson().toJson(data));
            return;
        }
        Log.w(TAG, "Unsupported data format!");
    }

    @Override
    public void setListener(SignalListener listener) {
        this.listener = (WebSocketListener) listener;
    }

    public String getServerAddr() {
        return serverAddr;
    }

    public int getServerPort() {
        return serverPort;
    }
}
