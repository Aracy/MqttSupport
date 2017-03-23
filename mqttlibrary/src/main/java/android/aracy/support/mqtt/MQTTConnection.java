package android.aracy.support.mqtt;


import android.app.Service;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


/**
 * MQTT的实例连接类
 */
class MQTTConnection {

    private static final String TAG = "MQTTConnection";

    private final static boolean CLEAN_START = false; // 连接服务器前是否清空上一次连接的订阅主题和没有接收的消息

    private final static short KEEP_ALIVE = 60;// 低耗网络，但是又需要及时获取数据，心跳30s

    private MqttAsyncClient mClient;

    private MqttConnectOptions mOptions;

    private MqttClientPersistence mPersistence;

    private AlarmPingSender pingSender;

    public MQTTConnection(String serverURI, String clientId, MqttCallback mqttCallback)
            throws MqttException {
        mOptions = new MqttConnectOptions();
        mOptions.setCleanSession(CLEAN_START);
        mOptions.setConnectionTimeout(15);
        mOptions.setKeepAliveInterval(KEEP_ALIVE);
        // mOptions.setWill("zeropartner/mqttLost", clientId.getBytes(), 0, false);

        mPersistence = new MemoryPersistence();
        mClient = new MqttAsyncClient(serverURI, clientId, mPersistence);
        mClient.setCallback(mqttCallback);
    }

    public MQTTConnection(String serverURI, String clientId, MqttCallback mqttCallback, Service service)
            throws MqttException {
        mOptions = new MqttConnectOptions();
        mOptions.setCleanSession(CLEAN_START);
        mOptions.setConnectionTimeout(15);
        mOptions.setKeepAliveInterval(KEEP_ALIVE);
        // mOptions.setWill("zeropartner/mqttLost", clientId.getBytes(), 0, false);

        pingSender = new AlarmPingSender(service);
        mPersistence = new MemoryPersistence();
        mClient = new MqttAsyncClient(serverURI, clientId, mPersistence, pingSender);
        mClient.setCallback(mqttCallback);
    }

    /**
     * 连接MQTT
     *
     * @param actionListener mqtt连接的监听事件
     * @see [类、类#方法、类#成员]
     */
    public synchronized void connect(IMqttActionListener actionListener) {
        if (mClient == null) {
            LogUtil.i(TAG, "MqttClient为空");
            return;
        }
        if (mClient.isConnected()) {
            LogUtil.i(TAG, "Mqtt已经正常连接");
            return;
        }
        try {
            mClient.connect(mOptions, null, actionListener);
        } catch (MqttException e) {
            switch (e.getReasonCode()) {
                case MqttException.REASON_CODE_CLIENT_CONNECTED:
                    LogUtil.e(TAG, "Mqtt已经正常连接");
                    return;
                case MqttException.REASON_CODE_CLIENT_ALREADY_DISCONNECTED:
                    LogUtil.e(TAG, "Mqtt已经手动断开连接");
                    return;
                case MqttException.REASON_CODE_CLIENT_DISCONNECTING:
                    LogUtil.e(TAG, "Mqtt正在断开连接");
                    return;
                case MqttException.REASON_CODE_CONNECT_IN_PROGRESS:
                    LogUtil.e(TAG, "Mqtt正在连接");
                    return;
                case MqttException.REASON_CODE_CLIENT_CLOSED:
                    LogUtil.e(TAG, "Mqtt已经关闭");
                    return;
                default:
                    break;
            }
            e.printStackTrace();
        }
    }

