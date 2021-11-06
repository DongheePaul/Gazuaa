package com.example.dlehd.gazuua.Profit_rate;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.dlehd.gazuua.R;

import java.util.ArrayList;

/**
 * Created by dlehd on 2018-01-11.
 */

public class Profit_listview_adapter extends BaseAdapter{

    private ArrayList<Profit_listview_item> profitlistviewitemlist = new ArrayList<Profit_listview_item>();



    @Override
    public Object getItem(int position) {
        return profitlistviewitemlist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();
        if(convertView ==null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.profit_listview_item, parent, false);
        }
        TextView name = (TextView)convertView.findViewById(R.id.tv_name1);
        TextView price = (TextView)convertView.findViewById(R.id.tv_price1);
        TextView cy = (TextView)convertView.findViewById(R.id.tv_yester1);
        TextView cy_per = (TextView)convertView.findViewById(R.id.tv_yesterPer1);

        Profit_listview_item profitListviewItem = profitlistviewitemlist.get(position);

        name.setText(profitListviewItem.getName());

        if(profitListviewItem.getPrice() == "불러오는 중..."){
         price.setText(profitListviewItem.getPrice());
            cy.setText(profitListviewItem.getCpYesterday());
            cy_per.setText(profitListviewItem.getcPYesterdayPercent());
        }

        else {
            String price_str = String.format("%,d", Integer.parseInt(profitListviewItem.getPrice()));
            price.setText(price_str + "원");

            String cp_str = String.format("%,d", Integer.parseInt(profitListviewItem.getCpYesterday()));
            String cp_per_str = String.format("%.2f", Double.parseDouble(profitListviewItem.getcPYesterdayPercent()));
            if (Double.parseDouble(profitListviewItem.getcPYesterdayPercent()) > 0) {
                cy.setText("▲" + cp_str + "원");
                cy.setTextColor((Color.parseColor("#FF0000")));
                cy_per.setText("▲" + cp_per_str + "%");
                cy_per.setTextColor((Color.parseColor("#FF0000")));
            } else {
                cy.setText("▼" + cp_str + "원");
                cy.setTextColor((Color.parseColor("#0000FF")));
                cy_per.setText("▼" + cp_per_str + "%");
                cy_per.setTextColor((Color.parseColor("#0000FF")));
            }
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return profitlistviewitemlist.size();
    }

    public void additem(Profit_listview_item item){
        profitlistviewitemlist.add(item);
        notifyDataSetChanged();

    }

    public void clear(){
        profitlistviewitemlist.clear();
        notifyDataSetChanged();
    }
}
