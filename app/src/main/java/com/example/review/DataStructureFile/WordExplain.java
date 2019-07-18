package com.example.review.DataStructureFile;

import android.support.annotation.NonNull;

import java.util.ArrayList;

public class WordExplain {
    public String            category = "";
    public ArrayList<String> explains = new ArrayList<>();

    public boolean ediable = true;

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

