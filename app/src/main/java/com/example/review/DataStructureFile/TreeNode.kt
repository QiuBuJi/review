package com.example.review.DataStructureFile

import java.util.*

class TreeNode<TYPE_VALUE, TYPE_INDEX> {
    constructor() {}
    constructor(value: TYPE_VALUE) {
        this.value = value
    }

    var value: TYPE_VALUE? = null
    var index: TYPE_INDEX? = null
    var node: ArrayList<TreeNode<TYPE_VALUE, TYPE_INDEX>>? = null
    override fun toString(): String {
        return value.toString()
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val treeNode = o as TreeNode<*, *>
        return value == treeNode.value
    }

    override fun hashCode(): Int {
        return Objects.hash(value)
    }
}