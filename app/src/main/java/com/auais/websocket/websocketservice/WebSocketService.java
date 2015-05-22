package com.auais.websocket.websocketservice;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

import de.tavendo.autobahn.WebSocket;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketConnectionHandler;
import de.tavendo.autobahn.WebSocketException;

public class WebSocketService extends Service {

    static final String TAG = "com.auais.websocket";

    private String room_address = "32011500010100000000";
    private String server_ip = "10.10.0.10";

    ObjectMapper objectMapper = new ObjectMapper();
    MyBroadcastReceiver receiver;

    //用来 Toast 显示相关内容。
    private void alert(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }


    private final WebSocket mConnection = new WebSocketConnection();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        //注册 ACTION_TIME_TICK BroadcastReceive 。
        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
        receiver = new MyBroadcastReceiver();
        registerReceiver(receiver, filter);

        start();
        return super.onStartCommand(intent, flags, startId);
    }

    /*
    * 链接 webSocket
    * */
    private void start() {

        //webSocket Server 对应的 ip 地址以及端口号
        final String wsuri = "ws://" + server_ip + ":" + "9002";

        try {
            //链接对应的 url
            mConnection.connect(wsuri, new WebSocketConnectionHandler() {
                @Override
                public void onOpen() {
                    String s = "{\"cmd\":\"login\",\"params\":{\"identity\":{\"app\":\"manage.apk.auais.com\",\"id\":\"" + room_address + "1" + "\"}}}";

                    //注册上服务器。
                    mConnection.sendTextMessage(s);
                }

                @Override
                public void onTextMessage(String payload) {
//                  alert("接收到消息 " + payload);
                    Logger.d("接收到消息 " + payload);
                    Comm comm;
                    //根据对应的命令 封装对应的 Intent 。
                    Intent intent = new Intent();
                    try {

                        // 转换对应的命令
                        comm = objectMapper.readValue(payload, Comm.class);
                        switch (comm.cmd) {
                            case "SVR_CMD_CARD_AUTH": //卡授权命令
                                intent.setAction("android.auais.UPDATE_CARDINFO_ACTION");
                                intent.putExtra("url", comm.url + room_address);
                                Log.e("CARD_AUTH", comm.url + room_address);
                                break;
                            case "SVR_CMD_SYSTEM_NORMAL_UPGRADE"://普通升级命令
                                intent.setAction("android.auais.UPDATE_SYSTEM_ACTION");
                                intent.putExtra("url", comm.url);
                                Log.e("SYSTEM_NORMAL_UPGRADE", comm.url);
                                break;
                            case "SVR_CMD_SYSTEM_IMMEDIATELY_UPGRADE"://立即升级命令
                                intent.setAction("android.auais.UPDATE_IMMEDIATELY_SYSTEM_ACTION");
                                intent.putExtra("url", comm.url);
                                Log.e("UPDATEIMMEDIATELYSYSTEM", comm.url);
                                break;
                            case "SVR_CMD_FILE_DOWNLOAD"://文件下载
                                intent.setAction("android.auais.FILE_DOWNLOAD_ACTION");
                                intent.putExtra("url", comm.url);
                                Log.e("FILE_DOWNLOAD", comm.url);
                                break;
                            case "SVR_CMD_SERVER_ALARM_NOTIFICATION"://通知命令
                                intent.setAction("android.auais.SERVER_ALARM_NOTIFICATION_ACTION");
                                intent.putExtra("url", comm.url);
                                Log.e("ALARM_NOTIFICATION", comm.url);
                                break;
                            case "SVR_CMD_REMOTE_UNLOCK"://远端开门
                                intent.setAction("android.auais.REMOTE_UNLOCK_ACTION");
                                intent.putExtra("url", comm.url + room_address);
                                Log.e("REMOTE_UNLOCK", comm.url);
                                break;
                            case "SVR_CMD_SYSTEM_FORCE_UPGRADE"://强制升级命令
                                intent.setAction("android.auais.SYSTEM_FORCE_UPGRADE_ACTION");
                                intent.putExtra("url", comm.url);
                                Log.e("SYSTEM_FORCE_UPGRADE", comm.url);
                                break;
                        }
                        //发送对应的广播。
                        sendBroadcast(intent);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Logger.d(e + "");
                    }
                }

                @Override
                public void onClose(int code, String reason) {
//                   alert("链接已关闭");
                    Logger.d("Websocket 链接已关闭");
                }
            });
        } catch (WebSocketException e) {
            Log.d(TAG, e.toString());
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mConnection.isConnected()) {
            mConnection.disconnect();
        }
    }

    @Override

    public IBinder onBind(Intent intent) {
        return null;
    }

    class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //每隔一分钟 检测 WebSocket 链接状态 ，如果未连接 直接重连。
            if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                if (mConnection.isConnected()) {
                    Logger.d("Websocket 连接中");
                } else {
                    Logger.d("检测 到 Websocket 连接断开 开始重连");
                    start();
                }
            }
        }
    }
}
