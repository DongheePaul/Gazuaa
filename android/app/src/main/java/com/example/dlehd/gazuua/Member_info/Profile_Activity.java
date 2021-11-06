package com.example.dlehd.gazuua.Member_info;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.dlehd.gazuua.MainActivity;
import com.example.dlehd.gazuua.R;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.R.attr.data;

public class Profile_Activity extends AppCompatActivity {
    String user_name, user_email, sessionID;

    String msg_from_db;

    //Uri. 보통 URL이라함은 인터넷 페이지의 주소를 의미하며 이보다 더 상위의 개념이 URI다. 즉, uri는 웹의 경로뿐 아니라 각각의 파일이 갖고 있는 경로
    Uri imageUri;

    //photoURI => 사진을 찍은 그 파일의 경로
    //albumURI => 사진을 저장할 파일의 경로
    Uri photoURI, albumURI;

    ImageView imageView;

    //현재 사용중인 사진 파일의 경로(디바이스 내의 파일 경로)를 의미한다
    //사진을 바로 찍은 경우라면 찍은 뒤 저장된 임시 파일의 경로일 것이고
    //앨범에서 가져온 파일의 경우 앨범에서 가져온 뒤 다시 생성한 임시 파일의 경로일 것이다.(createImageFile())
    String mCurrentPhotoPath;
    Uri provideruri;
    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}; //권한 설정 변수
    private static final int MULTIPLE_PERMISSIONS = 101; //권한 동의 여부 문의 후 CallBack 함수에 쓰일 변수
    private static final int REQUEST_GELLARY = 1111;
    private static final int REQUEST_TAKE_PHOTO = 2222;  //카메라 요청하는 인텐트에 들어갈 숫자.
    private static final int REQUEST_CROP = 3333; //크롭 요청하는 인텐트에 들어갈 숫자.
    profileImage profileImage;
    ProgressDialog dialog = null;
    Bitmap bm;

    @Override
    protected void onResume() {
        super.onResume();

        profileImage = new profileImage();
        profileImage.execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_);

        user_name = getIntent().getStringExtra("이름");
        user_email = getIntent().getStringExtra("이메일");
        sessionID = getIntent().getStringExtra("세션");


        imageView = (ImageView) findViewById(R.id.Profile_iv);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissions();
                //유저의 사진을 등록할 이미지뷰를 클릭하면 카메라와 앨범를 선택할 수 있다.
                AlertDialog.Builder dialog1 = new AlertDialog.Builder(Profile_Activity.this);
                dialog1.setMessage("프로필사진");
                dialog1.setCancelable(true);
                dialog1.setNeutralButton("사진 촬영", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //카메라 호출

                            //takePhoto();
                            Intent intent = new Intent(getApplicationContext(), OpencvCamera.class);
                            intent.putExtra("이름", user_name);
                            intent.putExtra("이메일", user_email);
                            intent.putExtra("세션", sessionID);

                            startActivity(intent);

                    }
                });
                dialog1.setNegativeButton("앨범에서 사진 선택", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //앨범 호출
                        galleryPic();
                    }
                });
                dialog1.create();
                dialog1.show();
            }
        });

        Button save_btn = (Button)findViewById(R.id.save_btn);
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile(mCurrentPhotoPath);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("user_name", user_name);
                intent.putExtra("user_email", user_email);
                intent.putExtra("SessionID", sessionID);
                startActivity(intent);
                finish();
            }
        });

    }


    //서버로부터 유저의 프로필 사진을 불러와서 이미지뷰에 뿌리는 클래스.
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

                //세션디스트로이 성공
                if(msg_from_db.equals("1"))
                {
                    Log.e("세션 파괴 결과값","세션 파괴 성공");
                }
                else
                {
                    Log.e("세션 파괴 결과","세션 파괴 중 에러 발생! ERRCODE = " + data);
                }
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
            //이미지 뷰에 유저의 프로필 사진 뿌려준다.
            Glide.with(Profile_Activity.this).load("http://222.239.249.149/"+msg_from_db).into(imageView);
        }
    }



    public void takePhoto() throws IOException {
        //카메라로 사진 찍는 메소드
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException e) {
        }

        if (photoFile != null) {
            // image의 uri는 file://로 시작, FileProvider(Content Provider 하위)는 content://로 시작
            // 누가(7.0)이상부터는 file://로 시작되는 Uri의 값을 다른 앱과 주고 받기(Content Provider)가 불가능
            //getUriForFile(getContext() --> contentURI를 얻는 방법.
            Uri provideruri1 = FileProvider.getUriForFile(Profile_Activity.this, "com.example.dlehd.gazuua", photoFile);
            Log.e("provider로 uri 변경", String.valueOf(provideruri));
            provideruri = provideruri1;

            Log.e("takeph에 provideruri 인", String.valueOf(provideruri));
            //인텐트에 넣어주는데, activityresult에서 못 받아온다. 왜지?
            intent.putExtra(MediaStore.EXTRA_OUTPUT, provideruri);
            startActivityForResult(intent, REQUEST_TAKE_PHOTO);

        } else {
            Log.e("takePhoto()", "에서 에러");
            finish();
        }
    }



    private File createImageFile() throws IOException {
        // Create an image file name.  new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String imageFileName = "IP" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures", "gyeom"); //test라는 경로에 이미지를 저장하기 위함
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        //파일 생성되지만 빈 껍데기. 해당 위치를 mCurrentPhotoPath에 저장.
        //여기서 해당 파일의 mCurrentPhotoPath(절대 경로)와 Uri값을 비교하면 uri로 받은 값은 앞에 file:///storage/emulated... 식으로 되며,
        // mCurrentPhotoPath로는 file:///을 제외한 storage/emulated..로 출력된다.
        // 중요한 것은 이 예제에서는 처음에 만들때 URI의 주소를 생성할 수는 없으며,
        // mCurrentPhotoPath란 위치, 경로 값을 세팅하고 이를 통해 URI의 주소 값을 구한 다음, 이 URI 값을 사용한다는 점이다.
        // 즉, URI의 값을 알려면 해당 Path, 경로를 알고(혹은 생성하고) 이를 URI.fromFile()이란 함수를 통해 얻어 활용하게 된다.
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        mCurrentPhotoPath = image.getAbsolutePath();

        return image;
    }


    public void cropImage() throws IOException {

        this.grantUriPermission("com.android.camera", provideruri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(provideruri, "image/*");

        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
        grantUriPermission(list.get(0).activityInfo.packageName, provideruri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        int size = list.size();
        if (size == 0) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Toast.makeText(this, "용량이 큰 사진의 경우 시간이 오래 걸릴 수 있습니다.", Toast.LENGTH_SHORT).show();
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);

            String timeStamp = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            }
            //사진파일명
            String imageFileName = "Gazuaa_board" + timeStamp + ".jpg";
            //사진파일이 저장될 디렉토리.                                                       갤러리에 gyeom 폴더를 생성,
            File storageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures", "gyeom");
            //gyeom 폴더가 없다면 생성시킨다.
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }

            //파일 생성,             경로,     파일명
            File image = new File(storageDir, imageFileName);

            if (image != null) {

                mCurrentPhotoPath = image.getAbsolutePath();
                provideruri = FileProvider.getUriForFile(this, "com.example.dlehd.gazuua", image);

                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);


                intent.putExtra("return-data", false);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, provideruri);
                intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString()); //Bitmap 형태로 받기 위해 해당 작업 진행

                Intent i = new Intent(intent);
                ResolveInfo res = list.get(0);
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                grantUriPermission(res.activityInfo.packageName, provideruri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

                i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                startActivityForResult(i, REQUEST_CROP);
            }
        }

    }


    //갤러리 새로고침. 앨범이나 카메라로 사진을 찍고 크랍한 이후 앨범을 새로고침 해주어야 함.
    // ACTION_MEDIA_MOUNTED는 하나의 폴더, FILE은 하나의 파일을 새로 고침할 때 사용함.
    public void mediaScan() {
        Log.e("mediascan", "call");
        //미디어 스캔을 위해서 Broadcast를 보낼 인텐트. (미디어스캐너)
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        // 해당 경로에 있는 파일을 객체화(새로 파일을 만든다는 것으로 이해하면 안 됨)
        //mCurrentPhotoPath ==> 생성된 파일의 절대경로가 저장됨.
        File f = new File(String.valueOf(mCurrentPhotoPath));
        Uri contentUri = Uri.fromFile(f);

        //미디어스캐너로 하여금 Uri에 대한 파일을 스캔하고 미디어 라이브러리에 파일을 추가하도록 한다.
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
        //Toast.makeText(this, "사진이 앨범에 저장되었습니다.", Toast.LENGTH_SHORT).show();
    }

    public void galleryPic() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        //setType 지정-> 인텐트 실행시 어떤 어플 선택할지 안내함.
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_GELLARY);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case REQUEST_TAKE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null) {
                        Log.e("데이터가", "ㅜnull?");
                    }
                    //비트맵 전환하기 전에 일단 그냥 세팅한다.
                    // data ==> null이다. Uri uri = data.getData()
                    Log.e("result에서 provideruri 확인", String.valueOf(provideruri));

                    //미디어 스캔하면 파일 생성되므로 주석처리. 후에 쓸거임.
                    //mediaScan();

                    //비트맵으로 변환하는 코드. 후에 쓸진 아직 모름,
          /*  try {
                bm = MediaStore.Images.Media.getBitmap(getContentResolver(), provideruri);
            } catch (IOException e) {
                e.printStackTrace();
            }*/

                    //생성된 파일 삭제. 절대경로로 해야함.
           /* File f = new File(mCurrentPhotoPath);
            if(f.exists()) {
                f.delete();
            }*/

                    try {
                        //크롭 메소드 호출한다.
                        cropImage();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {

                }
                break;
            case REQUEST_GELLARY:
                if (resultCode == Activity.RESULT_OK) {
                    Log.e("REQUEST_GALLARY", "들어옴");
                    try {
                        provideruri = data.getData();
                        cropImage();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                break;

            case REQUEST_CROP:
                if (resultCode == Activity.RESULT_OK) {
                    mediaScan();
                    imageView.setImageURI(provideruri);

                }
                break;
        }
    }


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
                        } else if (permissions[i].equals(this.permissions[2])) {
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

    public void uploadFile(String filePath) {
        String url = "http://222.239.249.149/upload.php";
        try {
            UploadFile1 uploadFile = new UploadFile1(Profile_Activity.this);
            uploadFile.setPath(filePath);
            uploadFile.execute(url);
        } catch (Exception e) {
        }
    }

    public class UploadFile1 extends AsyncTask<String, String, String> {

        Context context; // 생성자 호출 시
        ProgressDialog mProgressDialog; // 진행 상태 다이얼로그
        String fileName; // 파일 위치

        HttpURLConnection conn = null; // 네트워크 연결 객체
        DataOutputStream dos = null; // 서버 전송 시 데이터 작성한 뒤 전송

        String lineEnd = "\r\n"; // 구분자
        String twoHyphens = "--";
        String boundary = "*****";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024;
        File sourceFile;
        int serverResponseCode;
        String TAG = "FileUpload";


        public UploadFile1(Context context) {
            this.context = context;
        }

        public void setPath(String uploadFilePath) {
            this.fileName = uploadFilePath;
            this.sourceFile = new File(uploadFilePath);
        }

        @Override
        protected String doInBackground(String... strings) {


            if (!sourceFile.isFile()) { // 해당 위치의 파일이 있는지 검사
                Log.e(TAG, "sourceFile(" + fileName + ") is Not A File");
                return null;
            } else {
                String success = "Success";
                Log.i(TAG, "sourceFile(" + fileName + ") is A File");
                try {
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    URL url = new URL(strings[0]);
                    Log.i("strings[0]", strings[0]);

                    // Open a HTTP  connection to  the URL
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod("POST"); // 전송 방식
                    conn.setRequestProperty("Cookie", sessionID);
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary); // boundary 기준으로 인자를 구분함
                    conn.setRequestProperty("uploaded_file", fileName);
                    Log.e(TAG, "fileName: " + fileName);

                    // dataoutput은 outputstream이란 클래스를 가져오며, outputStream는 FileOutputStream의 하위 클래스이다.
                    // output은 쓰기, input은 읽기, 데이터를 전송할 때 전송할 내용을 적는 것으로 이해할 것
                    dos = new DataOutputStream(conn.getOutputStream());

                    // 사용자 이름으로 폴더를 생성하기 위해 사용자 이름을 서버로 전송한다. 하나의 인자 전달 data1 = newImage
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"data1\"" + lineEnd); // name으 \ \ 안 인자가 php의 key
                    dos.writeBytes(lineEnd);
                    dos.writeBytes("newImage"); // newImage라는 값을 넘김
                    dos.writeBytes(lineEnd);


                    // 이미지 전송, 데이터 전달 uploadded_file라는 php key값에 저장되는 내용은 fileName
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\"; filename=\"" + fileName + "\"" + lineEnd);
                    dos.writeBytes(lineEnd);

                    // create a buffer of  maximum size
                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {
                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }

                    // send multipart form data necesssary after file data..., 마지막에 two~~ lineEnd로 마무리 (인자 나열이 끝났음을 알림)
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    // Responses from the server (code and message)
                    serverResponseCode = conn.getResponseCode();
                    String serverResponseMessage = conn.getResponseMessage();

                    Log.e(TAG, "[UploadImageToServer] HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);

                    if (serverResponseCode == 200) {

                    }


                    // 결과 확인
                    BufferedReader rd = null;

                    rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    String line = null;
                    while ((line = rd.readLine()) != null) {
                        Log.e("Upload State", line);
                    }

                    //close the streams //
                    fileInputStream.close();
                    dos.flush();
                    dos.close();

                    dialog.dismiss();
                } catch (Exception e) {

                    Log.e(TAG + " Error", e.toString());
                }
                return success;
            }
        }


    }
}
