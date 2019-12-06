package com.example.review

import com.example.review.New.ArrayStoreList
import com.example.review.SortLib.SortStru
import java.io.*
import java.util.*
import kotlin.collections.ArrayList

class SortLib : ArrayStoreList<SortStru> {
    constructor()

    constructor(src: String) {
        decode(src)
    }

    constructor(src: File) {
        try {
            read(src)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun decode(src: String) {
        val sorts = src.split("\n").toTypedArray()
        for (sortRaw in sorts) {
            if (!sortRaw.isEmpty()) add(SortStru(sortRaw))
        }
    }

    fun getElementInSort(sort: String): List<String> {
        for (item in this) {
            if (item.matchSort(sort)) { //防止多次引用
                if (item.inUse) return ArrayList()
                item.inUse = true
                val elements = item.getElements()
                val temp = ArrayList<String>()
                val refer = ArrayList<String>()
                //把引用的元素列举出来
                for (element in elements) {
                    var element = element
                    if (element.contains("@")) {
                        refer.add(element)
                        element = element.substring(1)
                        val elementInSort = getElementInSort(element)
                        temp.addAll(elementInSort!!)
                    }
                }
                elements.addAll(temp)
                elements.removeAll(refer)
                item.inUse = false
                return elements
            }
        }
        return ArrayList()
    }

    override fun toString(): String {
        return toString(false)
    }

    fun toString(listAll: Boolean): String {
        val sb = StringBuilder()
        for (sortStru in this) sb.append("\n" + sortStru.toString(if (listAll) this else null))
        sb.replace(0, 1, "")
        return sb.toString()
    }

    inner class SortStru {
        var name: String = ""
        private var elements: MutableList<String> = LinkedList()
        var inUse = false

        constructor(src: String) {
            val split = src.split("[：:]").toTypedArray()
            name = split[0]
            if (split.size == 2) {
                val elements = split[1].split("[、,]").toTypedArray()
                this.elements.addAll(Arrays.asList(*elements))
            }
        }

        constructor(name: String, elements: MutableList<String>) {
            this.name = name
            setElements(elements)
        }

        fun setElements(elements: MutableList<String>) {
            this.elements = elements
        }

        fun getElements(): MutableList<String> {
            return ArrayList(elements)
        }

        fun matchSort(sortName: String?): Boolean {
            return sortName == name
        }

        fun getElements(sortStrus: SortLib): List<String>? {
            return sortStrus.getElementInSort(name)
        }

        override fun toString(): String {
            return toString(null)
        }

        fun toString(sortStrus: SortLib?): String {
            val sb = StringBuffer()
            val elements = sortStrus?.let { getElements(it) } ?: getElements()
            for (element in elements) sb.append(element).append('、')
            val length = sb.length
            sb.replace(length - 1, length, "")
            return String.format("%s：%s", name, sb)
        }
    }

    @Throws(IOException::class)
    override fun toBytes(dos: DataOutputStream) {
        val sb = StringBuffer()
        for (sortStru in this) {
            val str = sortStru.toString()
            sb.append("\n").append(str)
        }
        sb.replace(0, 1, "")
        val value = sb.toString()
        dos.write(value.toByteArray())
    }

    @Throws(IOException::class)
    override fun loadWith(dis: DataInputStream) {
        val buffer = ByteArray(1024)
        val baos = ByteArrayOutputStream()
        var length: Int
        while (dis.read(buffer).also { length = it } != -1) baos.write(buffer, 0, length)
        decode(String(baos.toByteArray()))
    }
}