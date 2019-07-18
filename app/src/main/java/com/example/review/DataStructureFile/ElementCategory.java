package com.example.review.DataStructureFile;


public class ElementCategory {
    public String   txt;
    public Category category;

    public ElementCategory() {
    }

    public ElementCategory(String txt, Category category) {
        this.txt = txt;
        this.category = category;
    }

    public enum Category {
        correct,
        malposition,
        unnecesary,
        missing
    }
}
