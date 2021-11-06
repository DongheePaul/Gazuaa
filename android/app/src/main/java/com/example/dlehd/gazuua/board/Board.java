package com.example.dlehd.gazuua.board;

import android.app.ProgressDialog;
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
import android.widget.Button;

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
 * 게시판 목록을 보여주는 프레그먼트.
 *
 */
public class Board extends Fragment {
    String user_name, user_email,sessionID;
    String id, title, smallest_num, time, writer, result_for_board_list;

    private OnFragmentInteractionListener mListener;

    RecyclerView recyclerView_board_list;
    Recycler_adapter_board recycler_adapter_board;
    ArrayList<Recycler_item_post> post = new ArrayList<>();

    ProgressDialog progressDialog;

    public Board() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static Board newInstance(String[] text) {
        Board fragment = new Board();
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


  /*  @Override
    public void onResume() {
        super.onResume();
        load_data_for_board Load = new load_data_for_board();
        Load.execute();
    }
*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
              //프레그먼트 생성.
        View v  =inflater.inflate(R.layout.fragment_board, container, false);
        /*adapter = new MainViewAdapter(getActivity(), list);
        이 부분을 액티비티에 작성할 경우 getActivity() 를 this로 작성할 수 있는데 이 클래스는 Fragment를 상속받으므로
        해당 Fragment를 관리하    는 Activity를 리턴하는 함수(getActivity())를 매개변수로 넘긴다*/

            //목록을 생성할 리사이클러뷰를 생성한다.
            recyclerView_board_list = (RecyclerView)v.findViewById(R.id.recycle);
            recyclerView_board_list.setHasFixedSize(true);
            //레이아웃 매니저를 생성한다.
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            //생성한 레이아웃매니저를 리사이클러뷰에 셋해준다.
            recyclerView_board_list.setLayoutManager(layoutManager);
            //리사이클러 어뎁터를 생성한다.
            recycler_adapter_board = new Recycler_adapter_board(getActivity(), post);
              //레이아웃매니저를 셋한 리사이클러뷰에 어뎁터를 셋해준다.
            recyclerView_board_list.setAdapter(recycler_adapter_board);

        //게시물 데이터를 불러와 게시판 목록을 생성하는 클래스를 실행한다.
        load_data_for_board Load = new load_data_for_board();
        Load.execute();

        //MainActivity에서 전달받은 유저정보.
        user_name = getArguments().getString("이름");
        user_email = getArguments().getString("이메일");
        sessionID = getArguments().getString("세션");

        //게시물 추가 버튼.
        Button add_post = (Button)v.findViewById(R.id.button6);
        add_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //게시물 추가하는 액티비티로 이동.
                Intent intent = new Intent(getActivity(), Post_write_Activity.class);
                intent.putExtra("user_name", user_name);
                intent.putExtra("user_email", user_email);
                intent.putExtra("SessionID", sessionID);
                startActivity(intent);

                getActivity().finish();
                Log.e("Board fragment", "onCreateView");
            }
        });


        return v;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public class load_data_for_board extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        }

        @Override
        protected String doInBackground(String... params) {
            try{
                URL url = new URL("http://222.239.249.149/board/board_list.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Accept-Charset", "UTF-8");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.connect();

                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                    Log.e("http response==", String.valueOf(conn.getResponseCode()));
                     /* 서버 -> 안드로이드 서버의 리턴값 전달 */
                    InputStream is = null;
                    BufferedReader in = null;
                    result_for_board_list = "";

                    //서버로부터 받은 인풋스트림을 스트링화
                    is = conn.getInputStream();
                    in = new BufferedReader(new InputStreamReader(is), 8 * 1024);

                    String line = null;
                    StringBuffer buff = new StringBuffer();
                    while ((line = in.readLine()) != null) {
                        buff.append(line + "\n");
                    }

                    //서버로부터의 응답(목록을 생성할 게시물 데이터.)
                    result_for_board_list = buff.toString().trim();

                }

            conn.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //리턴하면 onPostExecute의 파라미터로 전달된다.
            return result_for_board_list;
        }


        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                Log.e("Board에서 서버 리턴값", s);
                JSONArray obj = new JSONArray(s);
                for (int i = 0; i < 10; i++) {
                    JSONObject jsonObject = obj.getJSONObject(i);
                    //게시물의 글번호
                    id = jsonObject.getString("id");
                    //게시물의 제목
                    title = jsonObject.getString("title");
                    //한 페이지의 글들 중 가장 작은 글 번호. 다음 페이지의 글들을 불러오기 위해 필요하다.
                    smallest_num = jsonObject.getString("smallest");
                    //작성자
                    writer = jsonObject.getString("writer");
                    //작성시간
                    time = jsonObject.getString("time");

                    //게시물의 글번호, 제목, 시간, 가장 작은 글번호(invisible)을 아이템에 넣는다.
                    Recycler_item_post item_post = new Recycler_item_post(title, writer, time, id, smallest_num, sessionID, user_name, user_email);

                    //리사이클러뷰에 데이터를 집어 넣은 아이템을 추가한다.
                    post.add(item_post);

                }
            } catch (JSONException e) {
                Log.e("jsonException in Board", e.getMessage());
            }
            recycler_adapter_board.notifyDataSetChanged();
            progressDialog.dismiss();
        }
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
