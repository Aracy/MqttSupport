/*******************************************************************************
 * Copyright (c) 2014 IBM Corp.
 * <p/>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * <p/>
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package android.zeropartner.support.mqtt;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttPingSender;
import org.eclipse.paho.client.mqttv3.internal.ClientComms;

/**
 * Default ping sender implementation on Android. It is based on AlarmManager.
 * <p>
 * <p>
 * This class implements the {@link MqttPingSender} pinger interface allowing applications to send ping packet to server every keep alive interval.
 * </p>
 *
 * @see MqttPingSender
 */
class AlarmPingSender implements MqttPingSender {
    // Identifier for Intents, LogUtil messages, etc..
    static final String TAG = "AlarmPingSender";

    // TODO: Add LogUtil.
    private ClientComms comms;

    private Service service;

    private BroadcastReceiver alarmReceiver;

    private AlarmPingSender that;

    private PendingIntent pendingIntent;

    private volatile boolean hasStarted = false;

    public AlarmPingSender(Service service) {
        if (service == null) {
            throw new IllegalArgumentException("Neither service nor client can be null.");
        }
        this.service = service;
        that = this;
    }

    @Override
    public void init(ClientComms comms) {
        this.comms = comms;
        this.alarmReceiver = new AlarmReceiver();
    }

    @Override
    public void start() {
        String action = "MqttService.PingSender." + comms.getClient().getClientId();
        LogUtil.d(TAG, "Register alarmreceiver to MqttService" + action);
        service.registerReceiver(alarmReceiver, new IntentFilter(action));

        pendingIntent = PendingIntent.getBroadcast(service, 0, new Intent(action), PendingIntent.FLAG_UPDATE_CURRENT);

        schedule(comms.getKeepAlive());
        hasStarted = true;
    }

    @Override
    public void stop() {
        // Cancel Alarm.
        AlarmManager alarmManager = (AlarmManager) service.getSystemService(Service.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

        LogUtil.d(TAG, "Unregister alarmreceiver to MqttService" + comms.getClient().getClientId());
        if (hasStarted) {
            hasStarted = false;
            try {
                service.unregisterReceiver(alarmReceiver);
            } catch (IllegalArgumentException e) {
            }
        }
    }

    @Override
    public void schedule(long delayInMilliseconds) {
        long nextAlarmInMilliseconds = System.currentTimeMillis() + delayInMilliseconds;
        LogUtil.d(TAG, "Schedule next alarm at " + nextAlarmInMilliseconds);
        AlarmManager alarmManager = (AlarmManager) service.getSystemService(Service.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, nextAlarmInMilliseconds, pendingIntent);
    }

    /*
     * This class sends PingReq packet to MQTT broker
     */
    class AlarmReceiver extends BroadcastReceiver {
        private WakeLock wakelock;

        private String wakeLockTag = "MqttService.client." + that.comms.getClient().getClientId();

        @Override
        public void onReceive(Context context, Intent intent) {
            // According to the docs, "Alarm Manager holds a CPU wake lock as
            // long as the alarm receiver's onReceive() method is executing.
            // This guarantees that the phone will not sleep until you have
            // finished handling the broadcast.", but this class still get
            // a wake lock to wait for ping finished.
            int count = intent.getIntExtra(Intent.EXTRA_ALARM_COUNT, -1);
            LogUtil.d(TAG, "Ping " + count + " times.");

            LogUtil.d(TAG, "Check time :" + System.currentTimeMillis());
            IMqttToken token = comms.checkForActivity();

            // No ping has been sent.
            if (token == null) {
                return;
            }

            // Assign new callback to token to execute code after PingResq
            // arrives. Get another wakelock even receiver already has one,
            // release it until ping response returns.
            if (wakelock == null) {
                PowerManager pm = (PowerManager) service.getSystemService(Service.POWER_SERVICE);
                wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, wakeLockTag);
            }
            wakelock.acquire();
            token.setActionCallback(new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    LogUtil.d(TAG, "Success. Release lock(" + wakeLockTag + "):" + System.currentTimeMillis());
                    // Release wakelock when it is done.
                    if (wakelock != null && wakelock.isHeld()) {
                        wakelock.release();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    LogUtil.d(TAG, "Failure. Release lock(" + wakeLockTag + "):" + System.currentTimeMillis());
                    // Release wakelock when it is done.
                    if (wakelock != null && wakelock.isHeld()) {
                        wakelock.release();
                    }
                }
            });
        }
    }
}
