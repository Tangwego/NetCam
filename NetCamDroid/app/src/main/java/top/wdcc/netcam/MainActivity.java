package top.wdcc.netcam;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import top.wdcc.netcam.rtc.NetVideoProcesser;
import top.wdcc.netcam.rtc.RtcConnection;
import top.wdcc.netcam.utils.RtcUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static StartAction START_ACTION;
    public static StopAction STOP_ACTION;

//    private SurfaceViewRenderer surfaceViewRenderer;
    private Button monitorBtn;

    private RtcConnection rtcConnection;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        START_ACTION = new StartAction(this);
        STOP_ACTION = new StopAction(this);

        this.monitorBtn = findViewById(R.id.monitor_click);
        this.monitorBtn.setOnClickListener(this);

//        this.surfaceViewRenderer = findViewById(R.id.video_preview);

//        this.rtcConnection = new RtcConnection(this, this.surfaceViewRenderer);
        this.rtcConnection = new RtcConnection(this, null);
        this.rtcConnection.setVideoProcessor(new NetVideoProcesser());

//        this.surfaceViewRenderer.init(RtcUtils.getEglBaseContext(), null);
//        this.surfaceViewRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
//        this.surfaceViewRenderer.setMirror(true);
//        this.surfaceViewRenderer.setEnableHardwareScaler(false);
    }

    @Override
    public void onClick(View v) {
        try {
            if (this.rtcConnection.isStarted()) {
                this.rtcConnection.stopSignalServer();
//            this.surfaceViewRenderer.clearImage();
//                this.rtcConnection.stopVideoCapturer();
            } else {
                this.rtcConnection.startSignalServer();
//            this.rtcConnection.startVideoCapturer(this.surfaceViewRenderer.getWidth(), this.surfaceViewRenderer.getHeight(), 30);
//                this.rtcConnection.startVideoCapturer(this.getWindowManager().getDefaultDisplay().getWidth(),
//                        this.getWindowManager().getDefaultDisplay().getHeight(), 30);
            }
        } catch (Exception e) {

        }
    }

    public class StopAction implements Runnable {
        private MainActivity activity;
        public StopAction(MainActivity mainActivity) {
            this.activity = mainActivity;
        }

        @Override
        public void run() {
            // surfaceViewRenderer.clearImage();
            activity.monitorBtn.setText(R.string.monitor_start);
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
