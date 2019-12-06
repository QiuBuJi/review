package com.example.review.New

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

class LibraryList : ArrayStoreList<LibraryStruct>() {
    //取字节流
    @Throws(IOException::class)
    override fun toBytes(dos: DataOutputStream) {
        dos.writeInt(size)
        for (ls in this) {
            val bytes = ls.toBytes()
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
            val libraryStruct = LibraryStruct(bytes)
            add(libraryStruct)
        }
    }
}