package com.example.review.Keyboard;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Guideline;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.review.DataStructureFile.WordExplain;
import com.example.review.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HandleInterfaceType1 {

    public ConstraintLayout containerView;
    public Context          context;
    public MyAdapter        adapter;

    public        ArrayList<WordExplain> frameInput;
    public        WindowExplainHolder    windowExplainHolder;
    private final View                   windowExplain;

    public HandleInterfaceType1(Context context, ConstraintLayout containerView, ArrayList<WordExplain> frameInput) {
        this.context       = context;
        this.containerView = containerView;
        this.frameInput    = frameInput;

        windowExplain = LayoutInflater.from(context).inflate(R.layout.activity_window_explain, containerView, false);
        containerView.addView(windowExplain);

        windowExplainHolder = new WindowExplainHolder(windowExplain);
        windowExplainHolder.explainTitle.setText("");

        //解决退出后再进入时不显示内容的问题
        ViewGroup.LayoutParams lp = windowExplainHolder.explainBody.getLayoutParams();
        lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        windowExplainHolder.explainBody.setLayoutParams(lp);

        LinearLayoutManager layout = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        windowExplainHolder.explainBody.setLayoutManager(layout);
        adapter = new MyAdapter();
        windowExplainHolder.explainBody.setAdapter(adapter);
    }

    public void refresh() {
        adapter.notifyDataSetChanged();
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
            explainBody  = view.findViewById(R.id.recycler_explain_body);
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
        public final Guideline    guideline;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            prefix    = itemView.findViewById(R.id.tv_prefix);
            container = itemView.findViewById(R.id.ll_container);
            count     = itemView.findViewById(R.id.tv_explain_count);
            indicator = itemView.findViewById(R.id.tv_indicator);
            guideline = itemView.findViewById(R.id.guideline);
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

        int max = 0;

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            final ItemHolder holder  = (ItemHolder) viewHolder;
            WordExplain      weInput = frameInput.get(i);

            //解决前缀对齐问题
            if (max == 0) {
                int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                for (WordExplain we : frameInput) {
                    holder.prefix.setText(we.category);
                    holder.prefix.measure(spec, spec);
                    int width = holder.prefix.getMeasuredWidth();
                    if (width > max) max = width;
                }
            }
            holder.guideline.setGuidelineBegin(max);

            //设置前缀及其颜色
            holder.prefix.setText(weInput.category);
            holder.prefix.setTextColor(getColor(weInput.category));
            holder.indicator.setVisibility(View.GONE);

            //设置解释字符
            holder.container.removeAllViews();
            for (String explain : weInput.explains) {
                TextView txt = (TextView) LayoutInflater.from(context).inflate(
                        R.layout.activity_item_text, holder.container, false);
                txt.setText(explain);
                txt.setBackgroundColor(0xFF00C853);
                holder.container.addView(txt);
            }

            //设置该项内数据总量
            holder.count.setText(weInput.explains.size() + "");
            holder.count.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            if (frameInput.size() > 0) return frameInput.size();
            return 0;
        }
    }
}
