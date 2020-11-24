package com.example.our_capstone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class MyAdapter_rcv extends RecyclerView.Adapter<MyAdapter_rcv.MyViewHolder>{

    private static ArrayList<RecycleModel> items;
    private Context context;
    private static OnItemClickListener mListener = null;

    public MyAdapter_rcv(Context context, ArrayList<RecycleModel> items){
        this.context = context;
        this.items = items;
    }
    public interface OnItemClickListener{
        void onitemClick(View v, int pos, ArrayList<RecycleModel> items);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.mListener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.greedy_rooms,parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context)
                .load(items.get(position).getUri())
                .into(holder.im);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView im;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            im = itemView.findViewById(R.id.imageView1);

            // 아이템 클릭 이벤트 처리.
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    if(pos!=RecyclerView.NO_POSITION){
                        mListener.onitemClick(v,pos,items);
                    }
                }
            });
        }
    }
}
