package com.zeropartner.support.mqtt;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import android.zeropartner.support.mqtt.BaseRunnable;

/**
 * @author Sun.bl
 * @version [1.0, 2016/9/26]
 */
public class ProcessRunnable extends BaseRunnable {

    public static final String TAG = "ProcessRunnable";

    /**
     * mqtt接收消息的线程
     *
     * @param context
     * @param topic
     * @param revMsg
     */
    public ProcessRunnable(Context context, String topic, String revMsg) {
        super(context, topic, revMsg);
    }

    @Override
    public void run() {
        super.run();
        Log.i(TAG, "run");
    }
}
