package com.example.dlehd.gazuua.Chat;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.StrictMode;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.dlehd.gazuua.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

/**
 * 서버와 소켓통신하는 서비스. 클라이언트(액티비티)의 메시지를 서버로 전달하고, 서버로부터 온 메시지를 클라이언트에게 전달한다.
 */

public class ChatService extends Service {

    //클라이언트(액티비티)를 담을 어레이리스트.
    ArrayList<Messenger> ClientList = new ArrayList<Messenger>();
    //클라이언트(액티비티)를 mClients에 넣을 때(= 서비스바인드) Message의 what
    static final int MSG_REGISTER_CLIENT = 111;
    //클라이언트(액티비티)를 mClients에서 뺄때(= 서비스언바인드) Message의 what
    static final int MSG_UNREGISTER_CLIENT = 222;
    //클라이언트(액티비티)에서 서비스로 보내는 Message의 what
    static final int MSG_ACTIVITY_TO_SERVER = 333;
    //서버로부터 온 메시지를 클라이언트(액티비티)로 전달할 때 액티비티로 보내는 Message의 what
    static final int MSG_FROM_SERVER = 444;
    static final int MSG_FOR_LOAD_CHAT = 555;

    JSONObject jsonObject, jsonObjectForName;

    String state, roomID, name, friendname, content, nameOfUser;

    //서버와 통신할 소켓, printwriter, bufferedReader.
    private Socket s;
    private PrintWriter pw;
    private BufferedReader in;

    //소켓의 BufferReader를 계속 돌리는 쓰레드를 위한 플래그
    boolean isrunning = false;

    //서버로부터의 메시지.
    String str_MsgFromServer;

    NotificationManager manager;
    NotificationCompat.Builder builder;


    //컴포넌트로부터 서비스로 전달된 메시지를 핸들링할 핸들러. MessengerForService가 이 핸들러를 참조해 생성됨.
    class ReceiveFromActivityHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                //액티비티가 서비스와 바인드되면서 서비스로 메시지를 보내는 경우.
                case MSG_REGISTER_CLIENT:
                    if(msg.replyTo != null) {
                        ClientList.add(msg.replyTo);
                        Log.e("regi.service replyto", String.valueOf(msg.replyTo));
                    }
                    break;
                //액티비티가 서비스와 언바인드되면서
                case MSG_UNREGISTER_CLIENT:
                    ClientList.remove(msg.replyTo);
                    Log.e("Unre.service replyto", String.valueOf(msg.replyTo));
                    break;
                //액티비티로부터 전달되어 온 메시지를 서버로 보낼 때.
                /*
                TODO 서버와 연결한 소켓 생성 후 printWriter에 MSG_ACTIVITY_TO_SERVICE를 적어준다.
                 */
                case MSG_ACTIVITY_TO_SERVER:
                    String msg_from_activity = msg.getData().getString("msg");

