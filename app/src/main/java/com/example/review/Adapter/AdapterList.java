package com.example.review.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.example.review.DataStructureFile.DateTime;
import com.example.review.DataStructureFile.ReviewData;
import com.example.review.EditActivity;
import com.example.review.ListActivity;
import com.example.review.New.ReviewStruct;
import com.example.review.R;
import com.example.review.Util.SpanUtil;
import com.example.review.Util.Speech;

import java.util.ArrayList;
import java.util.LinkedList;

public class AdapterList extends RecyclerView.Adapter {
    Context    context = null;
    ReviewData data    = null;
    private Drawable     background;
    private ListActivity parent;
    public int posi;

    public AdapterList(Context context, ReviewData data) {
        this.data = data;
        this.context = context;
        parent = (ListActivity) context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_list_item, parent, false);
        background = view.getBackground();
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder_, final int position) {
        this.posi = position;
        final Holder       holder = (Holder) holder_;
        final ReviewStruct rs     = data.get(position);

        //填充条目内的数据
        holder.index.setText(position + 1 + ".");
        if (rs.joined) {
            holder.index.setTextColor(0xFFD81B60);
//            holder.index.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        } else {
//            holder.index.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            holder.index.setTextColor(0xFF747474);
        }
        holder.word.setText(rs.getMatch());
        holder.explain.setText(rs.getShow());
        holder.progress.setProgress(rs.getLevel());
        holder.levelNumber.setText(rs.getLevel() + "");

        int size  = rs.logs.size();
        int count = 0;


        for (byte[] log : rs.logs) {
            DateTime dateTime = new DateTime(log);
            int      second   = dateTime.getSecond();
            if (second < 0) {
                count++;
            }
        }

        holder.errorProgress.setMax(size);
//        holder.errorProgress.setProgress(size);
//        SpanUtil.create()
//                .addForeColorSection(size + "  ", Color.BLACK)
//                .addForeColorSection(count + "％", Color.WHITE)
//                .showIn(holder.errorNum);

        holder.errorProgress.setProgress(count);
        holder.errorNum.setText(count + " : " + size);

        //加入复习总开关
        if (ListActivity.switch_state) {
            holder.switch_.setOnCheckedChangeListener(null);
            holder.switch_.setChecked(rs.joined);
            holder.switch_.setVisibility(View.VISIBLE);
            holder.switch_.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    rs.joined = isChecked;
                    LinkedList<ReviewStruct> mInactivate = parent.data.mInactivate;

                    //加入复习计划按钮状态改变
                    if (isChecked) {

                        //如果没有重复的
                        if (!mInactivate.contains(rs)) {
                            int level = rs.getLevel();

                            if (level < 13) {
                                parent.data.sortAddToInactivate(rs);
                            } else {
                                //弹出对话框：要清零还是取消加入
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle("重新开始计划");
                                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        rs.setLevel(0);
                                        parent.data.sortAddToInactivate(rs);
                                        notifyDataSetChanged();
                                    }
                                });
                                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        holder.switch_.setChecked(false);
                                        rs.joined = false;
                                    }
                                });
                                builder.show();
                            }
                        }
                    } else {
                        parent.data.removeFromInavalable_Avalable(rs);
                    }
                }
            });
        } else holder.switch_.setVisibility(View.GONE);


        //选择&没被选择，的背景区别
        if (rs.selected) {
            holder.view.setBackgroundColor(Color.LTGRAY);
        } else holder.view.setBackground(background);

        //条目被长按
        holder.view.setOnLongClickListener(new LongClick(rs, position));
        //条目被单击
        holder.view.setOnClickListener(new Click(rs, position));
        //播放语音
        holder.playSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Speech.play_Baidu(rs.getMatch(), holder.playSound);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (data == null) return 0;
        return data.size();
    }

    class LongClick implements View.OnLongClickListener {
        ReviewStruct reviewStruct;
        private int position;

        public LongClick(ReviewStruct rs, int position) {
            this.reviewStruct = rs;
            this.position = position;
        }

        @Override
        public boolean onLongClick(View view) {
            //长按，进入选择状态
            if (ListActivity.checked) {

                if (reviewStruct.selected) {
                    for (ReviewStruct rs : data) rs.selected = true;
                    ListActivity.switch_state = false;
                    parent.buttonDelete.setText("删除");
                    parent.buttonDelete.setBackgroundColor(Color.RED);
                } else {
                    for (ReviewStruct rs : data) rs.selected = false;
                    ListActivity.switch_state = true;
                    parent.buttonDelete.setText("退出");
                    parent.buttonDelete.setBackgroundColor(Color.GREEN);
                }
                notifyDataSetChanged();
            } else {
                parent.buttonDelete.setText("删除");
                parent.buttonDelete.setBackgroundColor(Color.RED);
                ListActivity.checked = true;
                reviewStruct.selected = true;//设置该条被选择
                notifyItemChanged(position);

                parent.buttonDelete.setVisibility(View.VISIBLE);//显示删除按钮
            }
            return false;
        }
    }


    class Click implements View.OnClickListener {
        ReviewStruct reviewStruct;
        private int position;

        public Click(ReviewStruct rs, int position) {
            this.reviewStruct = rs;
            this.position = position;
        }

        @Override
        public void onClick(View view) {
            //选择按钮被打开
            if (ListActivity.checked) {
                parent.buttonDelete.setText("删除");
                parent.buttonDelete.setBackgroundColor(Color.RED);
                if (ListActivity.switch_state) {
                    ListActivity.switch_state = false;
                    notifyDataSetChanged();
                }

                //删除模式，背景的切换
                reviewStruct.selected = reviewStruct.selected ? false : true;
                notifyItemChanged(position);

            } else {//删除按钮没有打开

                //跳转页面，到编辑窗口
                ListActivity.currentClickedRs = reviewStruct;
                context.startActivity(new Intent(context, EditActivity.class));
            }
        }

    }

    class Holder extends RecyclerView.ViewHolder {

        TextView    errorNum;
        ProgressBar errorProgress;
        View        view = null;
        TextView    index;
        TextView    word;
        TextView    explain;
        ProgressBar progress;
        TextView    levelNumber;
        Switch      switch_;
        ImageView   playSound;

        public Holder(View itemView) {
            super(itemView);
            view = itemView;

            index = view.findViewById(R.id.item_textView_index);
            word = view.findViewById(R.id.item_textView_word);
            explain = view.findViewById(R.id.item_textView_explain);
            progress = view.findViewById(R.id.item_progressBar_Level_forward);
            levelNumber = view.findViewById(R.id.item_textView_level_number_up);
            switch_ = view.findViewById(R.id.item_switch_JoinReview);
            playSound = view.findViewById(R.id.item_imageView_play_sound);

            errorProgress = view.findViewById(R.id.item_progressBar_error);
            errorNum = view.findViewById(R.id.item_textView_error_num);
        }
    }

}
