package com.example.review.DataStructureFile;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.LinkedList;

public class WordExplain {
    public String             category = "";
    public LinkedList<String> explains = new LinkedList<String>();

    public boolean ediable = true;

    public WordExplain() {
    }

    public WordExplain(WordExplain we) {
        category = we.category;
        explains = new LinkedList<>(we.explains);
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String explain : explains) {
            sb.append(explain).append("；");
        }
        return category + sb.toString();
    }
}

