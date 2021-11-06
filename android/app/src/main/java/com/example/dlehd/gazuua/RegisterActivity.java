package com.example.dlehd.gazuua;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.login.widget.LoginButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;


/* 회원가입 기능
1. 어플의 회원가입 화면에서 회원정보(이름, 아이디, 비밀번호, 이메일)를 입력하고 완료버튼을 누르면 “회원가입 완료”라는 메시지가 뜬다(입력양식을 모두 지켰을 때). 그리고 로그인 화면으로 넘어간다.
2. 회원정보를 양식에 맞게 입력했다면 입력했던 회원정보는 회원정보를 관리하는 DB에 삽입된다.
3. 입력양식에 맞지 않거나 중복된 아이디 등 회원가입에 문제가 되는 사항이 있다면 완료버튼을 눌렀을 때 그 사항에 대한 안내 메시지가 뜬다. 그리고 다른 화면으로 넘어가지 않는다.
4. 취소버튼을 누르면 액티비티가 사라진다.

 */
public class RegisterActivity extends AppCompatActivity {
    EditText et_pw, et_pw_chk, et_name, et_email;    //비밀번호, 비밀번호 확인, 이름, 이메일을 입력하는 에디트 텍스트
    String s_pw, s_pw_chk, s_name, s_email, data;   //비밀번호, 비밀번호 확인, 이름, 이메일 값을 저장하는 스트링 변수.   data ==> 서버로부터의 리턴값을 저장
    String email_facebook, name_facebook, id_facebook, SessionID; //페이스북 로그인 시 유저의 이름, 이메일, 아이디
    String param;
    private CallbackManager callbackManager;
    LoginButton facebook_login;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
       /* FacebookSdk.sdkInitialize(this.getApplicationContext());
        //페북에서 회원정보 받아온다.
        facebook_login=(LoginButton)findViewById(R.id.FBlogin);
        callbackManager = CallbackManager.Factory.create();
        //페이스북으로 회원가입
        facebook_login.setReadPermissions(Arrays.asList("public_profile", "email"));
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.e("페이스북 유저아이디",loginResult.getAccessToken().getUserId());
                Log.e("페이스북 퍼미션 리스트",loginResult.getAccessToken().getPermissions()+"");
                GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.e("페이스북 로그인시 그래프 요청 결과 ", object.toString());
                        try{
                            email_facebook = object.getString("email");
                            name_facebook = object.getString("name");
                            name_facebook = name_facebook.replaceAll(" ", "");
                            id_facebook = object.getString("id");

                            //가져온 회원정보를 db에 넣는다.
                            regisDB regisDB = new regisDB();
                            regisDB.execute();
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

            }

            @Override
            public void onError(FacebookException error) {

            }
        });*/


