package com.example.review.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.review.DataStructureFile.DateTime;
import com.example.review.MainActivity;
import com.example.review.New.ReviewStruct;
import com.example.review.R;
import com.example.review.Setting;

import java.util.LinkedList;


public class OutlineFragment extends SortFragment {
    LinkedList<String> data = new LinkedList<>();

    @SuppressLint("ValidFragment")
    public OutlineFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_sort_fragment, container, false);
        recyclerView = view.findViewById(R.id.sort_fragment_recyclerView);
        tip = view.findViewById(R.id.sort_fragment_textView_noData);

        context = getContext();
        tip.setVisibility(View.INVISIBLE);
        displayField = Setting.getInt("displayField");
        selectPartToShow(displayField);

        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        recyclerView.setVerticalScrollBarEnabled(true);
        adapter = new AdapterSort1(context, data);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void selectPartToShow(int part) {
        data.removeAll(data);
        LinkedList<String> strings = getStrings(part);
        data.addAll(strings);
    }

    private LinkedList<String> getStrings(int part) {
        LinkedList<String> sb = new LinkedList<>();
        mData = MainActivity.data.mInactivate;
        super.selectPartToShow(part);

        int count = 1;
        for (ReviewStruct rs : mData) {
            if (rs.showed) {
                if (!sb.isEmpty()) {
                    String last = sb.getLast();
                    sb.removeLast();
                    sb.add(last + " (" + count + "条)");
                }

                DateTime time = new DateTime(rs.time);
                time.setZeroSegment(part + 1);
                sb.add(time.toStringTime());
                count = 1;
            } else {
                count++;
            }
        }
        if (!sb.isEmpty()) {
            String last = sb.getLast();
            sb.removeLast();
            sb.add(last + " (" + count + "条)");
        }
        return sb;
    }

    public class AdapterSort1 extends AdapterSort {
        Context            context;
        LinkedList<String> data;

        public AdapterSort1(Context context, LinkedList<String> data) {
            super(context, null);
            this.context = context;
            this.data = data;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View inflate = LayoutInflater.from(context).inflate(R.layout.activity_sort_item1, viewGroup, false);
            return new MyHolder(inflate);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int posi) {
            MyHolder holder = (MyHolder) viewHolder;

            holder.region.setText(data.get(posi));
            holder.index.setText((posi + 1) + ".");
        }

        @Override
        public int getItemCount() {
            if (data == null) return 0;
            return data.size();
        }

        class MyHolder extends RecyclerView.ViewHolder {
            public        View     view;
            private final TextView index;
            private final TextView region;

            public MyHolder(@NonNull View itemView) {
                super(itemView);
                view = itemView;

                index = view.findViewById(R.id.item_sort_textView_index);
                region = view.findViewById(R.id.item_sort_textView_region);
            }
        }

    }
}
