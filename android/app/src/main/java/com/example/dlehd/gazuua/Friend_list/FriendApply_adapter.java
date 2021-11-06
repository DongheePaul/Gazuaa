package com.example.dlehd.gazuua.Friend_list;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dlehd.gazuua.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * 받은 친구신청 목록을 생성하기 위한 어뎁터 클래스.
 * 로그인시 저장된 세션변수를 활용하기 위한 세션 아이디를 저장하고,
 * 친구 신청한 유저의 이메일을 보여주며
 * 친구신청 수락&거절 버튼을 눌렀을 떄의 이벤트를 처리한다.
 */

public class FriendApply_adapter extends RecyclerView.Adapter<FriendApply_adapter.ViewHolder> {
    Context context;
    ArrayList<FriendApply_item> list = new ArrayList<>();
    FriendApply_item item;
    String Session, result_for_accept_friendApply, email_from, answer;

    public FriendApply_adapter(Context context, ArrayList<FriendApply_item> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_friend_apply_item,parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        item = list.get(position);
        //친구신청한 유저의 이메일을 텍스트뷰에 셋해준다.
        holder.email_apply_from.setText(item.getEmail());
        //세션 아이디를 텍스트뷰에 셋해준다 (ui에서 확인 불가능)
        holder.sessionSave.setText(item.getSession());
        //수락버튼을 눌렀을 때
        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item = list.get(position);
                Session = item.getSession();
                email_from = item.getEmail();
                //받은 친구신청 목록에서 삭제한다.
                list.remove(item);
                notifyDataSetChanged();
                //받은 친구신청을 수락 => 친구가 되었다.
                accept_friend_apply accept = new accept_friend_apply();
                accept.execute("yes");

            }
        });
        //거절버튼을 눌렀을 때
        holder.deny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item = list.get(position);
                email_from = item.getEmail();
                Session = item.getSession();
                //받은 친구신청 목록에서 삭제한다.
                list.remove(item);
                notifyDataSetChanged();
                //받은 친구신청을 거절하고, db에서도 데이터 삭제.
                accept_friend_apply accept = new accept_friend_apply();
                accept.execute("no");
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    //inflate 한 레이아웃의 위젯들을 찾아주는 클래스.
    class ViewHolder extends RecyclerView.ViewHolder{

        TextView email_apply_from, sessionSave;
        Button accept, deny;
        public ViewHolder(View itemView) {
            super(itemView);
            email_apply_from = (TextView)itemView.findViewById(R.id.email_tv_for_friend_apply);
            accept = (Button)itemView.findViewById(R.id.btn_accept);
            deny = (Button)itemView.findViewById(R.id.btn_deny);
            sessionSave = (TextView)itemView.findViewById(R.id.session_save_tv_friendApply);

        }
    }

    //수락버튼을 눌렀을 때 일어나는 이벤트.
    //수락한 유저 & 친구신청을 한 유저가 친구가 된다.
    public class accept_friend_apply extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            try{
                //받은 친구 신청에 대한 수락&거절의사 => params(yes이거나 no. FriendApply_adapter 안의 onBindViewholder 메소드 안의 accept, deny 버튼 클릭시 yes나 no가 넘어옴)
                //수락, 거절 의사와 친구신청 받은 사람의 이메일을 서버로 보내준다.

                for(int i = 0; i<params.length; i++){
                    answer = params[i].toString();
                }
                Log.e("doingbackground 확인", answer);
                String param = "answer="+answer+"&from="+email_from;
                URL url = new URL("http://222.239.249.149/friend/insert_friend.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Accept-Charset", "UTF-8");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Cookie", Session);
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.connect();
                  /* 안드로이드 -> 서버로 이메일, 수락 의사 전달 */
                OutputStream outs = conn.getOutputStream();
                outs.write(param.getBytes("UTF-8"));
                outs.flush();
                outs.close();

                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                     /* 서버 -> 안드로이드 서버의 리턴값 전달 */
                    InputStream is = null;
                    BufferedReader in = null;
                    result_for_accept_friendApply = "";

                    //서버로부터 받은 인풋스트림을 스트링화
                    is = conn.getInputStream();
                    in = new BufferedReader(new InputStreamReader(is), 8 * 1024);

                    String line = null;
                    StringBuffer buff = new StringBuffer();
                    while ((line = in.readLine()) != null) {
                        buff.append(line + "\n");
                    }
                    //서버로부터의 응답 : 받은 친구신청을 수락했고, 친구 목록에 데이터를 삽입했다.
                    result_for_accept_friendApply = buff.toString().trim();
                }
                conn.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //서버의 응답값 onPostExecute의 파라미터로 전달된다.
            Log.e("친구 수락&거절 버튼 눌렀을 때 서버 응답", result_for_accept_friendApply);
            return result_for_accept_friendApply;
        }


        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equals("yes")){
                Toast.makeText(context, email_from + "님과 친구가 되었습니다." , Toast.LENGTH_SHORT).show();
            }

            if (s.equals("no")){
                Toast.makeText(context, email_from + "님의 친구신청을 거절했습니다." , Toast.LENGTH_SHORT).show();
            }
        }
    }
}
