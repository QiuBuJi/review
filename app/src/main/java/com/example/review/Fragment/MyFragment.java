package com.example.review.Fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.review.Adapter.MyAdapter;
import com.example.review.MainActivity;
import com.example.review.New.LibrarySet;
import com.example.review.New.ReviewSet;
import com.example.review.New.ReviewStruct;
import com.example.review.R;

import java.io.File;

public class MyFragment extends Fragment {

    private View       mainView;
    private Context    context;
    private LibrarySet librarySet;
    private ReviewSet  reviewSet;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View mainView = inflater.mainView(R.layout.activity_fragment_backward, container, false);
        mainView = inflater.inflate(R.layout.activity_list_temp, container, false);

        RecyclerView recyclerView = mainView.findViewById(R.id.list_recycler_list);
        TextView     textView     = mainView.findViewById(R.id.text);
        context = getContext();

        librarySet = new LibrarySet();
        reviewSet = new ReviewSet();

        long millis = System.currentTimeMillis();
        librarySet.readOf(new File(MainActivity.pathApp, "library.lib"));
        long millis1 = System.currentTimeMillis();
        millis = millis1 - millis;

        reviewSet.readOf(new File(MainActivity.pathApp, "nexus.lib"));
        long millis2 = System.currentTimeMillis();
        millis1 = millis2 - millis1;

        reviewSet.connectOf(librarySet);
        long millis3 = System.currentTimeMillis();
        millis2 = millis3 - millis2;

        textView.append("\nmillis :" + millis);
        textView.append("\nmillis1:" + millis1);
        textView.append("\nmillis2:" + millis2);

        Toast.makeText(context, "millis3-millis:" + (millis3 - millis), Toast.LENGTH_SHORT).show();

//        recyclerView.setAdapter(new MyAdapter(context, reviewSet));
//        recyclerView.setLayoutManager(new GridLayoutManager(context, 5));
//        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(5, StaggeredGridLayoutManager.VERTICAL));
//        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
//        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL));

        return mainView;
    }
}

