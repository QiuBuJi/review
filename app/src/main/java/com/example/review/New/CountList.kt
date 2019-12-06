package com.example.review.New

class CountList {
    var corrCount = 0
    var errCount = 0
    var totalNum = 0
    var corrRate = 0f
    var needCorrNum = 0

    constructor() {}
    constructor(corrCount: Int, errCount: Int, totalNum: Int) {
        this.corrCount = corrCount
        this.errCount = errCount
        this.totalNum = totalNum
    }

    fun copyOf(countList: CountList) {
        corrCount = countList.corrCount
        errCount = countList.errCount
        totalNum = countList.totalNum
    }
}