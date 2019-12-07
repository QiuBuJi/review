package com.example.review.Activity

import android.app.Activity
import android.app.AlertDialog.Builder
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Handler.Callback
import android.os.Message
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.OnFlingListener
import android.support.v7.widget.RecyclerView.OnScrollListener
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import com.example.review.DataStructureFile.DateTime
import com.example.review.DataStructureFile.ReviewData
import com.example.review.New.ReviewStruct
import com.example.review.R
import com.example.review.Util.Speech
import java.util.*
import java.util.regex.Pattern

class ListActivity : Activity(), OnClickListener, OnEditorActionListener {
    lateinit var ivBackButton: ImageView
    lateinit var ivAdd: ImageView
    lateinit var ivImport: ImageView
    lateinit var etSearch: EditText
    lateinit var btDelete: Button
    lateinit var title: TextView
    lateinit var floating: FloatingActionButton

    lateinit var list: RecyclerView
    lateinit var adapter: AdapterList
    private var searchList: ReviewData? = null
    private var mUpDown = 0
    lateinit var data: ReviewData
    internal var mDy = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        ivBackButton = findViewById(R.id.list_imageView_back_button)
        ivAdd = findViewById(R.id.imageView_add)
        ivImport = findViewById(R.id.imageView_import)
        etSearch = findViewById(R.id.editText_search)
        btDelete = findViewById(R.id.button_delete)
        list = findViewById(R.id.list_recycler_list)
        title = findViewById(R.id.list_textView_title)
        floating = findViewById(R.id.list_floatingActionButton)

        data = MainActivity.data

        //初始化RecyclerView
        adapter = AdapterList(this, data)
        list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        list.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        list.isVerticalScrollBarEnabled = true
        list.adapter = adapter

