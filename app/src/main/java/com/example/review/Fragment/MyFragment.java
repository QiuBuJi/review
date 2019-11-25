package com.example.review.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.review.Activity.MainActivity;
import com.example.review.New.LibraryList;
import com.example.review.New.ReviewList;
import com.example.review.R;

import java.io.File;
import java.io.IOException;

public class MyFragment extends Fragment {

    private View        mainView;
    private Context     context;
    private LibraryList librarySet;
    private ReviewList  reviewSet;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View mainView = inflater.mainView(R.layout.activity_fragment_backward, container, false);
        mainView = inflater.inflate(R.layout.activity_list_temp, container, false);

        TextView     textView     = mainView.findViewById(R.id.text);
        context = getContext();

        librarySet = new LibraryList();
        reviewSet = new ReviewList();

        long millis = System.currentTimeMillis();
        try {
            librarySet.read(new File(MainActivity.pathApp, "library.lib"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        long millis1 = System.currentTimeMillis();
        millis = millis1 - millis;

        try {
            reviewSet.read(new File(MainActivity.pathApp, "nexus.lib"));
        } catch (IOException e) {
            e.printStackTrace();
        }
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

