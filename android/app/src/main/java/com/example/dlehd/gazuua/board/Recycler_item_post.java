package com.example.dlehd.gazuua.board;

/**
 * Created by dlehd on 2018-02-22.
 * 게시판 목록을 위한 리사이클러뷰의 아이템에
 * 데이터를 셋한 아이템 클래스.
 */

public class Recycler_item_post {
    String title,  writer, time, id, smallest_num, sessionID, user_name, user_email;

    public Recycler_item_post(String titl, String write, String tim, String idid, String smallest, String sessionI, String user_name1, String user_email1) {
        title = titl;
        writer = write;
        time = tim;
        id = idid;
        smallest_num = smallest;

        sessionID = sessionI;

        user_name = user_name1;
        user_email = user_email1;
    }


    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }
    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSmallest_num() {
        return smallest_num;
    }

    public void setSmallest_num(String smallest_num) {
        this.smallest_num = smallest_num;
    }
}
