package com.example.dlehd.gazuua;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.dlehd.gazuua.Friend_list.FriendFragment;
import com.example.dlehd.gazuua.Friend_list.Friend_list_main;
import com.example.dlehd.gazuua.Member_info.Profile_Activity;
import com.example.dlehd.gazuua.Profit_rate.Profit_Rate;
import com.example.dlehd.gazuua.board.Board;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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

import static android.R.attr.data;

;

public class MainActivity extends AppCompatActivity implements ActionBar.TabListener{
    //네비게이션 드로어 안의 뷰들
    //네비게이션 드로어 안의 이미지뷰(프로필사진)
    ImageView imageView_in_draw;
    //이미지드로어
    DrawerLayout dlDrawer;

    //FCM을 위한 파이어베이스 토큰
    String firebaseToken;


    static String SessionID,msg_from_db_about_logout;
    TextView tv_name, tv_email;
    static String str_name, str_email, result_from_board_list, Profile_image_path;
    String user_id;
    GraphRequest  delPermRequest;
    //코인별 시세를 보여줄 리스트뷰
    ListView coinlist;
    //뒤로가기 버튼으로 어플을 종료시키는 클래스
    private BackPressCloseHandler backPressCloseHandler;
    FragmentPageAdapter fragmentPageAdapter;
    static ViewPager viewpager_for_main;

    //액션바의 홈 토글버튼.
    ActionBarDrawerToggle drawerToggle;

    /**
     * 메인 액티비티에서 코인별 시세를 확인할 수 있다.
     */



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //페이스북 sdk를 이용하기 위한 초기화.(페이스북 로그아웃을 사용하기 위해)
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        //세션 id 저장한 스트링 변수
        //로그인 액티비티에서 넘겨받은 회원정보(이메일, 이름).
        SessionID = getIntent().getStringExtra("SessionID");
        str_name = getIntent().getStringExtra("user_name");
        str_email = getIntent().getStringExtra("user_email");

        /**
         * FCM 메시지를 위해 토큰값을 받아온다.
         * 그리고 토큰값을 회원 db에 삽입해준다.
         */
        firebaseToken = FirebaseInstanceId.getInstance().getToken();
        Log.e("firebase token check", firebaseToken);
        sendFirebaseToken();


/**
 * 네비게이션 드로어에 각각의 요소를 셋하는 부분 */


        //네비게이션 드로어(왼쪽에서 튀어나오는 뷰)
        dlDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        dlDrawer.setDrawerListener(drawerToggle);
        //네비게이션 드로어 안에 있는 이미지뷰. 로그인한 유저의 프로필사진을 출력한다.
        imageView_in_draw = (ImageView)findViewById(R.id.img_in_Drawer);
        Button logout = (Button)findViewById(R.id.button_logout);

        //프로필 이미지를 불러와서 프로필사진 이미지뷰에 셋해준다.
        profileImage profileImage1 = new profileImage();
        profileImage1.execute();

        //프로필수정 액티비티로 이동하는 버튼.
        Button profile_edit = (Button)findViewById(R.id.profile_btn_drawer);
        profile_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Profile_Activity.class);
                intent.putExtra("이름", str_name);
                intent.putExtra("이메일", str_email);
                intent.putExtra("세션", SessionID);
                startActivity(intent);
                finish();
            }
        });

        TextView name_tv_drawer = (TextView)findViewById(R.id.name_drawer);
        name_tv_drawer.setText(str_name);

        TextView email_tv_drawer = (TextView)findViewById(R.id.email_drawer);
        email_tv_drawer.setText(str_email);

        //친구목록으로 이동하는 텍스트뷰
        TextView friend = (TextView)findViewById(R.id.friend_drawer);
        friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Friend_list_main.class);
                intent.putExtra("이름", str_name);
                intent.putExtra("이메일", str_email);
                intent.putExtra("세션", SessionID);
                startActivity(intent);
            }
        });

        //로그아웃 버튼을 누르면 로그아웃하고 어플이 종료된다.
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
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

