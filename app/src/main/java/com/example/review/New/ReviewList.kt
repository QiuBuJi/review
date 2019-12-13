package com.example.review.New

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

open class ReviewList : ArrayStoreList<ReviewStruct>() {
    //关联指定的库
    fun connectOf(libraries: LibraryList) {
        for (index in this.indices) {
            val rs = get(index)
            val index = index * 2
            rs.show = libraries[index + 1]
            rs.match = libraries[index]
        }
    }

    //取字节流
    @Throws(IOException::class)
    override fun toBytes(dos: DataOutputStream) {
        dos.writeInt(size)
        for (rl in this) {
            val bytes = rl.toBytes()
            dos.writeInt(bytes.size)
            dos.write(bytes)
        }
    }

    //写字节流
    @Throws(IOException::class)
    override fun loadWith(dis: DataInputStream) {
        val size = dis.readInt()
        for (i in 0 until size) {
            val len = dis.readInt()
            val bytes = ByteArray(len)
            dis.read(bytes)
            val rs = ReviewStruct(bytes)
            add(rs)
        }
    }
}