package top.wdcc.netcam.client;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

public class NetCamApp extends MultiDexApplication {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }
}
