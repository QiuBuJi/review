package com.example.review.Keyboard

import android.animation.ObjectAnimator
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
import android.widget.Toast
import com.example.review.DataStructureFile.WordExplain
import com.example.review.R

class HandleInterfaceType2(var context: Context, containerView: ConstraintLayout, var frameInput: ArrayList<WordExplain>, var frameRight: ArrayList<WordExplain>) {
    var adapter: MyAdapter
    var windowExplainHolder: WindowExplainHolder

    var indexOfItem = 0
    internal var windowExplain: View

    //取颜色，不同前缀有不同的对应的颜色
    fun getColor(prefix: String): Int {
        var prefix = prefix
        prefix = prefix.replace("\\s".toRegex(), "") //去除空格
        return when (prefix) {
            "n." -> Color.RED
            "v." -> Color.BLACK
            "vt." -> Color.BLACK
            "vi." -> Color.DKGRAY
            "adj." -> -0xff7685
            "pron." -> Color.BLUE
            "prep." -> -0x5500
            "conj." -> Color.MAGENTA
            "adv." -> Color.CYAN
            "intj." -> Color.GREEN
            "art." -> -0xfea865
            "*." -> -0x1fbf05
            else -> Color.GRAY
        }
    }

    fun setLightAnimation(lightUp: Boolean, duration: Int) {
        val valueAnim: ValueAnimator = if (lightUp) ValueAnimator.ofFloat(0f, 1f) else ValueAnimator.ofFloat(1f, 0f)
        valueAnim.duration = duration.toLong()
        valueAnim.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Float
            windowExplain.alpha = value
        }
        valueAnim.start()
    }

    //移动到上一项
    fun moveUp() {
        indexOfItem--
        if (indexOfItem < 0) indexOfItem = frameInput.size - 1
        adapter.notifyDataSetChanged()
    }

    //移动到下一项
    fun moveDown() {
        indexOfItem++
        if (indexOfItem == frameInput.size) indexOfItem = 0
        adapter.notifyDataSetChanged()
    }

    //向前删除
    fun delete(): String? {
        val we = frameInput[indexOfItem]
        var str: String? = null
        try {
            str = we.explains.removeLast()
        } catch (e: Exception) {
        }
        adapter.notifyDataSetChanged()
        return str
    }

    //清空所有输入的数据
    fun emptying() {
        indexOfItem = 0
        for (explain in frameInput) explain.explains.clear()
        adapter.notifyDataSetChanged()
    }

    //添加字符
    fun addSegment(segment: String): Boolean {
        var returnValue = false

        //解决退出后再进入时不显示内容的问题
        val lp = windowExplainHolder.explainBody.layoutParams
        lp.width = ViewGroup.LayoutParams.WRAP_CONTENT
        windowExplainHolder.explainBody.layoutParams = lp
        val we = frameInput[indexOfItem]
        var weTemp = frameRight[indexOfItem]

        //主动跳转下一行
        var sizeA = we.explains.size
        var sizeB = weTemp.explains.size
        if (sizeA < sizeB) {
            if (we.explains.contains(segment)) {
                Toast.makeText(context, "不能重复哦！", Toast.LENGTH_SHORT).show()
            } else {
                we.explains.add(segment)
                returnValue = true
            }
        } else {
            adapter.notifyDataSetChanged()
            return returnValue
        }
        sizeB = frameRight.size

        //目前项数据填满后，自动转移到下一项
        while (true) { //到底就不要再跳到初始位置了
            if (indexOfItem == sizeB - 1) break
            sizeA = we.explains.size
            sizeB = weTemp.explains.size
            if (sizeA == sizeB) moveDown() else break
            weTemp = frameInput[indexOfItem]
        }
        adapter.notifyDataSetChanged()
        return returnValue
    }

    fun refresh() {
        adapter.notifyDataSetChanged()
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

    internal var mDifferent = false
    fun showDifferent(different: Boolean) {
        mDifferent = different
        adapter.notifyDataSetChanged()
    }

    inner class MyAdapter : RecyclerView.Adapter<ItemHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ItemHolder {
            val inflate = LayoutInflater.from(context).inflate(
                    R.layout.activity_item_show, windowExplainHolder.explainBody, false)
            return ItemHolder(inflate)
        }

        var max = 0
        override fun onBindViewHolder(holder: ItemHolder, i: Int) {
            val weInput = frameInput[i]
            val weRight = frameRight[i]
            //解决前缀对齐问题
            if (max == 0) {
                val spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                for (we in frameInput) {
                    holder.prefix.text = we.category
                    holder.prefix.measure(spec, spec)
                    val width = holder.prefix.measuredWidth
                    if (width > max) max = width
                }
            }
            holder.guideline.setGuidelineBegin(max)
            //设置前缀及其颜色
            holder.prefix.text = weInput.category
            holder.prefix.setTextColor(getColor(weInput.category))
            //设置解释字符
            holder.container.removeAllViews()
            for (explain in weInput.explains) {
                val inflater = LayoutInflater.from(context)
                val txt = inflater.inflate(R.layout.activity_item_text, holder.container, false) as TextView
                txt.text = explain
                holder.container.addView(txt)
                if (mDifferent && !weRight.explains.contains(explain)) txt.setTextColor(Color.RED)
            }
            //设置该项内数据总量
            holder.count.text = weRight.explains.size.toString()
            //正确率超过这个阈值，切换不同文字颜色
            var countInput = 0
            var countRight = 0
            for (we in frameInput) countInput += we.explains.size
            for (we in frameRight) countRight += we.explains.size
            var threshold = (countRight * 0.6f).toInt()
            if (countRight <= 3) threshold = countRight
            //大于阈值时，显示全蓝色，提示可以按回车了
            if (countInput >= threshold) holder.count.setTextColor(-0xc0ae4b) else holder.count.setTextColor(Color.WHITE)
            //后缀填满后，不显示；
            countInput = weInput.explains.size
            countRight = weRight.explains.size
            if (countInput == countRight) holder.count.visibility = View.GONE else holder.count.visibility = View.VISIBLE
            //设置闪闪动画
            if (indexOfItem == i) {
                holder.indicator.visibility = View.VISIBLE
                ObjectAnimator.ofFloat(holder.indicator, "scaleX", 1f, 0f, 1f)
                        .setDuration(200)
                        .start()
            } else holder.indicator.visibility = View.GONE
        }

        override fun getItemCount() = frameInput.size
    }

    init {
        //把frameRight内成员顺序排列得跟frame一样
        for (i in frameInput.indices) {
            val we = frameInput[i]
            for (wordExplain in frameRight) {
                if (wordExplain.category.trim { it <= ' ' } == we.category.trim { it <= ' ' }) {
                    frameRight.remove(wordExplain)
                    frameRight.add(i, wordExplain)
                    break
                }
            }
        }

        windowExplain = LayoutInflater.from(context).inflate(R.layout.activity_window_explain, containerView, false)
        containerView.addView(windowExplain)
        windowExplainHolder = WindowExplainHolder(windowExplain)
        windowExplainHolder.explainTitle.text = "nothing!"
        val layout = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = MyAdapter()
        windowExplainHolder.explainBody.adapter = adapter
        windowExplainHolder.explainBody.layoutManager = layout
    }
}