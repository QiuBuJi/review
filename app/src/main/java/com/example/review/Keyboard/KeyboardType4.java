package com.example.review.Keyboard;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.review.New.KeyText;
import com.example.review.New.ReviewStruct;

import java.util.ArrayList;

public class KeyboardType4 extends Keyboard {
    public KeyboardType4(Context context, RecyclerView keyboardView, ConstraintLayout show, EditText input, ReviewStruct reviewStruct) {
        super(context, keyboardView, show, input, reviewStruct);
    }

    @Override
    void init() {
        span = 3;
        input.setInputType(InputType.TYPE_NULL);
    }

    @Override
    public void setLightAnimation(boolean lightUp, int duration) {

    }

    @Override
    public void refresh() {


    }

    @Override
    ArrayList<KeyText> getLayout() {
        ArrayList<KeyText> data = new ArrayList<>();
        data.add(new KeyText("a"));
        data.add(new KeyText("b"));
        data.add(new KeyText("c"));
        return data;
    }

    @Override
    public void onItemClick(View view, ArrayList<KeyText> data, int posi) {
        super.onItemClick(view, data, posi);
    }

    @Override
    public boolean keyDown(int keyCode, char key, int posi) {
        return false;
    }
}
