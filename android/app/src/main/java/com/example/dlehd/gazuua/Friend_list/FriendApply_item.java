package com.example.dlehd.gazuua.Friend_list;

/**
 * Created by dlehd on 2018-03-03.
 */

public class FriendApply_item {
    //받은 친구신청 목록에 보여질 이메일, 그리고 세션 변수를 활용하기 위한 세션아이디.
    String email;
    String session;

    public FriendApply_item(String email1, String session1) {
        email = email1;
        session = session1;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

}
