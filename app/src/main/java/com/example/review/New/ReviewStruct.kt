package com.example.review.New

import com.example.review.DataStructureFile.DateTime
import com.example.review.DataStructureFile.WordExplain
import com.example.review.Keyboard.KeyboardType3.TextCom
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

class ReviewStruct : StoreData {
    lateinit var show: LibraryStruct
    lateinit var match: LibraryStruct

    private var previousID = 0
    private var classType = 0
    var level = 0
    var posi = 0
    var viewCount = 0
    var joined = false
    var selected = false
    var showed = false
    var time = DateTime(0, 0, 0, 0, 0, 0)
    var logs = LinkedList<ByteArray>()
    var sort = ""

    //**********************************************************************************************
    constructor() {}

    constructor(rs: ReviewStruct) {
        show = rs.show
        showed = rs.showed
        time = rs.time
        match = rs.match
        joined = rs.joined
        logs = rs.logs
        posi = rs.posi
        level = rs.level
        classType = rs.classType
        previousID = rs.previousID
    }

    constructor(level: Int) {
        this.level = level
    }

    constructor(rawBytes: ByteArray) {
        loadWith(rawBytes)
    }

    //完成率，到这个阈值后判断为正确
    internal var corRate = 0.6f

    fun matching(wesInput: ArrayList<WordExplain>, countList: CountList): Boolean {
        val wesRight = matchWordExplains
        var corrCount = 0
        var errCount = 0
        var total = 0

        for (i in wesRight.indices) {
            val weRight = wesRight[i]
            var weInput: WordExplain? = null

            //挑出和matchWE.category相同的条目到we中
            for (weTemp in wesInput) {
                val trim = weTemp.category.trim { it <= ' ' }
                if (weRight.category == trim) {
                    weInput = WordExplain(weTemp)
                    break
                }
            }

            //统计总数
            total += weRight.explains.size
            assert(weInput != null)
            val wrongInput = ArrayList(weInput!!.explains)
            var k = 0
            while (k < wrongInput.size) {
                val word = wrongInput[k]
                val contains = weRight.explains.contains(word)

                //移除正确词语，留下不正确的
                if (contains) {
                    wrongInput.removeAt(k--)
                    corrCount++
                }
                k++
            }
            //累计错误数
            errCount += wrongInput.size
        }
        var needCorrectNum = (total * corRate).toInt()
        if (total <= 2) needCorrectNum = total
        countList.corrCount = corrCount
        countList.errCount = errCount
        countList.totalNum = total
        countList.corrRate = corRate
        countList.needCorrNum = needCorrectNum
        return errCount == 0 && corrCount >= needCorrectNum
    }

    fun matchingType3(tc: ArrayList<TextCom>, candidate: ArrayList<String>, countList: CountList): Boolean {
        var corrCount = 0
        var errCount = 0
        var total = 0
        val tcs = ArrayList<TextCom>()
        for (textCom in tc) if (textCom.isCandidate) tcs.add(textCom)
        for (i in tcs.indices) {
            val txt1 = candidate[i]
            val textCom = tcs[i]
            val equals1 = txt1 == textCom.text
            if (equals1) {
                corrCount++
            } else {
                errCount++
                textCom.isStrike = true
            }
            total++
        }
        val needCorrectNum = total
        countList.corrCount = corrCount
        countList.errCount = errCount
        countList.totalNum = total
        countList.corrRate = 1f
        countList.needCorrNum = needCorrectNum
        return errCount == 0 && corrCount >= needCorrectNum
    }

    fun matching(text: String): Boolean {
        return match.text == text
    }

    val frame: ArrayList<WordExplain>
        get() {
            val wordExplains = matchWordExplains
            for (we in wordExplains) we.explains.clear()
            return wordExplains
        }

    val matchWordExplains: ArrayList<WordExplain>
        get() {
            val match = match.text
            return getMatchWordExplains(match)
        }

    fun addData(show: LibraryStruct, match: LibraryStruct) {
        this.show = show
        this.match = match
    }

    fun resetLevel() {
        level = -1
    }

    @Throws(IOException::class)
    override fun toBytes(dos: DataOutputStream) {
        dos.writeInt(previousID)
        dos.writeByte(classType)
        dos.writeByte(level)
        //        dos.writeByte(viewCount);
        dos.writeBoolean(joined)
        dos.writeBoolean(selected)
        dos.writeBoolean(showed)
        dos.write(time.toBytes())

        dos.writeInt(logs.size)
        for (log in logs) dos.write(log)
    }


    @Throws(IOException::class)
    override fun loadWith(dis: DataInputStream) {
        previousID = dis.readInt()
        classType = dis.readByte().toInt()
        level = dis.readByte().toInt()
        //        viewCount = dis.readByte();
        joined = dis.readBoolean()
        selected = dis.readBoolean()
        showed = dis.readBoolean()
        val length = 7
        var bytes = ByteArray(length)
        dis.read(bytes)
        time = DateTime(bytes)

        val size = dis.readInt()
        for (i in 0 until size) {
            bytes = ByteArray(length)//do not simplify it
            dis.read(bytes)
            logs.add(bytes)
        }
    }

    override fun toString(): String = time.toString(true) + "  单词：${match.text} 解释：${show.text}"

    companion object {
        /**
         * 取字符串text中匹配regex的所有字符
         *
         * @param text  要从中检索的字符串
         * @param regex 要匹配的正则表达式
         * @return 返回所有匹配的字符串
         */
        fun toMatchList(text: String, regex: String?): LinkedList<String> {
            val mat = Pattern.compile(regex).matcher(text)
            val matchedList = LinkedList<String>()

            //寻找匹配regex的字符串
            while (mat.find()) {
                val start = mat.start()
                val end = mat.end()
                val strMatch = text.substring(start, end)
                matchedList.add(strMatch)
            }
            return matchedList
        }

        fun getMatchWordExplains(text: String): ArrayList<WordExplain> {
            val item = ArrayList<WordExplain>()
            val lines = text.split("\n").toTypedArray()

            //把字符串以\n拆分
            for (line in lines) { //去掉空字符串
                var line = line
                if (line != "") {
                    val we = WordExplain()
                    val index = line.indexOf('.') + 1

                    //找不到‘.’，分类设置为默认的“*.”
                    if (index == -1) we.category = "*." else {
                        we.category = line.substring(0, index)
                        if (we.category == "") we.category = "*."
                        line = line.substring(index)
                    }

                    //把词语分离出来
                    val words = line.split(Regex("[;；，,]")).toTypedArray()
                    for (word in words) if (word != "") we.explains.add(word)
                    item.add(we)
                }
            }
            return item
        }
    }
}