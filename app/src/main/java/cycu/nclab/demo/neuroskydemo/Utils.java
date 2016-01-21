package cycu.nclab.demo.neuroskydemo;

import android.util.Log;

/**
 * Created by hhliu on 2016/1/10.
 */
public class Utils {

    private static final String TAG = "Utils";

    public static final void logThread() {
        Thread t = Thread.currentThread();
        Log.d(TAG,
                "<" + t.getName() + ">id: " + t.getId() + ", Priority: "
                        + t.getPriority() + ", Group: "
                        + t.getThreadGroup().getName());
    }
}