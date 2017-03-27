package android.aracy.support.mqtt;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;


import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Message Service
 *
 * @author aracy
 * @version [1.0, 2016-07-04]
 */
public class AsyncMQTTService extends Service implements MqttCallback {

    public static final String TAG = "AsyncMQTTService";

    private static final int MQTT_CONNECT = 1002;
    /**
     * 是否有网络连接
     */
    private volatile boolean mNetConnected;
    /**
     * 网络状态改变的消息接收者
     */
    private NetWorkManagerReceiver mManagerReceiver;

    /**
     * 内部广播管理器
     */
    private LocalBroadcastManager mBroadcastManager;
    /**
     * 推送消息的广播接收者
     */
    private PublishMessageReceiver mMessageReceiver;
    /**
     * MQTT的重连次数
     */
    private int mMQTTConnectCount;
    /**
     * MQTT连接
     */
    private MQTTConnection mConnection;
    /**
     * 重连的消息处理者
     */
    private ReConnectHandler connectHandler;
    /**
     * 线程池
     */
    private ExecutorService mPool;
    /**
     * 订阅主题
     */
    private String[] topics;
    /**
     * 服务器地址
     */
    private String mServerUri;
    /**
     * 用户名
     */
    private String mUserName;
    /**
     * 密码
     */
    private String mPassWord;
    /**
     * 连接ID
     */
    private String mClientId;
    /**
     * 订阅主题的消息质量
     */
    private int[] qoses;

    @Override
    public void onCreate() {
        super.onCreate();
        initData();
        registerBroadcastReceiver();
    }

