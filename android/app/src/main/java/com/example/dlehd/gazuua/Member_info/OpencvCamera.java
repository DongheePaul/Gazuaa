package com.example.dlehd.gazuua.Member_info;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.example.dlehd.gazuua.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * openCV를 활용한 카메라를 사용하는 액티비티. 얼굴이 인식되면 촬영 버튼이 보이고 사진을 찍을 수 있다.
 * 얼굴이 인식되지 않으면 촬영 버튼이 보이지 않는다.
 */
public class OpencvCamera extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{
    //Cmake에 등록된 네이티브 라이브러리 호출.
    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }
    //카메라 뷰가 출력될 뷰.
    private CameraBridgeViewBase mOpenCvCameraView;
    //이미지 프레임을 담을 Mat 클래스.
    private Mat matInput;
    private Mat matResult;
    //인식한 얼굴 갯수를 담을 long 클래스.
    long detecValue;
    //cpp에 추가할 jni함수를 위한 네이티브 메소드 선언. (얼굴인식 메소드)
    public static native long loadCascade(String cascadeFileName );
    public static native long detect(long cascadeClassifier_face, long cascadeClassifier_eye, long matAddrInput, long matAddrResult);
    public long cascadeClassifier_face = 0;
    public long cascadeClassifier_eye = 0;

    Button btn_takephoto;

    Bitmap bitmap;

    String img_path;

    String user_name, user_email, sessionID;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //인식한 얼굴이 1개 이상이면
            if(msg.what == 1){
                //촬영 버튼이 보인다.
                btn_takephoto.setVisibility(View.VISIBLE);
                //촬영 버튼을 누르면
                        //1. 해당 프레임이 비트맵으로 저장
                        //2. 해당 비트맵을 이미지 파일로 내부 저장소에 저장
                        //3. 파일 경로를 인텐트에 담아 FilterActivity  실행.
                        btn_takephoto.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //파일명으로 사용될 현재시간.
                                String timeStamp = String.valueOf(System.currentTimeMillis());
                                //파일명
                                String imageFileName = "/image" + timeStamp+ ".jpg";
                                //외부 저장소 경로
                                String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
                        //폴더명
                        String foler_name = "/gyeom";
                        //외부저장소 경로 + 폴더명으로 파일 경로를 생성.
                        String string_path = ex_storage+foler_name;
                        File file_path;
                        try{
                            //mat 클래스를 비트맵으로 변환.
                            Log.e("matInput.cols()", String.valueOf(matInput.cols()));
                            Log.e("matInput.rows()", String.valueOf(matInput.rows()));
                            bitmap = Bitmap.createBitmap(matInput.cols(), matInput.rows(), Bitmap.Config.ARGB_8888);
                            Utils.matToBitmap(matInput, bitmap);
                            //외부저장소경로 + 폴더명으로 파일생성.... ==> 정확히 파일생성이라고 해야하나?
                            //뒤에 만들어질 외부저장소경로+폴더+파일명으로 된 파일을 위해 경로를 명시한다는 느낌.
                            file_path = new File(string_path);
                            if(!file_path.isDirectory()){
                                file_path.mkdirs();
                            }
                            //이미지 파일이 될 파일 생성.
                            File fileCacheItem = new File (string_path+imageFileName);
                            fileCacheItem.createNewFile();
                            FileOutputStream out = new FileOutputStream(fileCacheItem);
                            //생성된 파일에 비트맵을 입력.
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            out.flush();
                            out.close();

                            //비트맵으로 생성된 이미지 파일의 경로. 이것을 인텐트에 담아 FilterActivity를 실행한다.
                            img_path = fileCacheItem.getAbsolutePath();

                            //이미지 갤러리 새로고침.
                            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            // 해당 경로에 있는 파일을 객체화(새로 파일을 만든다는 것으로 이해하면 안 됨)
                            //해당 경로의 파일을 디바이스가 인식하게 한다.
                            File f = new File(String.valueOf(img_path));
                            Uri contentUri = Uri.fromFile(f);
                            //미디어스캐너로 하여금 Uri에 대한 파일을 스캔하고 미디어 라이브러리에 파일을 추가하도록 한다.
                            mediaScanIntent.setData(contentUri);
                            sendBroadcast(mediaScanIntent);

                        }catch (FileNotFoundException e) {
                            e.getMessage();
                        }catch (IOException e){
                            e.getMessage();
                        }
                        //이미지 파일의 경로를 담아 인텐트 실행.
                        Intent intent = new Intent(getApplicationContext(), FilterActivity.class);
                        intent.putExtra("img_path", img_path);
                        intent.putExtra("이름", user_name);
                        intent.putExtra("이메일", user_email);
                        intent.putExtra("세션", sessionID);
                        Log.e("image_path ==", String.valueOf(img_path));
                        startActivity(intent);
                    }
                });
            }
            //인식한 얼굴이 없다면 촬영 버튼이 보이지 않는다.
            else if(msg.what == 0){
                btn_takephoto.setVisibility(View.GONE);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_opencv_camera);

        user_name = getIntent().getStringExtra("이름");
        user_email = getIntent().getStringExtra("이메일");
        sessionID = getIntent().getStringExtra("세션");

        //액션바 숨기기.
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //퍼미션 상태 확인
            if (!hasPermissions(PERMISSIONS)) {

                //퍼미션 허가 안되어있다면 사용자에게 요청
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
            else  read_cascade_file(); //추가
        }
        else  read_cascade_file(); //추가

        //촬영 버튼
        btn_takephoto = (Button)findViewById(R.id.btn_takepicture);
        //카메라의 화면이 보이는 뷰
        mOpenCvCameraView = (CameraBridgeViewBase)findViewById(R.id.activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(1);
        // front-camera(1),  back-camera(0)
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);


    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }


    //카메라로부터 영상을 가져올 때마다 jni 함수 detect를 호출하도록 합니다.
    //얼굴 검출하는 cpp 코드를 호출하는 부분입니다.
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        //Mat : C++ API에서 가장 중요한 클래스 중 하나로 1채널 또는 다채널의 실수, 복소수, 행렬, 영상 등의 수치 데이터를 표현하는 n 차원 행렬 클래스
        //Mat이란 Matrix 즉, 행렬이라는 단어 앞글자에서 따온 말로 보통 내가 그린 이미지, 불러온 이미지 등을 저장하는 저장소.
        //카메라가 인식하는 화면을 matInput 클래스에 저장.
        matInput = inputFrame.rgba();

        //matInput을 객체로 새로운 Mat객체를 생성한다. 이 matresult가 화면에 출력됨.
        //release ==> 메모리 해제라고 함. 정확하게는 모르겠네
        if ( matResult != null ) matResult.release();
        matResult = new Mat(matInput.rows(), matInput.cols(), matInput.type());

        //ConvertRGBtoGray(matInput.getNativeObjAddr(), matResult.getNativeObjAddr());
        //전면 카메라의 경우 영상이 뒤집혀서 읽어지기 때문에 180도 회전시켜야 함.
        Core.flip(matInput, matInput, 1);

        //java로부터 영상이 들어오기 시작하면 CascadeClassifier 객체를 인자로 해서 호출되고, 얼굴 인식 결과를 영상에 표시해준다.
        //detecValue는 인식한 얼굴의 갯수이다.
        detecValue = detect(cascadeClassifier_face, cascadeClassifier_eye, matInput.getNativeObjAddr(), matResult.getNativeObjAddr());

        //인식한 얼굴이 1개 이상이면 => 핸들러에서 카메라 버튼 생성.
        if(detecValue > 0) {
            Message msg = Message.obtain();
            msg.what = 1;
            handler.sendMessage(msg);
        }

        //인식한 얼굴이 없으면 => 핸들러에서 카메라 버튼 생성 안함..
        else if(detecValue == 0 ){
            Message msg = Message.obtain();
            msg.what = 0;
            handler.sendMessage(msg);
        }

        return matResult;
    }

    //xml 파일(얼굴인식, 얼굴 인식 표시를 위한 xml파일)을 가져오기 위한 메소드
    //cpp 파일의 loadCascade 함수를 호출하도록 구현되어있는데 자바 함수를 사용하도록 변경해도 됩니다.
    //현재 이미 파일을 copyFile 메소드를 이용해서 가져온 경우에 대한 처리가 빠져있습니다.
    private void copyFile(String filename) {
        String baseDir = Environment.getExternalStorageDirectory().getPath();
        String pathDir = baseDir + File.separator + filename;

        //asset 파일에 접근할 수 있도록 하는 객체.
        AssetManager assetManager = this.getAssets();

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            Log.e( "OpenCvCamera", "copyFile :: 다음 경로로 파일복사 "+ pathDir);
            //파라미터로 전달된 파일명을 asset 폴더에서 찾는다.
            inputStream = assetManager.open(filename);
            //파일 경로를 향한 아웃풋 스트림을 만든다.
            outputStream = new FileOutputStream(pathDir);

            byte[] buffer = new byte[1024];
            int read;
            //asset 폴더의 파일을 지정한 경로에 옮긴다.
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            inputStream = null;
            outputStream.flush();
            outputStream.close();
            outputStream = null;
        } catch (Exception e) {
            Log.e( "OpenCvCamera", "copyFile :: 파일 복사 중 예외 발생 "+e.toString() );
        }

    }

    private void read_cascade_file(){
        //copyFile 메소드는 Assets에서 해당 파일을 가져와 외부 저장소 특정위치에 저장하도록 구현된 메소드입니다.
        //얼굴인식과 눈코입을 인식하기 위한 xml파일을 asset에서 가져와 외부저장소에 저장한다.
        copyFile("haarcascade_frontalface_alt.xml");
        copyFile("haarcascade_eye_tree_eyeglasses.xml");

        Log.e("OpenCvCamera", "read_cascade_file:");

        //loadCascade 메소드는 외부 저장소의 특정 위치에서 해당 파일을 읽어와서
        //CascadeClassifier 객체로 로드합니다.
        cascadeClassifier_face = loadCascade( "haarcascade_frontalface_alt.xml");
        Log.e("OpenCvCamera", "read_cascade_file:");

        cascadeClassifier_eye = loadCascade( "haarcascade_eye_tree_eyeglasses.xml");
    }

    //콜백 함수는 정의해 두기만 하면 사용자가 직접 호출하지 않더라도 운영체제가 알아서 호출해 주는 함수를 의미하는 용어입니다.
    //OpencvLoader가 동기화되면 카메라 화면을 출력할 뷰를 사용할 수 있다.
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };



    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }


    @Override
    public void onResume()
    {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCVCamera", "onResume :: Internal OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCvCamera", "onResum :: OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();

        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    //여기서부턴 퍼미션 관련 메소드
    static final int PERMISSIONS_REQUEST_CODE = 1000;
    String[] PERMISSIONS  = {"android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    private boolean hasPermissions(String[] permissions) {
        int result;

        //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
        for (String perms : permissions){

            result = ContextCompat.checkSelfPermission(this, perms);

            if (result == PackageManager.PERMISSION_DENIED){
                //허가 안된 퍼미션 발견
                return false;
            }
        }
        //모든 퍼미션이 허가되었음
        return true;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){

            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraPermissionAccepted = grantResults[0]
                            == PackageManager.PERMISSION_GRANTED;

                    boolean writePermissionAccepted = grantResults[1]
                            == PackageManager.PERMISSION_GRANTED;

                    if (!cameraPermissionAccepted || !writePermissionAccepted) {
                        showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
                        return;
                    }else{
                        read_cascade_file();
                    }
                }
                break;
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder( OpencvCamera.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id){
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        builder.create().show();
    }


}
