package com.example.review.DataStructureFile

import android.os.Handler
import android.os.Handler.Callback
import com.example.review.New.LibraryList
import com.example.review.New.LibraryStruct
import com.example.review.New.ReviewList
import com.example.review.New.ReviewStruct
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

class ReviewData : ReviewList {
    private var fileLibrary: File? = null
    private var fileNexus: File? = null

    var mInactivate = LinkedList<ReviewStruct>()
    var mActivate = LinkedList<ReviewStruct>()

    private var timer: Timer? = null

    val library = LibraryList()

    private val UPDATE_TO_AVALABLE = 0
    private val DATA_SAVE = 1
    private val DATA_SAVE_COMPLETE = 2

    constructor() {}
    constructor(path: File) {
        loadDataOf(path, library, this)
    }

    private val handler = Handler(Callback { msg ->
        when (msg.what) {
            UPDATE_TO_AVALABLE -> updateToAvailable(DateTime.getCurrentTime())
            DATA_SAVE -> stateSave?.onSaveCalled()
            DATA_SAVE_COMPLETE -> stateSave?.onSaveComplete()
        }
        false
    })

    fun setDefaultPath(fileLibrary: File?, fileNexus: File?) {
        this.fileLibrary = fileLibrary
        this.fileNexus = fileNexus
    }

    /**
     * 开启一条线程，保存数据
     */
    fun save() { //在数据有变动时，才会执行保存程序
        if (dataChangeCount == 0) return
        dataChangeCount = 0
        if (size == 0) return//没数据，不保存

        Thread(Runnable {
            saveDataTo(fileNexus!!)
            if (librarySaveMark) {
                library.save(fileLibrary!!)
                librarySaveMark = false
            }
        }).start()
    }

    fun setOnSave(stateSave: StateSave?) {
        this.stateSave = stateSave
    }

    private var stateSave: StateSave? = null

    interface StateSave {
        fun onSaveCalled()
        fun onSaveComplete()
    }

    @Throws(IOException::class)
    fun read() {
        if (fileNexus == null || fileLibrary == null) return

        read(fileNexus!!)
        library.read(fileLibrary!!)
        val sizeNexus = size * 2
        val sizeLibrary = library.size
        require(sizeNexus == sizeLibrary) { "数据不一致 sizeNexus*2 = $sizeNexus sizeLibrary = $sizeLibrary" }
        connectOf(library)
        mLibraries = library
    }

    /**
     * 保存数据，到外部存储器上。
     *
     * @param path 文件路径
     */
    fun saveDataTo(path: File) {
        handler.sendEmptyMessage(DATA_SAVE)
        super.save(path)
        handler.sendEmptyMessage(DATA_SAVE_COMPLETE)
    }

    fun loadDataOf(path: File) {
        library.clear()
        loadDataOf(path, library, this)
        var i = 0
        while (i < size) {
            var index = i * 2
            val match = LibraryStruct(library[index])
            val show = LibraryStruct(library[++index])
            val rs = ReviewStruct()
            library.add(++index, match)
            library.add(index, show)
            rs.match = show
            rs.show = match
            add(++i, rs)
            i++
        }
        connectOf(library)
        library.save(fileLibrary!!)
        save()
    }

    interface ProgressListener {
        fun onProgress(total: Int, posi: Int)
    }

    var progressListener: ProgressListener? = null
        set

