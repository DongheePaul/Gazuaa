package com.example.dlehd.gazuua;

import android.app.Activity;
import android.widget.Toast;

import com.example.dlehd.gazuua.Profit_rate.Profit_Rate;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;

/**
 * Created by dlehd on 2017-12-30.
 *
 * 뒤로가기를 1번 눌렀을 때 "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다." 메세지가 뜨고
 * 2초 내로 뒤로가기를 한번 더 누르면 어플 종료하는 클래스.
 */

public class BackPressCloseHandler {
    private long backKeyPressedTime = 0;
    private Toast toast;
    private Activity activity;

    public BackPressCloseHandler(Activity context) {
        this.activity = context;
    }

    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            MainActivity.SessionDestroy_DB destroy_db = new MainActivity(). new SessionDestroy_DB();
            destroy_db.execute();
            new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest.Callback() {
                @Override
                public void onCompleted(GraphResponse response) {
                    LoginManager.getInstance().logOut();

                }
            }).executeAsync();
            AccessToken.setCurrentAccessToken(null);
            //시세 보여주는 스레드 멈추기
            Profit_Rate profit_rate = new Profit_Rate();
            profit_rate.isRunning = false;
            activity.finish();
        }
            toast.cancel();
        }


    public void showGuide() {
        toast = Toast.makeText(activity, "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT); toast.show();
    }


}
