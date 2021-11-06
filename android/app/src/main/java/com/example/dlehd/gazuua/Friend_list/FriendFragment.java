package com.example.dlehd.gazuua.Friend_list;

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
 * 받은 친구신청 목록을 보여주는 프래그먼트.
 *
 */
public class FriendFragment extends Fragment {

    String user_name, user_email, user_id , sessionID, result_for_friendApply, from_email;
    RecyclerView recycle_friend_apply;
    FriendApply_adapter friendApplyAdapter;
    ArrayList<FriendApply_item> list = new ArrayList<>();

    private OnFragmentInteractionListener mListener;

    public FriendFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static FriendFragment newInstance(String[] text) {
        FriendFragment fragment = new FriendFragment();
        //메인으로부터 전달받은 이름, 이메일
        String name = text[0];
        String email = text[1];
        String sessionID = text[2];

        Bundle b = new Bundle();
        b.putString("이름", name);
        b.putString("이메일", email);
        b.putString("세션", sessionID);

        fragment.setArguments(b);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_friend, container, false);

        //리사이클러뷰를 찾아준다.
        recycle_friend_apply = (RecyclerView)v.findViewById(R.id.recycle_friendApply);
        recycle_friend_apply.setHasFixedSize(true);
        //레이아웃 매니저를 생성한다.
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        //리사이클러부와 레이아웃 매니저를 연결한다.
        recycle_friend_apply.setLayoutManager(layoutManager);
        //라사이클러뷰와 어뎁터를 연결한다.
        friendApplyAdapter = new FriendApply_adapter(getActivity(), list);
        recycle_friend_apply.setAdapter(friendApplyAdapter);

        user_name = getArguments().getString("이름");
        user_email = getArguments().getString("이메일");
        sessionID = getArguments().getString("세션");

        //받은 친구신청 데이터를 가져온다.
        load_data_for_friendApply loadDataForFriendApply = new load_data_for_friendApply();
        loadDataForFriendApply.execute();


        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        //해당 프레그먼트가 스와이프 될때마다 뉴인스턴스를 계속하기 때문에, 목록에 데이터가 계속해서 추가되는 현상이 발생
        //따라서 onResume에서 받은 친구신청 데이터를 저장하는 리스트를 비워준다.
        list.clear();
        friendApplyAdapter.notifyDataSetChanged();
    }

    //받은 친구신청 목록의 데이터를 받아오는 클래스.
    public class load_data_for_friendApply extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try{
                URL url = new URL("http://222.239.249.149/friend/friendApply.php");
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
                    result_for_friendApply = "";

                    //서버로부터 받은 인풋스트림을 스트링화
                    is = conn.getInputStream();
                    in = new BufferedReader(new InputStreamReader(is), 8 * 1024);

                    String line = null;
                    StringBuffer buff = new StringBuffer();
                    while ((line = in.readLine()) != null) {
                        buff.append(line + "\n");
                    }
                    //서버로부터의 응답(목록을 생성할 게시물 데이터.)
                    result_for_friendApply = buff.toString().trim();
                }
                conn.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //리턴하면 onPostExecute의 파라미터로 전달된다.
            return result_for_friendApply;
        }
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONArray obj = new JSONArray(s);
                int j = obj.length();
                for (int i = 0; i < j; i++) {
                    JSONObject jsonObject = obj.getJSONObject(i);
                    //게시물의 글번호
                    from_email = jsonObject.getString("from");
                    //게시물의 글번호, 제목, 시간, 가장 작은 글번호(invisible)을 아이템에 넣는다.
                    Log.e("뭐지?", from_email);
                    FriendApply_item item_post = new FriendApply_item(from_email, sessionID);

                    //리사이클러뷰에 데이터를 집어 넣은 아이템을 추가한다.
                    list.add(item_post);
                }
            } catch (JSONException e) {
                Log.e("jsonException in Frien", e.getMessage());
            }
            friendApplyAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
