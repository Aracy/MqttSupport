package android.zeropartner.support.mqtt;

/**
 * MQTT连接常量
 *
 * @author zl.peng
 * @version [1.0, 2016-07-04]
 */
public class MQTTConstant {

    // 广播KEY
    static final String BROADCAST_PUBLISH_TOPIC = "publishTopicBroadcast";

    static final String BROADCAST_PUBLISH_MESSAGE = "publishMessageBroadcast";

    static final String BROADCAST_PUBLISH_QOS = "publishQosBroadcast";

    static final String BROADCAST_PUBLISH_RETAIN = "publishRetainBroadcast";

    public static final String BROADCAST_RECEIVER_TOPIC = "receiveTopic";

    public static final String BROADCAST_RECEIVER_MESSAGE = "receiveMessage";


    // 广播ACTION

    /**
     * 发布消息的Action
     */
    public static final String ACTION_MESSAGE_PUBLISH = "com.zeropartner.support.publishMessageAction";

    public static final String ACTION_MESSAGE_RECEIVE = "com.zeropartner.support.MessageReceived";


}
