package com.example.dlehd.gazuua.board;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.dlehd.gazuua.R;

import java.util.ArrayList;

/**
 * Created by dlehd on 2018-02-22.
 */

public class Recycler_adapter_board extends RecyclerView.Adapter<Recycler_adapter_board.ViewHolder> {
    Context context;
    ArrayList<Recycler_item_post> post = new ArrayList<>();
    Recycler_item_post item_post;

    //생성자
    public Recycler_adapter_board(Context context, ArrayList<Recycler_item_post> posts) {
        Log.e("어뎁터", "생성자");
        this.context = context;
        this.post = posts;
    }

    /**
     * 필수 메소드 1: 새로운 뷰 생성.
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.board_recycler_item,parent,false);
        return new ViewHolder(view);
    }

    /**
     * 필수 메소드 2: 새로운 뷰에 데이터 셋해주는 메소드.
     */
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        //데이터를 담을 리스트의 포지션값을 가져온다.
        item_post = post.get(position);
        //입력된 데이터를 아이템의 각 항목에 셋해준다.
        holder.title.setText(item_post.getTitle());
        holder.writer.setText(item_post.getWriter());
        holder.time.setText(item_post.getTime());
        holder.id.setText(item_post.getId());
        holder.small_num.setText(item_post.getSmallest_num());
        holder.sessionSave.setText(item_post.getSessionID());
        holder.user_email.setText(item_post.getUser_email());
        holder.user_name.setText(item_post.getUser_name());


        //글 목록의 글을 클릭하면 글읽기 액티비티로 넘어간다.
        //해당 글의 번호를 인텐트에 저장해준다.
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item_post = post.get(position);
                Intent intent = new Intent(v.getContext(), Post_Read_Activity.class);
                //게시물의 글번호를 넘겨준다.
                intent.putExtra("sessionID", item_post.getSessionID());
                intent.putExtra("user_email", item_post.getUser_email());
                intent.putExtra("user_name", item_post.getUser_name());
                intent.putExtra("id", item_post.getId());
                v.getContext().startActivity(intent);
            }
        });

    }
    /**
     * 필수 메소드 3
     */
    @Override
    public int getItemCount() {
        return post.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView title, writer, time, id, small_num, sessionSave, user_name, user_email;
        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title_tv);
            writer = (TextView) itemView.findViewById(R.id.writer_tv_item);
            time = (TextView)itemView.findViewById(R.id.time_tv_item);
            id = (TextView)itemView.findViewById(R.id.num_save_tv);
            small_num = (TextView)itemView.findViewById(R.id.smallest_num_for_list);
            sessionSave = (TextView)itemView.findViewById(R.id.session_save_tv);
            user_name = (TextView)itemView.findViewById(R.id.user_name_save_tv);
            user_email = (TextView)itemView.findViewById(R.id.user_email_save_tv);
        }
    }


}
