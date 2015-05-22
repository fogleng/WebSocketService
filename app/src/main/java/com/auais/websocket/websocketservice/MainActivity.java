package com.auais.websocket.websocketservice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/*
* WebSocketService 的 封装Activity 用来启动 webSocketService .
*  */
public class MainActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, WebSocketService.class));
        this.finish();
    }
}
