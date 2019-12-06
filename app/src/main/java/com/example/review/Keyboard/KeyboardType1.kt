package com.example.review.Keyboard

import android.content.Context
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.example.review.Adapter.MyAdapter
import com.example.review.DataStructureFile.WordExplain
import com.example.review.New.KeyText
import com.example.review.New.ReviewStruct
import com.example.review.R
import com.example.review.Util.Speech
import java.io.IOException

class KeyboardType1(context: Context, keyboardView: RecyclerView, container: ConstraintLayout, input: EditText, reviewStruct: ReviewStruct) : Keyboard(context, keyboardView, container, input, reviewStruct) {
    var mp: MediaPlayer? = null
    lateinit var handleInterfaceType1: HandleInterfaceType1

    override fun init() {
        input.isEnabled = true
        input.requestFocus()
        input.hint = "请输入"
        input.setText("")
        //让实体键盘为全英文输入
        input.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
        //        input.setShowSoftInputOnFocus(false);
//        if (rs.getLevel() <= 3) {
//            input.setShowSoftInputOnFocus(false);
//            input.setInputType(InputType.TYPE_NULL);
//        } else {
//            input.setShowSoftInputOnFocus(true);
//            input.setInputType(InputType.TYPE_CLASS_TEXT);
//        }
        val frameRight: ArrayList<WordExplain> = ReviewStruct.Companion.getMatchWordExplains(rs.show.text)
        handleInterfaceType1 = HandleInterfaceType1(context, container, frameRight)
        handleInterfaceType1.setLightAnimation(true, 200)
    }

    override fun setLightAnimation(lightUp: Boolean, duration: Int) {}
    override fun refresh() {
        val text = rs.show.text
        val type = rs.show.type
        handleInterfaceType1.refresh()
        when (type) {
            Keyboard.Companion.TYPE_PICTURE -> {
                val drawable = Drawable.createFromPath(text)
                if (drawable == null) {
                    handleInterfaceType1.windowExplainHolder.explainTitle.hint = "路径内容不是图片！"
                } else {
                    handleInterfaceType1.windowExplainHolder.explainTitle.hint = ""
                    container.background = drawable
                }
            }
            Keyboard.Companion.TYPE_SOUND -> {
                //播放音频
                mp = MediaPlayer()
                try {
                    mp!!.setDataSource(text)
                    mp!!.prepare()
                    mp!!.start()
                    mp!!.setOnCompletionListener { mp -> mp.release() }
                } catch (e: IOException) {
                    Speech.play(text)
                }
                handleInterfaceType1.windowExplainHolder.explainTitle.hint = "听声音..."
            }
            else -> {
            }
        }
    }

    override fun stop() {
        super.stop()
        if (mp != null) mp!!.stop()
    }

    override fun adapterComplete() {
        MyAdapter.Companion.isShowNum = false
    }

    override fun getLayout(): ArrayList<KeyText> {
        val data: ArrayList<KeyText>
        span = 10
        //            data = getRandomLayout();
        data = regularLayout
        //        if (rs.getLevel() > UNDER_LEVEL) data = getLessLayout();
//        else data = getRegularLayout();
        return data
    }

    //去重复
    internal val randomLayout: ArrayList<KeyText>
        internal get() {
            val data = ArrayList<KeyText>()
            val strs = arrayOf(
                    Keyboard.Companion.COM_DONE, "", Keyboard.Companion.COM_LEFT, Keyboard.Companion.COM_RIGHT, Keyboard.Companion.COM_EMPTY, Keyboard.Companion.COM_DELETE, Keyboard.Companion.COM_DONE)
            span = 7
            val someChars = StringBuffer()
            val matchStr = redundancyGone(rs.match.text)
            val matchChars = matchStr.toCharArray()
            //去重复
            for (i in 'a'.toInt() until 'z'.toInt() + 1) {
                val ch = i.toChar()
                val contains = matchStr.contains(ch.toString() + "")
                if (!contains) someChars.append(ch)
            }
            for (str in strs) data.add(KeyText(str, true))
            val unionStrs = someChars.toString() + matchStr
            val Chars = getSomeRandomChars(StringBuffer(unionStrs))
                    .toCharArray()
            for (ch in Chars) data.add(KeyText(ch.toString() + ""))
            data.add(KeyText(""))
            data.add(KeyText(Keyboard.Companion.COM_DONE, true))
            return data
        }//空格不要，已经有了

    //        data.add(new KeyText("", false));
    private val regularLayout: ArrayList<KeyText>
        private get() {
            val data = ArrayList<KeyText>()
            val com = arrayOf(
                    Keyboard.Companion.COM_DONE, "", Keyboard.Companion.COM_LEFT, Keyboard.Companion.COM_RIGHT, "", Keyboard.Companion.COM_DELETE, "", Keyboard.Companion.COM_EMPTY, "", Keyboard.Companion.COM_DONE)
            val strs = arrayOf(
                    "q", "w", "e", "r", "t", "y", "u", "i", "o", "p",
                    "a", "s", "d", "f", "g", "", "h", "j", "k", "l",
                    "z", "x", "c", "v", "b", "", "n", "m")
            span = 10
            for (str in com) data.add(KeyText(str, true))
            for (str in strs) data.add(KeyText(str))
            data.add(KeyText(Keyboard.Companion.COM_SPACE, true))
            //        data.add(new KeyText("", false));
            val matchChars = rs.match.text.toCharArray()
            //去除重复的字符
            for (chAdd in matchChars) {
                var conti = false
                for (strExist in strs) {
                    val strCh = chAdd.toString() + ""
                    if (strExist == strCh || strCh == " ") { //空格不要，已经有了
                        conti = true
                        break
                    }
                }
                if (conti) continue
                data.add(KeyText(chAdd.toString() + ""))
            }
            return data
        }

