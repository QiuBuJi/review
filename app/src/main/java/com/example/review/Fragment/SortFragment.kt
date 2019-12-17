package com.example.review.Fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Handler.Callback
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.review.Activity.EditActivity
import com.example.review.Activity.ListActivity
import com.example.review.Activity.MainActivity
import com.example.review.Activity.SortActivity
import com.example.review.DataStructureFile.DateTime
import com.example.review.DataStructureFile.DateTime.TimeFieldEnum
import com.example.review.DataStructureFile.ReviewData
import com.example.review.New.ReviewStruct
import com.example.review.R
import com.example.review.Setting
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("ValidFragment")
open class SortFragment : Fragment {
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: AdapterSort
    lateinit var data: ReviewData
    var displayField = 0
    lateinit var mData: LinkedList<ReviewStruct>
    var mIsActivity = false
    lateinit var tip: TextView

    constructor()
    @SuppressLint("ValidFragment")
    constructor(activity: Boolean) {
        mIsActivity = activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_sort_fragment, container, false)

        displayField = Setting.getInt("displayField")
        data = MainActivity.data

        recyclerView = view.findViewById<View>(R.id.sort_fragment_recyclerView) as RecyclerView
        tip = view.findViewById<View>(R.id.sort_fragment_textView_noData) as TextView

