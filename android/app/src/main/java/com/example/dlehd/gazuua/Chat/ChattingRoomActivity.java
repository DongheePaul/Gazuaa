package com.example.dlehd.gazuua.Chat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.dlehd.gazuua.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;


/**
 * 채팅이 이루어지는 액티비티.
 *
 */
public class ChattingRoomActivity extends AppCompatActivity {
    private Socket s;
    private Handler networkdHandler;
    private PrintWriter pw;
    private BufferedReader in;
    EditText etMsg;
    Button chatBtn;
    RecyclerView chatRecycler;
    ChattingRoomAdapter chatAdapter;
    ArrayList<ChattingRoomItem> chatroomList = new ArrayList();
    String namev;
    String state, name, content, friendName, roomID, friendImageFrom, friendImageTo;
    //chatResponse()에서 쓰일 친구이름.
    String friendname;
    TextView tv_roomIDsave;
    //채팅을 위한 번들과 jsonObject
    Bundle bundle_chat;
    JSONObject jsonObject_chat;
    ImageView iv_friendProfile;

    //서비스와 액티비티 간 통신에 쓰일 플래그들
    //1. 액티비티를 서비스에 등록시킬 때(= 서비스바인드) Message의 what. 서비스 바인드 후 해당 what을 가진 메세지를 서비스로 보낸다. 서비스에선 해당 메신저의 replyto를 저장한다.
    static final int MSG_REGISTER_CLIENT = 111;
    //2. 액티비티를 서비스에서 제외(= 서비스언바인드) Message의 what. 해당 what을 가진 메세지를 서비스로 보내면 서비스에선 등록된 메신저의 replyto를 삭제한다.
    static final int MSG_UNREGISTER_CLIENT = 222;
    //3. 클라이언트(액티비티)에서 서버로 보내는 Message의 what. 이 경우 replyTo를 서비스에 저장시킬 필요 없다.
    //   서비스에서 1.에서의 replyTo를 이미 등록했기 때문.    등록된 replyTo로 MSG_FROM_SERVER를 what으로 해서 서버의 메시지를 클라이언트로 보낸다.
    static final int MSG_ACTIVITY_TO_SERVER = 333;
    //4. 서버로부터 온 메시지를 클라이언트(액티비티)로 전달할 때 사용되는 Message의 what. 서버의 응답이 서비스로 오고, 서비스에선 1.에서 등록된 replyTo로 서버의 응답을 액티비티로 전달해준다.
    static final int MSG_FROM_SERVER = 444;
    static final int MSG_FOR_LOAD_CHAT = 555;

    //서비스 바인드, 언바인드를 위한 플래그. 서비스 바인드시 true, 액티비티 종료되면서 서비서 언바인드시 false.
    private static boolean flagForBindService = false;

    //서비스로 메시지를 보낼 때 사용되는 메신저
    Messenger MessengerToService;
    //서비스로부터 오는 메시지를 핸들링할 메신저
    private final Messenger MessengerForActivity = new Messenger(new HandlerReceiveFromService());

    String load_chat, chatLoadFrom, chatLoadMSG, noty;