/**
 * 네비게이션 드로어에 각각의 요소를 셋하는 부분
 * 끝
 */

        //디바이스의 뒤로가기 버튼으로 어플 종료시키는 메소드
        backPressCloseHandler = new BackPressCloseHandler(this);


        //페이스북 로그인 시 페이스북 id값을 넘겨 받는다.
        if(getIntent().getStringExtra("user_id") != null){
            user_id = getIntent().getStringExtra("user_id");
            Log.e("페북 유저 아디", user_id);
        }


//스와이프 탭 구현하는 부분
//프레그먼트페이지어댑터를 생성한다. 섹션(액션바의 탭에 해당하는 화면)에 해당하는 프레그먼트를 리턴한다.
        fragmentPageAdapter = new FragmentPageAdapter(getSupportFragmentManager());


        //액션바를 생성한다.
        final ActionBar actionBar = getSupportActionBar();
        //액션바 코너에 있는 Home버튼을 활성화한다. 이 버튼을 누르면 화면 좌측에서 사용자정보 페이지가 뜬다.
        actionBar.setHomeButtonEnabled(true);

        //액션바에 탭을 보여줄 것이라고 지정한다.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        //뷰페이저와 프레그먼트페이지어댑터와 연결한다.
        viewpager_for_main = (ViewPager)findViewById(R.id.pager);
        viewpager_for_main.setAdapter(fragmentPageAdapter);
        viewpager_for_main.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            //사용자가 화면을 스와이프하면 화면이 바뀐다. 그때 일어나는 이벤트.
            @Override
            public void onPageSelected(int position) {
                //화면을 좌우로 스와이프하여 섹션(탭)을 이동할 경우, 현재 섹션의 위치다.
                //액션바의 탭위치를 해당 섹션과 일치시킨다.
                actionBar.setSelectedNavigationItem(position);
            }
        });
        for (int i = 0; i <fragmentPageAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            //어댑터에서 정의한 페이지 제목을 탭에 보이는 문자열로 사용한다.
                            .setText(fragmentPageAdapter.getPageTitle(i))
                            //TabListener 인터페이스를 구현할 액티비티 오브젝트도 지정한다.
                            .setTabListener(this));
        }
