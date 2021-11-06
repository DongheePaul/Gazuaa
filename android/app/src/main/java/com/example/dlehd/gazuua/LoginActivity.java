package com.example.dlehd.gazuua;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;


/**
 * 로그인
 * 1. 서버랑 통신해 입력한 아이디, 비밀번호가 맞는지 확인한다.
 * 2. 입력한 아이디, 비밀번호가 일치한다면 메인 액티비티로 이동한다
 * 3. 아이디, 비밀번호 불일치하거나 존재하지 않는 아이디면 메세지가 뜬다. 다른 액티비티로의 이동은 없다.
 *
 */
public class LoginActivity extends AppCompatActivity {
    String semail, sPw, data;    //에디트 텍스트에 입력한 값을 스트링 변수에 저장 (sId, sPw)   data==> 서버로부터의 리턴값 저장.
    EditText etmail, etpw;    //아이디, 비밀번호 입력하는 에디트 텍스트
    Button loginBtn;  //로그인 버튼
    String SessionID;  //세션id를 저장한 변수.
    private CallbackManager callbackManager;   //로그인 요청에 대한 페이스북의 응답을 받아주는 콜백매니저
    LoginButton facebook_login;
    String email_facebook, name_facebook, id_facebook; //페이스북 로그인 시 유저의 이름, 이메일, 아이디
    String str_result, str_name, str_email, str_id;  //서버로부터 온 회원정보
    String autoLogin_id_str, autoLogin_pw_str;

    SharedPreferences autoLogin;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etmail=(EditText)findViewById(R.id.editText);
        etpw=(EditText)findViewById(R.id.editText6);

