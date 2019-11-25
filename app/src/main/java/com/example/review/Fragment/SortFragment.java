package com.example.review.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.review.Activity.EditActivity;
import com.example.review.Activity.ListActivity;
import com.example.review.DataStructureFile.DateTime;
import com.example.review.DataStructureFile.ReviewData;
import com.example.review.Activity.MainActivity;
import com.example.review.New.ReviewStruct;
import com.example.review.R;
import com.example.review.Setting;
import com.example.review.Activity.SortActivity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("ValidFragment")
public class SortFragment extends Fragment {

    public  RecyclerView             recyclerView;
    public  AdapterSort              adapter;
    private ReviewData               data;
    public  int                      displayField;
    public  Context                  context;
    public  LinkedList<ReviewStruct> mData;
    public  boolean                  mIsActivity;
    public  TextView                 tip;

    public SortFragment() {
    }

    @SuppressLint("ValidFragment")
    public SortFragment(boolean activity) {
        this.mIsActivity = activity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_sort_fragment, container, false);

        displayField = Setting.getInt("displayField");
        data = MainActivity.data;
        context = getContext();
        recyclerView = (RecyclerView) view.findViewById(R.id.sort_fragment_recyclerView);
        tip = (TextView) view.findViewById(R.id.sort_fragment_textView_noData);

        if (mIsActivity) mData = data.mActivate;
        else mData = data.mInactivate;

        if (mData.isEmpty()) tip.setVisibility(View.VISIBLE);
        else tip.setVisibility(View.INVISIBLE);

        selectPartToShow(displayField);

        adapter = new AdapterSort(context, mData);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        recyclerView.setVerticalScrollBarEnabled(true);
        recyclerView.setAdapter(adapter);

