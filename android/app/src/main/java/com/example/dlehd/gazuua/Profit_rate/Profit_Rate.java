package com.example.dlehd.gazuua.Profit_rate;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.dlehd.gazuua.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *네트워크의 트래픽을 분석해 업비트가 코인별 정보를 가져오는 URL(편의상 코인url이라 칭함)을 찾음.
 * cURL을 활용, 서버에서 코인url에 접근해 데이터를 받고, 그 데이터를 가공함.
 * 클라이언트에서는 가공된 코인 데이터를 요청하고 받은 코인데이터를 해당 텍스트뷰에 세팅한다.
 */
public class Profit_Rate extends Fragment {
    private OnFragmentInteractionListener mListener;

    //스레드를 돌리기 위한 플래그
    public boolean isRunning=true;

    String resultFromServer;

    RecyclerView coinRecycler;

    ArrayList<Profit_listview_item> coinlist;

    adapter1 coinadapter;


    Handler mHandler = new Handler();

    String name, price, changePercent, changePrice;

    load Load;

    String[] stockList = {"비트코인", "이더리움", "퀀텀", "에이다", "리플", "네오", "스텔라루멘", "비트코인캐시", "이더클","오미세고"};
    public Profit_Rate() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static Profit_Rate newInstance(String text) {
        Profit_Rate profit_rate = new Profit_Rate();

        return profit_rate;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.profit_rate, container, false);
        coinRecycler = (RecyclerView) v.findViewById(R.id.coinList);
        coinRecycler.setHasFixedSize(true);
        coinlist = new ArrayList<>();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        coinRecycler.setLayoutManager(mLayoutManager);
        coinadapter = new adapter1(coinlist);
        coinRecycler.setAdapter(coinadapter);

        isRunning = true;
        for(int i = 0; i<10; i++){
            Log.e("와이 0부터 아닌가", stockList[i]);
            Profit_listview_item item = new Profit_listview_item("불러오는 중...", "불러오는 중...", "불러오는 중...", "불러오는 중...");
            coinlist.add(item);
        }
        coinadapter.notifyDataSetChanged();


      Load = new load();
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    @Override
    public void onPause() {
        super.onPause();
        //스레드를 멈춘다.
        isRunning = false;
        if(Load.isAlive() && Load != null){
            Load.interrupt();
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public void onResume() {
        super.onResume();
        isRunning = true;
        if(Load.getState() == Thread.State.NEW) {
            Load.start();
        }
    }

    public class load extends Thread{
        int i;
        @Override
        public void run() {
            while(isRunning){
                try {
                    URL url = new URL("http://222.239.249.149/cURL_Parcing.php");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    if(urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        readStream(in);
                        Log.e("확인", "확인");
                        urlConnection.disconnect();
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }// run 끝
    }


    public void readStream(InputStream in){
        final String data = readData(in);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONArray obj = new JSONArray(data);
                    for (int i = 0; i < coinlist.size(); i++) {
                        Log.e("I11", String.valueOf(i));
                        JSONObject jsonObject = obj.getJSONObject(i);
                        name = jsonObject.getString("code");
                        price = String.valueOf(Integer.parseInt(jsonObject.getString("tradePrice")));
                        Double changePercent1 = jsonObject.getDouble("signedChangeRate");
                        changePercent = String.valueOf(changePercent1 * 100);

                        Double changeprice = jsonObject.getDouble("signedChangePrice");
                        int changeprice_int = Integer.parseInt(String.valueOf(Math.round(changeprice)));
                        changePrice = String.valueOf(changeprice_int);

                        coinlist.get(i).setName(name);
                        coinlist.get(i).setPrice(price);
                        coinlist.get(i).setCpYesterday(changePrice);
                        coinlist.get(i).setcPYesterdayPercent(changePercent);

                    }
                    coinadapter.notifyDataSetChanged();

                }catch (JSONException e){
                    e.getMessage();
                }
            }
        });
    }
    public String readData(InputStream is){
        String data = "";
        Scanner s = new Scanner(is);
        while(s.hasNext()) data += s.nextLine() + "\n";
        s.close();
        return data;
    }


    public class adapter1 extends RecyclerView.Adapter<adapter1.holder>{
        ArrayList<Profit_listview_item> list;


        public adapter1(ArrayList<Profit_listview_item> list1) {
            this.list = list1;
        }

        @Override
        public holder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profit_listview_item, parent, false);
            holder holder = new holder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(holder holder, int position) {

            Profit_listview_item item = list.get(position);

            if(item.getPrice().equals("불러오는 중...")){
                holder.tv_name.setText(item.getName());
                holder.tv_price.setText(item.getPrice());
                holder.tv_yester.setText(item.getCpYesterday());
                holder.tv_yesterper.setText(item.getcPYesterdayPercent());
            }
            else {
                if(item.getName().equals("-BTC")){
                    holder.tv_name.setText("비트코인");
                }else if(item.getName().equals("-ETH")) {
                    holder.tv_name.setText("이더리움");
                }else if(item.getName().equals("-QTUM")) {
                    holder.tv_name.setText("퀀텀");
                }else if(item.getName().equals("-ADA")) {
                    holder.tv_name.setText("에이다");
                }else if(item.getName().equals("-XRP")) {
                    holder.tv_name.setText("리플");
                }else if(item.getName().equals("-NEO")) {
                    holder.tv_name.setText("네오");
                }else if(item.getName().equals("-XLM")) {
                    holder.tv_name.setText("스텔라루멘");
                }else if(item.getName().equals("-BCC")) {
                    holder.tv_name.setText("비트코인캐시");
                }else if(item.getName().equals("-ETC")) {
                    holder.tv_name.setText("이더클");
                }else if(item.getName().equals("-OMG")) {
                    holder.tv_name.setText("오미세고");
                }

                String price_str = String.format("%,d", Integer.parseInt(item.getPrice()));
                holder.tv_price.setText(price_str + "원");

                String cp_str = String.format("%,d", Integer.parseInt(item.getCpYesterday()));
                String cp_per_str = String.format("%.2f", Double.parseDouble(item.getcPYesterdayPercent()));
                if (Double.parseDouble(item.getcPYesterdayPercent()) > 0) {
                    holder.tv_yester.setText("▲" + cp_str + "원");
                    holder.tv_yester.setTextColor((Color.parseColor("#FF0000")));
                    holder.tv_yesterper.setText("▲" + cp_per_str + "%");
                    holder.tv_yesterper.setTextColor((Color.parseColor("#FF0000")));
                } else {
                    holder.tv_yester.setText("▼" + cp_str + "원");
                    holder.tv_yester.setTextColor((Color.parseColor("#0000FF")));
                    holder.tv_yesterper.setText("▼" + cp_per_str + "%");
                    holder.tv_yesterper.setTextColor((Color.parseColor("#0000FF")));
                }
            }


        }


        @Override
        public int getItemCount() {
            return list.size();
        }

        class holder extends RecyclerView.ViewHolder{
            TextView tv_name, tv_price, tv_yester, tv_yesterper;

            public holder(View v) {
                super(v);
                tv_name = (TextView)v.findViewById(R.id.tv_name1);
                tv_price = (TextView)v.findViewById(R.id.tv_price1);
                tv_yester = (TextView)v.findViewById(R.id.tv_yester1);
                tv_yesterper = (TextView)v.findViewById(R.id.tv_yesterPer1);
            }
        }


    }




    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}