    //서비스와 연결하는 서비스커넥션 클래스. 서비스와 연결되면 서비스 내의 리스트에 해당 액티비티를 등록해주고
    //                                       서비스와 연결해지되면 서비스 내의 리스트에서 해당 액티비티를 삭제.
    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            //이미 Friend_list_main에서 서비스 시작 했기 때문에. 본 액티비티에서는 서비스와 연결되면 서비스가 IBinder만 리턴한다. 리턴된 IBinder를 넣고 서비스로 보내는 메세지를 전담하는 메신저 생성.
            MessengerToService = new Messenger(service);
            //서비스에 해당 액티비티를 바인딩하기 위해 보내는 메세지.
            // 메세지의 what을 설정해주고
            Message msg = Message.obtain(null, MSG_REGISTER_CLIENT);
            //메세지의 replyTo를 MessengerForActivity로 설정함으로써 서비스는 이 replyTo로 해당 액티비티로 메세지를 보낼 수 있다.
            msg.replyTo = MessengerForActivity;
            try {
                MessengerToService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            try {
                //서버로 state:start 인 메시지를 보낸다(서비스를 통해). 그러면 서버에서 해당 유저를 리스트에 삽입.
                Bundle bundle_room = new Bundle();
                JSONObject jsonObg = new JSONObject();

                jsonObg.put("state", "room");
                jsonObg.put("roomID", "0000");
                jsonObg.put("from", namev);
                jsonObg.put("message", "내용읎다.");
                jsonObg.put("to", friendName);
                String msg1 = jsonObg.toString();
                bundle_room.putString("msg", msg1);
                Message message = Message.obtain(null, MSG_ACTIVITY_TO_SERVER);
                message.setData(bundle_room);
                MessengerToService.send(message);
            } catch (JSONException e) {
                e.printStackTrace();
            }catch (RemoteException e){
                e.getMessage();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // MessengerToService = null;
        }
    };

    //서비스로부터 오는 메시지를 핸들링한 핸들러 (서버로부터 오는 메세지 외에는 오는 메시지 없다)
    class HandlerReceiveFromService extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case ChatService.MSG_FROM_SERVER:
                    state = "";
                    name= "";
                    content ="";

                    String msg_from_server = msg.getData().getString("ChatFromServer");
                    Log.e("msg from server", msg_from_server);

                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(msg_from_server);
                        state = jsonObject.getString("state");
                        roomID = jsonObject.getString("roomID");
                        name = jsonObject.getString("from");
                        content = jsonObject.getString("message");
                        friendname = jsonObject.getString("to");
                        if(state.equals("room")){
                            Loadchat loadchat = new Loadchat();
                            loadchat.execute();
                            Log.e("tv_roomIDsave.setText", roomID);
                            tv_roomIDsave.setText(roomID);
                        }
                        friendImageFrom = jsonObject.getString("profileFrom");
                        friendImageTo = jsonObject.getString("profileTo");



                        //방번호를 저장하기 위해 tv_roomIDsave에 roomid를 저장한다.