    /**
     * 转换到本土数据
     *
     * @param path 需要转换为本土数据的文件路径
     * @return LinkedList 转换好的本土数据
     */
    fun loadDataOf(path: File, librarySet: LibraryList, reviewSet: ReviewList) {
        clear() //清空原有数据
        try {
            val fis = FileInputStream(path)
            val bytes = ByteArray(path.length().toInt())
            fis.read(bytes) //读入全部内容
            fis.close()
            var strRaw = String(bytes)
            strRaw = Pattern.compile("\r").matcher(strRaw).replaceAll("")
            val sb1 = StringBuilder(strRaw)
            sb1.delete(0, sb1.indexOf("@") + 1)
            val split = Pattern.compile("@").split(sb1) //先以'@'作为分格符号，分组内容
            var posi = 0

            for (str in split) {
                val part = Pattern.compile("#").split(str) //后以'#'作为分格符号，分组内容
                val strWord = part[0].trim { it <= ' ' }
                var strExplain: String? = part[1].trim { it <= ' ' }
                strExplain = Pattern.compile("[;,，]").matcher(strExplain).replaceAll("；")
                librarySet.add(LibraryStruct(strWord, 1))
                librarySet.add(LibraryStruct(strExplain, 2))
                val level = Integer.valueOf(part[5].trim { it <= ' ' })
                val reviewStruct = ReviewStruct(level)
                val strLog = Pattern.compile("\\r\\n|\\n").split(part[2])

                for (txt in strLog) {
                    var txt = txt
                    txt = txt.trim { it <= ' ' }
                    if (txt.length < 25) continue
                    val dt = toDateTime(txt)
                    reviewStruct.logs.add(dt!!.toBytes())
                }
                reviewSet.add(reviewStruct)
                progressListener?.onProgress(split.size, ++posi)
            }
        } catch (ignored: FileNotFoundException) {
        } catch (ignored: IOException) {
        }
    }

