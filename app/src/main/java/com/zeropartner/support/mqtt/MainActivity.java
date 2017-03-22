package com.zeropartner.support.mqtt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.zeropartner.support.mqtt.MQTTConstant;
import android.zeropartner.support.mqtt.MQTTManager;

import com.zeropartner.support.mqtt.example.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etAccount, etPassword;

    private Button btnStart, btnStop, btnSendMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化管理器

        IntentFilter intentFilter = new IntentFilter(MQTTConstant.ACTION_MESSAGE_RECEIVE);
        intentFilter.addAction("mqtt.lost");
        intentFilter.addAction("mqtt.connect");
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, intentFilter);

        initView();
    }

    private void initView() {

        etAccount = (EditText) findViewById(R.id.et_account);

        etPassword = (EditText) findViewById(R.id.et_password);

        btnStart = (Button) findViewById(R.id.btn_start);
        btnStart.setOnClickListener(this);

        btnStop = (Button) findViewById(R.id.btn_stop);
        btnStop.setOnClickListener(this);

        btnSendMessage = (Button) findViewById(R.id.btn_send_message);
        btnSendMessage.setOnClickListener(this);

    }

    @Override
    protected void onDestroy() {
        if (mMessageReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
            mMessageReceiver = null;
        }
        super.onDestroy();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.equals(action, "mqtt.lost")) {
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
                btnSendMessage.setEnabled(false);
                return;
            }
            if (TextUtils.equals(action, "mqtt.connect")) {
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
                btnSendMessage.setEnabled(true);
                return;
            }
            String message = intent.getStringExtra(MQTTConstant.BROADCAST_RECEIVER_MESSAGE);
            String topic = intent.getStringExtra(MQTTConstant.BROADCAST_RECEIVER_TOPIC);

            Toast.makeText(MainActivity.this, "Topic:" + topic + " message:" + message, Toast.LENGTH_SHORT).show();
        }
    };


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_start:
                startMqtt();
                break;
            case R.id.btn_stop:
                stopMqtt();
                break;
            case R.id.btn_send_message:
                MQTTManager.getInstance(this).publishMessage("support", "support message");
                break;
            default:
                break;
        }

    }

    private void startMqtt() {

        String account = etAccount.getText().toString().trim();
        String password = etPassword.getText().toString().trim();


        MQTTManager.getInstance(this)
                .setClientId("android_support_test") //ClientId
                .setServerURI("tcp://192.168.1.248:1883") //服务器地址
                .setLoginAccount(account, password) //服务器的账号密码
                .setTopic(new String[]{"support"}) //服务器主题
                .setRunnableClass(ProcessRunnable.class) //消息处理线程类
                .setDebugable(true) //是否打印debug
                .setBroadcast(true) //是否要广播
                .start(); //开启服务
    }

    private void stopMqtt() {

        MQTTManager.getInstance(this).stop();

    }


}
