package com.example.dlehd.gazuua.Friend_list;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.dlehd.gazuua.Chat.ChattingRoomActivity;
import com.example.dlehd.gazuua.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FriendList.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FriendList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendList extends Fragment {
    String sessionID, friend_imagePath, friend_email, user_name, user_email, friend_name;
    RecyclerView recycler_friendList;
    ImageView profileImage;
    FriendAdapter friendAdapter;
    ArrayList<FriendList_item> friendList = new ArrayList<>();
    String result_for_friendlist;


    private OnFragmentInteractionListener mListener;

    public FriendList() {
        // Required empty public constructor
    }

    /**
     *
     */
    // TODO: Rename and change types and number of parameters
    public static FriendList newInstance(String[] text) {
        FriendList fragment = new FriendList();
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
        user_name = getArguments().getString("이름");
        user_email = getArguments().getString("이메일");
        sessionID = getArguments().getString("세션");
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_friend_list, container, false);
        //친구목록을 형성할 리사이클러뷰를 생성한다.
        recycler_friendList = (RecyclerView) v.findViewById(R.id.recycle_friend);
        recycler_friendList.setHasFixedSize(true);
        //레이아웃 매니저를 생성한다.
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recycler_friendList.setLayoutManager(layoutManager);
        //리사이클러뷰와 어뎁터를 연결한다.
        friendAdapter = new FriendAdapter(getActivity(), friendList);
        recycler_friendList.setAdapter(friendAdapter);

        //친구목록의 데이터들을 불러온다.
        LoadFriendList loadFriendList = new LoadFriendList();
        loadFriendList.execute();

        return v;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);


    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

   
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    //친구 목록을 생성하는 클래스.
    public class LoadFriendList extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try{
                URL url = new URL("http://222.239.249.149/friend/FriendList.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Accept-Charset", "UTF-8");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                //세션에 저장된 아이디를 사용하기 위해 세션아이디를 쿠키에 넣어준다.
                conn.setRequestProperty("Cookie", sessionID);
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.connect();

                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                     /* 서버 -> 안드로이드 서버의 리턴값 전달 */
                    InputStream is = null;
                    BufferedReader in = null;
                    result_for_friendlist = "";

                    //서버로부터 받은 인풋스트림을 스트링화
                    is = conn.getInputStream();
                    in = new BufferedReader(new InputStreamReader(is), 8 * 1024);

                    String line = null;
                    StringBuffer buff = new StringBuffer();
                    while ((line = in.readLine()) != null) {
                        buff.append(line + "\n");
                    }
                    //서버로부터의 응답(목록을 생성할 게시물 데이터.)
                    result_for_friendlist = buff.toString().trim();
                }
                conn.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //리턴하면 onPostExecute의 파라미터로 전달된다.
            Log.e("doin에서 서버 응답값", result_for_friendlist);
            return result_for_friendlist;
        }
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.e("친구목록 데이터 결과값", s);
            try {
                JSONArray obj = new JSONArray(s);
                int j = obj.length();
                for (int i = 0; i < j; i++) {
                    JSONObject jsonObject = obj.getJSONObject(i);
                    //게시물의 글번호
                    friend_email = jsonObject.getString("from");
                    friend_imagePath = jsonObject.getString("image");
                    friend_name = jsonObject.getString("name");

                    //게시물의 글번호, 제목, 시간, 가장 작은 글번호(invisible)을 아이템에 넣는다.
                    FriendList_item item_post = new FriendList_item(friend_imagePath, friend_name, friend_email);

                    //리사이클러뷰에 데이터를 집어 넣은 아이템을 추가한다.
                    friendList.add(item_post);
                }
            } catch (JSONException e) {
                Log.e("jsonException in Frien", e.getMessage());
            }
            friendAdapter.notifyDataSetChanged();
        }
    }




    //친구목록의 데이터들을 처리할 어뎁터.
    public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder>{
        Context context;
        ArrayList<FriendList_item> friendlist = new ArrayList<>();
        FriendList_item friendlist_item;
        public FriendAdapter(Context context, ArrayList<FriendList_item> friendlist) {
            this.context = context;
            this.friendlist = friendlist;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_friend_list_item,parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    friendlist_item = friendlist.get(position);
                    Intent intent = new Intent(getActivity(), ChattingRoomActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("host", "222.239.249.149");
                    bundle.putString("port", "9999");
                    bundle.putString("name", user_name);
                    bundle.putString("friend", friendlist_item.getName());
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
            friendlist_item = friendlist.get(position);
            String image_path = friendlist_item.getImg_path();
            Glide.with(FriendList.this).load("http://222.239.249.149/"+image_path).into(profileImage);
            holder.name.setText(friendlist_item.getName());
            holder.email.setText(friendlist_item.getEmail());

        }

        @Override
        public int getItemCount() {
            return friendlist.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder{

            TextView name, email;
            public ViewHolder(View itemView) {
                super(itemView);
                profileImage = (ImageView)itemView.findViewById(R.id.iv_profile);
                name = (TextView)itemView.findViewById(R.id.tv_friendlistname);
                email = (TextView)itemView.findViewById(R.id.tv_friendlistemail);
            }
        }
    }

}
