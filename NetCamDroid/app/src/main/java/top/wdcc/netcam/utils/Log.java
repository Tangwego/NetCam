package top.wdcc.netcam.utils;



public class Log {

    public static int v(String TAG, String format, Object...objs) {
        return android.util.Log.v(TAG, String.format(format, objs));
    }

    public static int v(String TAG, Throwable tr, String format, Object...objs) {
        return android.util.Log.v(TAG, String.format(format, objs), tr);
    }

    public static int d(String TAG, String format, Object...objs) {
        return android.util.Log.d(TAG, String.format(format, objs));
    }

    public static int d(String TAG, Throwable tr, String format, Object...objs) {
        return android.util.Log.d(TAG, String.format(format, objs), tr);
    }
    public static int i(String TAG, String format, Object...objs) {
        return android.util.Log.i(TAG, String.format(format, objs));
    }

    public static int i(String TAG, Throwable tr, String format, Object...objs) {
        return android.util.Log.i(TAG, String.format(format, objs), tr);
    }

    public static int w(String TAG, String format, Object...objs) {
        return android.util.Log.w(TAG, String.format(format, objs));
    }

    public static int w(String TAG, Throwable tr, String format, Object...objs) {
        return android.util.Log.w(TAG, String.format(format, objs), tr);
    }

    public static int e(String TAG, String format, Object...objs) {
        return android.util.Log.e(TAG, String.format(format, objs));
    }

    public static int e(String TAG, Throwable tr, String format, Object...objs) {
        return android.util.Log.e(TAG, String.format(format, objs), tr);
    }
}
