package com.example.review.New;

public class CountList {
    public int   corrCount;
    public int   errCount;
    public int   totalNum;
    public float corrRate;
    public int   needCorrNum;

    public CountList() {

    }

    public CountList(int corrCount, int errCount, int totalNum) {
        this.corrCount = corrCount;
        this.errCount = errCount;
        this.totalNum = totalNum;
    }

    public void copyOf(CountList countList) {
        this.corrCount = countList.corrCount;
        this.errCount = countList.errCount;
        this.totalNum = countList.totalNum;
    }
}
