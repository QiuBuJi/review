package com.example.review.Keyboard

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.support.constraint.Guideline
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.example.review.DataStructureFile.WordExplain
import com.example.review.R
import java.util.*

class HandleInterfaceType1(var context: Context, var containerView: ConstraintLayout, var frameInput: ArrayList<WordExplain>) {
    var adapter: MyAdapter
    var windowExplainHolder: WindowExplainHolder
    private val windowExplain: View

    fun refresh() {
        adapter.notifyDataSetChanged()
    }

    fun setLightAnimation(lightUp: Boolean, duration: Int) {
        val valueAnim: ValueAnimator = if (lightUp) ValueAnimator.ofFloat(0f, 1f)
        else ValueAnimator.ofFloat(1f, 0f)

        valueAnim.duration = duration.toLong()
        valueAnim.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Float
            windowExplain.alpha = value
        }
        valueAnim.start()
    }

    private var mPriories = arrayOf("n.", "vt.", "adj.", "pron.", "conj.", "adv.", "intj.", "adv.", "art.", "vi.")
    private var strPriorities = listOf(*mPriories)

    //取颜色，不同前缀有不同的对应的颜色
    fun getColor(src: String): Int {
        var src = src
        src = src.replace("\\s".toRegex(), "") //去除空格
        val prefixColor: Int
        val i = strPriorities.indexOf(src)
        prefixColor = when (i) {
            0 -> Color.RED
            1 -> Color.GREEN
            2 -> Color.DKGRAY
            3 -> Color.BLUE
            4 -> Color.MAGENTA
            5 -> Color.CYAN
            else -> Color.GRAY
        }
        return prefixColor
    }

    /**
     * @link activity_window_explain.xml
     */
    inner class WindowExplainHolder internal constructor(view: View) {
        var explainBody: RecyclerView = view.findViewById(R.id.recycler_explain_body)
        var explainTitle: TextView = view.findViewById(R.id.tv_explain_title)
    }

    /**
     * @link activity_item_show.xml
     */
    inner class ItemHolder(itemView: View) : ViewHolder(itemView) {
        val prefix: TextView = itemView.findViewById(R.id.tv_prefix)
        val container: LinearLayout = itemView.findViewById(R.id.ll_container)
        val count: TextView = itemView.findViewById(R.id.tv_explain_count)
        val indicator: TextView = itemView.findViewById(R.id.tv_indicator)
        val guideline: Guideline = itemView.findViewById(R.id.guideline)
    }

    inner class MyAdapter : RecyclerView.Adapter<ItemHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ItemHolder {
            val inflate = LayoutInflater.from(context).inflate(
                    R.layout.activity_item_show, windowExplainHolder.explainBody, false)
            return ItemHolder(inflate)
        }

        var max = 0
        var maxPostfixLen = 0
        override fun onBindViewHolder(holder: ItemHolder, i: Int) {
            val weInput = frameInput[i]

            //解决前缀对齐问题
            if (max == 0) {
                val spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                for (we in frameInput) {
                    holder.prefix.text = we.category
                    holder.prefix.measure(spec, spec)
                    var width = holder.prefix.measuredWidth
                    if (width > max) max = width
                    val sb = StringBuffer()
                    for (explain in we.explains) sb.append(explain)
                    holder.prefix.text = sb
                    holder.prefix.measure(spec, spec)
                    width = holder.prefix.measuredWidth
                    if (width > maxPostfixLen) maxPostfixLen = width
                }
            }
            val marging = (containerView.width - (maxPostfixLen + max)) / 2 + max / 2
            holder.guideline.setGuidelineBegin(marging)

            //设置前缀及其颜色
            holder.prefix.text = weInput.category
            holder.prefix.setTextColor(getColor(weInput.category))
            holder.indicator.visibility = View.GONE

            //设置解释字符
            holder.container.removeAllViews()
            for (explain in weInput.explains) {
                val txt = LayoutInflater.from(context).inflate(
                        R.layout.activity_item_text, holder.container, false) as TextView
                txt.text = explain
                txt.setBackgroundColor(-0xff37ad)
                holder.container.addView(txt)
            }

            //设置该项内数据总量
            holder.count.text = weInput.explains.size.toString() + ""
            holder.count.visibility = View.GONE
        }

        override fun getItemCount() = frameInput.size
    }

    init {
        windowExplain = LayoutInflater.from(context).inflate(R.layout.activity_window_explain, containerView, false)
        containerView.addView(windowExplain)
        windowExplainHolder = WindowExplainHolder(windowExplain)
        windowExplainHolder.explainTitle.text = ""

        //解决退出后再进入时不显示内容的问题
        val lp: ViewGroup.LayoutParams = windowExplainHolder.explainBody.layoutParams
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT
        windowExplainHolder.explainBody.layoutParams = lp
        val layout = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        windowExplainHolder.explainBody.layoutManager = layout

        adapter = MyAdapter()
        windowExplainHolder.explainBody.setAdapter(adapter)
    }
}