    /**
     * 断开MQTT连接
     *
     * @param actionListener 断开连接的监听时间按
     * @see [类、类#方法、类#成员]
     */
    public void disConnect(IMqttActionListener actionListener) {
        if (mClient == null) {
            LogUtil.i(TAG, "MqttClient为空");
            return;
        }
        if (!mClient.isConnected()) {
            LogUtil.i(TAG, "Mqtt已经断开连接");
            return;
        }
        try {
            mClient.disconnect(null, actionListener);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭MQTT
     *
     * @see [类、类#方法、类#成员]
     */
    public void close() {
        if (mClient == null) {
            LogUtil.i(TAG, "MqttClient为空");
            return;
        }
        try {
            if (pingSender != null) {
                pingSender.stop();
            }
            mClient.close();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 订阅主题
     *
     * @param topics
     * @param qos
     * @see [类、类#方法、类#成员]
     */
    public void subscribe(String[] topics, int[] qos) {
        if (mClient == null || !mClient.isConnected()) {
            LogUtil.e(TAG, "主题订阅失败，原因：无可用的MQTT连接");
            return;
        }
        if (topics == null || topics.length == 0) {
            return;
        }
        if (qos == null || topics.length != qos.length) {
            qos = new int[topics.length];
        }
        try {
            mClient.subscribe(topics, qos, null, new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken arg0) {
                    LogUtil.i(TAG, "订阅成功");
                }

                @Override
                public void onFailure(IMqttToken arg0, Throwable arg1) {
                    LogUtil.e(TAG, "订阅失败");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void unSubscribe(String[] topics) {
        if (mClient == null || !mClient.isConnected()) {
            LogUtil.e(TAG, "主题取消失败，原因：无可用的MQTT连接");
            return;
        }
        if (topics == null || topics.length == 0) {
            return;
        }
        try {
            mClient.unsubscribe(topics, null, null);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 推送Mqtt消息
     *
     * @param topic   推送主题
     * @param message 推送消息
     * @see [类、类#方法、类#成员]
     */
    public void publishMqttMessage(String topic, MqttMessage message) {
        if (mClient == null || !mClient.isConnected()) {
            LogUtil.e(TAG, "消息发布失败，原因：无可用的MQTT连接");
            return;
        }
        try {
            mClient.publish(topic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断Mqtt是否已经连接
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    public boolean isConnected() {
        if (mClient == null) {
            return false;
        }
        return mClient.isConnected();
    }

    /**
     * 设置账号
     *
     * @param userName
     * @see [类、类#方法、类#成员]
     */
    public void setUserName(String userName) {
        if (userName == null) {
            return;
        }
        mOptions.setUserName(userName);
    }

    /**
     * 设置密码
     *
     * @param passWord
     * @see [类、类#方法、类#成员]
     */
    public void setPassWord(String passWord) {
        if (passWord == null) {
            return;
        }
        mOptions.setPassword(passWord.toCharArray());
    }

    /**
     * 设置是否清楚会话
     *
     * @param cleanSession
     * @see [类、类#方法、类#成员]
     */
    public void setCleanSession(boolean cleanSession) {
        mOptions.setCleanSession(cleanSession);
    }

    /**
     * 设置心跳时间
     *
     * @param keepAlive
     * @see [类、类#方法、类#成员]
     */
    public void setKeepAlive(int keepAlive) {
        mOptions.setKeepAliveInterval(keepAlive);
    }

    /**
     * 获取连接的用户名
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    public String getUserName() {
        return mOptions.getUserName();
    }

    /**
     * 获取连接密码
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    public String getPassWord() {
        return mOptions.getPassword().toString();
    }

    /**
     * 获取MQTT的连接心跳
     *
     * @return 返回心跳间隔时间
     * @see [类、类#方法、类#成员]
     */
    public int getKeepaLive() {
        return mOptions.getKeepAliveInterval();
    }

    /**
     * 判断连接是否清除回话
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    public boolean isCleanSession() {
        return mOptions.isCleanSession();
    }

    /**
     * 设置服务器集群地址
     *
     * @param serverURIs 集群服务器地址
     */
    public void setServerURIs(String[] serverURIs) {
        if (serverURIs == null || serverURIs.length == 0) {
            return;
        }
        mOptions.setServerURIs(serverURIs);
    }

}
