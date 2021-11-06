package com.example.dlehd.gazuua.board;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
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

import com.example.dlehd.gazuua.MainActivity;
import com.example.dlehd.gazuua.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Post_write_Activity extends AppCompatActivity {
    Button save, cancle;
    EditText title_et, content_et;
    TextView writer_tv;
    ImageView imageView;
    String title_str, content_str, path, sessionID, user_name, user_email;
    Uri provideruri;
    private OkHttpClient client;

    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}; //권한 설정 변수
    private static final int MULTIPLE_PERMISSIONS = 101; //권한 동의 여부 문의 후 CallBack 함수에 쓰일 변수
    private static final int REQUEST_GELLARY = 1111;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_write_);
        Intent intent = getIntent();
        sessionID = intent.getStringExtra("SessionID");
        user_email = intent.getStringExtra("user_email");
        user_name = intent.getStringExtra("user_name");
        title_et = (EditText)findViewById(R.id.title_et_for_write);
        content_et = (EditText)findViewById(R.id.content_tv_for_write);
        imageView = (ImageView)findViewById(R.id.imageView);
        
        save = (Button)findViewById(R.id.button7);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title_str = title_et.getText().toString();
                content_str = content_et.getText().toString();
                sendData();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleryPic();
                checkPermissions();
            }
        });
        

    }

    public void sendData(){
        new Thread(){
            public void run(){
                Log.e("before requestWebserver", sessionID);
                requestWebServer(sessionID, title_str, content_str, path, callback);
            }
        }.start();
    }

    /** 웹 서버로 요청을 한다. */
    public void requestWebServer(final String session, String title, String content, String uri, Callback callback) {
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

        String timeStamp = String.valueOf(System.currentTimeMillis());
        String imageFileName = "gazuaa_board"+timeStamp;
        RequestBody body = new MultipartBody.Builder()

                .setType(MultipartBody.FORM)
                .addFormDataPart("title", title)
                .addFormDataPart("content", content)
                .addFormDataPart("file", imageFileName, RequestBody.create(MultipartBody.FORM, new File(uri)))
                .build();

        Log.e("서버로 보낼 uri", uri);
        Request request = new Request.Builder()
                .url("http://222.239.249.149/board_insert.php")
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

    private final Callback callback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.e("Post_write_Activity", "콜백오류:"+e.getMessage());
        }
        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String body = response.body().string();
            Log.e("Post_write_Activity", "응답한 Body:"+body);

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("SessionID", sessionID);
            intent.putExtra("user_name", user_name);
            intent.putExtra("user_email", user_email);
            startActivity(intent);
            finish();
        }
    };




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
