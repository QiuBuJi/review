package com.example.review;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.review.DataStructureFile.WordExplain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HandleInterface {

    private final ConstraintLayout containerView;
    private final Context          context;
    private final MyAdapter        adapter;

    private ArrayList<WordExplain> frame;
    private ArrayList<WordExplain> frameTemp;
    public  WindowExplainHolder    windowExplainHolder;

    public HandleInterface(Context context, ConstraintLayout containerView, ArrayList<WordExplain> frame, ArrayList<WordExplain> frameTemp) {
        this.context = context;
        this.containerView = containerView;
        this.frame = frame;
        this.frameTemp = frameTemp;

        //把frameTemp内成员顺序排列得跟frame一样
        for (int i = 0; i < frame.size(); i++) {
            WordExplain we = frame.get(i);

            for (WordExplain wordExplain : frameTemp) {
                if (wordExplain.category.trim().equals(we.category.trim())) {
                    frameTemp.remove(wordExplain);
                    frameTemp.add(i, wordExplain);
                    break;
                }
            }
        }

        containerView.removeAllViews();

        View windowExplain = LayoutInflater.from(context).inflate(R.layout.activity_window_explain, containerView, false);
        containerView.addView(windowExplain);

        windowExplainHolder = new WindowExplainHolder(windowExplain);

        windowExplainHolder.explainTitle.setText("hello you guys");

        LinearLayoutManager layout = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        windowExplainHolder.explainBody.setLayoutManager(layout);
        adapter = new MyAdapter();
        windowExplainHolder.explainBody.setAdapter(adapter);
    }

    int indexOfItem = 0;

    //移动到上一项
    public void moveUp() {
        indexOfItem--;
        if (indexOfItem < 0) indexOfItem = frame.size() - 1;
        adapter.notifyDataSetChanged();
    }

    //移动到下一项
    public void moveDown() {
        indexOfItem++;
        if (indexOfItem == frame.size()) indexOfItem = 0;
        adapter.notifyDataSetChanged();
    }

    //向前删除
    public void delete() {
        WordExplain we;
        we = frame.get(indexOfItem);
        int position = we.explains.size();
        if (position > 0) we.explains.remove(--position);
        adapter.notifyDataSetChanged();
    }

    //清空所有输入的数据
    public void emptying() {
        indexOfItem = 0;
        for (WordExplain explain : frame) explain.explains.clear();
        adapter.notifyDataSetChanged();
    }

    //添加字符
    public void addSegment(String segment) {
        WordExplain we       = frame.get(indexOfItem);
        WordExplain wordTemp = frameTemp.get(indexOfItem);

        //主动跳转下一行
        int sizeA = we.explains.size(), sizeB = wordTemp.explains.size();
        if (sizeA < sizeB) {

            if (we.explains.contains(segment)) {
                Toast.makeText(context, "不能重复哦！", Toast.LENGTH_SHORT).show();
            } else we.explains.add(segment);
        } else {
            adapter.notifyDataSetChanged();
            return;
        }

        //目前项数据填满后，自动转移到下一项
        while (true) {
            if (indexOfItem == sizeA - 1) break;

            sizeA = we.explains.size();
            sizeB = wordTemp.explains.size();

            if (sizeA == sizeB) moveDown();
            else break;
            wordTemp = frame.get(indexOfItem);
        }

        adapter.notifyDataSetChanged();
    }

    String[]     mPriorites    = new String[]{"n.", "vt.", "adj.", "pron.", "conj.", "adv.", "intj.", "adv.", "art.", "vi."};
    List<String> strPriorities = Arrays.asList(mPriorites);

    //取颜色，不同前缀有不同的对应的颜色
    public int getColor(String src) {
        src = src.replaceAll("\\s", "");//去除空格
        int prefixColor;
        int i = strPriorities.indexOf(src);

        switch (i) {
            case 0:
                prefixColor = Color.RED;
                break;
            case 1:
                prefixColor = Color.GREEN;
                break;
            case 2:
                prefixColor = Color.DKGRAY;
                break;
            case 3:
                prefixColor = Color.BLUE;
                break;
            case 4:
                prefixColor = Color.MAGENTA;
                break;
            case 5:
                prefixColor = Color.CYAN;
                break;
            default:
                prefixColor = Color.GRAY;
        }

        return prefixColor;
    }

    /**
     * @link activity_window_explain.xml
     */
    public class WindowExplainHolder {

        public RecyclerView explainBody;
        public TextView     explainTitle;

        WindowExplainHolder(View view) {
            explainBody = view.findViewById(R.id.recycler_explain_body);
            explainTitle = view.findViewById(R.id.tv_explain_title);
        }
    }

    /**
     * @link activity_item_show.xml
     */
    public class ItemHolder extends RecyclerView.ViewHolder {

        public final TextView     prefix;
        public final LinearLayout container;
        public final TextView     count;
        public final TextView     indicator;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            prefix = itemView.findViewById(R.id.tv_prefix);
            container = itemView.findViewById(R.id.ll_container);
            count = itemView.findViewById(R.id.tv_explain_count);
            indicator = itemView.findViewById(R.id.tv_indicator);
        }
    }


    class MyAdapter extends RecyclerView.Adapter {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View inflate = LayoutInflater.from(context).inflate(
                    R.layout.activity_item_show, windowExplainHolder.explainBody, false);
            return new ItemHolder(inflate);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            ItemHolder  holder = (ItemHolder) viewHolder;
            WordExplain we     = frame.get(i);
            WordExplain weTemp = frameTemp.get(i);

            //设置前缀及其颜色
            holder.prefix.setText(we.category);
            holder.prefix.setTextColor(getColor(we.category));

            //设置解释字符
            holder.container.removeAllViews();
            for (String explain : we.explains) {
                TextView txt = (TextView) LayoutInflater.from(context).inflate(
                        R.layout.activity_item_text, holder.container, false);
                txt.setText(explain);
                holder.container.addView(txt);
            }

            //设置该项内数据总量
            holder.count.setText(weTemp.explains.size() + "");

            if (indexOfItem == i) holder.indicator.setVisibility(View.VISIBLE);
            else holder.indicator.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            if (frame.size() > 0) return frame.size();
            return 0;
        }
    }
}