                    try {
                        jsonObjectForName = new JSONObject(msg_from_activity);
                        nameOfUser = jsonObjectForName.getString("from");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Log.e("msg_fromActivity in SRV", msg_from_activity);
                    pw.println(msg_from_activity);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    //컴포넌트(액티비티)들로부터 오는 메시지를 핸들링할 메신저. 핸들러를 참조해 생성한다.
    final Messenger MessengerForService = new Messenger(new ReceiveFromActivityHandler());


    //서비스 시작되면 가장 먼저 호춮되는 게 OnCreate(). 서버와 통신할 소켓 생성해준다.
    @Override
    public void onCreate() {
        if(Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        isrunning = true;
        Log.e("Service", "onCreate()");

        super.onCreate();
        //소켓 생성하고 서버로 접속하기 위해서 액티비티로부터 로그인한 유저의 이름이 전달되어야 한다.
        SharedPreferences usrname = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE);
        String str_name = usrname.getString("id", "");
        Log.e("name check on SV", str_name);
        ServerConnection("222.239.249.149", str_name, 9999);
        ServerMessageReceive();

    }

    //소켓을 생성해 서버와 연결하는 메소드. onCreate에서 생성 ----> 로그인한 유저의 이름 필요없는거 같은데;
    private boolean ServerConnection(String hostv, String namev, int portv){
        try {
            s = new Socket(hostv,portv);
            pw = new PrintWriter(new BufferedOutputStream(s.getOutputStream()),true);
            in = new BufferedReader(new InputStreamReader(s.getInputStream(),"UTF-8"));
            Log.e("server connection", "success");
            return true;
        }catch (IOException e) {
            e.printStackTrace();

            Log.e("server connection", e.getMessage());
            Toast.makeText(this,"Error! on ServerConnection",Toast.LENGTH_LONG).show();
            return false;
        }
    }


    //서버로부터의 메시지를 수신하는 메소드. onCreate에서 생성
    private void ServerMessageReceive(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //액티비티로 전달될 번들객체 생성.
                    Bundle bundle_chat = new Bundle();
                    manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                     builder = new NotificationCompat.Builder(ChatService.this);
                    while(isrunning) {
                        //서버로부터 온 메시지를 읽는다.
                        str_MsgFromServer = in.readLine();
                        Log.e("Msg from SV to Service", str_MsgFromServer);
                        if(!str_MsgFromServer.equals("")){
                            try{
                                jsonObject = new JSONObject(str_MsgFromServer);
                                state = jsonObject.getString("state");
                                if(state.equals("chat")){
                                    friendname = jsonObject.getString("to");
                                    content = jsonObject.getString("message");
                                    roomID = jsonObject.getString("roomID");
                                    name = jsonObject.getString("from");
                                    if(friendname.equals(nameOfUser)) {
                                        Intent notificationIntent = new Intent(ChatService.this, ChattingRoomActivity.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putString("noty", "noty");
                                        bundle.putString("name", nameOfUser);
                                        bundle.putString("friend", name);
                                        bundle.putString("roomidFornoty", roomID);
                                        notificationIntent.putExtras(bundle);

                                        PendingIntent contentIntent = PendingIntent.getActivity(ChatService.this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                        builder.setContentTitle(name);
                                        builder.setContentText(content);
                                        builder.setTicker(content);
                                        builder.setSmallIcon(R.mipmap.ic_launcher);
                                        builder.setWhen(System.currentTimeMillis());
                                        builder.setContentIntent(contentIntent);

                                        manager.notify(1234, builder.build());


                                    }

                                }
                            }catch (JSONException e){
                                e.getMessage();
                            }

                            //서버로부터 온 메시지를 번들 객체에 넣어주고
                            bundle_chat.putString("ChatFromServer", str_MsgFromServer);
                            //이후 ClientList에 저장된 액티비티들에 전송한다. ClientList에는 각 액티비티로부터 온 Messenger 객체의 replyto가 들어있다.
                            for (int i=ClientList.size()-1; i>=0; i--){
                                try {
                                    //메세지의 what 설정 후
                                    Message msg = Message.obtain(null, MSG_FROM_SERVER);
                                    //번들과 메시지를 set 해준다.
                                    msg.setData(bundle_chat);
                                    ClientList.get(i).send(msg);
                                } catch (RemoteException e) {
                                    Log.e("ServerMessageReceive", e.getMessage());
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent,  int flags, int startId) {
        Log.e("onStartCommand" , "시작됨 in Service");
        return super.onStartCommand(intent, flags, startId);
    }

    public ChatService() {
    }

    //액티비티에서 서비스 바인드시 IBinder만 리턴한다. 리턴할 객체는 서비스의 메신저 객체.
    //액티비티에선 리턴받은 메신저 객체에게로 데이터를 보낸다.
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return MessengerForService.getBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //소켓종료
        try {
            s.close();
            Log.e("Service Destroy", "and socket close");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //소켓의 listening 스레드를 멈추는 플래그
        isrunning = false;
    }
}
