package com.example.dlehd.gazuua.Chat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * 채팅방 목록을 보여주는 프레그먼트
 */
public class ChatroomList extends Fragment {

    //로그인한 유저의 이름, 이메일, 세션아이디
    String user_name, user_email, sessionID;
    //채팅방 목록을 생성할 리사이클러뷰
    RecyclerView recyclerViewChatRoomList;
    //채팅방 목록 리사이클러뷰의 어뎁터
    ChatroomListAdapter chatroomListAdapter;
    //채팅방 목록 데이터
    String resultForChatRoomList;
    //채팅방(아이템)을 담을 어레이리스트
    ArrayList<ChatroomList_item> ChatroomArray = new ArrayList<>();
    ChatroomList_item chatroomlist_item;

    //채팅방 아이템에 셋될 상대 이름, 상대 프로필, 방 넘버, 마지막 메시지.
    String friendName, friendProfile, roomid, lastmsg;
    //채팅방의 친구의 프로필 사진을 띄울 이미지뷰
    ImageView friend_profile;

    private OnFragmentInteractionListener mListener;
    //서버 -> 서비스 -> 프래그먼트로 온 메시지
    String friendnameFornew, lastMsgFornew, friendprofileFrom, friendprofileTo, roomIdForNew, lastmsgForUpdate, stateForNew;
    String friendname1, friendname2;


    //1. 액티비티를 서비스에 등록시킬 때(= 서비스바인드) Message의 what. 서비스 바인드 후 해당 what을 가진 메세지를 서비스로 보낸다. 서비스에선 해당 메신저의 replyto를 저장한다.
    static final int MSG_REGISTER_CLIENT = 111;
    //2. 액티비티를 서비스에서 제외(= 서비스언바인드) Message의 what. 해당 what을 가진 메세지를 서비스로 보내면 서비스에선 등록된 메신저의 replyto를 삭제한다.
    static final int MSG_UNREGISTER_CLIENT = 222;
    //3. 클라이언트(액티비티)에서 서버로 보내는 Message의 what. 이 경우 replyTo를 서비스에 저장시킬 필요 없다.
    //   서비스에서 1.에서의 replyTo를 이미 등록했기 때문.    등록된 replyTo로 MSG_FROM_SERVER를 what으로 해서 서버의 메시지를 클라이언트로 보낸다.
    static final int MSG_ACTIVITY_TO_SERVER = 333;
    //4. 서버로부터 온 메시지를 클라이언트(액티비티)로 전달할 때 사용되는 Message의 what. 서버의 응답이 서비스로 오고, 서비스에선 1.에서 등록된 replyTo로 서버의 응답을 액티비티로 전달해준다.
    static final int MSG_FROM_SERVER = 444;

    //서비스 바인드, 언바인드를 위한 플래그. 서비스 바인드시 true, 액티비티 종료되면서 서비서 언바인드시 false.
    private static boolean flagForBindService = false;
    int ChatroomArraySize;

    //서비스로 메시지를 보낼 때 사용되는 메신저
    Messenger MessengerToService1;
    //서비스로부터 오는 메시지를 핸들링할 메신저
    Messenger MessengerForFragment = new Messenger(new HandleMessageFromService());

