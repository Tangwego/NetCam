package top.wdcc.netcam.client.net;

import java.util.Map;

public interface IClient {
    void connect(String ip, int port);

    void connect(String uri);

    void send(Object data);

    void setListener(SignalListener listener);

    boolean isConnected();

    void close();
}
