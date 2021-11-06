package com.example.dlehd.gazuua.board;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.dlehd.gazuua.MainActivity;
import com.example.dlehd.gazuua.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * 게시글 읽기 (Post_Read_Activity)에서 수정버튼을 누르면 나타나는 게시물 수정 액티비티
 */
public class PostEditActivity extends AppCompatActivity {
    String board_id, id, title, writer, time, sessionID, user_name, user_email, image_path, content, same, result;
    Uri provideruri;
    //선택한 갤러리 이미지의 주소;
    String path, title_str, content_str;
    EditText title_et, content_et;
    Button edit_btn, cancle_btn;
    TextView writer_tv, time_tv;
    ImageView imageView;

    private OkHttpClient client;

    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}; //권한 설정 변수
    private static final int MULTIPLE_PERMISSIONS = 101; //권한 동의 여부 문의 후 CallBack 함수에 쓰일 변수
    private static final int REQUEST_GELLARY = 1111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_edit);
        Intent intent = getIntent();
        board_id = intent.getStringExtra("id");
        sessionID = intent.getStringExtra("sessionID");
        user_name = intent.getStringExtra("user_name");
        user_email = intent.getStringExtra("user_email");

        title_et = (EditText)findViewById(R.id.title_et_for_edit);
        content_et = (EditText)findViewById(R.id.content_tv_for_edit);

        time_tv = (TextView)findViewById(R.id.time_tv);
        writer_tv = (TextView)findViewById(R.id.writer);

        edit_btn = (Button)findViewById(R.id.save_btn_for_edit);
        cancle_btn = (Button)findViewById(R.id.cancle_btn1);
        //이미지뷰를 클릭하면 갤러리의 이미지를 선택할 수 있고. 선택된 이미지는 이미지뷰에 출력된다. 후에 저장버튼을 누르면 해당 이미지가 서버에 업로드 된다.
        imageView = (ImageView)findViewById(R.id.imageView3);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //권한 확인하는 메소드
                checkPermissions();
                //갤러리에서 이미지 선택하는 메소드
                galleryPic();
            }
        });

        //취소버튼을 누르면 해당 액티비티가 사라진다.
        cancle_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //게시물의 데이터를 불러온다.
        LoadData_for_edit loadData_for_edit = new LoadData_for_edit();
        loadData_for_edit.execute();

        //수정 완료 버튼을 누르면
        edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //제목, 내용이 스트링변수에 저장(sendData 메소드 안에서 서버에 업로드 된다)되고, 서버에 게시물 정보를 업로드 하는 sendData 메소드가 실행된다.
                title_str = title_et.getText().toString();
                content_str = content_et.getText().toString();
                sendData();
            }
        });

    }


    //액티비티에서 작성한 데이터를 서버로 전송하는 메소드.
    public void sendData(){
        new Thread(){
            public void run(){
                Log.e("requestse. check id", board_id);
                requestWebServer(sessionID, board_id, title_str, content_str, path, callback);
            }
        }.start();
    }

    /** 웹 서버로 요청을 한다. */
    public void requestWebServer(final String session, String board_id1,  String title, String content, String uri, Callback callback) {
        this.client = new OkHttpClient().newBuilder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        Request authorized = original.newBuilder()
                                .addHeader("Cookie", session)
                                .build();
                        return chain.proceed(authorized);
                    }
                })
                .build();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "gazuaa_board"+timeStamp;
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("title", title)
                .addFormDataPart("content", content)
                .addFormDataPart("num", board_id1)
                .addFormDataPart("file", imageFileName, RequestBody.create(MultipartBody.FORM, new File(uri)))
                .build();

        Log.e("서버로 보낼 uri in postEdit", uri);
        Request request = new Request.Builder()
                .url("http://222.239.249.149/board_edit.php")
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

    private final Callback callback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.e("postEditActivity", "콜백오류:"+e.getMessage());
        }
        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String body = response.body().string();
            Log.e("Post_edit 에서", "응답한 Body:"+body);

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("SessionID", sessionID);
            intent.putExtra("user_name", user_name);
            intent.putExtra("user_email", user_email);
            finish();
            startActivity(intent);

        }
    };










    //액티비티가 시작되면 서버로부터 유저의 프로필 사진을 불러와서 이미지뷰에 뿌리는 클래스.
    public class LoadData_for_edit extends AsyncTask<Void, Integer, Void> {
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

                Log.e("Post_Edit", result);
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

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //이미지 뷰에 유저의 프로필 사진 뿌려준다.

            title_et.setText(title);
            time_tv.setText(time);
            writer_tv.setText(writer);
            content_et.setText(content);

            Log.e("in post_read, image", image_path);
            String image = image_path.replace("\\","");
            Glide.with(PostEditActivity.this).load("http://222.239.249.149/"+image).into(imageView);
        }
    }








//갤러리를 실행하는 메소드.
    public void galleryPic() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        //setType 지정-> 인텐트 실행시 어떤 어플 선택할지 안내함.
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_GELLARY);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_GELLARY:
                if(resultCode == Activity.RESULT_OK){
                    //선택한 이미지의 경로.
                    provideruri = data.getData();
                    Log.e("uri 값", String.valueOf(provideruri));
                    //선택한 이미지의 저장소 내 절대경로를 구한다. getPath()
                    path = getPath(provideruri);
                    imageView.setImageURI(provideruri);
                    Log.e("절대경로 확인", path);
                }
        }
    }

    //이미지 절대경로를 구하는 메소드.
    //왜냐면 선택된 이미지의 경로는 디바이스 내 경로와 다르기 때문에, 디바이스 내의 경로(절대경로)를 구해줘야 한다.
    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        startManagingCursor(cursor);
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(columnIndex);
    }

    //권한 확인하는 메소드
    private boolean checkPermissions() {
        int result;
        List<String> permissionList = new ArrayList<>();
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(this, pm);
            if (result != PackageManager.PERMISSION_GRANTED) { //사용자가 해당 권한을 가지고 있지 않을 경우 리스트에 해당 권한명 추가
                permissionList.add(pm);
            }
        }
        if (!permissionList.isEmpty()) { //권한이 추가되었으면 해당 리스트가 empty가 아니므로 request 즉 권한을 요청합니다.
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }


    //아래는 권한 요청 Callback 함수입니다. PERMISSION_GRANTED로 권한을 획득했는지 확인할 수 있습니다. 아래에서는 !=를 사용했기에
//권한 사용에 동의를 안했을 경우를 if문으로 코딩되었습니다.
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        if (permissions[i].equals(this.permissions[0])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();
                            }
                        } else if (permissions[i].equals(this.permissions[1])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();
                            }
                        }
                    }
                } else {
                    showNoPermissionToastAndFinish();
                }
                return;
            }
        }
    }

    //권한 획득에 동의를 하지 않았을 경우 아래 Toast 메세지를 띄우며 해당 Activity를 종료시킵니다.
    private void showNoPermissionToastAndFinish() {
        Toast.makeText(this, "권한 요청에 동의 해주셔야 이용 가능합니다. 설정에서 권한을 허용해주세요.", Toast.LENGTH_SHORT).show();
        finish();
    }

}
