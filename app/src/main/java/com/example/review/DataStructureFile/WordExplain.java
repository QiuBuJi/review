package com.example.review.DataStructureFile;

import java.util.ArrayList;

public class WordExplain {
    public String            category = "";
    public ArrayList<String> explains = new ArrayList<>();

    public boolean ediable = true;

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (String explain : explains) {
            sb.append(explain + "；");
        }
        return category + sb.toString();
    }
}

