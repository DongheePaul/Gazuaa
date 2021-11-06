package com.example.dlehd.gazuua.board;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.dlehd.gazuua.MainActivity;
import com.example.dlehd.gazuua.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 게시물 목록(Board.java)에서 선택한 게시물을 읽는 액티비티.
 *
 */
public class Post_Read_Activity extends AppCompatActivity {

    TextView title_tv, writer_tv, time_tv, content_tv, id_tv;
    String board_id, id, title, writer, time, sessionID, user_name, user_email, image_path, content, same, result;
    ProgressDialog progressDialog;
    ImageView imageView;

    Button Edit_btn, Delete_btn;

    //PostEditActivity (게시물 수정 액티비티)에서 게시물 수정 후 완료버튼을 누르면
    //Post_Read_Activity(현재 액티비티)로 넘어오는데, 이때 onResume부터 생명주기가 시작되므로
    //게시물 수정 후 게시글읽기(현재 액티비티)에서 수정된 내용을 바로 확인할 수 있기 위해
    //onResume에서도 게시글 정보를 불러온다.
    @Override
    protected void onResume() {
        super.onResume();
        //PostEditActivity (게시물 수정 액티비티)에서 보낸 인텐트.
        Intent intent = getIntent();
        //PostEditActivity (게시물 수정 액티비티)에서 보낸 인텐트 안에 들어있는 글번호.
        board_id = intent.getStringExtra("id");
        //선택된 게시글에 대한 데이터를 가져오는 클래스.
        BoardImage boardImage = new BoardImage();
        boardImage.execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post__read_);
        Intent intent = getIntent();
        //해당 글의 번호.
        board_id = intent.getStringExtra("id");
        sessionID = intent.getStringExtra("sessionID");
        user_name = intent.getStringExtra("user_name");
        user_email = intent.getStringExtra("user_email");


        title_tv = (TextView) findViewById(R.id.title_tv_for_read);
        writer_tv = (TextView) findViewById(R.id.writer_tv_for_read);
        time_tv = (TextView) findViewById(R.id.time_tv_for_read);
        content_tv = (TextView) findViewById(R.id.content_tv_for_read);
        id_tv = (TextView) findViewById(R.id.id_tv_for_read);
        imageView = (ImageView) findViewById(R.id.imageView2);


        Edit_btn = (Button) findViewById(R.id.edit_btn);
        Delete_btn = (Button) findViewById(R.id.delete_btn);
        //삭제버튼 클릭시 게시글을 삭제.
        Delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            post_delete();
            }
        });

        //수정버튼 클릭시 수정 액티비티로 넘어간다.
        Edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(), PostEditActivity.class);
                intent1.putExtra("sessionID", sessionID);
                intent1.putExtra("user_name", user_name);
                intent1.putExtra("user_email", user_email);
                intent1.putExtra("id", board_id);
                startActivity(intent1);
                finish();
            }
        });

        /**
         * 친구신청을 클릭하면 친구신청이 서버로 전송됨.
         */
        //친구신청 버튼을 누르면
        Button friend_btn = (Button)findViewById(R.id.btn_for_friend);
        friend_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //게시물 작성자의 이메일이 서버로 전송됨.
                String writer = writer_tv.getText().toString();
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .addInterceptor(new Interceptor() {
                            @Override
                            public Response intercept(Chain chain) throws IOException {
                                Request original = chain.request();
                                Request authorized = original.newBuilder()
                                        .addHeader("Cookie", sessionID)
                                        .build();
                                return chain.proceed(authorized);
                            }
                        })
                        .build();

                HttpUrl.Builder urlbuilder = HttpUrl.parse("http://222.239.249.149/firebase/sendMessage.php").newBuilder();
                String requestUrl = urlbuilder.build().toString();
                RequestBody body = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        //작성자의 이메일을 http 프로토콜에 넣어 서버로 보낸다.
                        .addFormDataPart("writer", writer)
                        .build();

                final Request request = new Request.Builder()
                        .url(requestUrl)
                        .post(body)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("에러", e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String result = response.body().string();
                        Log.e("firebaseToken 삽입 확인", result);
                    }
                });

            }
        });
    }



    //해당 글을 삭제하는 메소드.
    public void post_delete(){
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlbuilder = HttpUrl.parse("http://222.239.249.149/board/board_delete.php").newBuilder();
        String requestUrl = urlbuilder.build().toString();
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                //게시글 번호를 넣어서 서버로 보낸다.
                .addFormDataPart("num", board_id)
                .build();

        final Request request = new Request.Builder()
                .url(requestUrl)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("에러", e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.e("넘어온 거 확인", result);
                //게시글이 삭제되면 메인액티비티로 이동한다.
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                Log.e("user_name second", user_name);
                intent.putExtra("SessionID", sessionID);
                intent.putExtra("user_name", user_name);
                intent.putExtra("user_email", user_email);
                intent.putExtra("id", board_id);
                startActivity(intent);
                finish();
            }
        });


    }

    //서버로부터 유저의 프로필 사진을 불러와서 이미지뷰에 뿌리는 클래스.
    public class BoardImage extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            String param = "board_id="+ board_id;
            try{
                URL url = new URL("http://222.239.249.149/board/board_read.php");
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

                //클라이언트에서 서버로 보내는 회원정보
                OutputStream outs = conn.getOutputStream();
                //통신하기 위한 url 인코딩
                outs.write(param.getBytes("UTF-8"));
                outs.flush();
                outs.close();

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
                result = buff.toString().trim();

                    Log.e("post_read", result);
                    JSONObject jsonObject = new JSONObject(result);
                    id = jsonObject.getString("id");
                    title = jsonObject.getString("title");
                    time = jsonObject.getString("time");
                    writer = jsonObject.getString("writer");
                    image_path = jsonObject.getString("image");
                    content = jsonObject.getString("content");
                    same = jsonObject.getString("same");

                }
               catch (IOException e1) {
                e1.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }


        //이미지 뷰에 유저의 프로필 사진 뿌려준다.
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //로그인한 유저와 작성자가 같지 않다면 수정, 삭제버튼이 보이지 않는다.
            if (same.equals("1")) {
                Edit_btn.setVisibility(View.INVISIBLE);
                Delete_btn.setVisibility(View.INVISIBLE);
            }

            id_tv.setText(id);
            title_tv.setText(title);
            time_tv.setText(time);
            writer_tv.setText(writer);
            content_tv.setText(content);

            String image = image_path.replace("\\","");
            Glide.with(Post_Read_Activity.this).load("http://222.239.249.149/"+image).into(imageView);
        }
    }


}