                                if(state.equals("chat")){
                                    //해당 대화방의 내용이 아니면 나오지 않게
                                    if(tv_roomIDsave.getText().equals(roomID)) {
                                        //채팅메시지가 내 것일 경우
                                        if (name.equals(namev)){
                                            ChattingRoomItem chattingRoomItem = new ChattingRoomItem(name, content, friendImageFrom);
                                            chatroomList.add(chattingRoomItem);
                                        }
                                        //채팅메시지가 친구 것일 경우
                                        else{
                                            ChattingRoomItem chattingRoomItem = new ChattingRoomItem(name, content, friendImageTo);
                                            chatroomList.add(chattingRoomItem);
                                        }
                                    }
                                }
                                chatAdapter.notifyDataSetChanged();


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting_room);
        //sdk 버전이 9보다 높으면 필요한 코드.
        if(Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        tv_roomIDsave = (TextView)findViewById(R.id.roomidsave);

        //isrunning=true;
        //서비스에 바인드한다.
        Intent i = new Intent(ChattingRoomActivity.this, ChatService.class);
        i.setPackage("com.example.dlehd");
        bindService(i, conn, Context.BIND_AUTO_CREATE);
        flagForBindService = true;

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        //접속할 서버의 ip 주소
        String hostv = bundle.getString("host");
        //클라이언트(사용자)의 이름.
        namev = bundle.getString("name");
        friendName = bundle.getString("friend");
        Log.e("친구이름 확인", friendName);
        int portv = 9999;
        noty = bundle.getString("noty");
        String roomidForNoty = bundle.getString("roomidFornoty");
        if(noty != null && noty.equals("noty")){
            roomID = roomidForNoty;
            Loadchat loadchat = new Loadchat();
            loadchat.execute();

        }


        //채팅 메시지를 서버로 보낼때 사용될 번들


        //메시지를 작성할 에디트텍스트
        etMsg = (EditText)findViewById(R.id.et_msg);
        //작성한 메시지를 서비스로  보낸다.
        chatBtn = (Button)findViewById(R.id.btn_send_msg);

        bundle_chat = new Bundle();
        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    String msg = etMsg.getText().toString().trim();
                    JSONObject jsonObg = new JSONObject();
                    jsonObg.put("state", "chat");
                    jsonObg.put("roomID", roomID);
                    jsonObg.put("from", namev);
                    jsonObg.put("message", msg);
                    jsonObg.put("to", friendName);
                    String msgToServer = jsonObg.toString();

                    Message msg_chat = Message.obtain(null, MSG_ACTIVITY_TO_SERVER);
                    bundle_chat.putString("msg", msgToServer);
                    msg_chat.setData(bundle_chat);
                    MessengerToService.send(msg_chat);
                    Log.e("서버로 보내는 msg, button", msgToServer);
                   // pw.println(msgToServer);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }catch (RemoteException e){
                    e.getMessage();
                }
                //pw.println("talk/chat/"+namev+"/"+msg);
                etMsg.setText("");
                etMsg.requestFocus();
            }
        });

        //서버로부터 받은 메시지를 리사이클러뷰의 아이템에 추가해서 화면에 출력하는 핸들러. 생성한다.
        //networkdHandler = new Handler();
        //액티비티가 실행되고 서버와 연결하는 소켓을 생성(=방 생성)한다. 그리고 첫 메시지를 보낸다. 첫 메시지에는 사용자의 이름이 들어가는데 이후 채팅하는 유저의 식별자로 사용된다.
        //testServer(hostv, namev, portv);
        //서버로부터 수신한 메시지를 어뎁터에 넣어서 액티비티에 출력하는 메소드.
        //chatResponse();

        //수신한 메시지를 출력할 리사이클러뷰.
        chatRecycler = (RecyclerView)findViewById(R.id.listView1);
        //리사이클러뷰 자체의 크기가 변하지 않는다면 해당 옵션 설정 시 성능 개선에 도움.
        chatRecycler.setHasFixedSize(true);
        //레이아웃메니저와 리사이클러뷰와 연결시킨다.
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        chatRecycler.setLayoutManager(layoutManager);
        //리사이클러뷰와 어뎁터를 연결시킨다.
        chatAdapter = new ChattingRoomAdapter(getApplicationContext(), chatroomList);
        chatRecycler.setAdapter(chatAdapter);
    }
    //onCreate 끝


