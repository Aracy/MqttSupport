package android.aracy.support.mqtt;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

/**
 *
 *
 * @author aracy
 * @version [1.0, 2016-07-04]
 */
class MqttUtil {

    private static final String MQTT_FILE_NAME = "mqttFile";

    private static final String MQTT_SERVER_URI = "mqttServerUri";

    private static final String MQTT_CLIENT_ID = "mqttClientId";

    private static final String MQTT_LOGIN_USERNAME = "mqttLoginUserName";

    private static final String MQTT_LOGIN_PASSWORD = "mqttLoginPassword";

    private static final String MQTT_SUBCRIBE_TOPICS = "mqttSubcribeTopics";

    private static final String MQTT_SUBCRIBE_QOSES = "mqttSubcribeQoses";

    private static final String MQTT_SERVER_URI_ARRAY = "mqttServerArray";

    private static final String MQTT_SERVICE_AUTO_START = "MQTTServiceStartAuto";

    private static final String MQTT_RUNNABLE_CLASS_NAME = "MQTTRunnable";

    private static final String MQTT_BROADCAST = "MQTTBroadcast";

    /**
     * 设置服务器的URI
     *
     * @param context   上下文环境
     * @param serverUri 服务器地址
     * @see [类、类#方法、类#成员]
     */
    static void setServerURI(Context context, String serverUri) {
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(MQTT_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(MQTT_SERVER_URI, serverUri);

        editor.apply();
    }

    /**
     * 设置多组服务器地址
     *
     * @param context    上下文环境
     * @param serverURIs 服务器地址
     */
    static void setServerURIArray(Context context, String[] serverURIs) {

        if (serverURIs == null || serverURIs.length == 0) {
            return;
        }

        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(MQTT_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        StringBuilder builder = new StringBuilder(serverURIs[0]);

        for (int i = 1; i < serverURIs.length; i++) {
            builder.append(",").append(serverURIs[i]);
        }

        editor.putString(MQTT_SERVER_URI_ARRAY, builder.toString());
        editor.apply();

    }

    /**
     * 设置ClientID
     *
     * @param context  上下文环境
     * @param clientId MQTT连接标识符
     * @see [类、类#方法、类#成员]
     */
    static void setClientId(Context context, String clientId) {
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(MQTT_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(MQTT_CLIENT_ID, clientId);

        editor.apply();
    }

    /***
     * 设置服务器的登录账号
     *
     * @param context  上下文环境
     * @param userName 登录名称
     * @param passWord 登录密码
     * @see [类、类#方法、类#成员]
     */
    static void setLoginAccount(Context context, String userName, String passWord) {
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(MQTT_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(MQTT_LOGIN_USERNAME, userName);
        editor.putString(MQTT_LOGIN_PASSWORD, passWord);

        editor.apply();
    }

    /**
     * 设置订阅主题
     *
     * @param context 上下文环境
     * @param topics  主题
     * @see [类、类#方法、类#成员]
     */
    static void setTopics(Context context, String[] topics) {
        if (topics == null) {
            return;
        }
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(MQTT_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        StringBuilder builder = new StringBuilder();
        for (String topic : topics) {
            builder.append(TextUtils.isEmpty(builder) ? "" : ",").append(topic);
        }

        editor.putString(MQTT_SUBCRIBE_TOPICS, builder.toString());
        editor.apply();
    }

    /***
     * 设置主题质量
     *
     * @param context 上下文环境
     * @param Qoses   主题质量
     * @see [类、类#方法、类#成员]
     */
    static void setQoses(Context context, int[] Qoses) {
        if (Qoses == null) {
            return;
        }
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(MQTT_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        StringBuilder builder = new StringBuilder(Qoses[0]);

        for (int i = 1; i < Qoses.length; i++) {
            builder.append(TextUtils.isEmpty(builder) ? "" : ",").append(Qoses[i]);
        }

        editor.putString(MQTT_SUBCRIBE_QOSES, builder.toString());
        editor.apply();
    }

    /**
     * 存储处理消息的class
     *
     * @param context       上下文环境
     * @param runnableClass 处理消息的Class
     */
    static void setRunnableClass(Context context, Class<?> runnableClass) {
        if (runnableClass == null) {
            return;
        }
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(MQTT_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(MQTT_RUNNABLE_CLASS_NAME, runnableClass.getName());
        editor.apply();
    }

    /**
     * 设置是否启动
     *
     * @param context   上下文环境
     * @param autoStart 自动启动
     */
    static void setAutoStart(Context context, boolean autoStart) {
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(MQTT_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(MQTT_SERVICE_AUTO_START, autoStart);

        editor.apply();
    }

    /**
     * 存储是否广播
     *
     * @param context   上下文环境
     * @param broadcast 广播
     */
    static void setBroadcastReceiver(Context context, boolean broadcast) {

        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(MQTT_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(MQTT_BROADCAST, broadcast);
        editor.apply();
    }


    /**
     * 获取ClientId
     *
     * @param context 上下文环境
     * @return clientId
     * @see [类、类#方法、类#成员]
     */
    static String getClientId(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(MQTT_FILE_NAME, Context.MODE_PRIVATE);

        return preferences.getString(MQTT_CLIENT_ID, "");
    }

    /**
     * 获取服务器的URI
     *
     * @param context 上下文环境
     * @return 服务器地址
     * @see [类、类#方法、类#成员]
     */
    static String getServerURI(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(MQTT_FILE_NAME, Context.MODE_PRIVATE);

        return preferences.getString(MQTT_SERVER_URI, "");
    }

    /***
     * 获取登录服务器的账号
     *
     * @param context 上下文环境
     * @return string[0] 登录名称 string[1] 登录密码
     * @see [类、类#方法、类#成员]
     */
    static String[] getLoginAccount(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(MQTT_FILE_NAME, Context.MODE_PRIVATE);

        String[] acctount = new String[2];
        acctount[0] = preferences.getString(MQTT_LOGIN_USERNAME, null);
        acctount[1] = preferences.getString(MQTT_LOGIN_PASSWORD, null);

        return acctount;
    }

    /**
     * 判断是否要自动启动
     *
     * @param context 上下文环境
     * @return 自动启动
     */
    static boolean getAutoStart(Context context) {

        SharedPreferences preferences = context.getSharedPreferences(MQTT_FILE_NAME, Context.MODE_PRIVATE);

        return preferences.getBoolean(MQTT_SERVICE_AUTO_START, false);
    }


    /**
     * 获取订阅主题
     *
     * @param context 上下文环境
     * @return 主题
     * @see [类、类#方法、类#成员]
     */
    static String[] getTopics(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(MQTT_FILE_NAME, Context.MODE_PRIVATE);
        String topicsStr = preferences.getString(MQTT_SUBCRIBE_TOPICS, null);

        if (TextUtils.isEmpty(topicsStr)) {
            return null;
        }

        return TextUtils.split(topicsStr, ",");
    }

    /**
     * 获取主题质量
     *
     * @param context 上下文环境
     * @return 主题质量
     * @see [类、类#方法、类#成员]
     */
    static int[] getQoses(Context context) {
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(MQTT_FILE_NAME, Context.MODE_PRIVATE);
        String qosesStr = sharedPreferences.getString(MQTT_SUBCRIBE_QOSES, "");

        if (TextUtils.isEmpty(qosesStr)) {
            return null;
        }
        String[] qosesStrArray = TextUtils.split(qosesStr, ",");

        int[] Qoses = new int[qosesStrArray.length];
        try {
            for (int i = 0; i < qosesStrArray.length; i++) {
                Qoses[i] = Integer.valueOf(qosesStrArray[i]);
            }
        } catch (Exception exception) {
            return null;
        }

        return Qoses;
    }

    /**
     * 获取用于处理消息的Class
     *
     * @param context 上下文环境
     * @return 处理消息的Class
     * @throws ClassNotFoundException
     */
    static Class<?> getRunnableClass(Context context) throws ClassNotFoundException {
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(MQTT_FILE_NAME, Context.MODE_PRIVATE);

        String className = sharedPreferences.getString(MQTT_RUNNABLE_CLASS_NAME, "");
        if (TextUtils.isEmpty(className)) {
            return null;
        }

        return Class.forName(className);
    }

    /**
     * 获取服务器地址的数组
     *
     * @param context 上下文环境
     * @return 服务器地址数组
     */
    static String[] getServerURIs(Context context) {
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(MQTT_FILE_NAME, Context.MODE_PRIVATE);
        String uris = sharedPreferences.getString(MQTT_SERVER_URI_ARRAY, "");

        if (TextUtils.isEmpty(uris)) {
            return null;
        }
        return uris.split(",");
    }

    /**
     * 获取存储的广播标记
     *
     * @param context 上下文环境
     * @return 是否广播
     */
    static boolean isBroadCast(Context context) {
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(MQTT_FILE_NAME, Context.MODE_PRIVATE);

        return sharedPreferences.getBoolean(MQTT_BROADCAST, false);
    }

    /**
     * 判断是否有网络连接
     *
     * @param context 上下文，用来获取连接服务
     * @return 网络状态
     */
    static boolean isNetworkConnected(Context context) {
        if (context == null) {
            return false;
        }

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo[] info = cm.getAllNetworkInfo();
        if (info == null) {
            return false;
        }
        for (NetworkInfo networkInfo : info) {
            if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                return true;
            }
        }

        return false;
    }
}
