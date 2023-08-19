package top.wdcc.netcam.client;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import top.wdcc.netcam.client.net.SignalClient;
import top.wdcc.netcam.client.rtc.Peer;
import top.wdcc.netcam.client.utils.RtcUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static StartAction START_ACTION;
    public static StopAction STOP_ACTION;

    private SurfaceViewRenderer surfaceViewRenderer;
    private Button monitorBtn;

    private Peer peer;

    private SignalClient signalClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        START_ACTION = new StartAction(this);
        STOP_ACTION = new StopAction(this);

        this.monitorBtn = findViewById(R.id.monitor_click);
        this.monitorBtn.setOnClickListener(this);

        this.surfaceViewRenderer = findViewById(R.id.video_preview);
        this.signalClient = new SignalClient();
        this.peer = new Peer(this);
        this.peer.setVideoSink(this.surfaceViewRenderer);
        this.peer.setSignalClient(this.signalClient);

        this.surfaceViewRenderer.init(RtcUtils.getEglBaseContext(), null);
        this.surfaceViewRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        this.surfaceViewRenderer.setMirror(true);
        this.surfaceViewRenderer.setEnableHardwareScaler(false);
    }

    @Override
    public void onClick(View v) {
        if (this.peer.isConnected()) {
            // 断开
            this.peer.close();
        } else {
            View contentView = View.inflate(this, R.layout.server_input_layout, null);
            EditText inputServer = contentView.findViewById(R.id.server_addr);
            inputServer.setText(this.signalClient.getServerAddr());
            EditText inputPort = contentView.findViewById(R.id.server_port);
            inputPort.setText(String.valueOf(this.signalClient.getServerPort()));
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("请输入监控服务器地址")
                    .setView(contentView)
                    .setNegativeButton(R.string.cancel_connect, (dialog, which) -> dialog.dismiss());
            builder.setPositiveButton(R.string.sure_connect, (dialog, which) -> {
                String serverAddr = inputServer.getText().toString();
                int serverPort = Integer.parseInt(inputPort.getText().toString());
                peer.connect(serverAddr, serverPort);
            });
            builder.show();
        }
    }

    public class StopAction implements Runnable {
        private MainActivity activity;
        public StopAction(MainActivity mainActivity) {
            this.activity = mainActivity;
        }

        @Override
        public void run() {
            activity.monitorBtn.setText(R.string.monitor_start);
            activity.surfaceViewRenderer.clearImage();
        }
    }

    public class StartAction implements Runnable {
        private MainActivity activity;
        public StartAction(MainActivity mainActivity) {
            this.activity = mainActivity;
        }

        @Override
        public void run() {
            activity.monitorBtn.setText(R.string.monitor_stop);
        }
    }
}