        mData = if (mIsActivity) data.mActivate else data.mInactivate
        tip.visibility = if (mData.isEmpty()) View.VISIBLE else View.INVISIBLE
        selectPartToShow(displayField)
        adapter = AdapterSort(context, mData)

        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.isVerticalScrollBarEnabled = true
        recyclerView.adapter = adapter
        return view
    }

    //设置要显示的时间区域
    open fun selectPartToShow(part: Int) {
        displayField = part
        var temp: DateTime? = null

        for (rs in mData) {
            val dateTime = DateTime(rs.time)
            dateTime.setZeroSegment(part + 1)

            if (temp == null) rs.showed = true
            else rs.showed = temp != dateTime
            temp = dateTime
        }
    }

    internal var oldPosi = -1
    internal var oldView: View? = null
    internal var background: Drawable? = null

    open inner class AdapterSort(internal var context: Context?, internal var data: LinkedList<ReviewStruct>) : RecyclerView.Adapter<AdapterSort.MyHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MyHolder {
            val inflate = LayoutInflater.from(context).inflate(R.layout.activity_sort_item, viewGroup, false)
            return MyHolder(inflate)
        }

        override fun onBindViewHolder(holder: MyHolder, posi: Int) {
            val item = data[posi]
            holder.index.text = String.format("%d.", posi + 1)
            if (item.viewCount > 0) holder.level2.text = String.format("%d次", item.viewCount) else holder.level2.text = ""
            if (mIsActivity) {
                holder.title1.text = String.format("类型：%d", item.match.type)
                holder.timeTips.text = ""
                holder.view.setOnLongClickListener {
                    val posiOrigion = data.indexOf(item)
                    data.remove(item)
                    data.addFirst(item)
                    val posiDest = data.indexOf(item)
                    adapter.notifyItemMoved(posiOrigion, posiDest)
                    false
                }
                holder.view.setOnClickListener {
                    if (oldPosi != -1) {
                        val dest = data.indexOf(item)

                        //整个类型的数据置顶
                        if (oldPosi == dest) {
                            val rss = ArrayList<ReviewStruct>()
                            val srcType = item.match.type
                            for (i in data.indices.reversed()) {
                                val rs = data[i]
                                val destType = rs.match.type
                                if (destType == srcType) {
                                    data.remove(rs)
                                    rss.add(0, rs)
                                }
                            }
                            data.addAll(0, rss)
                            notifyItemRangeRemoved(0, data.size)

                            //单条数据，换位置
                        } else {
                            val rs = data[oldPosi]
                            data.removeAt(oldPosi)
                            data.add(dest, rs)
                            adapter.notifyItemMoved(oldPosi, dest)
//                            oldView.setBackground(background);
                            val handler = Handler(Callback {
                                oldView!!.background = background
                                false
                            })
                            Timer().schedule(object : TimerTask() {
                                override fun run() {
                                    handler.sendEmptyMessage(1)
                                }
                            }, 300)
                        }
                        oldPosi = -1
                    } else {
                        oldPosi = data.indexOf(item)
                        oldView = holder.view
                        background = oldView!!.background
                        holder.view.setBackgroundColor(Color.LTGRAY)
                    }
                }
            } else {
                holder.title1.text = item.match.text
                val dateTime: DateTime = DateTime.getCurrentTime()
                val tempTime = DateTime(item.time)
                tempTime.subtractOf(dateTime)
                holder.timeTips.text = tempTime.toAboutValue()

                //长按进入编辑界面
                holder.view.setOnClickListener {
                    //跳转页面，到编辑窗口
                    ListActivity.currentClickedRs = item
                    context!!.startActivity(Intent(context, EditActivity::class.java))
                }
            }
            holder.title2.text = item.show.text
            holder.level1.text = String.format("%d", item.level)

            //分组显示，显示时间的一排设置程序
            if (item.showed && !mIsActivity) {
                holder.region.visibility = View.VISIBLE
                holder.line.visibility = View.VISIBLE
                make(posi, holder)
            } else {
                holder.region.visibility = View.GONE
                holder.line.visibility = View.INVISIBLE
            }
        }

        private fun make(posi: Int, holder: MyHolder) {
            val sb = StringBuilder()
            val item = data[posi]
            try {
                sb.append(item.time.year).append("年")
                if (SortActivity.fragment!!.displayField == DateTime.YEAR) {
                    addExtraText(item.time, TimeFieldEnum.MONTH, sb, "（本年）")
                    throw Exception()
                }
                sb.append(item.time.month + 1).append("月")
                if (SortActivity.fragment!!.displayField == DateTime.MONTH) {
                    addExtraText(item.time, TimeFieldEnum.DAY, sb, "（本月）")
                    throw Exception()
                }
                sb.append(item.time.day).append("日")
                if (SortActivity.fragment!!.displayField == DateTime.DAY) {
                    val time = DateTime(item.time)
                    val b = addExtraText(time, TimeFieldEnum.HOUR, sb, "（今天）")
                    if (!b) {
                        time.day = time.day - 1
                        addExtraText(time, TimeFieldEnum.HOUR, sb, "（明天）")
                    }
                    throw Exception()
                }
                sb.append("   ").append(item.time.hour).append("时")
                if (SortActivity.fragment!!.displayField == DateTime.HOUR) {
                    addExtraText(DateTime(item.time), TimeFieldEnum.MINUTE, sb, "（本小时）")
                    throw Exception()
                }
                sb.append(item.time.minute).append("分")
                if (SortActivity.fragment!!.displayField == DateTime.MINUTE) {
                    addExtraText(DateTime(item.time), TimeFieldEnum.SECOND, sb, "（本分钟）")
                    throw Exception()
                }
                sb.append(item.time.second).append("秒")
            } catch (e: Exception) {
                var count = 0
                var i = posi + 1
                while (i < data.size) {
                    if (data[i].showed) break else count++
                    i++
                }
                ++count
                sb.append("  ").append(count).append("条")
                holder.region.text = sb
            }
        }

        private fun addExtraText(time: DateTime, part: TimeFieldEnum, sb: StringBuilder, str: String): Boolean {
            val currTime: DateTime = DateTime.getCurrentTime()
            currTime.setZeroSegment(part)
            val dateTime = DateTime(time)
            dateTime.setZeroSegment(part)
            if (dateTime == currTime) {
                sb.append(str)
                return true
            }
            return false
        }

        inner class MyHolder(var view: View) : ViewHolder(view) {
            val index: TextView = view.findViewById<View>(R.id.item_sort_textView_index) as TextView
            val title1: TextView = view.findViewById<View>(R.id.item_sort_textView_title1) as TextView
            val title2: TextView = view.findViewById<View>(R.id.item_sort_textView_title2) as TextView
            val region: TextView = view.findViewById<View>(R.id.item_sort_textView_region) as TextView
            val level1: TextView = view.findViewById<View>(R.id.item_sort_textView_level1) as TextView
            val level2: TextView = view.findViewById<View>(R.id.item_sort_textView_level2) as TextView
            val timeTips: TextView = view.findViewById<View>(R.id.item_sort_textView_timeTips) as TextView
            val line: ImageView = view.findViewById<View>(R.id.item_sort_imageView_line) as ImageView
        }

        override fun getItemCount(): Int = data.size

    }
}