//저장된 대화내용 불러오는 클래스.
    public class Loadchat extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String param = "roomid="+roomID;
            try{
                URL url = new URL("http://222.239.249.149/chat/chat_load.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Accept-Charset", "UTF-8");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.connect();


                //클라이언트에서 서버로 보내는 회원정보
                OutputStream outs = conn.getOutputStream();
                //통신하기 위한 url 인코딩
                outs.write(param.getBytes("UTF-8"));
                outs.flush();
                outs.close();
                //if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                     /* 서버 -> 안드로이드 서버의 리턴값 전달 */
                    InputStream is = null;
                    BufferedReader in = null;
                    load_chat = "";

                    //서버로부터 받은 인풋스트림을 스트링화
                    is = conn.getInputStream();
                    in = new BufferedReader(new InputStreamReader(is), 8 * 1024);

                    String line = null;
                    StringBuffer buff = new StringBuffer();
                    while ((line = in.readLine()) != null) {
                        buff.append(line + "\n");
                    }
                    //서버로부터의 응답(목록을 생성할 게시물 데이터.)
                    load_chat = buff.toString().trim();
               // }
                conn.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //리턴하면 onPostExecute의 파라미터로 전달된다.
            Log.e("채팅메시지 로드.", load_chat);
            return load_chat;
        }
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e("chatLoad 데이터 결과값", s);
            try {
                JSONArray obj = new JSONArray(s);
                int j = obj.length();
                for (int i = 0; i < j; i++) {
                    JSONObject jsonObject = obj.getJSONObject(i);
                    //게시물의 글번호
                    chatLoadFrom = jsonObject.getString("from");
                    chatLoadMSG = jsonObject.getString("message");
                    String imagePath = jsonObject.getString("profile");

                            Log.e("name 확인 chat_load", chatLoadFrom);
                            Log.e("content 확인 chat_loadr", chatLoadMSG);
                            ChattingRoomItem chattingRoomItem = new ChattingRoomItem(chatLoadFrom, chatLoadMSG, imagePath);
                            chatroomList.add(chattingRoomItem);
                }
            } catch (JSONException e) {
                Log.e("jsonException in Frien", e.getMessage());
            }
            chatAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //isrunning=false;
        //서비스에 연결된 액티비티를 언바인드한다.
        tv_roomIDsave.setText("");
        if(flagForBindService){
            Message msg = Message.obtain(null, MSG_UNREGISTER_CLIENT);
            msg.replyTo = MessengerForActivity;
            try{
                MessengerToService.send(msg);
                MessengerToService = null;
            }catch (RemoteException e){
                e.getMessage();
            }
            unbindService(conn);
            flagForBindService = false;
        }
        /*
        try {
            s.close();
            in.close();
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }


    public class ChattingRoomItem{
        String name;
        String content;
        String visible;
        String time;
        String imagePath;

        public String getImagePath() {
            return imagePath;
        }

        public void setImagePath(String imagePath) {
            this.imagePath = imagePath;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public ChattingRoomItem(String nam, String con, String imagepath){
            name = nam;
            content = con;
            imagePath = imagepath;
        }
        public String getVisible() {
            return visible;
        }

        public void setVisible(String visible) {
            this.visible = visible;
        }



        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }


    public class ChattingRoomAdapter extends RecyclerView.Adapter<ChattingRoomAdapter.ViewHolder>{
        Context context;
        ArrayList<ChattingRoomItem> chatroomList;
        ChattingRoomItem item;

        public ChattingRoomAdapter(Context context, ArrayList<ChattingRoomItem> chatroomList) {
            this.context = context;
            this.chatroomList = chatroomList;
        }

        //: 뷰 홀더를 생성하고 뷰를 붙여주는 부분입니다.
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_chatitem,parent, false);
            return new ViewHolder(view);
        }


        //재활용 되는 뷰가 호출하여 실행되는 메소드, 뷰 홀더를 전달하고 어댑터는 position 의 데이터를 결합시킵니다.
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            item = chatroomList.get(position);

            //내가 작성한 메시지라면 화면 오른쪽에 메시지를 위치시킨다.
          if(item.getName().equals(namev)){
              holder.layout_right.setVisibility(View.VISIBLE);
              holder.tv_content_right.setText(item.getContent());
          }
          //타인이 작성한 메시지라면 화면 왼쪽에 메시지를 위치시킨다.
          else
          {
              holder.layout_left.setVisibility(View.VISIBLE);
              holder.tv_content_left.setText(item.getContent());
              holder.tv_name_left.setText(item.getName());
              String imagePath = item.getImagePath();
              Glide.with(ChattingRoomActivity.this).load("http://222.239.249.149/"+imagePath).into(iv_friendProfile);
          }

        }

        @Override
        public int getItemCount() {
            return chatroomList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            TextView tv_content_left, tv_name_left, tv_content_right,  visible;
            RelativeLayout layout_left, layout_right;


            public ViewHolder(View itemView) {
                super(itemView);
               tv_content_left = (TextView)itemView.findViewById(R.id.tv_content_left);
                tv_name_left = (TextView)itemView.findViewById(R.id.tv_name_left);
                tv_content_right = (TextView)itemView.findViewById(R.id.tv_content_right);
                visible = (TextView)itemView.findViewById(R.id.tv_visible);
                layout_left = (RelativeLayout)itemView.findViewById(R.id.layout_left);
                layout_right = (RelativeLayout)itemView.findViewById(R.id.layout_right);
                iv_friendProfile = (ImageView)itemView.findViewById(R.id.iv_friendprofile);

            }
        }

    }

}
