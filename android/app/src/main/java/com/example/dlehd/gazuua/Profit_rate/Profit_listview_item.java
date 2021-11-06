package com.example.dlehd.gazuua.Profit_rate;

/**
 * Created by dlehd on 2018-01-11.
 */

public class Profit_listview_item {

    String name;
    String price;
    String cpYesterday;
    String cPYesterdayPercent;

    public Profit_listview_item(String name, String price, String cpYesterday, String cPYesterdayPercent) {
        this.name = name;
        this.price = price;
        this.cpYesterday = cpYesterday;
        this.cPYesterdayPercent = cPYesterdayPercent;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCpYesterday() {
        return cpYesterday;
    }

    public void setCpYesterday(String cpYesterday) {
        this.cpYesterday = cpYesterday;
    }

    public String getcPYesterdayPercent() {
        return cPYesterdayPercent;
    }

    public void setcPYesterdayPercent(String cPYesterdayPercent) {
        this.cPYesterdayPercent = cPYesterdayPercent;
    }
}
