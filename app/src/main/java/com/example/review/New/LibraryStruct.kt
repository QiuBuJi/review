package com.example.review.New

import com.example.review.DataStructureFile.ReviewData
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

class LibraryStruct : StoreData {
    var text = ""
        set(text) = if (refer > 0) field = "" else field = text
        get() {
            if (refer > 0) {
                for (ls in ReviewData.mLibraries) if (ls.id == refer) return ls.text
                return "**引用丢失！**"
            }
            return field
        }

    var type = 0
        get
        set
    var refer = 0
    var id = 0

    constructor() {}
    constructor(ls: LibraryStruct) {
        text = ls.text
        type = ls.type
    }

    fun copyOf(ls: LibraryStruct) { //是引用的话，则不能拷贝
        if (refer > 0) return
        //拷贝数据
        text = ls.text
        type = ls.type
    }

    constructor(text: String, type: Int) {
        this.text = text
        this.type = type
    }

    constructor(rawBytes: ByteArray) {
        try {
            loadWith(rawBytes)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun setIdAuto(value: Int) {
        if (ReviewData.Companion.mLibraries != null) {
            var maxId = 0
            for (ls in ReviewData.Companion.mLibraries) if (ls.id > maxId) maxId = ls.id
            id = maxId + 1 + value
        }
    }

    fun setIdAuto(library: LibraryList) {
        var maxId = 0
        for (ls in library) if (ls.id > maxId) maxId = ls.id
        id = maxId + 1
    }

    @Throws(IOException::class)
    override fun toBytes(dos: DataOutputStream) {
        dos.writeUTF(text)
        dos.writeInt(type)
        dos.writeInt(id)
        dos.writeInt(refer)
    }

    @Throws(IOException::class)
    override fun loadWith(dis: DataInputStream) {
        text = dis.readUTF()
        type = dis.readInt()
        id = dis.readInt()
        refer = dis.readInt()
    }

    override fun toString(): String = if (refer > 0) String.format("ID:%d", id) else String.format("type[%d]： %d", type, text)
}