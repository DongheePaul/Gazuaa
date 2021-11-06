package com.example.dlehd.gazuua.firebase;

import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * 파이어베이스 토큰이 새로 발급될 때
 * onTokenRefresh 메소드 실행된다.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService{
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
    }

    private void sendRegistrationToServer(String token) {
// TODO: Implement this method to send token to your app server.
        //바뀐 토큰을 저장해줘야 한다.
        //db에 저장해서 필요할 때 불러쓰거나
        //앱 자체의 sharedpreferences에 저장해서 불러쓰면 된다.
    }
}
