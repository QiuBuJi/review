package com.example.review.DataStructureFile

class ElementCategory {
    var txt: String? = null
    var category: Category? = null

    constructor() {}
    constructor(txt: String?, category: Category?) {
        this.txt = txt
        this.category = category
    }

    override fun toString(): String {
        return txt + " - ( " + category.toString() + " )"
    }

    enum class Category {
        correct, malposition, unnecesary, missing
    }
}