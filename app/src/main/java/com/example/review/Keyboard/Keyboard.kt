package com.example.review.Keyboard

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Handler
import android.os.Handler.Callback
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.example.review.New.ItemClickListener
import com.example.review.New.KeyText
import com.example.review.New.ReviewStruct
import com.example.review.R
import java.util.*
import kotlin.collections.ArrayList

abstract class Keyboard(var context: Context, var keyboardView: RecyclerView, val container: ConstraintLayout, var input: EditText, var rs: ReviewStruct) : ItemClickListener {
    var span = 0
    var index = 0
    var adapter: MyAdapter? = null
    var strData: ArrayList<KeyText> = ArrayList()

    abstract fun init()
    abstract fun setLightAnimation(lightUp: Boolean, duration: Int)
    abstract fun refresh()
    abstract fun getLayout(): ArrayList<KeyText>

    open fun adapterComplete() {}

    fun buildKeyboard(millis: Int) {
        val handler = Handler(Callback {
            buildKeyboard()
            false
        })
        Timer().schedule(object : TimerTask() {
            override fun run() {
                handler.sendEmptyMessage(0)
            }
        }, millis.toLong())
    }

    fun buildKeyboard(): Keyboard {
        refreshLayout()
        return this
    }

    fun refreshLayout() {
        strData = getLayout()
        adapter = MyAdapter(context, strData, this)
        adapterComplete()
        keyboardView.adapter = adapter
        keyboardView.layoutManager = GridLayoutManager(context, span)
        //main.recyclerViewKeyboard.setLayoutManager(new StaggeredGridLayoutManager(span, StaggeredGridLayoutManager.VERTICAL));
    }

    interface OnKeyDownListener {
        fun onKeyDown(keyText: KeyText)
    }

    var onKeyDownListener: OnKeyDownListener? = null

    override fun onItemClick(view: View?, data: ArrayList<KeyText>, posi: Int) {
        if (posi < 0) return
        onKeyDownListener?.onKeyDown(data[posi])
        keyDown(0, 0.toChar(), posi)
    }

    abstract fun keyDown(keyCode: Int, key: Char, posi: Int): Boolean

    fun clearKeyboard() {
        if (adapter != null) {
            strData.clear()
            adapter!!.notifyDataSetChanged()
        }
    }

    open fun stop() {}

    fun clear() {
        container.removeAllViews()
        strData.clear()
        adapter!!.notifyDataSetChanged()
    }

    companion object {
        const val COM_SPACE = "空格"
        const val COM_DONE = "完成"
        const val COM_DELETE = "←"
        const val COM_EMPTY = "清空"
        const val COM_LEFT = "<"
        const val COM_RIGHT = ">"
        const val COM_UP = "↑"
        const val COM_DOWN = "↓"
        const val TYPE_WORD = 1
        const val TYPE_EXPLAIN = 2
        const val TYPE_CHOOSE = 3
        const val TYPE_PICTURE = 4
        const val TYPE_SOUND = 5

        fun <E> makeRandom(list: MutableList<E>) {
            val random = Random(System.currentTimeMillis())
            val size = list.size
            for (i in list.indices) {
                val str = list[i]
                list.remove(str)
                val index = random.nextInt(size)
                list.add(index, str)
            }
        }

        fun <E> removeRedundancy(link: List<E>) {
            for (i in link.indices) {
                val match = link[i]
                var k = i + 1

                while (k < link.size) {
                    val temp = link[k]
                    if (match == temp) {
//                        link.removeAt(k)
                        link.drop(k)//todo where 'k' has beeen deleted?
                        k--
                    }
                    k++
                }
            }
        }

        //按字符长度排序
        fun sortByCharLen(link: MutableList<String>) {
            val linkTemp = LinkedList<String>()
            val linkc = LinkedList<String>()
            var len = 0
            while (!link.isEmpty()) {
                len++
                var i = 0
                while (i < link.size) {
                    val str = link[i]
                    if (str.length == len) {
                        linkTemp.add(str)
                        link.removeAt(i)
                        i--
                    }
                    i++
                }
                makeRandom(linkTemp)
                linkc.addAll(linkTemp)
                linkTemp.clear()
            }
            link.addAll(linkc)
        }
    }

    init {
        container.removeAllViews()
        init()
    }

    class MyAdapter(val context: Context, val data: ArrayList<KeyText>, val clickListener: ItemClickListener?) : RecyclerView.Adapter<MyAdapter.MyHolder>() {
        var textSize = 0
        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MyHolder {
            val inflate = LayoutInflater.from(context).inflate(R.layout.activity_keyboard_item, viewGroup, false)
            return MyHolder(inflate)
        }

        override fun onBindViewHolder(holder: MyHolder, position: Int) {
            val item = data[position]
            item.view = holder.view

            if (item.isCom) { //功能键，配置
                holder.textView.maxLines = 10
                holder.textView.textSize = 16f
                //            holder.textView.setTextColor(Color.BLUE);
                holder.textView.setTextColor(Color.WHITE)
                holder.textView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                holder.view.setBackgroundResource(R.drawable.bg_ripple_keyboard_click_commen)

            } else { //非功能键，配置
                holder.textView.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                holder.textView.maxLines = 6
                holder.textView.setTextColor(Color.BLACK)
                holder.view.setBackgroundResource(R.drawable.bg_ripple_keyboard_click)

                if (textSize > 0) {
                    when (item.text.length) {
                        1, 2 -> holder.textView.textSize = textSize.toFloat()
                        3 -> {
                            holder.textView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                            holder.textView.textSize = textSize - 2.toFloat()
                        }
                        4, 5 -> holder.textView.textSize = textSize - 4.toFloat()
                        6 -> holder.textView.textSize = textSize - 6.toFloat()
                        else -> holder.textView.textSize = textSize - 6.toFloat()
                    }
                }
            }
            if (isShowNum) {
                if (item.key.toInt() != 1) {
                    holder.textViewNum.text = String.format("%c", item.key)
                    holder.textViewNum.visibility = View.VISIBLE
                } else holder.textViewNum.visibility = View.INVISIBLE
            } else holder.textViewNum.visibility = View.INVISIBLE

            //被单击过，就不显示该字符
            if (item.isPressed && !item.isCom) {
                holder.textView.text = ""
                holder.view.isEnabled = false
                //holder.view.setAlpha(0f);
            } else {
                holder.view.isEnabled = true
                holder.textView.text = item.text
            }

            //键盘按键被单击事件处理
            holder.view.setOnClickListener { view: View? -> clickListener?.onItemClick(view, data, position) }
            holder.view.visibility = if (item.text == "") View.INVISIBLE else View.VISIBLE
        }

        class MyHolder(val view: View) : RecyclerView.ViewHolder(view) {
            val textView: TextView = view.findViewById(R.id.item_textView_word)
            val textViewNum: TextView = view.findViewById(R.id.item_textView_number)
        }

        companion object {
            var isShowNum = false
        }

        override fun getItemCount() = data.size

    }
}