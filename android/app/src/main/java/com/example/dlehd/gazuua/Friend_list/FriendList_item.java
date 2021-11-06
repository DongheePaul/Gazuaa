package com.example.dlehd.gazuua.Friend_list;

public class FriendList_item{
String img_path, email, name;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public FriendList_item(String img_path1, String name1, String email1) {
        img_path = img_path1;
        name = name1;
        email = email1;

    }

    public String getImg_path() {
        return img_path;
    }

    public void setImg_path(String img_path) {
        this.img_path = img_path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
