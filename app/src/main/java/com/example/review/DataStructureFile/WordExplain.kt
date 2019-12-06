package com.example.review.DataStructureFile

import java.util.*

class WordExplain {
    var category = ""
    var explains = LinkedList<String>()
    var ediable = true

    constructor() {}
    constructor(we: WordExplain) {
        category = we.category
        explains = LinkedList(we.explains)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        for (explain in explains) {
            sb.append(explain).append("；")
        }
        return category + sb.toString()
    }
}