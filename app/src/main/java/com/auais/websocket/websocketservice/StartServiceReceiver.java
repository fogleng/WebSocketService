package com.auais.websocket.websocketservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/*
* 用来接收开机广播 ，开机后 直接启动Service 。
* */
public class StartServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, WebSocketService.class);
        serviceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startService(serviceIntent);
    }
}
 
