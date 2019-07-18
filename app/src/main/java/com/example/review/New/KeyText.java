package com.example.review.New;

import android.view.View;

public class KeyText {
    public String  text;
    public boolean isCom;

    public int  keyCode = -1;
    public char key     = 1;

    public View view = null;

    public KeyText(String text, boolean isCom) {
        this.text = text;
        this.isCom = isCom;
    }

    public KeyText(String text, boolean isCom, int keyCode) {
        this.text = text;
        this.isCom = isCom;
        this.keyCode = keyCode;
    }

    public KeyText(String text, boolean isCom, int keyCode, char key) {
        this.text = text;
        this.isCom = isCom;
        this.keyCode = keyCode;
        this.key = key;
    }

    public KeyText(String text) {
        this.text = text;
    }
}
