package com.example.review.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.review.New.ItemClickListener;
import com.example.review.New.KeyText;
import com.example.review.R;

import java.util.ArrayList;
import java.util.Locale;

public class MyAdapter extends RecyclerView.Adapter {
    public        int                textSize  = 0;
    static public boolean            isShowNum = false;
    private       ArrayList<KeyText> data;
    private final Context            context;
    private       ItemClickListener  clickListener;

    public MyAdapter(Context context, ArrayList<KeyText> data, ItemClickListener click) {
        this.context       = context;
        this.data          = data;
        this.clickListener = click;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.activity_keyboard_item, viewGroup, false);
        return new MyHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        final MyHolder holder = (MyHolder) viewHolder;
        final KeyText  item   = data.get(position);
        item.view = holder.view;

        if (item.isCom) {
            //功能键，配置
            holder.textView.setMaxLines(10);
            holder.textView.setTextSize(16);
//            holder.textView.setTextColor(Color.BLUE);
            holder.textView.setTextColor(Color.WHITE);
            holder.textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            holder.view.setBackgroundResource(R.drawable.bg_ripple_keyboard_click_commen);
        } else {
            //非功能键，配置
            holder.textView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            holder.textView.setMaxLines(6);
            holder.textView.setTextColor(Color.BLACK);
            holder.view.setBackgroundResource(R.drawable.bg_ripple_keyboard_click);

            if (textSize > 0) {
                int length = item.text.length();
                switch (length) {
                    case 1:
                    case 2:
                        holder.textView.setTextSize(textSize);
                        break;
                    case 3:
                        holder.textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                        holder.textView.setTextSize(textSize - 2);
                        //holder.textView.setTextColor(Color.MAGENTA);
                        break;
                    case 4:
                    case 5:
                        holder.textView.setTextSize(textSize - 4);
                        break;
                    case 6:
                    default:
                        holder.textView.setTextSize(textSize - 6);
                }
            }
        }
        if (isShowNum) {
            if (item.key != 1) {
                holder.textViewNum.setText(String.format("%c", item.key));
                holder.textViewNum.setVisibility(View.VISIBLE);
            } else holder.textViewNum.setVisibility(View.INVISIBLE);
        } else holder.textViewNum.setVisibility(View.INVISIBLE);

        //被单击过，就不显示该字符
        if (item.isPressed && !item.isCom) {
            holder.textView.setText("");
            holder.view.setEnabled(false);
//                holder.view.setAlpha(0f);
        } else {
            holder.view.setEnabled(true);
            holder.textView.setText(item.text);
        }

        //键盘按键被单击事件处理
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clickListener != null) clickListener.onItemClick(view, data, position);
            }
        });

        if (item.text.equals("")) holder.view.setVisibility(View.INVISIBLE);
        else holder.view.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        if (data == null) return 0;
        return data.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        View view;
        private final TextView textView;
        private final TextView textViewNum;

        private MyHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;

            textView    = view.findViewById(R.id.item_textView_word);
            textViewNum = view.findViewById(R.id.item_textView_number);
        }
    }
}
