package com.example.review.Keyboard

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.example.review.Activity.MainActivity
import com.example.review.New.KeyText
import com.example.review.New.LibraryList
import com.example.review.New.ReviewStruct
import com.example.review.R
import com.example.review.Util.SpanUtil
import com.example.review.Util.Speech
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

class KeyboardType3(context: Context, keyboardView: RecyclerView, show: ConstraintLayout, input: EditText, reviewStruct: ReviewStruct) : Keyboard(context, keyboardView, show, input, reviewStruct) {
    var mCandidate: ArrayList<String>? = null
    private lateinit var candidate: ArrayList<String>
    lateinit var mCandidateType: ArrayList<TextCom>
    private lateinit var patterBitPar: Pattern
    private lateinit var patternPar: Pattern
    private var show: TextView? = null
    private lateinit var inflate: View

    override fun setLightAnimation(lightUp: Boolean, duration: Int) {
        val valueAnim: ValueAnimator = if (lightUp) ValueAnimator.ofFloat(0f, 1f) else ValueAnimator.ofFloat(1f, 0f)
        valueAnim.duration = duration.toLong()
        valueAnim.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Float
            inflate.alpha = value
        }
        valueAnim.start()
    }

    override fun init() {
        input.setText("")
        input.hint = "↑↑↑在上面操作↑↑↑"
        input.showSoftInputOnFocus = false
        input.inputType = InputType.TYPE_NULL

        inflate = LayoutInflater.from(context).inflate(R.layout.activity_text_view, container, false)
        show = inflate.findViewById(R.id.tv_text)
        container.addView(inflate)
        val strMatch = rs.match.text

        patternPar = Pattern.compile("\\(.*?\\)")
        var split = patternPar.split(strMatch)
        val matcher = patternPar.matcher(strMatch)

        candidate = ArrayList()
        mCandidate = ArrayList()
        mCandidateType = ArrayList()

        //找出备选词语
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            val word = strMatch.substring(start + 1, end - 1)
            mCandidate!!.add(word)
        }
        if (split.isEmpty()) {
            split = arrayOfNulls(mCandidate!!.size)
            for (i in split.indices) split[i] = " "
        }
        val libs: LibraryList = MainActivity.data.library
        val list = ArrayList<String>()

        //在库内取一些数据
        for ((index, lib) in libs.withIndex()) {
            val type = lib.type
            if (type == 1) list.add(lib.text)
            if (index == 100) break
        }

        //随机取出数据
        val random = Random(System.currentTimeMillis())
        for (i in 0..9) {
            val str = list[random.nextInt(list.size)]
            candidate.add(str)
        }

        //小括号，备选词
        val show = rs.show.text
        val mat = patternPar.matcher(show)
        while (mat.find()) {
            val start = mat.start()
            val end = mat.end()
            val word = show.substring(start + 1, end - 1)
            candidate.add(word)
        }

        //大括号，备选词
        patterBitPar = Pattern.compile("\\{.*\\}")
        val matcherBigPar = patterBitPar.matcher(show)
        while (matcherBigPar.find()) {
            val start = matcherBigPar.start()
            val end = matcherBigPar.end()
            val strArray = show.substring(start + 1, end - 1)
            val words = Pattern.compile("[,，]").split(strArray)
            Collections.addAll(candidate, *words)
        }
        candidate.addAll(mCandidate!!)
        removeRedundancy(candidate)
        makeRandom(candidate)
        var length = mCandidate!!.size
        for (s in split) {
            mCandidateType.add(TextCom(s))
            if (length-- == 0) break
            mCandidateType.add(TextCom("    ", true))
        }
    }

    override fun adapterComplete() {
        super.adapterComplete()
        adapter!!.textSize = 14
    }

    override fun refresh() {
        var posi = 0
        val spanBuilder = SpanUtil.create()
        var show = rs.show.text
        //不显示，备选内容
        show = patternPar.matcher(show).replaceAll("")
        show = patterBitPar.matcher(show).replaceAll("")
        //show有该字符串，则显示它
        if (show != "") spanBuilder.addSection(show + "\n")
        for (tc in mCandidateType) {
            if (tc.isCandidate) { //设置选中备选框背景，为灰色
                if (posi == index) spanBuilder.addBackColorSection(tc.text, Color.LTGRAY).setStyle(tc.text!!, Typeface.BOLD) else spanBuilder.addForeColorSection(tc.text, Color.BLACK).setStyle(tc.text!!, Typeface.BOLD)
                //设置显示字符串带删除线
                if (tc.isStrike) { //全部是空格，不显示删除线
                    if (!tc.text!!.matches(Regex("\\s+"))) spanBuilder.setStrikethrough(tc.text!!)
                }
                //设置字符串有下划线
                spanBuilder.setUnderline(tc.text!!)
                posi++
            } else spanBuilder.addForeColorSection(tc.text, Color.GRAY)
        }
        spanBuilder.showIn(this.show!!)
        this.show!!.hint = "格式不对！"
    }

    override fun getLayout(): ArrayList<KeyText> {
        span = 6
        val kts = arrayOf(
                KeyText(COM_DONE, true, KeyEvent.KEYCODE_ENTER),
                KeyText(COM_LEFT, true, KeyEvent.KEYCODE_DPAD_UP, 'l'),
                KeyText(COM_RIGHT, true, KeyEvent.KEYCODE_DPAD_DOWN, 'r'),
                KeyText(COM_EMPTY, true, KeyEvent.KEYCODE_FORWARD_DEL),
                KeyText(COM_DELETE, true, KeyEvent.KEYCODE_DEL),
                KeyText(COM_DONE, true, KeyEvent.KEYCODE_ENTER))
        val data = ArrayList(listOf(*kts))
        sortByCharLen(candidate)
        var key = 'a'
        for (str in candidate) {
            if (key == 'l' || key == 'r') key++
            data.add(KeyText(str, false, 0, key))
            key++
        }
        return data
    }

    inner class TextCom {
        constructor(text: String?) {
            this.text = text
        }

        constructor(text: String?, isCandidate: Boolean) {
            this.text = text
            this.isCandidate = isCandidate
        }

        var text: String?
        var isCandidate = false
        var isStrike = false
        override fun toString(): String {
            return String.format("[%s]  备选=%b  删除=%b", text, isCandidate, isStrike)
        }
    }

    override fun keyDown(keyCode: Int, key: Char, posi: Int): Boolean {
        var keyText: KeyText? = null
        if (posi >= 0) {
            keyText = strData[posi]
        } else {
            for (kt in strData) {
                if (kt.keyCode == keyCode || kt.key == key) {
                    keyText = kt
                    if (kt.view != null) {
                        kt.view!!.performClick()
                        kt.view!!.isPressed = true
                        kt.view!!.isPressed = false
                        return true
                    }
                    break
                }
            }
        }
        if (keyText == null) return false
        val tcs = ArrayList<TextCom>()
        for (textCom in mCandidateType) {
            if (textCom.isCandidate) tcs.add(textCom)
        }
        val textCom: TextCom
        if (keyText.isCom) {
            when (keyText.text) {
                COM_DONE -> {
                }
                COM_LEFT -> if (--index < 0) index = tcs.size - 1
                COM_RIGHT -> if (++index == tcs.size) index = 0
                COM_EMPTY -> if (tcs.isNotEmpty()) {
                    for (com in tcs) {
                        if (com.isCandidate) {
                            com.text = "    "
                            com.isStrike = false
                        }
                    }
                    index = 0
                }
                COM_DELETE -> if (tcs.isNotEmpty()) {
                    val textCom1 = tcs[index]
                    textCom1.text = "    "
                    textCom1.isStrike = false
                }
                else -> {
                }
            }
        } else {
            if (tcs.isNotEmpty()) {
                textCom = tcs[index]
                textCom.text = keyText.text
                textCom.isStrike = false
                Speech.play_Baidu(keyText.text)
                if (++index == tcs.size) index = 0
            }
        }
        refresh()
        return true
    }
}