    //        data.add(new KeyText(""));
    //去重复
    //        span = 4;
//填补空洞
    internal val lessLayout: ArrayList<KeyText>
        internal get() {
            val data = ArrayList<KeyText>()
            data.add(KeyText(Keyboard.Companion.COM_DONE, true))
            //        data.add(new KeyText(""));
            data.add(KeyText(Keyboard.Companion.COM_EMPTY, true))
            data.add(KeyText(Keyboard.Companion.COM_DONE, true))
            val someChars = StringBuffer()
            val matchStr = redundancyGone(rs.match.text)
            val matchChars = matchStr.toCharArray()
            val length = rs.match.text.length
            var more: Int = (length * 1.8).toInt()

            if (more < 40) {
                var column = more / 4
                if (column < 4) {
                    column = more / 3
                    if (column < 3) {
                        column = more / 2
                        span = if (column < 2) more else column
                    } else span = column
                } else span = column
            }
            //去重复
            for (i in 'a'.toInt() until 'z'.toInt() + 1) {
                var conti = false
                for (ch in matchChars) {
                    if (i == ch.toInt()) {
                        conti = true
                        break
                    }
                }
                if (conti) continue
                someChars.append(i.toChar())
            }
            for (matchChar in matchChars) {
                someChars.append(matchStr)
            }
            //        span = 4;
//填补空洞
            more += span - more % span
            val extraStr = getSomeRandomChars(someChars, more - matchChars.size)
            val unionStrs = extraStr + matchStr
            val keys1 = StringBuffer("qwertyuiop")
            val keys2 = StringBuffer("asdfghjkl")
            val keys3 = StringBuffer("zxcvbnm")
            val keys = arrayOf(keys1, keys2, keys3)
            for (i in keys.indices) {
                val key = keys[i]
                var k = 0
                while (k < key.length) {
                    val ch = key[k]
                    val contains = unionStrs.contains(ch.toString() + "")
                    if (!contains) {
                        key.deleteCharAt(k)
                        k--
                    }
                    k++
                }
            }
            for (key in keys) {
                val chars = key.toString().toCharArray()
                for (aChar in chars) data.add(KeyText(aChar.toString() + ""))
            }
            return data
        }

    internal fun redundancyGone(tobeRemoved: String?): String {
        val sb = StringBuffer(tobeRemoved)
        for (i in 0 until sb.length) {
            val ch = sb[i]
            var k: Int
            while (sb.indexOf(ch.toString() + "", i + 1).also { k = it } != -1) {
                sb.deleteCharAt(k)
            }
        }
        return sb.toString()
    }

    internal fun getSomeRandomChars(text: StringBuffer?, number: Int): String {
        var number = number
        val sb = StringBuffer(text)
        val temp = StringBuffer()
        if (number > sb.length) number = sb.length
        for (i in 0 until number) {
            val random = (Math.random() * 10000).toInt() % sb.length
            temp.append(sb[random])
            sb.deleteCharAt(random)
        }
        return temp.toString()
    }

    internal fun getSomeRandomChars(text: StringBuffer): String {
        return getSomeRandomChars(text, text.length)
    }

    override fun onItemClick(view: View?, data: ArrayList<KeyText>, posi: Int) {
        super.onItemClick(view, data, posi)
        val textViewnum = view!!.findViewById<TextView>(R.id.item_textView_number)
        val inputText = input.text.toString()
        val keyText = data[posi]

        if (keyText.isCom) {
            var selection: Int
            selection = input.selectionStart

            when (keyText.text) {
                COM_SPACE -> input.append(" ")
                COM_DONE -> {
                }
                COM_DELETE -> {
                    val editable = input.text
                    if (0 > selection - 1) else editable.delete(selection - 1, selection)
                }
                COM_LEFT -> {
                    if (selection > 0) --selection
                    input.setSelection(selection)
                }
                COM_RIGHT -> {
                    if (selection < inputText.length) ++selection
                    input.setSelection(selection)
                }
                COM_EMPTY -> {
                    if (inputText != "") input.setText("")
                    if (rs.level < UNDER_LEVEL) adapter!!.notifyDataSetChanged()
                }
            }
        } else {
            val editableText = input.editableText
            val selection = input.selectionStart
            editableText.insert(selection, keyText.text)

            if (rs.level < UNDER_LEVEL) {
                val num = textViewnum.text as String
                var value = Integer.valueOf(num)
                textViewnum.setText((++value).toString())
                textViewnum.visibility = View.VISIBLE
            }
        }
    }

    override fun keyDown(keyCode: Int, key: Char, posi: Int): Boolean {
        return false
    }

    companion object {
        private const val UNDER_LEVEL = 3
    }
}