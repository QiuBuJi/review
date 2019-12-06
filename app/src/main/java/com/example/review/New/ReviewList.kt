package com.example.review.New

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

open class ReviewList : ArrayStoreList<ReviewStruct>() {
    //关联指定的库
    fun connectOf(libraries: LibraryList) {
        for (i in this.indices) {
            val rs = get(i)
            val index = i * 2
            rs.show = libraries[index + 1]
            rs.match = libraries[index]
        }
    }

    //取字节流
    @Throws(IOException::class)
    override fun toBytes(dos: DataOutputStream) {
        dos.writeInt(size)
        for (libraryStruct in this) {
            val bytes = libraryStruct.toBytes()
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
            val libraryStruct = ReviewStruct(bytes)
            add(libraryStruct)
        }
    }
}