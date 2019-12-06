package com.example.review.Adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.review.New.ItemClickListener
import com.example.review.New.KeyText
import com.example.review.R

class MyAdapter(val context: Context, val data: ArrayList<KeyText>, val clickListener: ItemClickListener?) : RecyclerView.Adapter<MyAdapter.MyHolder>() {
    var textSize = 0
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MyHolder {
        val inflate = LayoutInflater.from(context).inflate(R.layout.activity_keyboard_item, viewGroup, false)
        return MyHolder(inflate)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val item = data!![position]
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
                val length = item.text.length
                when (length) {
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
            //                holder.view.setAlpha(0f);
        } else {
            holder.view.isEnabled = true
            holder.textView.text = item.text
        }
        //键盘按键被单击事件处理
        holder.view.setOnClickListener { view: View? -> clickListener?.onItemClick(view, data, position) }
        if (item.text == "") holder.view.visibility = View.INVISIBLE else holder.view.visibility = View.VISIBLE

    }

    class MyHolder(val view: View) : ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.item_textView_word)
        val textViewNum: TextView = view.findViewById(R.id.item_textView_number)
    }

    companion object {
        var isShowNum = false
    }

    override fun getItemCount() = data.size

}