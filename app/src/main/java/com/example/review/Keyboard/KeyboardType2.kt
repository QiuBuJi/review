package com.example.review.Keyboard

import android.content.Context
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.view.KeyEvent
import android.widget.EditText
import com.example.review.Activity.MainActivity
import com.example.review.DataStructureFile.WordExplain
import com.example.review.New.KeyText
import com.example.review.New.LibraryList
import com.example.review.New.ReviewStruct
import com.example.review.Setting
import com.example.review.Util.SpanUtil
import com.example.review.Util.Speech
import java.util.*
import kotlin.collections.ArrayList

class KeyboardType2(context: Context, keyboardView: RecyclerView, container: ConstraintLayout, input: EditText, reviewStruct: ReviewStruct)
    : Keyboard(context, keyboardView, container, input, reviewStruct) {
    var frameInput: ArrayList<WordExplain>? = null
    var frameRight: ArrayList<WordExplain>? = null
    var handleInterface: HandleInterfaceType2? = null

    override fun init() {
        input.isEnabled = false
        input.setText("")
        input.hint = ""
        input.showSoftInputOnFocus = false
        input.inputType = InputType.TYPE_NULL
        frameInput = rs.frame
        frameRight = rs.matchWordExplains
        makeRandom(frameInput!!)
        span = 6
        Speech.play_Baidu(rs.show.text)
        handleInterface = HandleInterfaceType2(context, container, frameInput!!, frameRight!!)
        val camPlay = Setting.getBoolean("开启朗读")
        //单词提示；没开启朗读，则显示单词
        val spanBuilder = SpanUtil.create()
        if (rs.level <= 3 || !camPlay) showWord() else {
            spanBuilder.addForeColorSection("--- ", Color.LTGRAY)
                    .addForeColorSection("?", Color.RED)
                    .addForeColorSection(" ---", Color.LTGRAY)
                    .showIn(handleInterface!!.windowExplainHolder.explainTitle)
            handleInterface!!.windowExplainHolder.explainTitle.setOnClickListener { showWord() }
        }
        handleInterface!!.setLightAnimation(true, 200)
    }

    override fun setLightAnimation(lightUp: Boolean, duration: Int) {
        handleInterface!!.setLightAnimation(lightUp, duration)
    }

    internal fun showWord() {
        val spanBuilder = SpanUtil.create()
        spanBuilder.addForeColorSection("--- ", Color.LTGRAY)
                .addForeColorSection(rs.show.text, Color.BLACK)
                .addForeColorSection(" ---", Color.LTGRAY)
                .showIn(handleInterface!!.windowExplainHolder.explainTitle)
    }

    override fun refresh() {
        handleInterface!!.refresh()
    }

    override fun getLayout(): ArrayList<KeyText> = wideLayout()

    //在库内取出类型为2且不是引用的数据
    //把数据解释取出来
    //收集为单列数据
    //添加类型集合的数据
    //去重复
    //本地词语数超过这个阈值，则增加一行的混淆词语
    //去掉多余的数据
    //键盘索引字符

    //libTemp没数据就要弄点数据了
    private fun wideLayout(): ArrayList<KeyText> {
        val wesNative = rs.matchWordExplains
        val kts = Arrays.asList(
                KeyText(COM_DONE, true, KeyEvent.KEYCODE_ENTER),
                KeyText(COM_UP, true, KeyEvent.KEYCODE_DPAD_UP, 'u'),
                KeyText(COM_DOWN, true, KeyEvent.KEYCODE_DPAD_DOWN, 'd'),
                KeyText(COM_EMPTY, true, KeyEvent.KEYCODE_FORWARD_DEL),
                KeyText(COM_DELETE, true, KeyEvent.KEYCODE_DEL),
                KeyText(COM_DONE, true, KeyEvent.KEYCODE_ENTER))
        val data = ArrayList(kts)
        //libTemp没数据就要弄点数据了
        if (libTemp == null) {
            libTemp = LibraryList()
            val library: LibraryList = MainActivity.data.library
            //在库内取出类型为2且不是引用的数据
            for (lib in library) if (lib.type == 2 && lib.refer == 0) libTemp!!.add(lib)
        }
        var size = libTemp!!.size
        val random = Random(System.currentTimeMillis())
        val wesAdded = ArrayList<WordExplain>()
        //把数据解释取出来
        for (i in 0..4) {
            val ramdomNum = random.nextInt(size)
            val ls = libTemp!![ramdomNum]
            val mwe: ArrayList<WordExplain> = ReviewStruct.getMatchWordExplains(ls.text)
            wesAdded.addAll(mwe)
        }
        //收集为单列数据
        val wordsNative = LinkedList<String>()
        val wordsAdded = LinkedList<String>()
        for (we in wesNative) wordsNative.addAll(we.explains)
        for (we in wesAdded) wordsAdded.addAll(we.explains)

        //添加类型集合的数据
        val category = frameRight!![handleInterface!!.indexOfItem].category.replace(".", "")
        val element: List<String> = MainActivity.sorts!!.getElementInSort(category)
        wordsAdded.addAll(element)

        //去重复
        removeRedundancy(wordsNative)
        removeRedundancy(wordsAdded)
        wordsAdded.removeAll(wordsNative)
        val nativeSize = wordsNative.size
        var num = nativeSize / 6
        val mode = nativeSize % 6
        if (mode > 0) num++
        val nativeRate = wordsNative.size / (num * 6f - 1)
        if (nativeRate >= 0.5) num++ //本地词语数超过这个阈值，则增加一行的混淆词语
        size = wordsNative.size + wordsAdded.size + 1
        size -= num * 6
        //去掉多余的数据
        for (i in 0 until size) {
            if (!wordsAdded.isEmpty()) wordsAdded.removeFirst()
        }
        wordsAdded.addAll(wordsNative)
        makeRandom(wordsAdded)
        sortByCharLen(wordsAdded)
        //键盘索引字符
        var key = 'a'
        for (i in wordsAdded.indices) {
            val str = wordsAdded[i]
            if (key == 'u' || key == 'd') key++
            data.add(KeyText(str, false, 0, key))
            key++
        }
        data.add(KeyText("播放", true, KeyEvent.KEYCODE_SPACE))
        return data
    }

    override fun adapterComplete() {
        super.adapterComplete()
        adapter!!.textSize = 14
    }

    override fun keyDown(keyCode: Int, key: Char, posi: Int): Boolean {
        var keyText: KeyText? = null
        if (posi >= 0) keyText = strData[posi] else {
            for (kt in strData) {
                if (kt.keyCode == keyCode || kt.key == key) {
                    keyText = kt
                    //模拟单击
                    if (kt.view != null) {
                        kt.view?.performClick()
                        kt.view?.isPressed = true
                        kt.view?.isPressed = false
                        return true
                    }
                    break
                }
            }
        }
        if (keyText == null) return false
        if (keyText.isCom) {
            when (keyText.text) {
                COM_DONE -> {
                }
                COM_UP -> {
                    handleInterface!!.moveUp()
                    refreshLayout()
                }
                COM_DOWN -> {
                    handleInterface!!.moveDown()
                    refreshLayout()
                }
                COM_EMPTY -> {
                    handleInterface!!.emptying()
                    //设置给按键，显示字符
                    for (kt in strData) kt.isPressed = false
                    adapter?.notifyDataSetChanged()
                }
                COM_DELETE -> {
                    val deletedStr = handleInterface!!.delete()
                    //设置给按键，显示字符
                    var i = 0
                    while (i < strData.size) {
                        val kt = strData[i]
                        if (kt.text == deletedStr) {
                            kt.isPressed = false
                            adapter?.notifyItemChanged(i)
                            break
                        }
                        i++
                    }
                }
                "播放" -> {
                    Speech.play_Baidu(rs.show.toString())
                    handleInterface!!.windowExplainHolder.explainTitle.performClick()
                }
            }
        } else {
            val isSuccess = handleInterface!!.addSegment(keyText.text)
            //设置给按键，不显示字符
            if (isSuccess) {
                var wordCountRight = 0
                var wordCountInput = 0
                //累计keyText.text在frameRight中重复数量
                for (we in frameRight!!) {
                    for (word in we.explains) if (word == keyText.text) wordCountRight++
                }
                //累计keyText.text在frameInput中重复数量
                for (we in frameInput!!) {
                    for (word in we.explains) if (word == keyText.text) wordCountInput++
                }
                //wordCountRight为0，则keyText.text它不是正确的，就只能单击1次
                if (wordCountRight == wordCountInput || wordCountRight == 0) keyText.isPressed = true else keyText.isPressed = false
                adapter?.notifyItemChanged(posi)
            }
        }
        return true
    }

    companion object {
        var libTemp: LibraryList? = null
    }
}