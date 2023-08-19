package top.wdcc.netcam.client.net;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class SimpleWebSocketClient extends WebSocketClient {
    private WebSocketListener listener;

    public SimpleWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    public void setListener(WebSocketListener listener) {
        if (this.listener == null) {
            this.listener = listener;
        }
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        if (this.listener != null) {
            this.listener.onOpen();
        }
    }

    @Override
    public void onMessage(String message) {
        if (this.listener != null) {
            this.listener.onMessage(message);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (this.listener != null) {
            this.listener.onClose(code, reason, remote);
        }
    }

    @Override
    public void onError(Exception ex) {
        if (this.listener != null) {
            this.listener.onError(ex);
        }
    }
}
