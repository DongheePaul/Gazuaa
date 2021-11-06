package com.example.dlehd.gazuua.Member_info;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.dlehd.gazuua.MainActivity;
import com.example.dlehd.gazuua.R;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.os.Build.VERSION_CODES.M;

public class FilterActivity extends AppCompatActivity {
    String user_name, user_email, sessionID, img_pathForUpload;
    ImageView toGray, tocandy, imageView, iv_BGR, iv_HSV, iv_2YcRcb, iv_Lab, iv_Luv;
    Mat img_matInput, img_matOutput;
    Bitmap bitmapToGray, BitmapToUpload;
    Button btn_save;


    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }

    public native void ConvertRGBtoGray(long matAddrInput, long matAddrResult);

    public native void imageprocessing(long inputImage, long outputImage);

    public native void ConvertRGBtoBGR(long inputimage, long outputImage);

    public native void ConvertRGBtoHSV(long inputimage, long outputimage);

    public native void ConvertRGBto2YcRcb(long inputimage, long outputimage);

    public native void ConvertRGBtoLab(long inputimage, long outputimage);

    public native void ConvertRGBtoLuv(long inputimage, long outputimage);


    static final int PERMISSION_REQUEST_CODE = 1;
    String[] PERMISSIONS  = {"android.permission.WRITE_EXTERNAL_STORAGE"};


    private void imageprocess_and_showResult() {

        //imageprocessing(img_matInput.getNativeObjAddr());

        Bitmap bitmapInput = Bitmap.createBitmap(img_matInput.cols(), img_matInput.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img_matInput, bitmapInput);
        toGray.setImageBitmap(bitmapInput);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        if (!hasPermissions(PERMISSIONS)) { //퍼미션 허가를 했었는지 여부를 확인
            requestNecessaryPermissions(PERMISSIONS);//퍼미션 허가안되어 있다면 사용자에게 요청
        }
        iv_Lab = (ImageView)findViewById(R.id.iv_Lab);
        iv_HSV = (ImageView)findViewById(R.id.iv_2HSV);
        iv_BGR = (ImageView)findViewById(R.id.iv_2BGR);
        imageView = (ImageView)findViewById(R.id.iv_image);
        toGray = (ImageView)findViewById(R.id.iv_2gray);
        tocandy = (ImageView)findViewById(R.id.iv_2candy);
        iv_2YcRcb = (ImageView)findViewById(R.id.iv_2YCrCb);
        iv_Luv = (ImageView)findViewById(R.id.iv_Luv);

        user_name = getIntent().getStringExtra("이름");
        user_email = getIntent().getStringExtra("이메일");
        sessionID = getIntent().getStringExtra("세션");

        //사진 찍은 파일의 경로.
        String img_path = getIntent().getStringExtra("img_path");
        //비트맵으로 변환.
        Bitmap btm = BitmapFactory.decodeFile(img_path);
        Log.e("bytearraychk in filter", img_path);
        imageView.setImageBitmap(btm);

        //비트맵을 맷으로 변환한다 (이미지 필터 입히기 위해).
        img_matInput = new Mat();
        Utils.bitmapToMat(btm, img_matInput);
        img_matOutput = new Mat();
        Utils.bitmapToMat(btm, img_matOutput);

        //갈색화면 필터
        ConvertRGBtoGray(img_matInput.getNativeObjAddr(), img_matOutput.getNativeObjAddr());
        bitmapToGray = Bitmap.createBitmap(img_matOutput.cols(), img_matOutput.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img_matOutput, bitmapToGray);
        toGray.setImageBitmap(bitmapToGray);
        toGray.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageBitmap(bitmapToGray);
            }
        });

        //비트맵을 맷으로 변환한다 (이미지 필터 입히기 위해)
        Mat matInputToCandy = new Mat();
        Mat MatOutputToCandy = new Mat();
        Utils.bitmapToMat(btm, matInputToCandy);
        Utils.bitmapToMat(btm, MatOutputToCandy);
        //candy 화면 필터를 적용한 비트맵 생성 후 이미지뷰에 셋.
        imageprocessing(matInputToCandy.getNativeObjAddr(), MatOutputToCandy.getNativeObjAddr());
        final Bitmap bitmapToCandy = Bitmap.createBitmap(MatOutputToCandy.cols(), MatOutputToCandy.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(MatOutputToCandy, bitmapToCandy);
        tocandy.setImageBitmap(bitmapToCandy);
        tocandy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageBitmap(bitmapToCandy);
            }
        });

        //비트맵을 맷으로 변환한다 (이미지 필터 입히기 위해)
        Mat matInputToBGR = new Mat();
        Mat MatOutputToBGR = new Mat();
        Utils.bitmapToMat(btm, matInputToBGR);
        Utils.bitmapToMat(btm, MatOutputToBGR);
        //bgr 화면 필터를 적용한 비트맵 생성 후 이미지뷰에 셋.
        ConvertRGBtoBGR(MatOutputToBGR.getNativeObjAddr() ,matInputToBGR.getNativeObjAddr());
        final Bitmap bitmapToBGR = Bitmap.createBitmap(matInputToBGR.cols(), matInputToBGR.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(matInputToBGR, bitmapToBGR);
        iv_BGR.setImageBitmap(bitmapToBGR);
        iv_BGR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageBitmap(bitmapToBGR);
            }
        });

        //비트맵을 맷으로 변환한다 (이미지 필터 입히기 위해)
        Mat matInputToHSV = new Mat();
        Mat matOutputToHSV = new Mat();
        Utils.bitmapToMat(btm, matInputToHSV);
        Utils.bitmapToMat(btm, matOutputToHSV);
        //HSV 화면 필터를 적용한 비트맵 생성 후 이미지뷰에 셋한다.
        ConvertRGBtoHSV(matInputToHSV.getNativeObjAddr(), matOutputToHSV.getNativeObjAddr());
        final Bitmap bitmapToHSV = Bitmap.createBitmap(matOutputToHSV.cols(), matOutputToHSV.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(matOutputToHSV, bitmapToHSV);
        iv_HSV.setImageBitmap(bitmapToHSV);
        iv_HSV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageBitmap(bitmapToHSV);
            }
        });

        //비트맵을 맷으로 변환한다 (이미지 필터 입히기 위해)
        Mat matInputTo2YCrCb = new Mat();
        Mat matOutputTo2YCrCb = new Mat();
        Utils.bitmapToMat(btm, matInputTo2YCrCb);
        Utils.bitmapToMat(btm, matOutputTo2YCrCb);
        //2YCrCb 화면 필터를 적용한 비트맵 생성 후 이미지뷰에 셋한다.
        ConvertRGBto2YcRcb(matInputTo2YCrCb.getNativeObjAddr(), matOutputTo2YCrCb.getNativeObjAddr());
        final Bitmap bitmapTo2YCrCb = Bitmap.createBitmap(matOutputTo2YCrCb.cols(), matOutputTo2YCrCb.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(matOutputTo2YCrCb, bitmapTo2YCrCb);
        iv_2YcRcb.setImageBitmap(bitmapTo2YCrCb);
        iv_2YcRcb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageBitmap(bitmapTo2YCrCb);
            }
        });


        //비트맵을 맵으로 변환한다(이미지 필터 입히기 위해)
        Mat matInputToLab = new Mat();
        Mat matOutputToLab= new Mat();
        Utils.bitmapToMat(btm, matInputToLab);
        Utils.bitmapToMat(btm, matOutputToLab);
        //2YCrCb 화면 필터를 적용한 비트맵 생성 후 이미지뷰에 셋한다.
        ConvertRGBtoLab(matInputToLab.getNativeObjAddr(), matOutputToLab.getNativeObjAddr());
        final Bitmap bitmapToLab = Bitmap.createBitmap(matOutputToLab.cols(), matOutputToLab.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(matOutputToLab, bitmapToLab);
        iv_Lab.setImageBitmap(bitmapToLab);
        iv_Lab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageBitmap(bitmapToLab);
            }
        });

        //비트맵을 맵으로 변환한다(이미지 필터 입히기 위해)
        Mat matInputToLuv = new Mat();
        Mat matOutputToLuv= new Mat();
        Utils.bitmapToMat(btm, matInputToLuv);
        Utils.bitmapToMat(btm, matOutputToLuv);
        //2YCrCb 화면 필터를 적용한 비트맵 생성 후 이미지뷰에 셋한다.
        ConvertRGBtoLuv(matInputToLuv.getNativeObjAddr(), matOutputToLuv.getNativeObjAddr());
        final Bitmap bitmapToLuv = Bitmap.createBitmap(matOutputToLuv.cols(), matOutputToLuv.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(matOutputToLuv, bitmapToLuv);
        iv_Luv.setImageBitmap(bitmapToLuv);
        iv_Luv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageBitmap(bitmapToLuv);
            }
        });

        btn_save = (Button)findViewById(R.id.btn_saveProfile);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    //선택된 사진을 비트맵으로 만든다.
                    BitmapToUpload = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
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
                    BitmapToUpload.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();

                    //비트맵으로 생성된 이미지 파일의 경로. 이것을 인텐트에 담아 FilterActivity를 실행한다.
                    img_pathForUpload = fileCacheItem.getAbsolutePath();

                    //이미지 갤러리 새로고침.
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    // 해당 경로에 있는 파일을 객체화(새로 파일을 만든다는 것으로 이해하면 안 됨)
                    //해당 경로의 파일을 디바이스가 인식하게 한다.
                    File f = new File(String.valueOf(img_pathForUpload));
                    Uri contentUri = Uri.fromFile(f);
                    //미디어스캐너로 하여금 Uri에 대한 파일을 스캔하고 미디어 라이브러리에 파일을 추가하도록 한다.
                    mediaScanIntent.setData(contentUri);
                    sendBroadcast(mediaScanIntent);
                }catch (IOException e){
                    e.getMessage();
                }
                uploadFile(img_pathForUpload);
            }
        });

    }
    public void uploadFile(String filePath) {
        String url = "http://222.239.249.149/upload.php";
        try {
            UploadFile1 uploadFile = new UploadFile1(FilterActivity.this);
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

                } catch (Exception e) {

                    Log.e(TAG + " Error", e.toString());
                }

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("user_name", user_name);
                intent.putExtra("user_email", user_email);
                intent.putExtra("SessionID", sessionID);
                startActivity(intent);
                finish();
                return success;
            }
        }


    }


    //권한 가졌는지 여부 확인.
    private boolean hasPermissions(String[] permissions) {
        int ret = 0;
        //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
        for (String perms : permissions){
            ret = checkCallingOrSelfPermission(perms);
            if (!(ret == PackageManager.PERMISSION_GRANTED)){
                //퍼미션 허가 안된 경우
                return false;
            }
        }
        //모든 퍼미션이 허가된 경우
        return true;
    }

    //필수권한 요청하는 메소드.
    private void requestNecessaryPermissions(String[] permissions) {
        //마시멜로( API 23 )이상에서 런타임 퍼미션(Runtime Permission) 요청
        if (Build.VERSION.SDK_INT >= M) {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults){
        switch(permsRequestCode){

            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean writeAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (Build.VERSION.SDK_INT >= M) {

                        if (!writeAccepted )
                        {
                            showDialogforPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
                            return;
                        }else
                        {
                            imageprocess_and_showResult();
                        }
                    }
                }
                break;
        }
    }

    private void showDialogforPermission(String msg) {

        final AlertDialog.Builder myDialog = new AlertDialog.Builder(FilterActivity.this);
        myDialog.setTitle("알림");
        myDialog.setMessage(msg);
        myDialog.setCancelable(false);
        myDialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                if (Build.VERSION.SDK_INT >= M) {
                    requestPermissions(PERMISSIONS, PERMISSION_REQUEST_CODE);
                }

            }
        });
        myDialog.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        myDialog.show();
    }

}
