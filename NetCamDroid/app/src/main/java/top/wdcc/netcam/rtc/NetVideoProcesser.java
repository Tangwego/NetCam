package top.wdcc.netcam.rtc;

import android.util.Log;

import org.webrtc.VideoFrame;
import org.webrtc.VideoProcessor;
import org.webrtc.VideoSink;

public class NetVideoProcesser implements VideoProcessor {
    private static final String TAG = NetVideoProcesser.class.getSimpleName();
    private VideoSink videoSink;

    public NetVideoProcesser() {

    }

    @Override
    public void setSink(VideoSink videoSink) {
        Log.d(TAG, "set sink...");
        this.videoSink = videoSink;
    }

    @Override
    public void onCapturerStarted(boolean b) {
        if (b) {
            Log.d(TAG, "capturer started...");
        }
    }

    @Override
    public void onCapturerStopped() {
        Log.d(TAG, "capturer stopped...");
    }

    @Override
    public void onFrameCaptured(VideoFrame videoFrame) {
//        Log.d(TAG, "video frame..." + videoFrame.toString());
        if (this.videoSink != null) {
            this.videoSink.onFrame(videoFrame);
        }
        /*
        byte[] y = videoFrame.getBuffer().toI420().getDataY().array();
        byte[] u = videoFrame.getBuffer().toI420().getDataU().array();
        byte[] v = videoFrame.getBuffer().toI420().getDataV().array();


        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream((y.length + Integer.BYTES) + (u.length + Integer.BYTES) + (v.length + Integer.BYTES));
        byteArrayOutputStream.write(y.length);
        byteArrayOutputStream.write(y, 0, y.length);
        byteArrayOutputStream.write(u.length);
        byteArrayOutputStream.write(u, 0, u.length);
        byteArrayOutputStream.write(v.length);
        byteArrayOutputStream.write(v, 0, v.length);
        byte[] videoDatas = byteArrayOutputStream.toByteArray();

        DatagramPacket packet = new DatagramPacket(videoDatas, videoDatas.length, server.getRemote());
        server.sendPacket(packet);

        try {
            byteArrayOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
         */
    }
}