        //返回按钮
        ivBackButton.setOnClickListener(this)
        //添加按钮
        ivAdd.setOnClickListener(this)
        //导入按钮，导入电脑数据
        ivImport.setOnClickListener(this)
        //删除按钮被单击
        btDelete.setOnClickListener(this)
        //设置输入法，搜索图标
        etSearch.imeOptions = EditorInfo.IME_ACTION_SEARCH
        //设置IME搜索图标被按下的事件
        etSearch.setOnEditorActionListener(this) //*** onEditorAction ***
        //悬浮按钮，点击事件
        floating.setOnClickListener(this)
        //列表滑动事件
        list.onFlingListener = object : OnFlingListener() {
            override fun onFling(i: Int, i1: Int): Boolean {
                mUpDown = i1
                return false
            }
        }
        //悬浮按钮，长按事件
        floating.setOnLongClickListener {
            val lm = list.getLayoutManager()!!
            val itemCount = lm.itemCount - 1
            list.scrollToPosition(itemCount / 2)
            false
        }
        list.addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                mDy = dy
            }
        })
    }

    //***************************** onEditorAction *****************************
    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean {
        val txt = etSearch.text.toString()

        //输入:回车搜索（被按下）、IME_ACTION_SEARCH 任意一个后，搜索内容
        if (actionId == EditorInfo.IME_ACTION_SEARCH ||
            event.action == KeyEvent.ACTION_DOWN &&
            actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
            searchList = ReviewData()

            if (txt == "") {
                val size = data.size
                var match: String
                var match1: String
                var rsFirst: ReviewStruct
                var rsLast: ReviewStruct
                var hasFound: Boolean

                for (i in 0 until size) {
                    rsFirst = data[i]
                    match = rsFirst.match.text
                    hasFound = false
                    //if (rsFirst.match.getType() != 1) continue;
                    for (k in i + 1 until size) {
                        rsLast = data[k]
                        match1 = rsLast.match.text
                        //if (rsLast.match.getType() != 1) continue;
                        if (match == match1) {
                            hasFound = true
                            searchList!!.add(rsLast)
                        }
                    }
                    if (hasFound) searchList!!.add(rsFirst)
                }
            } else { //遍历数据，查找包含txt内容的数据
                val compile = Pattern.compile("\\d*:.+")
                var index = 0
                //匹配格式
                if (compile.matcher(txt).matches()) {
                    val split = Pattern.compile(":").split(txt)

                    //查找：类型未指定
                    if (split[0] == "") {
                        for (rs in data) {
                            if (rs.match.text == split[1] || rs.show.text == split[1]) {
                                rs.posi = index
                                searchList!!.add(rs)
                            }
                            index++
                        }
                    } else { //查找：指定了类型
                        val num = split[0].toInt()
                        for (rs in data) {
                            if (rs.match.text == split[1] && rs.match.type == num ||
                                rs.show.text == split[1] && rs.show.type == num) {
                                rs.posi = index
                                searchList!!.add(rs)
                            }
                            index++
                        }
                    }
                } else { //模糊查找
                    for (rs in data) {
                        if (rs.match.text.contains(txt) || rs.show.text.contains(txt)) {
                            rs.posi = index
                            searchList!!.add(rs)
                        }
                        index++
                    }
                }
            }
            if (searchList!!.size > 0) {
                adapter = AdapterList(this@ListActivity, searchList!!)
                list.adapter = adapter
                Toast.makeText(this@ListActivity, "一共搜索到" + searchList!!.size + "个", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this@ListActivity, "没有找到数据！", Toast.LENGTH_SHORT).show()
        }
        return true
    }

    private fun importData() { //弹出对话框
        val builder = Builder(this@ListActivity)
        builder.setPositiveButton("确定", onClickListener)
        builder.setNegativeButton("取消", null)
        builder.setIcon(R.mipmap.warnning_icon1)
        builder.setTitle("确定要导入数据&删除所有当前数据？")
        builder.show()
    }

    //进度条******************************************************************************
    private val onClickListener: DialogInterface.OnClickListener
        get() = DialogInterface.OnClickListener { dialog, which ->
            //进度条******************************************************************************
            val pd = ProgressDialog(this@ListActivity)
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            pd.setTitle("进度：")

            //handler过程
            val handler = Handler(Callback { message ->
                when (message.what) {
                    1 -> pd.setTitle(message.obj as CharSequence)
                    0 -> {
                        pd.dismiss()
                        Toast.makeText(this@ListActivity, "一共导入" + data.size + "个", Toast.LENGTH_SHORT).show()
                        adapter.notifyDataSetChanged()
                    }
                }
                false
            })

            //进度回调函数*************************************************************************
            val temp = System.currentTimeMillis()
            data.progressListener = object : ReviewData.ProgressListener {
                override fun onProgress(total: Int, posi: Int) {
                    pd.max = total
                    pd.incrementProgressBy(1)
                    var millis = System.currentTimeMillis() - temp
                    millis /= 1000
                    val msg = Message()
                    msg.obj = millis.toString() + "秒"
                    msg.what = 1
                    handler.sendMessage(msg)
                }
            }

            pd.show()

            //开启线程读取数据*********************************************************************
            Thread(Runnable {
                data.loadDataOf(MainActivity.pathInit)
                handler.sendEmptyMessage(0)
            }).start()
        }

    override fun onStart() {
        super.onStart()
        switchState = false
        adapter.notifyDataSetChanged()
        checked = false
    }

    override fun onBackPressed() {
        if (checked) {
            switchState = false
            btDelete.visibility = View.GONE //隐藏删除按钮

            //把dt数据中的checked复位false，这样recyclerView就不会显示灰色背景了
            for (dt in data) dt.selected = false
            adapter.notifyDataSetChanged()
            checked = false
        } else {
            if (searchList != null) {
                adapter = AdapterList(this@ListActivity, data)
                list.adapter = adapter
                etSearch.setText("")
                searchList = null
                adapter.notifyDataSetChanged()
            } else finish()
        }
    }

    override fun onClick(v: View) {
        val count = 0
        when (v.id) {
            R.id.list_imageView_back_button -> {
                //把dt数据中的checked复位false，这样recyclerView就不会显示灰色背景了
                for (rs in data) rs.selected = false
                finish()
            }
            R.id.imageView_add -> {
                currentClickedRs = null
                startActivity(Intent(this@ListActivity, EditActivity::class.java))
            }
            R.id.imageView_import -> importData()
            R.id.button_delete -> buttonDelete(count)
            R.id.list_floatingActionButton -> {
                val scrollState = list.scrollState
                val lm = list.layoutManager!!
                val itemCount = lm.itemCount - 1

                //在滑动的情况下
                if (scrollState > 0) {
                    if (mUpDown < 0) //上滑
                        list.scrollToPosition(0) else if (mUpDown > 0) //下滑
                        list.scrollToPosition(itemCount)
                } else { //下滑
                    if (mDy >= 0) { //向下寻找未加入复习的
                        var i = adapter.posi
                        while (i < data.size) {
                            val ele = data[i]
                            if (!ele.joined) {
                                list.scrollToPosition(i)
                                Toast.makeText(this, "位置:" + (i + 1), Toast.LENGTH_SHORT).show()
                                break
                            }
                            i++
                        }
                    } else { //上滑
                        //向上寻找未加入复习的
                        var i = adapter.posi
                        while (i >= 0) {
                            val ele = data[i]
                            if (!ele.joined) {
                                list.scrollToPosition(i)
                                Toast.makeText(this, "位置:" + (i + 1), Toast.LENGTH_SHORT).show()
                                break
                            }
                            i--
                        }
                    }
                }
            }
        }
    }

    private fun buttonDelete(count: Int) {
        var count = count
        val txt = btDelete.text.toString()
        if (txt == "退出") {
            onBackPressed()
            data.save()
            return
        }

        //搜索状态下，删除条目
        if (searchList != null) {
            var index = 0
            while (index < searchList!!.size) {
                val rs = searchList!![index]

                //checked为true，则删除该条项目
                if (rs.selected) {
                    data.removeFromInavailable_Available(rs)
                    data.removeAt(rs.posi)
                    data.removeLibraryItem(rs.posi * 2)
                    for (i in index until searchList!!.size) {
                        val reviewStruct = searchList!![i]
                        reviewStruct.posi--
                    }

                    //通知adapter它的posi位置上的数据被删除了
                    searchList!!.removeAt(index)
                    adapter.notifyItemRemoved(index)
                    index-- //删除了1像数据，它的位置不变。这里自减1，下次加一就和原来一样了
                    count++
                }
                index++
            }
        } else { //正常状态，删除条目
            var index = 0
            while (index < data.size) {
                val rs = data[index]

                //checked为true，则删除该条项目
                if (rs.selected) {
                    data.removeFromInavailable_Available(rs)
                    data.removeAt(index)
                    data.removeLibraryItem(index * 2)

                    //通知adapter它的posi位置上的数据被删除了
                    adapter.notifyItemRemoved(index)
                    index-- //删除了1像数据，它的位置不变。这里自减1，下次加一就和原来一样了
                    count++
                }
                index++
            }
        }
        val sizeData = data.size * 2
        val sizeLibrary = data.library.size
        check(sizeData == sizeLibrary) { "数据不一致" }

        //显示删除了多少条目
        Toast.makeText(this@ListActivity, "一共删除" + count + "项", Toast.LENGTH_SHORT).show()
        val handler = Handler(Callback {
            onBackPressed()
            false
        })

        //延时启动handler
        var ms = 500
        if (count == 0) ms = 0
        Timer().schedule(object : TimerTask() {
            override fun run() {
                handler.sendEmptyMessage(0)
            }
        }, ms.toLong())

        //如果数据有更改，保存数据
        data.save()
        data.saveLibrary()
    }

    override fun onDestroy() {
        super.onDestroy()
        data.save() //保存数据
    }

    companion object {
        var checked = false
        var switchState = false
        var currentClickedRs: ReviewStruct? = null
    }

    class AdapterList(private val context: Context, private val data: ReviewData) : RecyclerView.Adapter<AdapterList.Holder>() {
        private var background: Drawable? = null
        private val parent: ListActivity = context as ListActivity
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
            holder.index.setTextColor(if (rs.joined) -0x27e4a0 else -0x8b8b8c)
//          holder.index.setTypeface(Typeface.defaultFromStyle(if (rs.joined)Typeface.BOLD else Typeface.NORMAL));

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
            //holder.errorProgress.setProgress(size);
            //SpanUtil.create()
            //        .addForeColorSection(size + "  ", Color.BLACK)
            //        .addForeColorSection(count + "％", Color.WHITE)
            //        .showIn(holder.errorNum);
            holder.errorProgress.progress = count
            holder.errorNum.text = String.format(Locale.CHINA, "%d : %d", count, size)

            //加入复习总开关
            if (switchState) {
                holder.switch.setOnCheckedChangeListener(null)
                holder.switch.isChecked = rs.joined
                holder.switch.visibility = View.VISIBLE
                holder.switch.setOnCheckedChangeListener { buttonView, isChecked ->
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
                                            holder.switch.isChecked = false
                                            rs.joined = false
                                        }
                                        .show()
                            }
                        }
                    } else {
                        parent.data.removeFromInavailable_Available(rs)
                    }
                }
            } else holder.switch.visibility = View.GONE

            //选择&没被选择，的背景区别
            if (rs.selected) holder.view.setBackgroundColor(Color.LTGRAY)
            else holder.view.background = background

            //条目被长按
            holder.view.setOnLongClickListener(LongClick(rs, position))
            //条目被单击
            holder.view.setOnClickListener(Click(rs, position))
            //播放语音
            holder.playSound.setOnClickListener { Speech.play_Baidu(rs.match.text, holder.playSound) }
        }

        override fun getItemCount(): Int = data.size

        internal inner class LongClick(private var reviewStruct: ReviewStruct, private val position: Int) : View.OnLongClickListener {
            override fun onLongClick(view: View): Boolean { //长按，进入选择状态
                if (checked) {
                    if (reviewStruct.selected) {
                        for (rs in data) rs.selected = true
                        ListActivity.switchState = false
                        parent.btDelete.text = "删除"
                        parent.btDelete.setBackgroundColor(Color.RED)
                    } else {
                        for (rs in data) rs.selected = false
                        ListActivity.switchState = true
                        parent.btDelete.text = "退出"
                        parent.btDelete.setBackgroundColor(Color.GREEN)
                    }
                    notifyDataSetChanged()
                } else {
                    parent.btDelete.text = "删除"
                    parent.btDelete.setBackgroundColor(Color.RED)
                    checked = true
                    reviewStruct.selected = true //设置该条被选择
                    notifyItemChanged(position)
                    parent.btDelete.visibility = View.VISIBLE //显示删除按钮
                }
                return false
            }

        }

        internal inner class Click(private var reviewStruct: ReviewStruct, private val position: Int) : OnClickListener {
            override fun onClick(view: View) { //选择按钮被打开
                if (checked) {
                    parent.btDelete.text = "删除"
                    parent.btDelete.setBackgroundColor(Color.RED)
                    if (switchState) {
                        switchState = false
                        notifyDataSetChanged()
                    }
                    //删除模式，背景的切换
                    reviewStruct.selected = !reviewStruct.selected
                    notifyItemChanged(position)

                } else { //删除按钮没有打开
                    //跳转页面，到编辑窗口
                    currentClickedRs = reviewStruct
                    context.startActivity(Intent(context, EditActivity::class.java))
                }
            }

        }

        inner class Holder(var view: View) : RecyclerView.ViewHolder(view) {
            var ivLockUp: ImageView = view.findViewById(R.id.ivLockUp)
            var ivLockDown: ImageView = view.findViewById(R.id.ivLockDown)
            var errorNum: TextView = view.findViewById(R.id.item_textView_error_num)
            var errorProgress: ProgressBar = view.findViewById(R.id.item_progressBar_error)
            var index: TextView = view.findViewById(R.id.item_textView_index)
            var word: TextView = view.findViewById(R.id.item_textView_word)
            var explain: TextView = view.findViewById(R.id.item_textView_explain)
            var progress: ProgressBar = view.findViewById(R.id.item_progressBar_Level_forward)
            var levelNumber: TextView = view.findViewById(R.id.item_textView_level_number_up)
            var switch: Switch = view.findViewById(R.id.item_switch_JoinReview)
            var playSound: ImageView = view.findViewById(R.id.item_imageView_play_sound)
        }

    }
}