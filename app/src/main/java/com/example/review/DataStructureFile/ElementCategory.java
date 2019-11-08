package com.example.review.DataStructureFile;


import android.support.annotation.NonNull;

public class ElementCategory {
    public String   txt;
    public Category category;

    public ElementCategory() {
    }

    public ElementCategory(String txt, Category category) {
        this.txt = txt;
        this.category = category;
    }

    @NonNull
    @Override
    public String toString() {
        return txt + " - ( " + category.toString() + " )";
    }

    public enum Category {
        correct,
        malposition,
        unnecesary,
        missing
    }
}
