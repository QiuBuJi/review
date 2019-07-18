package com.example.review.New;

import android.view.View;

import java.util.ArrayList;

public interface ItemClickListener {
    void onItemClick(View view, ArrayList<KeyText> data, int posi);
}
