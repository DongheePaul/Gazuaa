package com.example.dlehd.gazuua.Friend_list;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.dlehd.gazuua.Chat.ChatService;
import com.example.dlehd.gazuua.Chat.ChatroomList;
import com.example.dlehd.gazuua.R;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * 친구목록, 채팅방 목록 프로그먼트를 생성하는 Activity. *
 * 서비스와 바인드하지만 서비스로부터 메시지 받을 필요가 없음. => MSG_REGISTER_CLIENT 통해서 서비스의 리스트에 등록하진 않음.
 */
public class Friend_list_main extends AppCompatActivity implements ActionBar.TabListener {
    static String SessionID;
    static String str_name;
    static String str_email;
    AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    //ViewPager에는 한번에 하나의 섹션만 보여진다.
    static ViewPager mViewPager;
    Messenger MessengerToService;
    static final int MSG_REGISTER_CLIENT = 111;
    static final int MSG_ACTIVITY_TO_SERVER = 333;

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e("SVC conn F_L_M", "conn");
            MessengerToService = new Messenger(service);
            //서비스에 해당 액티비티를 바인딩하기 위해 보내는 메세지.
            // 메세지의 what을 설정해주고
            try { //서버로 state:start 인 메시지를 보낸다(서비스를 통해). 그러면 서버에서 유저를 유저리스트에 삽입.
                Bundle bundle_room = new Bundle();
                JSONObject jsonObg = new JSONObject();
                Message message = Message.obtain(null, MSG_ACTIVITY_TO_SERVER);
                jsonObg.put("state", "start");
                jsonObg.put("roomID", "0000");
                jsonObg.put("from", str_name);
                jsonObg.put("message", "내용읎다.");
                jsonObg.put("to", "anybody");
                String msg1 = jsonObg.toString();
                Log.e("jsonObje. service conn", msg1);
                bundle_room.putString("msg", msg1);
                message.setData(bundle_room);
                MessengerToService.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }catch (JSONException e){
                e.getMessage();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frien_list_main);
        if(Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        //채팅 소켓을 생성하는 서비스 시작
        Intent intent = new Intent(Friend_list_main.this, ChatService.class);
        intent.setPackage("com.example.dlehd");
        bindService(intent, conn, Context.BIND_AUTO_CREATE);

        SessionID = getIntent().getStringExtra("세션");
        str_name = getIntent().getStringExtra("이름");
        str_email = getIntent().getStringExtra("이메일");

        //어댑터를 생성한다. 섹션마다 프래그먼트를 생성하여 리턴해준다.
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());
        //액션바를 설정한다.
        final ActionBar actionBar = getSupportActionBar();

        //액션바 코너에 있는 Home버튼을 비활성화 한다.
        actionBar.setHomeButtonEnabled(true);

        //탭을 액션바에 보여줄 것이라고 지정한다.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        //ViewPager를 설정하고
        mViewPager = (ViewPager) findViewById(R.id.FriendViewpager);
        //ViewPager에 어댑터를 연결한다.
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        //사용자가 섹션사이를 스와이프할때 발생하는 이벤트에 대한 리스너를 설정한다.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override //스와이프로 페이지 이동시 호출됨
            public void onPageSelected(int position) {
                //화면을 좌우로 스와이핑하여 섹션 사이를 이동할 때, 현재 선택된 탭의 위치이다.
                //액션바의 탭위치를 페이지 위치에 맞춘다.
                actionBar.setSelectedNavigationItem(position);
            }
        });

        //각각의 섹션을 위한 탭을 액션바에 추가한다.
        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            //어댑터에서 정의한 페이지 제목을 탭에 보이는 문자열로 사용한다.
                            .setText(mAppSectionsPagerAdapter.getPageTitle(i))
                            //TabListener 인터페이스를 구현할 액티비티 오브젝트도 지정한다.
                            .setTabListener(this));
        }

    }


    //소켓 실행하는 서비스 종료.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
        MessengerToService = null;
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        //액션바에서 선택된 탭에 대응되는 페이지를 뷰페이지에서 현재 보여지는 페이지로 변경한다.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {
        private FragmentManager fm;

        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fm = fm;
        }

        @Override
        public Fragment getItem(int pos) {
            //태그로 프래그먼트를 찾는다.
            Fragment fragment = fm.findFragmentByTag("android:switcher:" + mViewPager.getId() + ":" + getItemId(pos));
            String[] member_info = {str_name, str_email, SessionID};

            //프래그먼트가 이미 생성되어 있는 경우에는 리턴
            if (fragment != null) {
                return fragment;
            }

            //프래그먼트의 인스턴스를 생성한다.
            switch (pos) {
                case 0:
                    return FriendList.newInstance(member_info);
                case 1:
                    return ChatroomList.newInstance(member_info);
                /*
                case 2: return ThirdFragment.newInstance("ThirdFragment, Instance 1");*/
                default:
                    return FriendList.newInstance(member_info);
            }
        }

        //프래그먼트를 최대 5개를 생성할 것임
        @Override
        public int getCount() {
            return 2;
        }

        //탭의 제목으로 사용되는 문자열 생성
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "친구목록";
                case 1:
                    return  "채팅목록";
                /*case 2:
                    return  "친구신청";*/
                default:
                    return null;
            }
        }
    }
}
