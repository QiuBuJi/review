package com.example.review.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.review.New.ItemClickListener;
import com.example.review.New.KeyText;
import com.example.review.R;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter {
    public        int     textSize  = 0;
    static public boolean isShowNum = false;
    ArrayList<KeyText> data;
    private final Context           context;
    private       ItemClickListener clickListener;

    public MyAdapter(Context context, ArrayList<KeyText> data, ItemClickListener click) {
        this.context = context;
        this.data = data;
        this.clickListener = click;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.activity_keyboard_item, viewGroup, false);
        return new MyHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int posi) {
        MyHolder holder = (MyHolder) viewHolder;
        KeyText  item   = data.get(posi);
        item.view = holder.view;

        if (item.isCom) {
            holder.textView.setMaxLines(10);
            holder.textView.setTextSize(16);
            holder.textView.setTextColor(Color.BLUE);
            holder.textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        } else {
            holder.textView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            holder.textView.setMaxLines(6);
            if (textSize > 0) {
                int length = item.text.length();
                if (length > 2) {
                    holder.textView.setTextSize(textSize - 4);
                } else if (length > 3) {

                } else {
                    holder.textView.setTextSize(textSize);
                }
                switch (length) {
                    case 1:
                    case 2:
                        holder.textView.setTextSize(textSize);
                        break;
                    case 3:
//                        holder.textView.setTextColor(Color.MAGENTA);
                    case 4:
                        holder.textView.setTextSize(textSize - 4);
                        break;

                }
            }
        }
        if (isShowNum) {
            if (item.key != 1) {
                holder.textViewnum.setText(item.key + "");
                holder.textViewnum.setVisibility(View.VISIBLE);
            } else {
                holder.textViewnum.setVisibility(View.INVISIBLE);
            }
        } else {
            holder.textViewnum.setVisibility(View.INVISIBLE);
        }

        holder.textView.setText(0 + "");


        holder.textView.setText(item.text);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clickListener != null)
                    clickListener.onItemClick(view, data, posi);
            }
        });

        if (item.text.equals("")) {
            holder.view.setVisibility(View.INVISIBLE);
        } else {
            holder.view.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        if (data == null) return 0;
        return data.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        View view;
        private final TextView textView;
        private final TextView textViewnum;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;

            textView = view.findViewById(R.id.item_textView_word);
            textViewnum = view.findViewById(R.id.item_textView_number);
        }
    }
}
