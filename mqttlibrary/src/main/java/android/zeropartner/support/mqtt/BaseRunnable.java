package android.zeropartner.support.mqtt;

import android.content.Context;

/**
 * @author zl.peng
 * @version [1.0, 2016-07-05]
 */
public class BaseRunnable implements Runnable {

    protected Context mContext;

    protected String mTopic;

    protected String mRevMsg;

    /**
     * mqtt接收消息的线程
     */
    public BaseRunnable(Context context, String topic, String revMsg) {
        this.mTopic = topic;
        this.mRevMsg = revMsg;
        this.mContext = context;
    }

    @Override
    public void run() {

    }


}