        autoLogin = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE);
        autoLogin_id_str = autoLogin.getString("id", "");
        autoLogin_pw_str = autoLogin.getString("pw", "");
        if(!autoLogin_id_str.equals("")){
            semail = autoLogin_id_str;
            sPw = autoLogin_pw_str;
            Log.e("자동로그인 아이디", semail);
            Log.e("자동로그인 비번", sPw);
            loginDB lDB=new loginDB();
            lDB.execute();

        }
        //페이스북 로그인
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        //페이스북 로그인 버튼에 권한 설정.
        facebook_login=(LoginButton)findViewById(R.id.facebook_login);
        facebook_login.setReadPermissions(Arrays.asList("public_profile", "email"));
        //로그인 결과에 응답하기 위해 로그인매니저에 페이스북콜백 등록
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            //로그인 성공
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.e("페이스북 유저아이디",loginResult.getAccessToken().getUserId());
                Log.e("페이스북 퍼미션 리스트",loginResult.getAccessToken().getPermissions()+"");
                //로그인에 성공하면 loginResult 매개변수에 새로운 accessToken과 권한이 포함된다.
                GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.e("페이스북 로그인시 그래프 요청 결과 ", object.toString());
                        try{
                            email_facebook = object.getString("email");
                            name_facebook = object.getString("name");
                            name_facebook = name_facebook.replaceAll(" ", "");
                            id_facebook = object.getString("id");

                            FBloginDB fbloginDB = new FBloginDB();
                            fbloginDB.execute();
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                });
                //회원정보 조회를 위한 그래프 요청.
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, name, email, gender, birthday");
                graphRequest.setParameters(parameters);
                graphRequest.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.e("페이스북 로그인 취소 ", "onCancle");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("페이스북 로그인 에러",error.toString());

            }
        });

        //아이디 입력하는 에디트텍스트
        etmail = (EditText)findViewById(R.id.editText);
        //비번 입력하는 에디트텍스트
        etpw=(EditText)findViewById(R.id.editText6);
        //로그인 버튼
        loginBtn = (Button)findViewById(R.id.button);
        loginBtn.setOnClickListener(new View.OnClickListener() {

            //로그인버튼 클릭시
            @Override
            public void onClick(View v) {
            //로그인 화면에서 입력한 에디트텍스트 값들 스트링화하여 변수(sId, sPw)에 저장
                try{
                    //입력한 이메일 값을 스트링 변수에 저장.
                    semail = etmail.getText().toString();
                    //입력한 비밀번호 값을 스트링 변수에 저장.
                    sPw = etpw.getText().toString();
                }catch(NullPointerException e){
                    Log.e("로그인 시 입력한 값들의 스트링화 에러", e.getMessage());
                }

                //아이디, 비밀번호에 공백을 입력했을 경우
                if(semail.length() == 0 || sPw.length() == 0){
                    Toast.makeText(LoginActivity.this, "아이디와 비밀번호를 입력해주세요.", Toast.LENGTH_LONG).show();
                    return;
                }
                //로그인 위해 서버랑 통신하는 클래스 실행
                loginDB lDB=new loginDB();
                lDB.execute();
            }
        });
    }

    //페이스북 로그인 결과를 콜백매니저에세 전달.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    //페북 계정으로 로그인할 때 서버와 통신하는 클래스
    public  class FBloginDB extends AsyncTask<Void, Integer, Void> {
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.e("서버로부터의 로그인 리턴값", data);
            //로그인 성공시 서버에서 1을 리턴한다. 서버에서 1을 리턴시 메인으로 이동
            if(str_result.equals("1")){
                Log.e("RESULT", "성공적으로 로긴처리");
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("SessionID", SessionID);
                //페북 이메일
                intent.putExtra("user_email", str_email);
                //페북 이름
                intent.putExtra("user_name", str_name);

                String token = AccessToken.getCurrentAccessToken().getToken();
                //Log.e("토큰 값 확인", token);
                startActivity(intent);
                finish();

            }
            //에러 발생시 알럴트 창 띄우기
            else
            {
                Log.e("Result", "에러발생! ERRORCODE = "+data);
                AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
                dialog.setTitle("알림");
                dialog.setMessage("에러가 발생했습니다. 이 메세지가 계속된다면 개발자에게 문의해주시길 부탁드립니다. \n E-mail:dlehdgml0480@naver.com ");
                dialog.setCancelable(true);
                dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //finish();
                    }
                });
                dialog.create();
                dialog.show();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            //서버로 전달할 페이스북 회원정보를 담은 파라미터.
            String param = "u_email=" + email_facebook + "&u_name=" + name_facebook + "&u_id=" + id_facebook;
            Log.e("페북 메일, 이름, 아이디", param);

            try {
                //로그인에 사용되는 파일에 접근하기 위한 url값 저장
                URL url = new URL("http://222.239.249.149/login.php");

                //httpurlconnection 생성
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                //conn 설정
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

                /* 서버 -> 안드로이드 서버의 리턴값 전달 */
                InputStream is = null;
                BufferedReader in = null;
                data = "";

                //서버로부터 온 응답에서 헤더의 Set-Cookie를 추출해 스트링변수에 저장.
                String cookieTemp = conn.getHeaderField("Set-Cookie");
                if (cookieTemp != null) {
                    int idx = cookieTemp.indexOf(";");
                    //쿠키에 있는 세션id 저장한 변수.
                    SessionID = cookieTemp.substring(0, idx);
                    Log.d("cookie", SessionID);
                }

                //서버로부터 받은 인풋스트림을 스트링화
                is = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);

                String line = null;
                StringBuffer buff = new StringBuffer();
                while ((line = in.readLine()) != null) {
                    buff.append(line + "\n");
                }

                //회원정보 서버로부터의 리턴값
                data = buff.toString().trim();
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    str_result = jsonObject.getString("result");
                    str_email = jsonObject.getString("email");
                    str_name = jsonObject.getString("name");
                    str_id = jsonObject.getString("id");
                }
                catch (JSONException e){
                    e.printStackTrace();
                }

                /* 서버에서 한 응답을 보여주는 로그 */
               if (str_result.equals("1")) {
                    Log.e("RESULT", "성공적으로 처리되었습니다!");
                } else {
                    Log.e("RESULT", "에러 발생! ERRCODE = " + data);
                }

                conn.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    //페북 계정으로 로그인할 때 서버와 통신하는 클래스
    //끝

    //회원가입한 계정으로 로그인 하기 위해 서버랑 통신하는 클래스
    public class loginDB extends AsyncTask<Void, Integer, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            //아이디, 비밀번호를 전달할 파라미터 변수
            String param = "u_email=" + semail + "&u_pw=" + sPw;
            Log.e("POST",param);

            try {
                //로그인에 사용되는 파일에 접근하기 위한 url값 저장
                URL url = new URL("http://222.239.249.149/login.php");
                //httpurlconnection 생성
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                //conn 설정
                conn.setRequestProperty("Accept-Charset", "UTF-8");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.connect();

                  /* 안드로이드 -> 서버로 아이디, 비밀번호 값 전달 */
                OutputStream outs = conn.getOutputStream();
                //통신하기 위한 url 인코딩
                outs.write(param.getBytes("UTF-8"));
                outs.flush();
                outs.close();

                /* 서버 -> 안드로이드 서버의 리턴값 전달 */
                InputStream is = null;
                BufferedReader in = null;
                data = "";

                //서버로부터 온 응답에서 헤더의 Set-Cookie를 추출해 스트링변수에 저장.
                String cookieTemp = conn.getHeaderField("Set-Cookie");
                if (cookieTemp != null)
                {
                    int idx = cookieTemp.indexOf(";");
                    //쿠키에 있는 세션id 저장한 변수.
                    SessionID = cookieTemp.substring(0, idx);
                    Log.e("cookie", SessionID);
                }

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

                data = buff.toString().trim();
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    str_result = jsonObject.getString("result");
                    str_email = jsonObject.getString("email");
                    str_name = jsonObject.getString("name");
                }
                catch (JSONException e){
                    e.printStackTrace();
                }
                /* 서버에서 한 응답을 보여주는 로그 */
                Log.e("제이슨 확인",data);
                Log.e("리저트 확인", str_result);
                Log.e("이메일 확인", str_email);
                Log.e("이름 확인", str_name);

                if(data.equals("1"))
                {
                    Log.e("RESULT","성공적으로 처리되었습니다!");
                }
                else
                {
                    Log.e("RESULT","에러 발생! ERRCODE = " + data);
                }
            }catch (MalformedURLException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }

            return null;
        }

        //서버의 리턴값에 해당하는 이벤트 실행한다.
        //로그인 성공, 존재하지 않는 아이디, 비밀번호 불일치, 에러 발생시
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //
            Log.e("RECV DATA", data);

            //로그인 성공시 서버에서 1을 리턴한다. 서버에서 1을 리턴시 메인으로 이동
            if(str_result.equals("1")){

                Log.e("RESULT", "성공적으로 로긴처리");
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                         intent.putExtra("SessionID", SessionID);
                        intent.putExtra("user_email", str_email);
                        intent.putExtra("user_name", str_name);
                        startActivity(intent);

                editor = autoLogin.edit();
                editor.putString("id", semail);
                editor.putString("pw", sPw);
                editor.commit();
                Log.e("자동로그인 저장 확인.id", autoLogin.getString("id",""));
                Log.e("자동로그인 저장 확인.pw", autoLogin.getString("pw",""));

                finish();
            }

            //비밀번호 불일치 시 서버에서 0을 리턴한다. 서버에서 0을 리턴시 다이얼로그창을 띄운다.
            else if(str_result.equals("0")){
                Log.e("RESULT","비밀번호가 일치하지 않습니다.");
                AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
                dialog.setTitle("알림");
                dialog.setMessage("아이디와 비밀번호를 확인하세요.");
                dialog.setCancelable(true);
                dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //finish();
                    }
                });
                dialog.create();
                dialog.show();

            }

            //존재하지 않는 계정 입력 할 시 서버에서 B를 리턴한다.
            //서버에서 B를 리턴 시 토스트메세지를 띄운다.
            else if (str_result.equals("B")){
                Log.e("존재하지 않는 계정", "존재하지 않는 계정");
                Toast.makeText(LoginActivity.this, "존재하지 않는 아이디입니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            //에러 발생시 알럴트 창 띄우기
            else
            {
                Log.e("Result", "에러발생! ERRORCODE = "+data);
                AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
                dialog.setTitle("알림");
                dialog.setMessage("에러가 발생했습니다. 이 메세지가 계속된다면 개발자에게 문의해주시길 부탁드립니다. \n E-mail:dlehdgml0480@naver.com ");
                dialog.setCancelable(true);
                dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //finish();
                    }
                });
                dialog.create();
                dialog.show();
            }

        }
    }
    //회원가입한 계정으로 로그인 하기 위해 서버랑 통신하는 클래스
    //끝


    //회원가입 텍스트뷰 눌렀을 때 일어나는 이벤트 => 회원가입 액티비티로 넘어가기
    public void TextView_Go_Register(View view){
        Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(intent);
    }


}