//스와이프 탭 구현 끝
//스와이프 탭 구현 끝. onCreate에서.


    }





    /**
     * 파이어베이스토큰을 로그인한 유저의 db 테이블에 저장하는 메소드
     */
    public void sendFirebaseToken(){
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        Request authorized = original.newBuilder()
                                //세션 변수에 저장된 이메일 값을 사용하기 위해 세션id를 http 헤드에 넣어준다.
                                .addHeader("Cookie", SessionID)
                                .build();
                        return chain.proceed(authorized);
                    }
                })
                .build();


        HttpUrl.Builder urlbuilder = HttpUrl.parse("http://222.239.249.149/firebase/insertToken.php").newBuilder();
        String requestUrl = urlbuilder.build().toString();
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                //게시글 번호를 넣어서 서버로 보낸다.
                .addFormDataPart("Token", firebaseToken)
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
                while ( ( line = in.readLine() ) != null )
                {
                    buff.append(line + "\n");
                }
                //서버의 리턴값
                Profile_image_path = buff.toString().trim();

                /* 서버에서 한 응답을 보여주는 로그 */
                Log.e("프로필이미지 서버의리턴값",Profile_image_path);


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
            Glide.with(getApplicationContext()).load("http://222.239.249.149/"+Profile_image_path).into(imageView_in_draw);
        }
    }


    /**
     * 뷰 페이저 관련 메소드들 시작
     * */
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        viewpager_for_main.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }




    /**
     * 뷰 페이저 관련 메소드들 끝
     * */

    //어뎁터 클래스. 해당 섹션에 대응하는 프래그먼트를 리턴한다
    //FragmentPagerAdapter는 메모리에 프래그먼트를 로드한 상태로 유지하지만(3개 프래그먼트 유지하는게 적당함)
    //FragmentStatePagerAdapter는 화면에 보이지 않는 프래그먼트는 메모리에서 제거한다.
    // TODO: 2018-01-05  fragmentstate 어댑터 써보자
    public static class FragmentPageAdapter extends FragmentPagerAdapter {
        FragmentManager fm;

        public FragmentPageAdapter(FragmentManager fm) {
            super(fm);
            this.fm = fm;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = fm.findFragmentByTag("android:switcher:" + viewpager_for_main.getId() + ":" + getItemId(position));

            String[] member_info = {str_name, str_email, SessionID};
            String[] for_Board = {str_name, str_email, SessionID, result_from_board_list};
            //프래그먼트가 이미 생성되어 있는 경우에는 리턴
            if (fragment != null) {
                return fragment;
            }

            //프래그먼트의 인스턴스를 생성한다.
            switch (position) {
                case 0:
                    //코인별 정보를 나타내는 프레그먼트를 생성한다.
                    return Profit_Rate.newInstance(str_name);
                case 1:
                    //회원정보를 나타내는 프레그먼트를 생성한다.
                    return Board.newInstance(member_info);
                case 2:
                    //회원정보를 나타내는 프레그먼트를 생성한다.
                    return FriendFragment.newInstance(member_info);
                default:
                    return Profit_Rate.newInstance(str_name);
            }

        }
        //프래그먼트의 갯수
        @Override
        public int getCount() {
            return 3;
        }
        //탭의 제목으로 사용되는 문자열 생성
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0:
                    return  "코인 시세";
                case 1:
                    return  "게시판";
                case 2:
                    return  "친구신청";
                default: return null;
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
           // super.destroyItem(container, position, object);
        }
    }

    ///어뎁터 클래스. 해당 섹션에 대응하는 프래그먼트를 리턴한다
    //끝
    //스와이프 탭 구현 끝
    //스와이프 탭 구현 끝


    //디바이스의 뒤로가기 버튼으로 어플 종료시키는 메소드
    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        //뒤로가기 버튼으로 어플 종료시키는 클래스 실행.
        backPressCloseHandler.onBackPressed();
    }
    //디바이스의 뒤로가기 버튼으로 어플 종료시키는 메소드
    //끝


    //서버에 있는 session_destroy php파일에 접근하는 클랙스
    //서버에 있는 session_destroy php파일에 접근하는 클랙스
    //서버에 있는 session_destroy php파일에 접근하는 클랙스
    public class SessionDestroy_DB extends AsyncTask<Void, Integer, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            try{
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
                while ( ( line = in.readLine() ) != null )
                {
                    buff.append(line + "\n");
                }
                //서버의 리턴값
                msg_from_db_about_logout = buff.toString().trim();

                /* 서버에서 한 응답을 보여주는 로그 */
                Log.e("로그아웃시 서버의리턴값",msg_from_db_about_logout);

                //세션디스트로이 성공
                if(msg_from_db_about_logout.equals("1"))
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
            if(msg_from_db_about_logout.equals("1")){
                if(getBaseContext() == null){

                }else {
                    //자동로그인에 저장된 값들 초기화
                    SharedPreferences autoLogin = getSharedPreferences("autoLogin", MODE_PRIVATE);
                    SharedPreferences.Editor editor = autoLogin.edit();
                    editor.remove("id");
                    editor.remove("pw");
                    editor.commit();

                    Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                    //액티비티 스택 내의 액티비티 파괴 (루트 액티비티 제외)
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    //로그인 액티비티로 넘어간다.
                    startActivity(i);
                    finish();
                    Toast.makeText(MainActivity.this, "성공적으로 로그아웃되었습니다.", Toast.LENGTH_SHORT).show();

                }
            }
        }
    }
    //서버에 있는 session_destroy php파일에 접근하는 클랙스
    //서버에 있는 session_destroy php파일에 접근하는 클랙스
    //서버에 있는 session_destroy php파일에 접근하는 클랙스
    //끝




}







