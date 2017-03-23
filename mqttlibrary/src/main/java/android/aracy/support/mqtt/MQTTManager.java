package android.aracy.support.mqtt;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

/**
 * MQTT消息管理器
 *
 * @author aracy
 * @version [1.0, 2016/9/26]
 */
public class MQTTManager {

    private Context mContext; //上下文环境

    private static MQTTManager sMQTTManager;

    private MQTTManager(Context context) {
        this.mContext = context.getApplicationContext();
    }

    /**
     * 获取管理器
     *
     * @param context 上下文环境
     * @return 管理器
     */
    public static MQTTManager getInstance(Context context) {
        if (sMQTTManager == null) {
            sMQTTManager = new MQTTManager(context);
        }
        return sMQTTManager;
    }

    /**
     * 设置客户端ID
     *
     * @param clientId 客户端ID
     * @return 管理器
     */
    public MQTTManager setClientId(String clientId) {
        MqttUtil.setClientId(mContext, clientId);
        return this;
    }

    /**
     * 设置服务器URI
     *
     * @param serverURI 服务器地址
     * @return 管理器
     */
    public MQTTManager setServerURI(String serverURI) {
        MqttUtil.setServerURI(mContext, serverURI);
        return this;
    }

    public MQTTManager setRunnableClass(Class<?> runnableClass) {
        if (!BaseRunnable.class.isAssignableFrom(runnableClass)) {
            throw new IllegalArgumentException("runnable must from BaseRunnable!");
        }
        MqttUtil.setRunnableClass(mContext, runnableClass);
        return this;
    }

    /**
     * 设置集群URI数组
     *
     * @param serverURIArray 服务器集群地址
     * @return 管理器
     */
    public MQTTManager setServerURIArray(String[] serverURIArray) {
        MqttUtil.setServerURIArray(mContext, serverURIArray);
        return this;
    }

    /**
     * 设置登录账号
     *
     * @param uerName  账号
     * @param passWord 密码
     * @return 管理器
     */
    public MQTTManager setLoginAccount(String uerName, String passWord) {
        MqttUtil.setLoginAccount(mContext, uerName, passWord);
        return this;
    }

    /**
     * 设置主题
     *
     * @param topic 主题
     * @return 管理器
     */
    public MQTTManager setTopic(String[] topic) {
        MqttUtil.setTopics(mContext, topic);
        return this;
    }

    /***
     * 设置主题
     *
     * @param topic 主题
     * @param qos   主题质量
     * @return 管理器
     */
    public MQTTManager setTopic(String[] topic, int[] qos) {
        MqttUtil.setTopics(mContext, topic);
        MqttUtil.setQoses(mContext, qos);
        return this;
    }

    /**
     * 设置是否需要广播消息(广播为APP内广播)
     *
     * @param broadcast 广播
     * @return 管理器
     */
    public MQTTManager setBroadcast(boolean broadcast) {
        MqttUtil.setBroadcastReceiver(mContext, broadcast);
        return this;
    }

    /***
     * 设置是否debug
     *
     * @param debugable debug标示
     * @return 管理器
     */
    public MQTTManager setDebugable(boolean debugable) {
        LogUtil.sDebug = debugable;
        return this;
    }

    /**
     * 开启服务
     */
    public void start() {
        if (TextUtils.isEmpty(MqttUtil.getClientId(mContext))) {
            throw new IllegalArgumentException("clientId is null");
        } else if (TextUtils.isEmpty(MqttUtil.getServerURI(mContext))) {
            throw new IllegalArgumentException("serverURI is null");
        }
        MqttUtil.setAutoStart(mContext, true);
        Intent intent = new Intent(mContext, AsyncMQTTService.class);
        mContext.startService(intent);
    }

    /**
     * 结束服务
     */
    public void stop() {
        MqttUtil.setAutoStart(mContext, false);
        Intent intent = new Intent(mContext, AsyncMQTTService.class);
        mContext.stopService(intent);
    }

    /***
     * 发送消息
     *
     * @param topic   消息主题
     * @param message 消息
     */
    public void publishMessage(String topic, String message) {
        publishMessage(topic, message, 0, false);
    }

    /***
     * 发布消息
     *
     * @param topic    消息主题
     * @param message  消息
     * @param qos      消息质量
     * @param retained 是否保留副本
     */
    public void publishMessage(String topic, String message, int qos, boolean retained) {
        Intent intent = new Intent(MQTTConstant.ACTION_MESSAGE_PUBLISH);
        intent.putExtra(MQTTConstant.BROADCAST_PUBLISH_TOPIC, topic);
        intent.putExtra(MQTTConstant.BROADCAST_PUBLISH_MESSAGE, message);
        intent.putExtra(MQTTConstant.BROADCAST_PUBLISH_QOS, qos);
        intent.putExtra(MQTTConstant.BROADCAST_PUBLISH_RETAIN, retained);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }


}
