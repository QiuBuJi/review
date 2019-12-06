package com.example.review.Adapter

import android.app.AlertDialog.Builder
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import com.example.review.Activity.EditActivity
import com.example.review.Activity.ListActivity
import com.example.review.Activity.ListActivity.Companion.checked
import com.example.review.DataStructureFile.DateTime
import com.example.review.DataStructureFile.ReviewData
import com.example.review.New.ReviewStruct
import com.example.review.R
import com.example.review.Util.Speech
import java.util.*

class AdapterList(private val context: Context, private val data: ReviewData) : RecyclerView.Adapter<AdapterList.Holder>() {
    private var background: Drawable? = null
    private val parent: ListActivity
    var posi = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.activity_list_item, parent, false)
        background = view.background
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        posi = position
        val rs = data[position]

        //填充条目内的数据
        holder.index.text = String.format(Locale.CHINA, "%d.", position + 1)
        if (rs.joined) {
            holder.index.setTextColor(-0x27e4a0)
            //            holder.index.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        } else { //            holder.index.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            holder.index.setTextColor(-0x8b8b8c)
        }
        holder.word.text = rs.match.text
        holder.explain.text = rs.show.text
        holder.progress.max = ReviewData.reviewRegions.size - 1
        holder.progress.progress = rs.level
        holder.levelNumber.text = String.format(Locale.CHINA, "%d", rs.level)

        //显示隐藏锁图标
        if (rs.match.refer == 0) holder.ivLockUp.visibility = View.GONE else holder.ivLockUp.visibility = View.VISIBLE
        if (rs.show.refer == 0) holder.ivLockDown.visibility = View.GONE else holder.ivLockDown.visibility = View.VISIBLE
        val size = rs.logs.size
        var count = 0
        for (log in rs.logs) {
            val dateTime = DateTime(log)
            val second = dateTime.second
            if (second < 0) count++
        }
        holder.errorProgress.max = size
        //        holder.errorProgress.setProgress(size);
//        SpanUtil.create()
//                .addForeColorSection(size + "  ", Color.BLACK)
//                .addForeColorSection(count + "％", Color.WHITE)
//                .showIn(holder.errorNum);
        holder.errorProgress.progress = count
        holder.errorNum.text = String.format(Locale.CHINA, "%d : %d", count, size)

        //加入复习总开关
        if (ListActivity.switch_state) {
            holder.switch_.setOnCheckedChangeListener(null)
            holder.switch_.isChecked = rs.joined
            holder.switch_.visibility = View.VISIBLE
            holder.switch_.setOnCheckedChangeListener { buttonView, isChecked ->
                rs.joined = isChecked
                val mInactivate = parent.data.mInactivate

                //加入复习计划按钮状态改变
                if (isChecked) { //如果没有重复的
                    if (!mInactivate.contains(rs)) {
                        val level = rs.level
                        if (level < 13) {
                            parent.data.sortAddToInactivate(rs)
                        } else { //弹出对话框：要清零还是取消加入
                            Builder(context)
                                    .setTitle("重新开始计划")
                                    .setPositiveButton("确认") { dialogInterface, i ->
                                        rs.level = 0
                                        parent.data.sortAddToInactivate(rs)
                                        notifyDataSetChanged()
                                    }
                                    .setNegativeButton("取消") { dialogInterface, i ->
                                        holder.switch_.isChecked = false
                                        rs.joined = false
                                    }
                                    .show()
                        }
                    }
                } else {
                    parent.data.removeFromInavailable_Available(rs)
                }
            }
        } else holder.switch_.visibility = View.GONE

        //选择&没被选择，的背景区别
        if (rs.selected) {
            holder.view.setBackgroundColor(Color.LTGRAY)
        } else holder.view.background = background
        //条目被长按
        holder.view.setOnLongClickListener(LongClick(rs, position))
        //条目被单击
        holder.view.setOnClickListener(Click(rs, position))
        //播放语音
        holder.playSound.setOnClickListener { Speech.play_Baidu(rs.match.text, holder.playSound) }
    }

    override fun getItemCount(): Int = data.size

    internal inner class LongClick(var reviewStruct: ReviewStruct, private val position: Int) : OnLongClickListener {
        override fun onLongClick(view: View): Boolean { //长按，进入选择状态
            if (checked) {
                if (reviewStruct.selected) {
                    for (rs in data) rs.selected = true
                    ListActivity.switch_state = false
                    parent.buttonDelete.text = "删除"
                    parent.buttonDelete.setBackgroundColor(Color.RED)
                } else {
                    for (rs in data) rs.selected = false
                    ListActivity.switch_state = true
                    parent.buttonDelete.text = "退出"
                    parent.buttonDelete.setBackgroundColor(Color.GREEN)
                }
                notifyDataSetChanged()
            } else {
                parent.buttonDelete.text = "删除"
                parent.buttonDelete.setBackgroundColor(Color.RED)
                checked = true
                reviewStruct.selected = true //设置该条被选择
                notifyItemChanged(position)
                parent.buttonDelete.visibility = View.VISIBLE //显示删除按钮
            }
            return false
        }

    }

    internal inner class Click(var reviewStruct: ReviewStruct, private val position: Int) : OnClickListener {
        override fun onClick(view: View) { //选择按钮被打开
            if (checked) {
                parent.buttonDelete.text = "删除"
                parent.buttonDelete.setBackgroundColor(Color.RED)
                if (ListActivity.switch_state) {
                    ListActivity.switch_state = false
                    notifyDataSetChanged()
                }
                //删除模式，背景的切换
                reviewStruct.selected = !reviewStruct.selected
                notifyItemChanged(position)
            } else { //删除按钮没有打开
//跳转页面，到编辑窗口
                ListActivity.currentClickedRs = reviewStruct
                context.startActivity(Intent(context, EditActivity::class.java))
            }
        }

    }

    inner class Holder(var view: View) : ViewHolder(view) {
        var ivLockUp: ImageView
        var ivLockDown: ImageView
        var errorNum: TextView
        var errorProgress: ProgressBar
        var index: TextView
        var word: TextView
        var explain: TextView
        var progress: ProgressBar
        var levelNumber: TextView
        var switch_: Switch
        var playSound: ImageView

        init {
            index = view.findViewById(R.id.item_textView_index)
            word = view.findViewById(R.id.item_textView_word)
            explain = view.findViewById(R.id.item_textView_explain)
            progress = view.findViewById(R.id.item_progressBar_Level_forward)
            levelNumber = view.findViewById(R.id.item_textView_level_number_up)
            switch_ = view.findViewById(R.id.item_switch_JoinReview)
            playSound = view.findViewById(R.id.item_imageView_play_sound)
            errorProgress = view.findViewById(R.id.item_progressBar_error)
            errorNum = view.findViewById(R.id.item_textView_error_num)
            ivLockUp = view.findViewById(R.id.ivLockUp)
            ivLockDown = view.findViewById(R.id.ivLockDown)
        }
    }

    init {
        parent = context as ListActivity
    }
}