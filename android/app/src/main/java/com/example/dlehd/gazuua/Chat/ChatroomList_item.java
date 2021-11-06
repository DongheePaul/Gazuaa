package com.example.dlehd.gazuua.Chat;

public class ChatroomList_item  {
    String img_path, friend_name, roomid, lastmsg;


    public ChatroomList_item(String img_path, String friend_name, String roomid, String lastmsg) {
        this.img_path = img_path;
        this.friend_name = friend_name;
        this.roomid = roomid;
        this.lastmsg = lastmsg;
    }

    public String getImg_path() {
        return img_path;
    }

    public void setImg_path(String img_path) {
        this.img_path = img_path;
    }

    public String getFriend_name() {
        return friend_name;
    }

    public void setFriend_name(String friend_name) {
        this.friend_name = friend_name;
    }

    public String getRoomid() {
        return roomid;
    }

    public void setRoomid(String roomid) {
        this.roomid = roomid;
    }

    public String getLastmsg() {
        return lastmsg;
    }

    public void setLastmsg(String lastmsg) {
        this.lastmsg = lastmsg;
    }
}
