package top.wdcc.netcam.client.net;

public interface WebSocketListener extends SignalListener {
    void onOpen();

    void onMessage(String message);

    void onClose(int code, String reason, boolean remote);

    void onError(Exception e);
}
