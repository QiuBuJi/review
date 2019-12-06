package com.example.review.New

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.*

class SaveCategoryStruct : StoreData() {
    var names = LinkedList<String>()
    @Throws(IOException::class)
    override fun toBytes(dos: DataOutputStream) {
        dos.writeInt(names.size)
        for (str in names) dos.writeUTF(str)
    }

    @Throws(IOException::class)
    override fun loadWith(dis: DataInputStream) {
        val size = dis.readInt()
        for (i in 0 until size) {
            val str = dis.readUTF()
            names.add(str)
        }
    }

    init {
        names.add("默认分组")
    }
}