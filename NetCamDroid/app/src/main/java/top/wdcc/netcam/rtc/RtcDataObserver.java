package top.wdcc.netcam.rtc;

import android.util.Log;

import org.webrtc.DataChannel;


public class RtcDataObserver implements DataChannel.Observer {
    private static final String TAG = RtcDataObserver.class.getSimpleName();

    public RtcDataObserver() {

    }

    @Override
    public void onBufferedAmountChange(long l) {
        Log.d(TAG, "onBufferedAmountChange: " + l);
    }

    @Override
    public void onStateChange() {
        Log.d(TAG, "onStateChange");
    }

    @Override
    public void onMessage(DataChannel.Buffer buffer) {
        Log.d(TAG, "onMessage");
    }
}