    //서비스와 연결하는 서비스커넥션 객체. 서비스와 연결되면 서비스 내의 리스트에 해당 프래그먼트를 등록하고
                                        // 서비스와 연결 해지되면 서비스 내의 리스트에서 해당 프래그먼트를 삭제.

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e("ChatroomList & service", "Connected");
            MessengerToService1 = new Messenger(service);
            Message msg1 = Message.obtain(null, MSG_REGISTER_CLIENT);
            msg1.replyTo = MessengerForFragment;
            try{
                MessengerToService1.send(msg1);
            }catch (RemoteException e){
                e.getMessage();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    //프래그먼트 파괴시, 서비스 언바인드

    @Override
    public void onStop() {
        super.onStop();

    }



    @Override
    public void onStart() {
        super.onStart();
        //서비스와 연결한다.
        Intent i = new Intent(getActivity(), ChatService.class);
        i.setPackage("com.example.dlehd");
        getActivity().bindService(i, conn,  BIND_AUTO_CREATE);
        flagForBindService = true;

    }

    //서버 -> 서비스 -> 프래그먼트로 오는 메시지를 핸들링하는 클래스.
    //채팅방이 없다면 생성, 채팅방이 있다면 최신 메시지 업데이트
    class HandleMessageFromService extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case ChatService.MSG_FROM_SERVER:
                    stateForNew = "";
                    friendnameFornew = "";

                    String msg_from_server = msg.getData().getString("ChatFromServer");
                    Log.e("msg check chatroomlist", msg_from_server);

                    JSONObject jsonObject = null;
                    try{
                        jsonObject = new JSONObject(msg_from_server);
                        stateForNew = jsonObject.getString("state");
                        roomIdForNew = jsonObject.getString("roomID");
                        friendname1 = jsonObject.getString("to");
                        friendname2 = jsonObject.getString("from");
                        lastMsgFornew = jsonObject.getString("message");
                        friendprofileFrom = jsonObject.getString("profileFrom");
                        friendprofileTo = jsonObject.getString("profileTo");

                        if(ChatroomArray.size() == 0 ){
                            if(friendname1.equals(user_name)) {
                                ChatroomList_item chatroomList_item = new ChatroomList_item(friendprofileFrom, friendname2, roomIdForNew, lastMsgFornew);
                                ChatroomArray.add(chatroomList_item);
                            }
                            else if(friendname2.equals(user_name)){
                                ChatroomList_item chatroomList_item = new ChatroomList_item(friendprofileTo, friendname1, roomIdForNew, lastMsgFornew);
                                ChatroomArray.add(chatroomList_item);
                            }
                        }
                        ChatroomArraySize = ChatroomArray.size() - 1;
                        //채팅방 목록 중에 해당 채팅방이 존재하는지 확인한다.
                        for(int i = 0 ; i<ChatroomArray.size(); i++){
                            chatroomlist_item = ChatroomArray.get(i);
                            String roomidInList =chatroomlist_item.getRoomid();
                            //채팅방 목록에 채팅방이 있다면 => 최신 메시지 업데이트
                            if(roomidInList.equals(roomIdForNew)){
                                chatroomlist_item.setLastmsg(lastMsgFornew);
                            }
                            //채팅방 목록에 채팅방이 없다면 => 채팅방 생성.
                            else if (!roomidInList.equals(roomIdForNew) && i == ChatroomArraySize){
                                if(friendname1.equals(user_name)) {
                                    ChatroomList_item chatroomList_item = new ChatroomList_item(friendprofileFrom, friendname2, roomIdForNew, lastMsgFornew);
                                    ChatroomArray.add(chatroomList_item);
                                }
                                else if(friendname2.equals(user_name)){
                                    ChatroomList_item chatroomList_item = new ChatroomList_item(friendprofileTo, friendname1, roomIdForNew, lastMsgFornew);
                                    ChatroomArray.add(chatroomList_item);
                                }
                            }
                        }
                        chatroomListAdapter.notifyDataSetChanged();
                    }catch (JSONException e){
                        e.getMessage();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    public ChatroomList() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ChatroomList newInstance(String[] text) {
        ChatroomList fragment = new ChatroomList();
        Bundle args = new Bundle();

        String name = text[0];
        String email = text[1];
        String sessionID = text[2];

        args.putString("이름", name);
        args.putString("이메일", email);
        args.putString("세션", sessionID);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chatroom_list, container, false);
        user_name = getArguments().getString("이름");
        user_email = getArguments().getString("이메일");
        sessionID = getArguments().getString("세션");


        //채팅방 목록을 형성할 리사이클러뷰를 생성한다.
        recyclerViewChatRoomList = (RecyclerView)v.findViewById(R.id.recycle_ChatroomList);
        recyclerViewChatRoomList.setHasFixedSize(true);
        //레이아웃 매니저를 생성해 리사이클러뷰와 연결한다.
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewChatRoomList.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewChatRoomList.setLayoutManager(layoutManager);
        //리사이클러뷰와 어뎁터를 연결한다.
        chatroomListAdapter = new ChatroomListAdapter(getActivity(), ChatroomArray);
        recyclerViewChatRoomList.setAdapter(chatroomListAdapter);


        //채팅방 목록을 불러온다.
        Load_Chatroom load_chatroom = new Load_Chatroom();
        load_chatroom.execute();
        return v;
    }


    public class ChatroomListAdapter extends RecyclerView.Adapter<ChatroomListAdapter.ViewHolder>{
        Context context;
        ArrayList<ChatroomList_item> ChatroomArray;
        ChatroomList_item chatroomListItem;

        public ChatroomListAdapter(Context context, ArrayList<ChatroomList_item> chatroomArray) {
            this.context = context;
            this.ChatroomArray = chatroomArray;
        }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatroom_list_item,parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chatroomListItem = ChatroomArray.get(position);
                    holder.iv_newMessage.setVisibility(View.GONE);
                    Intent intent = new Intent(getActivity(), ChattingRoomActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("host", "222.239.249.149");
                    bundle.putString("port", "9999");
                    bundle.putString("name", user_name);
                    bundle.putString("friend", chatroomListItem.getFriend_name());
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });

            chatroomListItem = ChatroomArray.get(position);
            String image_path = chatroomListItem.getImg_path();
            Glide.with(ChatroomList.this).load("http://222.239.249.149/"+image_path).into(friend_profile);
            holder.roomid.setText(chatroomListItem.getRoomid());
            holder.lastMsg.setText(chatroomListItem.getLastmsg());
            holder.friendName.setText(chatroomListItem.getFriend_name());
        }
        @Override
        public int getItemCount() {
            return ChatroomArray.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder{

            TextView friendName, lastMsg, roomid;
            ImageView iv_newMessage;

            public ViewHolder(View itemView) {
                super(itemView);
                friend_profile = (ImageView)itemView.findViewById(R.id.iv_friendProfile);
                friendName = (TextView)itemView.findViewById(R.id.tv_FriendName);
                lastMsg = (TextView)itemView.findViewById(R.id.tv_LastMessage);
                roomid = (TextView)itemView.findViewById(R.id.tv_Roomid);
                iv_newMessage = (ImageView)itemView.findViewById(R.id.iv_newmessageicon);
            }
        }
    }




