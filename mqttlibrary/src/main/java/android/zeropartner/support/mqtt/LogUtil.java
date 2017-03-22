package android.zeropartner.support.mqtt;

import android.text.TextUtils;
import android.util.Log;

/***
 * Log的工具类
 *
 * @author Sun.bl
 * @version [1.0, 2016/2/29]
 */
public class LogUtil {

    static boolean sDebug = true;

    private LogUtil() {
        throw new AssertionError("this is util class");
    }


    static void i(String tag, String msg) {
        if (!sDebug) {
            return;
        }
        msg = judgeMsg(msg);
        Log.i(tag, msg);
    }

    static void e(String tag, String msg) {
        if (!sDebug) {
            return;
        }
        msg = judgeMsg(msg);
        Log.e(tag, msg);
    }

    static void w(String tag, String msg) {

        if (!sDebug) {
            return;
        }
        msg = judgeMsg(msg);
        Log.w(tag, msg);

    }

    static void v(String tag, String msg) {

        if (!sDebug) {
            return;
        }
        msg = judgeMsg(msg);
        Log.v(tag, msg);

    }

    static void d(String tag, String msg) {

        if (!sDebug) {
            return;
        }
        msg = judgeMsg(msg);
        Log.d(tag, msg);

    }

    static void d(String tag, String msg, Throwable tr) {
        if (!sDebug) {
            return;
        }
        msg = judgeMsg(msg);
        Log.d(tag, msg, tr);
    }

    static void e(String tag, String msg, Throwable tr) {
        if (!sDebug) {
            return;
        }
        msg = judgeMsg(msg);
        Log.e(tag, msg, tr);
    }

    static String judgeMsg(String msg) {

        if (msg == null) {
            return "msg为空指针";
        }
        if (TextUtils.equals(msg, "")) {
            return "msg为空字符";
        }

        return msg;

    }


}