    /**
     * 初始化数据
     *
     * @see [类、类#方法、类#成员]
     */
    private void initData() {
        mMQTTConnectCount = 0;
        connectHandler = new ReConnectHandler(this);
        // 初始化线程池
        int threadCount = Runtime.getRuntime().availableProcessors() * 3;
        mPool = Executors.newFixedThreadPool(threadCount);
        // 初始化MQTT
        getMQTTParameterFromLocal();
        if (TextUtils.isEmpty(mClientId)) {
            // 如果Client为空则连接Mqtt的Service就没有运行的必要的必要
            stopSelf();
            return;
        }
        try {
            mConnection = new MQTTConnection(mServerUri, mClientId, this, this);
            mConnection.setUserName(mUserName);
            mConnection.setPassWord(mPassWord);
            mConnection.setCleanSession(false);
            String[] serverURIs = MqttUtil.getServerURIs(this);
            mConnection.setServerURIs(serverURIs);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 注册广播接收者
     *
     * @see [类、类#方法、类#成员]
     */
    private void registerBroadcastReceiver() {
        mBroadcastManager = LocalBroadcastManager.getInstance(this);

        // 注册发布消息的广播接收者
        mMessageReceiver = new PublishMessageReceiver();
        IntentFilter messageFilter = new IntentFilter(MQTTConstant.ACTION_MESSAGE_PUBLISH);
        messageFilter.addAction("stop");
        mBroadcastManager.registerReceiver(mMessageReceiver, messageFilter);

        // 注册网络状态改变的广播接收者
        mManagerReceiver = new NetWorkManagerReceiver();
        IntentFilter netWorkFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mManagerReceiver, netWorkFilter);

    }

    /***
     * MQTT连接
     */
    public void connectMQTT() {
        if (mConnection == null || mConnection.isConnected()) {
            return;
        }
        mConnection.connect(new MqttConnectListener());
    }

    /**
     * 获取本地存储的MQTT连接数据
     *
     * @see [类、类#方法、类#成员]
     */
    private void getMQTTParameterFromLocal() {

        mClientId = MqttUtil.getClientId(this);
        mServerUri = MqttUtil.getServerURI(this);

        String[] account = MqttUtil.getLoginAccount(this);
        mUserName = account[0];
        mPassWord = account[1];

        topics = MqttUtil.getTopics(this);
        qoses = MqttUtil.getQoses(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, " AsyncMQTTService onStartCommand");
        boolean autoStart = MqttUtil.getAutoStart(this);
        if (!autoStart) {
            stopSelf();
            return START_NOT_STICKY;
        }
        if (mConnection != null) {
            mConnection.connect(new MqttConnectListener());
        }
        return START_STICKY;
    }

    @Override
    public void connectionLost(Throwable cause) {
        LogUtil.e(TAG, "MQTT断开连接...");
        afterConnectFailed();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String msg = new String(message.getPayload(), "UTF-8");
        LogUtil.i(TAG, "topic:" + topic + " message:" + new String(message.getPayload(), "UTF-8"));
        Class<?> runnableClass = MqttUtil.getRunnableClass(this);
        if (runnableClass != null) {
            Constructor<?> constructor = runnableClass.getConstructor(Context.class, String.class, String.class);
            BaseRunnable runnable = (BaseRunnable) constructor.newInstance(this, topic, msg);
            mPool.execute(runnable);
        }
        if (MqttUtil.isBroadCast(this)) {
            Intent intent = new Intent(MQTTConstant.ACTION_MESSAGE_RECEIVE);
            intent.putExtra(MQTTConstant.BROADCAST_RECEIVER_TOPIC, topic);
            intent.putExtra(MQTTConstant.BROADCAST_RECEIVER_MESSAGE, msg);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        boolean complete = token.isComplete();
        LogUtil.i(TAG, complete ? "推送完成" : "推送失败");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.i(TAG, "onDestroy");
        // 释放
        releaseMQTTConnection();
        // 关闭线程池
        if (mPool != null && !mPool.isShutdown()) {
            mPool.shutdown();
        }
        // 清空Handler的消息队列
        clearHandlerMessage();
        // 反注册
        unregisterBroadcastReceiver();
    }

    /***
     * 释放MQTT资源
     *
     * @see [类、类#方法、类#成员]
     */
    private void releaseMQTTConnection() {
        if (mConnection == null) {
            return;
        }
        mConnection.disConnect(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                LocalBroadcastManager.getInstance(AsyncMQTTService.this).sendBroadcast(new Intent(MQTTConstant.ACTION_CONNECT_LOST));
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {

            }
        });
        mConnection.close();
    }

    /***
     * 清空Handler的消息队列
     *
     * @see [类、类#方法、类#成员]
     */
    private void clearHandlerMessage() {
        // 连接消息队列
        if (connectHandler != null) {
            connectHandler.removeCallbacksAndMessages(null);
            connectHandler = null;
        }
    }

    /**
     * 反注册广播接收者
     *
     * @see [类、类#方法、类#成员]
     */
    private void unregisterBroadcastReceiver() {
        if (mMessageReceiver != null) {
            mBroadcastManager.unregisterReceiver(mMessageReceiver);
            mMessageReceiver = null;
        }

        if (mManagerReceiver != null) {
            unregisterReceiver(mManagerReceiver);
            mManagerReceiver = null;
        }
    }

    /**
     * 重新连接的消息处理者
     *
     * @author sun.bl
     * @version [1.0, 2015-11-18]
     */
    private static class ReConnectHandler extends Handler {

        private WeakReference<AsyncMQTTService> wrService;

        public ReConnectHandler(AsyncMQTTService service) {
            wrService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            AsyncMQTTService service = wrService.get();
            if (service == null) {
                LogUtil.i("ReConnectHandler", "Service已经被回收");
                return;
            }
            if (msg.what == MQTT_CONNECT) {
                service.connectMQTT();
            }
        }
    }

    /**
     * MQTT链接监听事件
     *
     * @author sun.bl
     * @version [1.0, 2015-11-17]
     * @see [相关类/方法]
     * @since [产品/模块版本]
     */
    private class MqttConnectListener implements IMqttActionListener {
        @Override
        public void onFailure(IMqttToken token, Throwable e) {
            LogUtil.e(TAG, "MQTT连接失败...");
            e.printStackTrace();
            afterConnectFailed();
            LocalBroadcastManager.getInstance(AsyncMQTTService.this).sendBroadcast(new Intent(MQTTConstant.ACTION_CONNECT_LOST));
        }

        @Override
        public void onSuccess(IMqttToken token) {
            LogUtil.i(TAG, "MQTT连接成功...");
            subscribe();
            mMQTTConnectCount = 0;
            connectHandler.removeMessages(MQTT_CONNECT);
            LocalBroadcastManager.getInstance(AsyncMQTTService.this).sendBroadcast(new Intent(MQTTConstant.ACTION_CONNECT_SUCCESS));
        }
    }

    /**
     * 网络状态改变的广播接收者
     *
     * @author sun.bl
     * @version [1.0, 2015-11-18]
     */
    private class NetWorkManagerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!TextUtils.equals(ConnectivityManager.CONNECTIVITY_ACTION, intent.getAction())) {
                return;
            }
            mNetConnected = MqttUtil.isNetworkConnected(context);
            LogUtil.i(TAG, "mNetConnected:" + mNetConnected);
            if (mNetConnected) {
                mConnection.connect(new MqttConnectListener());
            }
        }
    }

    /**
     * 重连失败之后
     *
     * @see [类、类#方法、类#成员]
     */
    private void afterConnectFailed() {
        if (!mNetConnected) {
            LogUtil.e(TAG, "没有可用的网络连接，等待网络连接");
            return;
        }
        if (mMQTTConnectCount >= 5) {
            connectHandler.sendEmptyMessageDelayed(MQTT_CONNECT, 300000);
            LogUtil.i(TAG, "等待5分钟重连...");
            return;
        }
        connectHandler.sendEmptyMessageDelayed(MQTT_CONNECT, mMQTTConnectCount * 5000);
        LogUtil.i(TAG, "开始第" + mMQTTConnectCount + "次重连，" + mMQTTConnectCount * 5 + "秒后重连...");
        mMQTTConnectCount += 1;
    }

    private void subscribe() {
        mConnection.unSubscribe(topics);
        mConnection.subscribe(topics, qoses);
    }

    /**
     * 发送信息的广播接收者
     *
     * @author sun.bl
     * @version [1.0, 2015-11-18]
     */
    private class PublishMessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.equals(action, "stop")) {
                releaseMQTTConnection();
                return;
            }
            // ACTION验证
            if (!TextUtils.equals(MQTTConstant.ACTION_MESSAGE_PUBLISH, intent.getAction()) || mConnection == null) {
                return;
            }

            String topic = intent.getStringExtra(MQTTConstant.BROADCAST_PUBLISH_TOPIC);
            String message = intent.getStringExtra(MQTTConstant.BROADCAST_PUBLISH_MESSAGE);
            // 参数验证
            if (TextUtils.isEmpty(message) || TextUtils.isEmpty(topic)) {
                return;
            }
            int qos = intent.getIntExtra(MQTTConstant.BROADCAST_PUBLISH_QOS, 0);
            boolean retained = intent.getBooleanExtra(MQTTConstant.BROADCAST_PUBLISH_RETAIN, false);
            // 构建MQTTMessage
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            mqttMessage.setQos(qos);
            mqttMessage.setRetained(retained);

            mConnection.publishMqttMessage(topic, mqttMessage);
        }
    }
}
