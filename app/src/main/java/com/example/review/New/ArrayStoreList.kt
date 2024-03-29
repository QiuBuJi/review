package com.example.review.New

import java.io.*

abstract class ArrayStoreList<E> : ArrayList<E>(), SaveDataInterface {
    /**
     * 把内部数据，转换为字节流
     */
    override fun toBytes(): ByteArray? {
        val out = ByteArrayOutputStream()
        val dos = DataOutputStream(out)
        var rawBytes: ByteArray? = null
        try {
            toBytes(dos)
            rawBytes = out.toByteArray()
            dos.close()
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return rawBytes
    }

    /**
     * 设置要转换为字节流的数据
     *
     * @param dos 通过dos把数据输出
     */
    @Throws(IOException::class)
    abstract override fun toBytes(dos: DataOutputStream)

    /**
     * 用字节流，装载数据
     *
     * @param rawBytes 字节流
     */
    @Throws(IOException::class)
    override fun loadWith(rawBytes: ByteArray) {
        val `in` = ByteArrayInputStream(rawBytes)
        val dis = DataInputStream(`in`)
        loadWith(dis)
        dis.close()
        `in`.close()
    }

    /**
     * 把从字节流读取出来的数据，安需求设置到内部数据中
     *
     * @param dis 保存数据的参数
     */
    @Throws(IOException::class)
    abstract override fun loadWith(dis: DataInputStream)

    /**
     * 保存数据到该路径
     *
     * @param path 路径
     */
    override fun save(path: File) {
        try {
            val out = FileOutputStream(path)
            val bytes = toBytes()
            out.write(bytes)
            out.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 从路径读取数据
     *
     * @param path 路径
     */
    @Throws(IOException::class)
    override fun read(path: File) {
        clear()
        val fileIn = FileInputStream(path)
        val bytes = fileIn.readBytes()
        loadWith(bytes)
        fileIn.close()
    }
}