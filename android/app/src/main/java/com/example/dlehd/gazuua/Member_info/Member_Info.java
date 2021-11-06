package com.example.dlehd.gazuua.Member_info;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.dlehd.gazuua.R;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static android.R.attr.data;


/**
 *
 회원정보를 보여주는 프래그먼트(로그인하면 바로 생성된다)
 프로필사진, 이메일, 아름을 보여준다.
 */
public class Member_Info extends Fragment {
    TextView name_show, email_show;
    String user_name, user_email, user_id , sessionID;
    String SessionID,msg_from_db_about_logout;
    Button btn_logout;
    ImageView imageView;
    private OnFragmentInteractionListener mListener;
    static String name, email;

    String msg_from_db;

    public Member_Info() {
        // Required empty public constructor
    }


    public static Member_Info newInstance(String[] text) {
        Member_Info fragment = new Member_Info();
        //메인으로부터 받은 이름, 이메일
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_member__info, container, false);

        name_show = (TextView)v.findViewById(R.id.nameTV_content);
        email_show = (TextView)v.findViewById(R.id.emailTV_content);
        btn_logout = (Button)v.findViewById(R.id.btn_logout);
        imageView = (ImageView)v.findViewById(R.id.imageView1);

        user_name = getArguments().getString("이름");
        user_email = getArguments().getString("이메일");
        sessionID = getArguments().getString("세션");

        Log.e("멤버인포에서 이름 전달받음", user_name);
        name_show.setText(user_name);
        email_show.setText(user_email);

        Button profie_edit_button = (Button)v.findViewById(R.id.edit_button);
        profie_edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Profile_Activity.class);
                intent.putExtra("이름", user_name);
                intent.putExtra("이메일", user_email);
                intent.putExtra("세션", sessionID);
                startActivity(intent);
                getActivity().finish();
            }
        });


        //로그아웃버튼 누르면 로그아웃한다.
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle("로그아웃");
                dialog.setMessage("로그아웃 하시겠습니까?.");
                dialog.setCancelable(true);
                dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //페이스북 로그인 했을 경우에 페이스북 로그아웃을 실행한다.
                        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest.Callback() {
                            @Override
                            public void onCompleted(GraphResponse response) {
                                LoginManager.getInstance().logOut();
                            }
                        }).executeAsync();
                        //토큰에 null 셋하기
                        AccessToken.setCurrentAccessToken(null);
                        //페이스북 로그아웃
                        // 끝

                        //서버에 있는 session_destroy php파일에 접근하는 클랙스를 실행한다.
                        SessionDestroy_DB destroySession = new SessionDestroy_DB();
                        destroySession.execute();
                    }
        });
                dialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                dialog.create();
                dialog.show();
            }
        });

        //로그아웃 끝
        profileImage profileImage = new profileImage();
        profileImage.execute();
        return v;
    }


    public void setimage(){
        Glide.with(getContext()).load("http://222.239.249.149/"+msg_from_db).into(imageView);
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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


    //저장된 프로필 이미지를 불러와서 회원정보 화면에 셋해주는 클래스.
    public class profileImage extends AsyncTask<Void, Integer, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            try{
                URL url = new URL("http://222.239.249.149/profile_Image_send.php");
                //httpurlconnection 생성
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                //conn 설정
                conn.setRequestProperty("Accept-Charset", "UTF-8");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Cookie", sessionID);
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.connect();

                /* 서버 -> 안드로이드 서버의 리턴값 전달 */
                InputStream is = null;
                BufferedReader in = null;

                //서버로부터 받은 인풋스트림을 스트링화
                is = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);

                String line = null;
                StringBuffer buff = new StringBuffer();
                while ( ( line = in.readLine() ) != null )
                {
                    buff.append(line + "\n");
                }
                //서버의 리턴값
                msg_from_db = buff.toString().trim();

                /* 서버에서 한 응답을 보여주는 로그 */
                Log.e("프로필이미지 서버의리턴값",msg_from_db);


            }catch (MalformedURLException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Glide.with(getContext()).load("http://222.239.249.149/"+msg_from_db).into(imageView);
        }
    }



    public class SessionDestroy_DB extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL("http://222.239.249.149/logout.php");
                //httpurlconnection 생성
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                //conn 설정
                conn.setRequestProperty("Accept-Charset", "UTF-8");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Cookie", SessionID);
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.connect();

                /* 서버 -> 안드로이드 서버의 리턴값 전달 */
                InputStream is = null;
                BufferedReader in = null;

                //서버로부터 받은 인풋스트림을 스트링화
                is = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);

                String line = null;
                StringBuffer buff = new StringBuffer();
                while ((line = in.readLine()) != null) {
                    buff.append(line + "\n");
                }
                //서버의 리턴값
                msg_from_db_about_logout = buff.toString().trim();

                /* 서버에서 한 응답을 보여주는 로그 */
                Log.e("로그아웃시 서버의리턴값", msg_from_db_about_logout);

                //세션디스트로이 성공
                if (msg_from_db_about_logout.equals("1")) {
                    Log.e("세션 파괴 결과값", "세션 파괴 성공");

                } else {
                    Log.e("세션 파괴 결과", "세션 파괴 중 에러 발생! ERRCODE = " + data);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

}


