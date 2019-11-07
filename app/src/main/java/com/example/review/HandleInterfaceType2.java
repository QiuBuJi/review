package com.example.review;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
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

public class HandleInterfaceType2 {

    public ConstraintLayout containerView;
    public Context          context;
    public MyAdapter        adapter;

    public ArrayList<WordExplain> frameInput;
    public ArrayList<WordExplain> frameRight;
    public WindowExplainHolder    windowExplainHolder;
    int indexOfItem = 0;
    private final View windowExplain;

    public HandleInterfaceType2(Context context, ConstraintLayout containerView, ArrayList<WordExplain> frameInput, ArrayList<WordExplain> frameRight) {
        this.context = context;
        this.containerView = containerView;
        this.frameInput = frameInput;
        this.frameRight = frameRight;

        //把frameRight内成员顺序排列得跟frame一样
        for (int i = 0; i < frameInput.size(); i++) {
            WordExplain we = frameInput.get(i);

            for (WordExplain wordExplain : frameRight) {
                if (wordExplain.category.trim().equals(we.category.trim())) {
                    frameRight.remove(wordExplain);
                    frameRight.add(i, wordExplain);
                    break;
                }
            }
        }

        windowExplain = LayoutInflater.from(context).inflate(R.layout.activity_window_explain, containerView, false);
        containerView.addView(windowExplain);

        windowExplainHolder = new WindowExplainHolder(windowExplain);
        windowExplainHolder.explainTitle.setText("nothing!");

        LinearLayoutManager layout = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        adapter = new MyAdapter();
        windowExplainHolder.explainBody.setAdapter(adapter);
        windowExplainHolder.explainBody.setLayoutManager(layout);
    }

    public void setLightAnimation(boolean lightUp, int duration) {
        ValueAnimator valueAnim;
        if (lightUp)
            valueAnim = ValueAnimator.ofFloat(0f, 1f);
        else valueAnim = ValueAnimator.ofFloat(1f, 0f);

        valueAnim.setDuration(duration);
        valueAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (Float) valueAnimator.getAnimatedValue();
                windowExplain.setAlpha(value);
            }
        });
        valueAnim.start();
    }

    //移动到上一项
    public void moveUp() {
        indexOfItem--;
        if (indexOfItem < 0) indexOfItem = frameInput.size() - 1;
        adapter.notifyDataSetChanged();
    }

    //移动到下一项
    public void moveDown() {
        indexOfItem++;
        if (indexOfItem == frameInput.size()) indexOfItem = 0;
        adapter.notifyDataSetChanged();
    }

    //向前删除
    public void delete() {
        WordExplain we;
        we = frameInput.get(indexOfItem);
        int position = we.explains.size();
        if (position > 0) we.explains.remove(--position);
        adapter.notifyDataSetChanged();
    }

    //清空所有输入的数据
    public void emptying() {
        indexOfItem = 0;
        for (WordExplain explain : frameInput) explain.explains.clear();
        adapter.notifyDataSetChanged();
    }

    //添加字符
    public void addSegment(String segment) {
        //解决退出后再进入时不显示内容的问题
        ViewGroup.LayoutParams lp = windowExplainHolder.explainBody.getLayoutParams();
        lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        windowExplainHolder.explainBody.setLayoutParams(lp);

        WordExplain we     = frameInput.get(indexOfItem);
        WordExplain weTemp = frameRight.get(indexOfItem);

        //主动跳转下一行
        int sizeA = we.explains.size(), sizeB = weTemp.explains.size();
        if (sizeA < sizeB) {

            if (we.explains.contains(segment)) {
                Toast.makeText(context, "不能重复哦！", Toast.LENGTH_SHORT).show();
            } else we.explains.add(segment);
        } else {
            adapter.notifyDataSetChanged();
            return;
        }

        sizeB = frameRight.size();
        //目前项数据填满后，自动转移到下一项
        while (true) {
            //到底就不要再跳到初始位置了
            if (indexOfItem == sizeB - 1) break;

            sizeA = we.explains.size();
            sizeB = weTemp.explains.size();

            if (sizeA == sizeB) moveDown();
            else break;
            weTemp = frameInput.get(indexOfItem);
        }

        adapter.notifyDataSetChanged();
    }

    public void refresh() {
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

    boolean mDifferent = false;

    public void showDifferent(boolean different) {
        mDifferent = different;
        adapter.notifyDataSetChanged();
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
            final ItemHolder holder  = (ItemHolder) viewHolder;
            WordExplain      weInput = frameInput.get(i);
            WordExplain      weRight = frameRight.get(i);

            //设置前缀及其颜色
            holder.prefix.setText(weInput.category);
            holder.prefix.setTextColor(getColor(weInput.category));

            //设置解释字符
            holder.container.removeAllViews();
            for (String explain : weInput.explains) {
                TextView txt = (TextView) LayoutInflater.from(context).inflate(
                        R.layout.activity_item_text, holder.container, false);
                txt.setText(explain);
                holder.container.addView(txt);

                if (mDifferent && !weRight.explains.contains(explain)) {
                    txt.setTextColor(Color.RED);
                }
            }

            //设置该项内数据总量
            holder.count.setText(weRight.explains.size() + "");

            //正确率超过这个阈值，切换不同文字颜色
            int countInput = 0, countRight = 0;
            for (WordExplain we : frameInput) countInput += we.explains.size();
            for (WordExplain we : frameRight) countRight += we.explains.size();
            if (countInput >= countRight * 0.6f) holder.count.setTextColor(0xff3F51B5);
            else holder.count.setTextColor(Color.WHITE);

            //设置闪闪动画
            if (indexOfItem == i) {
                holder.indicator.setVisibility(View.VISIBLE);

                ObjectAnimator.ofFloat(holder.indicator, "scaleX", 1f, 0f, 1f)
                              .setDuration(200)
                              .start();
            } else {
                holder.indicator.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            if (frameInput.size() > 0) return frameInput.size();
            return 0;
        }
    }
}
