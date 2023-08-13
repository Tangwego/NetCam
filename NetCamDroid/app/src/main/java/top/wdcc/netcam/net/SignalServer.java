package top.wdcc.netcam.net;

import androidx.annotation.NonNull;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import top.wdcc.netcam.rtc.RtcConnection;
import top.wdcc.netcam.utils.Log;

public class SignalServer implements Runnable {

    private static final String TAG = SignalServer.class.getSimpleName();

    private static final EventLoopGroup BOSS_GROUP = new NioEventLoopGroup(1);

    private static final EventLoopGroup WORK_GROUP = new NioEventLoopGroup();

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    private ServerBootstrap serverBootstrap;

    private Channel channel;

    private int port;

    private SignalHandler signalHandler;

    private boolean isRunning = false;

    public SignalServer(int port) {
        this.port = port;
        this.signalHandler = new SignalHandler();
    }

    private ServerBootstrap setup() {
        return new ServerBootstrap().group(BOSS_GROUP, WORK_GROUP)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(@NonNull SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();

                        pipeline.addLast(new HttpServerCodec());
                        pipeline.addLast(new ChunkedWriteHandler());
                        pipeline.addLast(new HttpObjectAggregator(8192));
                        pipeline.addLast(new WebSocketServerProtocolHandler("/"));
                        pipeline.addLast(signalHandler);
                    }
                });
    }

    public void setPeer(RtcConnection peer) {
        this.signalHandler.setPeer(peer);
    }

    public void start() {
        if (this.isRunning) return;
        this.isRunning = true;
        EXECUTOR_SERVICE.execute(this);
    }

    public void stop() {
        if (!this.isRunning) return;
        this.isRunning = false;
        if (this.channel != null) {
            this.channel.close();
            this.channel = null;
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void send(String number, Map<String, Object> data) {
        this.signalHandler.send(number, data);
    }

    @Override
    public void run() {
        try {
            this.isRunning = true;
            if (this.serverBootstrap == null) {
                this.serverBootstrap = setup();
            }
            Log.i(TAG, "signal server start at: %d", this.port);
            this.channel = serverBootstrap.bind(port).sync()
                    .channel();
            this.channel.closeFuture().sync();
            this.isRunning = false;
            Log.i(TAG, "signal server shutdown success.");
        } catch (InterruptedException e) {
            this.isRunning = false;
            throw new RuntimeException(e);
        }
    }
}