    //채팅방 목록을 불러오는 클래스.
    public class Load_Chatroom extends AsyncTask<String, String, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected String doInBackground(String... params) {
            String param = "name="+user_name;
            try{
                URL url = new URL("http://222.239.249.149/chat/chatroomList.php");

                //httpurlconnection 생성
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


                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                      /* 서버 -> 안드로이드 서버의 리턴값 전달 */
                    InputStream is = null;
                    BufferedReader in = null;
                    resultForChatRoomList = "";

                    //서버로부터 받은 인풋스트림을 스트링화
                    is = conn.getInputStream();
                    in = new BufferedReader(new InputStreamReader(is), 8 * 1024);

                    String line = null;
                    StringBuffer buff = new StringBuffer();
                    while ((line = in.readLine()) != null) {
                        buff.append(line + "\n");
                    }

                    resultForChatRoomList = buff.toString().trim();
                }
                conn.disconnect();
            }catch (MalformedURLException e){
                e.getMessage();
            }catch (ProtocolException e){
                e.getMessage();
            }catch (IOException e){
                e.getMessage();
            }
            return resultForChatRoomList;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e("chatroomList from sv", s);
            try{
                JSONArray obj = new JSONArray(s);
                int j = obj.length();
                for(int i = 0; i<j; i++){
                    JSONObject jsonObject = obj.getJSONObject(i);
                    friendProfile = jsonObject.getString("friend_profile");
                    friendName = jsonObject.getString("friend");
                    lastmsg = jsonObject.getString("lastmsg");
                    roomid = jsonObject.getString("roomid");

                    ChatroomList_item chatroomList_item = new ChatroomList_item(friendProfile, friendName, roomid, lastmsg);

                    ChatroomArray.add(chatroomList_item);
                }
            }catch (JSONException e){
                e.getMessage();
            }
            chatroomListAdapter.notifyDataSetChanged();
        }
    }




    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;

        if(flagForBindService){
            Message msg1 = Message.obtain(null, MSG_UNREGISTER_CLIENT);
            msg1.replyTo = MessengerForFragment;
            try{
                MessengerToService1.send(msg1);
                MessengerToService1=null;
            }catch (RemoteException e){
                e.getMessage();
            }
            getActivity().unbindService(conn);
            flagForBindService = false;
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