    fun toDateTime(strLog: String?): DateTime? {
        val split = Pattern.compile("\\s").split(strLog)
        var text = split[0]
        val i = text.indexOf(".")
        text = text.substring(i + 1)
        val split1 = Pattern.compile("/").split(text)
        var split2: Array<String?>? = null
        try {
            split2 = Pattern.compile(":").split(split[1])
        } catch (e: Exception) {
            e.printStackTrace()
        }
        var time: DateTime? = null
        try {
            time = DateTime(
                    Integer.valueOf(split1[0]),
                    Integer.valueOf(split1[1]),
                    Integer.valueOf(split1[2]),
                    Integer.valueOf(split2!![0]),
                    Integer.valueOf(split2[1]),
                    Integer.valueOf(split2[2]))
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
        val state = !split[2].contains("F")

        val second = time!!.second
        if (!state) time.second = -second
        return time
    }

    /**
     * 从总数据中取要复习的数据
     */
    fun retrieveInvaluable() {
        mInactivate.clear()
        mActivate.clear()
        val dateTime = DateTime()

        for (rs in this) { //分离出不符合条件的数据
            //导入以前的复习时间
            if (rs.joined && rs.time > dateTime) {
                try {
//                    rs.time = DateTime(rs.logs.last)//todo 忘记是解决什么问题了
                    sortAddToInactivate(rs)//从小到大的时间排序
                } catch (e: Exception) {
                    e.printStackTrace()
                    //没有log数据
                }
            }
        }
    }

    internal fun sortToAvailable(reviewStruct: ReviewStruct): Boolean {
        if (mActivate.isNotEmpty()) {
            val matchType = reviewStruct.match.type

            for (i in mActivate.indices.reversed()) {
                val type = mActivate[i].match.type

                if (matchType == type) {
                    mActivate.add(i + 1, reviewStruct)
                    return true
                }
            }
        }
        return false
    }

    /**
     * mInactivate更新到mAvalable中
     */
    private fun updateToAvailable(currTime: DateTime) {
        var count = 0
        while (mInactivate.isNotEmpty()) {
            val rs = mInactivate.first

            if (currTime > rs.time) {
                if (!sortToAvailable(rs)) mActivate.add(rs)
                mInactivate.removeFirst()
                availableUpdate?.onUpdatingToAvailable(rs)
                count++
            } else break
        }
        if (count > 0) {
            availableUpdate?.onUpdateToAvailableComplete(count)
            availableComplete?.onAvailableComplete()
        } else {
            availableUpdate?.onUpdatedNoChange()
        }
    }

    private var availableComplete: AvailableComplete? = null
    fun setOnAvailableComplete(availablecomplete: AvailableComplete?) {
        availableComplete = availablecomplete
    }

    interface AvailableComplete {
        fun onAvailableComplete()
    }

    fun setOnAvailableUpdate(availableUpdate: AvailableUpdate?) {
        this.availableUpdate = availableUpdate
    }

    private var availableUpdate: AvailableUpdate? = null

    interface AvailableUpdate {
        fun onUpdateToAvailableComplete(count: Int)
        fun onUpdatingToAvailable(reviewStruct: ReviewStruct?)
        fun onUpdatedNoChange()
    }

    /**
     * 自动动更新到mAvalable中
     */
    fun updateToAvailableAuto(period: Int) {
        timer?.cancel()
        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                handler.sendEmptyMessage(UPDATE_TO_AVALABLE)
            }
        }, 0, period.toLong())
    }

    fun stopTimer() = timer?.cancel()

    private var dataChangeCount = 0
    /**
     * mAvalable更新到mInactivate中
     */
    fun updateInavailable_AddLevel(rs: ReviewStruct) {
        dataChangeCount++
        var level = rs.level
        mActivate.remove(rs)
        level++ //增加水平
        rs.level = level

        //低于1级别的计划，快速再次复习加深印象
        if (level <= 0) {
            try {
                mActivate.add(2, rs)
            } catch (e: Exception) {
                mActivate.addLast(rs)
            }
            return
        }

        rs.time = DateTime.getCurrentTime() //取当前时间
        rs.time.add(reviewRegions[level]) //todo level下标越界怎么办？

        //正常复习
        try {
            sortAddToInactivate(rs)
        } catch (e: IllegalArgumentException) { //todo 如果水平为13，则复习完成，取消复习计划。
            removeFromInavailable_Available(rs)
        }
    }

    /**
     * 排序加入到mInactivate中
     */
    fun sortAddToInactivate(rsNew: ReviewStruct) {
        if (mActivate.contains(rsNew)) return
        mInactivate.remove(rsNew) //删除旧数据

        mInactivate.forEachIndexed { index, rs ->
            if (rs.time > rsNew.time) {
                mInactivate.add(index, rsNew)
                return
            }
        }

        //添加至末尾
        mInactivate.add(rsNew)
    }

    fun removeFromInavailable_Available(rs: ReviewStruct) {
        rs.joined = false
        mInactivate.remove(rs)
        mActivate.remove(rs)
    }

    fun saveLibrary() {
        Thread(Runnable { library.save(fileLibrary!!) }).start()
    }

    fun addLibrary(posi: Int, match: LibraryStruct, show: LibraryStruct) {
        library.add(posi, show)
        library.add(posi, match)
        dataChangeCount++
    }

    fun removeLibraryItem(posi: Int) {
        library.removeAt(posi)
        library.removeAt(posi)
        //        remove(posi);
        dataChangeCount++
    }

    fun removeLibraryItem(rs: ReviewStruct) {
        var count = 0
        var i = 0
        while (i < library.size) {
            val ls = library[i]
            if (ls === rs.match || ls === rs.show) {
                library.removeAt(i)
                i--
                count++
            }
            i++
        }
        check(count == 2)
        //        library.remove(rs.match);
//        library.remove(rs.show);
        dataChangeCount++
    }

    internal var librarySaveMark = false
    /**
     * 调用此函数才能保存library内的数据到本地
     */
    fun setLibrarySaveMark() {
        librarySaveMark = true
    }

    companion object {
        var reviewRegions = arrayOf(
                DateTime("0秒"),  // 0级
//              DateTime("2秒"),      // 1级，插入mActivate第三个位置-----------
                DateTime("10秒"),  // 2级
                DateTime("5分"),  // 3级
                DateTime("15分"),  // 4级
                DateTime("3时"),  // 5级
                DateTime("12时"),  // 6级
                DateTime("1日"),  // 7级
                DateTime("3日"),  // 8级
                DateTime("5日"),  // 9级
                DateTime("10日"),  //10级
                DateTime("1月"),  //11级
                DateTime("3月"),  //12级
                DateTime("7月"),  //13级
                DateTime("1年"),  //14级
                DateTime("3年")) //15级
        var mLibraries: LibraryList = LibraryList()
    }
}