        return view;
    }

    //设置要显示的时间区域
    public void selectPartToShow(int part) {
        displayField = part;
        DateTime temp = null;
        for (ReviewStruct rs : mData) {
            DateTime dateTime = new DateTime(rs.time);
            dateTime.setZeroSegment(part + 1);

            if (temp == null) {
                rs.showed = true;
            } else {
                boolean b = temp.compareTo(dateTime) == 0;
                rs.showed = !b;
            }
            temp = dateTime;
        }
    }

    int      oldPosi = -1;
    View     oldView;
    Drawable background;

    public class AdapterSort extends RecyclerView.Adapter {
        Context                  context;
        LinkedList<ReviewStruct> data;

        public AdapterSort(Context context, LinkedList<ReviewStruct> data) {
            this.context = context;
            this.data = data;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View inflate = LayoutInflater.from(context).inflate(R.layout.activity_sort_item, viewGroup, false);
            return new MyHolder(inflate);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int posi) {
            final AdapterSort.MyHolder holder = (AdapterSort.MyHolder) viewHolder;
            final ReviewStruct         item   = data.get(posi);

            holder.index.setText(String.format("%d.", posi + 1));
            if (item.viewCount > 0) holder.level2.setText(String.format("%d次", item.viewCount));
            else holder.level2.setText("");

            if (mIsActivity) {
                holder.title1.setText(String.format("类型：%d", item.match.getType()));
                holder.timeTips.setText("");

                holder.view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        int posiOrigion = data.indexOf(item);
                        data.remove(item);
                        data.addFirst(item);
                        int posiDest = data.indexOf(item);

                        adapter.notifyItemMoved(posiOrigion, posiDest);
                        return false;
                    }
                });

                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (oldPosi != -1) {
                            int dest = data.indexOf(item);

                            //整个类型的数据置顶
                            if (oldPosi == dest) {
                                ArrayList<ReviewStruct> rss     = new ArrayList<>();
                                int                     srcType = item.match.getType();

                                for (int i = data.size() - 1; i >= 0; i--) {
                                    ReviewStruct rs       = data.get(i);
                                    int          destType = rs.match.getType();

                                    if (destType == srcType) {
                                        data.remove(rs);
                                        rss.add(0, rs);
                                    }
                                }
                                data.addAll(0, rss);
                                notifyItemRangeRemoved(0, data.size());


                                //单条数据，换位置
                            } else {
                                ReviewStruct rs = data.get(oldPosi);
                                data.remove(oldPosi);
                                data.add(dest, rs);
                                adapter.notifyItemMoved(oldPosi, dest);

//                            oldView.setBackground(background);


                                final Handler handler = new Handler(new Handler.Callback() {
                                    @Override
                                    public boolean handleMessage(Message message) {
                                        oldView.setBackground(background);
                                        return false;
                                    }
                                });

                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        handler.sendEmptyMessage(1);
                                    }
                                }, 300);
                            }
                            oldPosi = -1;

                        } else {
                            oldPosi = data.indexOf(item);
                            oldView = holder.view;
                            background = oldView.getBackground();
                            holder.view.setBackgroundColor(Color.LTGRAY);
                        }

                    }
                });
            } else {
                holder.title1.setText(item.getMatch());

                DateTime dateTime = DateTime.getCurrentTime();
                DateTime tempTime = new DateTime(item.time);
                tempTime.subtractOf(dateTime);
                holder.timeTips.setText(tempTime.toAboutValue());

                //长按进入编辑界面
                holder.view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        //跳转页面，到编辑窗口
                        ListActivity.currentClickedRs = item;
                        context.startActivity(new Intent(context, EditActivity.class));
                        return false;
                    }
                });
            }
            holder.title2.setText(item.getShow());
            holder.level1.setText(String.format("%d", item.getLevel()));


            //分组显示，显示时间的一排设置程序
            if (item.showed && !mIsActivity) {
                holder.region.setVisibility(View.VISIBLE);
                holder.line.setVisibility(View.VISIBLE);
                make(posi, holder);
            } else {
                holder.region.setVisibility(View.GONE);
                holder.line.setVisibility(View.INVISIBLE);
            }
        }

        private void make(int posi, MyHolder holder) {
            StringBuilder sb   = new StringBuilder();
            ReviewStruct  item = data.get(posi);

            try {
                sb.append(item.time.getYear()).append("年");
                if (SortActivity.fragment.displayField == DateTime.YEAR) {
                    addExtraText(item.time, DateTime.TimeFieldEnum.MONTH, sb, "（本年）");
                    throw new Exception();
                }
                sb.append(item.time.getMonth() + 1).append("月");
                if (SortActivity.fragment.displayField == DateTime.MONTH) {
                    addExtraText(item.time, DateTime.TimeFieldEnum.DAY, sb, "（本月）");
                    throw new Exception();
                }
                sb.append(item.time.getDay()).append("日");
                if (SortActivity.fragment.displayField == DateTime.DAY) {
                    DateTime time = new DateTime(item.time);
                    boolean  b    = addExtraText(time, DateTime.TimeFieldEnum.HOUR, sb, "（今天）");

                    if (!b) {
                        time.setDay(time.getDay() - 1);
                        addExtraText(time, DateTime.TimeFieldEnum.HOUR, sb, "（明天）");
                    }
                    throw new Exception();
                }
                sb.append("   ").append(item.time.getHour()).append("时");
                if (SortActivity.fragment.displayField == DateTime.HOUR) {
                    addExtraText(new DateTime(item.time), DateTime.TimeFieldEnum.MINUTE, sb, "（本小时）");
                    throw new Exception();
                }
                sb.append(item.time.getMinute()).append("分");
                if (SortActivity.fragment.displayField == DateTime.MINUTE) {
                    addExtraText(new DateTime(item.time), DateTime.TimeFieldEnum.SECOND, sb, "（本分钟）");
                    throw new Exception();
                }
                sb.append(item.time.getSecond()).append("秒");
            } catch (Exception e) {
                int count = 0;
                for (int i = posi + 1; i < data.size(); i++) {
                    if (data.get(i).showed) break;
                    else count++;
                }
                ++count;
                sb.append("  ").append(count).append("条");
                holder.region.setText(sb);
            }
        }

        boolean addExtraText(DateTime time, DateTime.TimeFieldEnum part, StringBuilder sb, String str) {
            DateTime currTime = DateTime.getCurrentTime();
            currTime.setZeroSegment(part);
            DateTime dateTime = new DateTime(time);
            dateTime.setZeroSegment(part);

            if (dateTime.equals(currTime)) {
                sb.append(str);
                return true;
            }
            return false;
        }

        @Override
        public int getItemCount() {
            if (data == null) return 0;
            return data.size();
        }

        class MyHolder extends RecyclerView.ViewHolder {
            public        View      view;
            private final TextView  index;
            private final TextView  title1;
            private final TextView  title2;
            private final TextView  region;
            private final TextView  level1;
            private final TextView  level2;
            private final TextView  timeTips;
            private final ImageView line;

            MyHolder(@NonNull View itemView) {
                super(itemView);
                view = itemView;

                index = (TextView) view.findViewById(R.id.item_sort_textView_index);
                title1 = (TextView) view.findViewById(R.id.item_sort_textView_title1);
                title2 = (TextView) view.findViewById(R.id.item_sort_textView_title2);
                region = (TextView) view.findViewById(R.id.item_sort_textView_region);
                level1 = (TextView) view.findViewById(R.id.item_sort_textView_level1);
                level2 = (TextView) view.findViewById(R.id.item_sort_textView_level2);
                timeTips = (TextView) view.findViewById(R.id.item_sort_textView_timeTips);
                line = (ImageView) view.findViewById(R.id.item_sort_imageView_line);
            }
        }

    }
}