        et_pw = (EditText)findViewById(R.id.editText1); //비밀번호 입력창
        et_pw_chk = (EditText)findViewById(R.id.editText6); //비밀번호 확인 입력창
        et_name = (EditText)findViewById(R.id.editText3);   //이름 입력창
        et_email = (EditText)findViewById(R.id.editText5);  //이메일 입력창


    }

    /**
     *  회원가입에서 완료 버튼 눌렀을 때 일어나는 이벤트.
     1. 회원정보 입력값들이 변수에 저장됨
     2. 회원정보 입력 양식에 맞는지 확인하는 조건문
     3. 서버와 통신하기 위한 클래스 실행.
     *
     */


    public void bt_Register(View view){
        s_pw=et_pw.getText().toString();    //에디트텍스트에 입력한 비밀번호 값을 스트링 변수에 저장
        s_pw_chk=et_pw_chk.getText().toString();    //에디트텍스트에 입력한 비밀번호 확인 값을 스트링 변수에 저장
        s_name=et_name.getText().toString();    //에디트텍스트에 입력한 이름 값을 스트링 변수에 저장
        s_email=et_email.getText().toString();  //에디트텍스트에 입력한 이메일 주소를 스트링 변수에 저장.

        //회원가입 페이지에서 입력 항목이 하나라도 공백일 때 띄우는 토스트메세지
        if(s_pw.length() == 0 || s_name.length() == 0 || s_email.length() == 0) {
            Toast.makeText(RegisterActivity.this, "모든 항목을 입력해주세요", Toast.LENGTH_LONG).show();
            return;
        }

        //회원가입 페이지에서 이름에 관련된 예외처리       -> 완료
        //1. 회원가입 페이지에서 이름이 최소 2글자가 아닐 떄.
        //2. 한글과 영문 외에 입력시
       if(s_name.length() <= 1 || !Pattern.matches("^[가-힣a-zA-Z]*$", s_name)){
            Toast.makeText(RegisterActivity.this, "이름 입력 양식을 지켜주세요", Toast.LENGTH_LONG).show();
            return;
        }

        //회원가입 페이지에서 비밀번호 관련된 예외처리
        //1. 영어, 숫자를 혼합한 8~20자리 문자열
        if(!Pattern.matches("^(?=.*[a-zA-Z]+)(?=.*[0-9]+).{8,20}$", s_pw)){
            Toast.makeText(RegisterActivity.this, "비밀번호 입력 양식을 지켜주세요.", Toast.LENGTH_LONG).show();
            return;
        }

        //2.비밀번호, 비밀번호 확인 불일치
        if(!s_pw.equals(s_pw_chk)){
            Toast.makeText(RegisterActivity.this, "비밀번호 확인을 정확히 해주세요.", Toast.LENGTH_LONG).show();
            return;
        }

        //회원가입 페이지에서 이메일 관련된 예외처리
      if(!Patterns.EMAIL_ADDRESS.matcher(s_email).matches()){
            Toast.makeText(RegisterActivity.this, "이메일 형식을 확인해주세요.", Toast.LENGTH_LONG).show();
            return;
        }

        //모든 예외처리 통과하면 회원정보를 db에 삽입하는 웹페이지로 연결하는 클래스 실행.
        regisDB rdb = new regisDB();
        rdb.execute();
    }

    //회원가입 페이지에서 취소버튼 눌렀을 때 이벤트. 액티비티가 사라진다.
    public void bt_Register_cancle(View view){
        finish();
    }

    //회원정보를 관리하는 db와 통신하는 클래스
    /** 회원정보를 관리하는 db와 통신하는 클래스.
     * 1. 서버로 회원정보를 담은 파라미터 전송
     * 2. 서버로부터의 응답을 스트링화해서 변수에 저장
     * 3. 서버의 응답 값에 따라 메세지 띄움
     */
    public class regisDB extends AsyncTask<Void, Integer, Void> {
        @Override
        /**
         * 서버에 요청하고 리턴값을 읽는다.
         */
        protected Void doInBackground(Void... params) {
            //서버에 전달할 회원정보 파라미터 변수
            //페이스북으로 회원가입할 때

            //일반 회원가입
                param = "u_pw=" + s_pw + "&u_name=" + s_name + "&u_email=" + s_email;

            Log.e("회원가입시 값 넘어가는거 확인", param);
            try{
                //회원등록하는 파일에 접근
                URL url = new URL("http://222.239.249.149/register.php");
                //httpurlconnection 생성
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                //httpurlconnection 형식
                conn.setRequestProperty("Accept-Charset", "UTF-8");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");   //http 통신을 요청하기 위한 형식
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.connect();

                /* 안드로이드 -> 서버.  db에 삽입하기 위한 회원정보 파라메터값 전달 */
                OutputStream outs = conn.getOutputStream();
                //utf-8 형태의 바이트로 파라미터를 인코딩해 아웃풋스트림에 적어준다.
                outs.write(param.getBytes("UTF-8"));
                outs.flush();
                outs.close();
                String out_chk = outs.toString();
                Log.d("파라미터", param);
                Log.d("아웃풋확인", out_chk);
                Log.d("아웃풋확인2", String.valueOf(param.getBytes("UTF-8")));


                /* 서버 -> 안드로이드.  회원정보 삽입에 대한 서버의 리턴값 전달 */
                InputStream is = null;
                BufferedReader in = null;
                data = "";

                //서버로부터 온 응답에서 헤더의 Set-Cookie를 추출해 스트링변수에 저장.
                String cookieTemp = conn.getHeaderField("Set-Cookie");
                //Log.e("cookieTemp", cookieTemp);
                if (cookieTemp != null) {
                    int idx = cookieTemp.indexOf(";");
                    //쿠키에 있는 세션id 저장한 변수.
                    SessionID = cookieTemp.substring(0, idx);
                    Log.e("cookie", SessionID);
                }

                //서버로부터 받은 인풋스트림을 스트링화
                is = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(is));
                String line = null;
                StringBuffer buff = new StringBuffer();
                while ( ( line = in.readLine() ) != null )
                {
                    //파라미터가 2개 이상이면 파라미터 연결에 \n을 사용해 스윙치할 변수 생성.
                    buff.append(line + "\n");
                }

                //Log.d("인풋스트림 line!!", String.valueOf(buff));
                //회원정보 삽입에 대한 서버의 리턴값
                data = buff.toString().trim();
                Log.d("RECV DATA",data);
                //리턴값이 0이면 성공했다는 뜻.
                if(data.equals("0"))
                {
                    Log.e("RESULT","성공적으로 처리되었습니다!");
                }
                else
                {
                    Log.e("RESULT","에러 발생! ERRCODE = " + data);
                }




            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * 서버로부터 리턴값을 받으면 메세지를 띄운다.
         * 1. 회원가입 성공한 경우
         * 2. 아이디 중복일 경우
         * 3. 그 외의 문제가 발생한 경우
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Log.e("서버로부터 응답 확인", data);

            //회원정보 등록이 성공했다는 것을 유저에게 알리기 위한 알럴트  창
            //회원정보 삽입을 서버에 요청했고, 서버가 보낸 리턴값이 0이면 회원정보 등록 성공
            if(data.equals("0")){
                Log.e("RESULT", "성공적으로 리턴값 받음");
                AlertDialog.Builder dialog = new AlertDialog.Builder(RegisterActivity.this);
                dialog.setTitle("알림");
                dialog.setMessage("성공적으로 등록되었습니다.");
                dialog.setCancelable(true);
                dialog.setPositiveButton("확인", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                dialog.create();
                dialog.show();
            }

            else if(data.equals("fb")){
                Log.e("페이스북 게정 회원가입", "db에서 잘 확인함.");
                Intent intent = new Intent(RegisterActivity.this,  MainActivity.class);
                intent.putExtra("SessionID", SessionID);
                intent.putExtra("user_email", email_facebook);
                intent.putExtra("user_name", name_facebook);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                startActivity(intent);

                finish();
            }
            else if(data.equals("fbd")){
                Log.e("페이스북 게정 회원가입 중복아디.", "db에서 잘 확인함.");
                Intent intent = new Intent(RegisterActivity.this,  MainActivity.class);
                intent.putExtra("SessionID", SessionID);
                intent.putExtra("user_email", email_facebook);
                intent.putExtra("user_name", name_facebook);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);

                finish();

            }



            //아이디가 중복일 때 유저에게 알리는 토스트메세지 창
            else if(data.equals("D")){
                Log.e("RESULT", "이메일 중복");
                Toast.makeText(RegisterActivity.this, "이미 사용중인 이메일입니다.", Toast.LENGTH_LONG).show();
                return;
            }

            //회원정보 등록 실패를 유저에게 알리기 위한 알럴트 창.
            //회원정보 삽입을 서버에 요청했고, 서버가 보낸 리턴값이 0이 아니면 회원정보 등록 실패.
                else{
                Log.e("RESULT","에러 발생! ERRCODE = " + data);
                AlertDialog.Builder dialog = new AlertDialog.Builder(RegisterActivity.this);
                dialog.setTitle("알림");
                dialog.setMessage("에러가 발생했습니다. 해당 메시지가 계속된다면 개발자에게 문의 부탁드립니다. \\n email: dlehdgml0480@naver.com");
                dialog.setCancelable(true);
                dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                dialog.create();
                dialog.show();

            }
        }


    }


    //페이스북 로그인 결과를 콜백매니저에세 전달.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
