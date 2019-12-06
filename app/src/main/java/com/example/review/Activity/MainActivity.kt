package com.example.review.Activity

import android.annotation.TargetApi
import android.app.AlertDialog.Builder
import android.app.NotificationManager
import android.content.*
import android.graphics.Color
import android.graphics.Point
import android.os.*
import android.os.Handler.Callback
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.LayoutParams
import android.text.Editable
import android.text.Html
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import com.example.review.Adapter.MyAdapter
import com.example.review.Animator.TextColorAnimator
import com.example.review.DataStructureFile.DateTime
import com.example.review.DataStructureFile.ElementCategory.Category
import com.example.review.DataStructureFile.ReviewData
import com.example.review.DataStructureFile.ReviewData.AvailableUpdate
import com.example.review.DataStructureFile.ReviewData.StateSave
import com.example.review.Keyboard.Keyboard
import com.example.review.Keyboard.Keyboard.OnKeyDownListener
import com.example.review.Keyboard.KeyboardType1
import com.example.review.Keyboard.KeyboardType2
import com.example.review.Keyboard.KeyboardType3
import com.example.review.New.CountList
import com.example.review.New.KeyText
import com.example.review.New.ReviewStruct
import com.example.review.R
import com.example.review.ReviewService
import com.example.review.ReviewService.LocalBinder
import com.example.review.Setting
import com.example.review.SortLib
import com.example.review.Util.ColorfulText
import com.example.review.Util.SpanUtil
import com.example.review.Util.Speech
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), ServiceConnection, Callback {
    //**************************************** Views ************************************************
    internal lateinit var textViewPercent: TextView
    internal lateinit var tvLastDuration: TextView
    internal lateinit var textViewArrival: TextView
    internal lateinit var textViewAbout: TextView
    internal lateinit var tvReviewedNum: TextView
    internal lateinit var textViewTime: TextView
    internal lateinit var lastText: TextView
    internal lateinit var tvTitle: TextView
    internal lateinit var tvLevel: TextView
    internal lateinit var tvNext: TextView
    internal lateinit var tips: TextView
    internal lateinit var imageViewPlaySound: ImageView
    internal lateinit var imageViewDetail: ImageView
    internal lateinit var imageViewSort: ImageView
    internal lateinit var imageButtonSetting: ImageButton
    internal lateinit var editSorts: ImageButton
    internal lateinit var recyclerViewKeyboard: RecyclerView
    internal lateinit var progressBarProgress: ProgressBar
    internal lateinit var etInput: EditText
    internal lateinit var entireBackground: ConstraintLayout
    internal lateinit var mainContainer: ConstraintLayout
    //***********************************************************************************************
    internal val HANDLER_UPDATE_SHOWING = 4
    internal val HANDLER_START_TIMER = 3
    internal var correct = false
    internal var handler = Handler(Callback { msg: Message -> handleMessage(msg) })
    lateinit var service: ReviewService
    internal var keyboard: Keyboard? = null
    internal var mReviewedNum = 0
    internal var libIndex = 0
    internal var state = 2
    internal lateinit var pathBoth: List<PathBoth>
    internal var clearInput = 0
    //***********************************************************************************************
    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            1 -> {
            }
            HANDLER_START_TIMER -> {
            }
            HANDLER_UPDATE_SHOWING -> refreshShowing(true)
            5 -> {
            }
        }
        return false
    }

    //刷新即将到来的时间
    private fun refreshArrivalTime() { //显示下一个待复习数据到现在的剩余时间
        if (!data.mInactivate.isEmpty()) {
            val dateTime = DateTime(data.mInactivate[0].time)
            dateTime.subtractOf(DateTime.getCurrentTime())
            textViewArrival.text = dateTime.toNoneZeroString()
        }
    }

    internal var lastRs: ReviewStruct? = null
    //刷新显示界面文字
    internal fun refreshShowing(isChange: Boolean) {
        if (keyboard != null) keyboard!!.stop()
        //软件界面没有显示，则不启动
        if (!isShowedScreen) return
        //初始化显示界面
        mainContainer.setBackgroundResource(R.drawable.bg_text_show)
        //避开下标越界，有复习数据*********************************************************************
        if (!data.mActivate.isEmpty()) {
            val rs = data.mActivate.first
            tvLevel.text = rs.level.toString()
            tips.text = ""
            //显示距离上次复习间隔了多久
            val dateTime: DateTime
            var text: String
            try {
                dateTime = DateTime(rs.logs.last)
                val subtract: DateTime = DateTime.getCurrentTime().subtract(dateTime)
                text = subtract.toAboutValue()
            } catch (e: Exception) {
                text = "编辑"
            }
            SpanUtil.create()
                    .addUnderlineSection(text)
                    .showIn(tvLastDuration)
            //跳转页面，到编辑窗口
            tvLastDuration.setOnClickListener { view: View? ->
                ListActivity.currentClickedRs = rs
                this@MainActivity.startActivity(Intent(this@MainActivity, EditActivity::class.java))
            }
            //不让重复刷新
            if (isChange || lastRs !== rs) {
                lastRs = rs
                val type = rs.match.type
                when (type) {
                    Keyboard.TYPE_WORD -> {
                        keyboard = KeyboardType1(this, recyclerViewKeyboard, mainContainer, etInput, rs)
                        keyboard!!.buildKeyboard()
                    }
                    Keyboard.TYPE_EXPLAIN -> {
                        keyboard = KeyboardType2(this, recyclerViewKeyboard, mainContainer, etInput, rs)
                        keyboard!!.buildKeyboard()
                    }
                    Keyboard.TYPE_CHOOSE -> {
                        keyboard = KeyboardType3(this, recyclerViewKeyboard, mainContainer, etInput, rs)
                        keyboard!!.buildKeyboard()
                    }
                    Keyboard.TYPE_PICTURE -> {
                    }
                    Keyboard.TYPE_SOUND -> {
                    }
                }
            }
            if (keyboard != null) {
                keyboard!!.onKeyDownListener = getOnKeyDownListener()
                keyboard!!.refresh()
            }
            state = 1
        } else { //当没有复习数据时，要配置的参数************************************************************
            state = 2
            lastRs = null
            if (keyboard != null) keyboard!!.clear()
            etInput.showSoftInputOnFocus = true
            tvLevel.text = "☺"
            tvLastDuration.text = "---"
            tvLastDuration.setOnClickListener(null)
            etInput.hint = ""
            etInput.isEnabled = true
            etInput.inputType = InputType.TYPE_CLASS_TEXT
            //显示界面，显示文字“当前没有复习计划”
            mainContainer.removeAllViews()
            val textView = TextView(this)
            textView.hint = "当前没有复习计划"
            textView.gravity = Gravity.CENTER
            val lp = LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
            textView.layoutParams = lp
            mainContainer.addView(textView)
            if (state == 1 && !data.isEmpty()) data.save()
            if (keyboard != null) keyboard!!.clearKeyboard()
            data.save()
        }
    }

    private fun getOnKeyDownListener(): OnKeyDownListener {
        return object : OnKeyDownListener {
            override fun onKeyDown(keyText: KeyText) {
                if (keyText.isCom && keyText.text == Keyboard.COM_DONE) matchInput()
            }
        }
    }

    //刷新进度信息
    private fun refreshProgress() {
        val activateSize = data.mActivate.size
        val InactivateSize = data.mInactivate.size
        progressBarProgress.progress = activateSize
        progressBarProgress.max = activateSize + InactivateSize
        textViewPercent.text = String.format(Locale.CHINA, "%d : %d", activateSize, InactivateSize)
        tvReviewedNum.text = mReviewedNum.toString()
    }

    /**
     * Feature Log
     *
     *
     * 2019年4月12日 MainActivity主界面更改、增加新功能
     *
     *
     * 2019年4月13日 ListActivity创建与完善、EditActive&activity_edit内容创建与完善、activity_list、activity_item完善、保存数据
     *
     *
     * 2019年4月14日 EditActive&activity_edit内容创建与完善、activity_list、activity_item完善、保存数据
     *
     *
     * 2019年4月15日 调用讯飞语音.jar来生成语音实现发音功能、完善EditActive&activity_edit内容、创建AboutActivity&Activity_about和其他
     *
     *
     * 2019年4月16日 百度语音.jar来生成语音实现发音功能（未能发音成功）、完善Speech类、其它小更改
     *
     *
     * 2019年4月17日 懒得写记录...
     *
     *
     * 2019年4月18日 懒得写记录...
     *
     *
     * 2019年4月19日 懒得写记录...
     *
     *
     * 2019年4月20日 找了一天关于Android Studio问题的解决办法，晚上才终于可以正常开发
     *
     *
     * 2019年4月21日 懒得写记录...
     *
     *
     * 2019年4月22日 懒得写记录...
     *
     *
     * 2019年4月23日 懒得写记录...
     *
     *
     * 2019年4月24日 增加notification、一些小修改、
     *
     *
     * 2019年4月25日 懒得写记录...
     *
     *
     * 2019年4月26日 懒得写记录...
     *
     *
     * 2019年4月27日 懒得写记录...
     *
     *
     * 2019年4月28日 半天
     *
     *
     * 2019年4月29日 半天
     *
     *
     * 2019年4月30日 半天
     *
     *
     * 2019年5月01日 半天
     *
     *
     * 2019年5月02日 半天
     *
     *
     * 2019年5月03日 半天
     *
     *
     * 2019年5月04日 半天，自己的键盘布局，和其他
     *
     *
     * 2019年5月05日 半天，在库中挑选内容到，编辑或者添加复习内容的界面中，自己的键盘布局的一些修改
     *
     *
     * 2019年5月06日 半天，增加了复习中&待复习中的复习中列表界面，可以用蓝牙键盘输入
     *
     *
     * 2019年5月07日 半天
     *
     *
     * 2019年5月08日 半天，单词解释的复习功能、其他
     *
     *
     * 2019年5月09日 半天，一些界面的小更改、其他...
     *
     *
     * 2019年5月10日 半天
     *
     *
     * 2019年5月11日 半天，自己的键盘，的一些修改，提取类，方便以后开发
     *
     *
     * 2019年5月12日 图片选择对话框、键盘优化、显示图片
     *
     *
     * 2019年5月13日 播放显示内容的声音、
     *
     *
     * 2019年5月16日 增加播放语音库功能、百度语音tts
     *
     *
     * 2019年5月17日 半天
     *
     *
     * 2019年5月18日 半天
     *
     *
     * 2019年5月25日 半天，一些小修改、修复bug
     *
     *
     * 2019年6月11日 半天，简单的大纲视图、修复一些bug
     *
     *
     * 2019年6月1*日 半天，选择库内容，比如单词库
     * 2019年6月23日 2hour 单词未发音，点击文字显示单词文字。
     */
    //主窗口被创建-----------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //界面数据保存 SharedPreferences
        Setting.init(this)

        var prefix = ""
        pathBoth = getPathBoth()
        if (pathBoth.isNotEmpty()) {
            val libName = Setting.sp!!.getString("libName", "")

            for (pb in pathBoth) {
                if (pb.prefix == libName) {
                    libIndex = pathBoth.indexOf(pb)
                    break
                }
            }

            val pathBoth = pathBoth[libIndex]
            pathNexus = File(pathApp, pathBoth.nexus)
            pathLibrary = File(pathApp, pathBoth.library)
            prefix = pathBoth.prefix
        }
        //***********************************************************************************************

        //启动&绑定服务
        val intentService = Intent(this, ReviewService::class.java)
        startService(intentService)
        bindService(intentService, this, Context.BIND_AUTO_CREATE)

        //各种初始化
        initViews()
        initListener()
        initVariable()

        tvTitle.text = prefix
    }

    override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
        val binder = iBinder as LocalBinder
        service = binder.service
        data = service.data
        sorts = service.sortLib
        dataPrepared()
    }

    internal fun dataPrepared() {
        data.setOnAvailableUpdate(object : AvailableUpdate {
            override fun onUpdateToAvailableComplete(count_: Int) {
                refreshShowing(false)
            }

            override fun onUpdatingToAvailable(dtUnion: ReviewStruct?) {
                if (SortActivity.fragment != null) {
                    SortActivity.fragment!!.adapter.notifyItemRemoved(0)
                    if (!SortActivity.fragment!!.mData.isEmpty()) {
                        val rs: ReviewStruct = SortActivity.fragment!!.mData.get(0)
                        rs.showed = true
                    }
                    SortActivity.fragment!!.adapter.notifyItemChanged(0)
                }
            }

            override fun onUpdatedNoChange() {
                if (SortActivity.fragment != null) {
                    val scrollState: Int = SortActivity.fragment!!.recyclerView.getScrollState()
                    val xPosi: Int = SortActivity.xPosi
                    //不上下、左右滚动后，可以通知数据改变
                    if (scrollState == 0 && xPosi == 0) SortActivity.fragment!!.adapter.notifyDataSetChanged()
                }
                refreshArrivalTime()
                refreshProgress()
            }
        })
        data.setOnSave(object : StateSave {
            override fun onSaveCalled() {
                tips.text = "保存数据中..."
            }

            override fun onSaveComplete() {
                tips.text = ""
                //Toast.makeText(MainActivity.this, "数据保存完毕！", Toast.LENGTH_SHORT).show();
            }
        })

        refreshArrivalTime()
        refreshProgress()
        refreshShowing(false)
    }

    override fun onServiceDisconnected(componentName: ComponentName) {}

    //初始化Views
    private fun initViews() {
//        textViewTime = findViewById(R.id.fragment_textView_time)
        textViewAbout = findViewById(R.id.main_textView_about)
        textViewPercent = findViewById(R.id.main_textView_persent)
        textViewArrival = findViewById(R.id.main_textView_time_arrival)
        imageViewDetail = findViewById(R.id.main_imageView_Detail)
        imageViewSort = findViewById(R.id.main_imageView_sort)
        imageViewPlaySound = findViewById(R.id.main_imageView_play_sound)
        progressBarProgress = findViewById(R.id.main_progressBar_progress)
        etInput = findViewById(R.id.main_editText_input)
        tips = findViewById(R.id.main_textView_tips)
        lastText = findViewById(R.id.main_textView_lastText)
        imageButtonSetting = findViewById(R.id.main_imageButton_setting)
        recyclerViewKeyboard = findViewById(R.id.main_recycllerView_keyboard)
        tvLevel = findViewById(R.id.main_textView_level)
        entireBackground = findViewById(R.id.entire_background)
        tvTitle = findViewById(R.id.main_about_textView_title)
        tvNext = findViewById(R.id.main_textView_next)
        tvReviewedNum = findViewById(R.id.main_textView_reviewedNum)
        mainContainer = findViewById(R.id.cl_main_container)
        tvLastDuration = findViewById(R.id.tvLastDuration)
        editSorts = findViewById(R.id.main_imageButton_editSorts)
    }

    //被单击监听器
    internal val onClickListener: OnClickListener
        get() = OnClickListener { view: View ->
            when (view.id) {
                R.id.main_imageView_sort -> pickDataDialog()
                R.id.main_textView_next -> if (!data.mActivate.isEmpty()) {
                    val first = data.mActivate.removeFirst()
                    data.mActivate.addLast(first)
                    refreshShowing(true)
                }
                R.id.main_textView_about -> startActivity(Intent(this@MainActivity, AboutActivity::class.java))
                R.id.main_imageView_play_sound -> Speech.play_Baidu(etInput.text.toString(), imageViewPlaySound)
                R.id.main_imageView_Detail -> startActivity(Intent(this@MainActivity, ListActivity::class.java))
                R.id.main_progressBar_progress -> startActivity(Intent(this@MainActivity, SortActivity::class.java))
                R.id.main_imageButton_setting -> startActivity(Intent(this@MainActivity, SettingActivity::class.java))
                R.id.main_textView_tips -> tips()
                R.id.main_textView_time_arrival -> {
                    val intent = Intent(this@MainActivity, SortActivity::class.java)
                    intent.putExtra("posi", 2)
                    startActivity(intent)
                }
                R.id.fragment_textView_textShow -> {
                }
                R.id.main_editText_input -> {
                }
                R.id.main_imageButton_editSorts -> startActivity(Intent(this, EditSortsActivity::class.java))
            }
        }

    private fun tips() {
        if (!data.mActivate.isEmpty()) {
            val rs = data.mActivate.first
            val sb = getTips(rs)
            Toast.makeText(this@MainActivity, sb, Toast.LENGTH_LONG).show()
            rs.resetLevel() //重置水平
            if (rs.match.type == 1) Speech.play_Baidu(rs.match.text) //播放单词发音
            tvLevel.text = String.format("%d", rs.level)
            correct = false
            canJoinLog++
            addLog(rs)
            clearInput = 1
        }
    }

    //初始化监听器-----------------------------------------------------------------------------------
    private fun initListener() { //显示框背景
        textViewArrival.setOnClickListener(onClickListener)
        //输入框
        etInput.addTextChangedListener(watcher)
        //分类列表按钮
        imageViewSort.setOnClickListener(onClickListener)
        //关于按钮
        textViewAbout.setOnClickListener(onClickListener)
        //播放音频按钮
        imageViewPlaySound.setOnClickListener(onClickListener)
        //跳转页面到ActivityList
        imageViewDetail.setOnClickListener(onClickListener)
        //待复习进度条被单击
        progressBarProgress.setOnClickListener(onClickListener)
        //设置界面
        imageButtonSetting.setOnClickListener(onClickListener)
        //输入框的一些行为
        etInput.imeOptions = EditorInfo.IME_ACTION_DONE
        etInput.setOnEditorActionListener(onEditorActionListener)
        //下一个被单击
        tvNext.setOnClickListener(onClickListener)
        //Toast显示提示内容
        tips.setOnClickListener(onClickListener)
        //居中显示提示内容
        tips.setOnLongClickListener(longClickListener)
        //长按显示上一个复习条目
        tvNext.setOnLongClickListener(longClickListener)
        //长按进入复习库配置
        imageViewSort.setOnLongClickListener(longClickListener)
        editSorts.setOnClickListener(onClickListener)
    }

    private val longClickListener: OnLongClickListener
        get() = OnLongClickListener { view: View ->
            when (view.id) {
                R.id.main_imageView_sort -> startActivityForResult(Intent(this@MainActivity, MoveDataActivity::class.java), 1)
                R.id.main_textView_tips -> if (!data.mActivate.isEmpty()) {
                    val rs = data.mActivate.first
                    val size = Point()
                    val inflate = View.inflate(this@MainActivity, R.layout.activity_popup_window, null)
                    val showText = inflate.findViewById<TextView>(R.id.popupWindow_textView_txt)
                    windowManager.defaultDisplay.getSize(size)
                    val popupWindow = PopupWindow(inflate, -2, -2)
                    showText.text = getTips(rs)
                    popupWindow.isOutsideTouchable = true
                    popupWindow.showAtLocation(entireBackground, Gravity.CENTER, 0, 0)
                    rs.resetLevel()
                    if (rs.match.type == 1) Speech.play_Baidu(rs.match.text)
                    correct = false
                    canJoinLog++
                    addLog(rs)
                    refreshShowing(false)
                }
                R.id.main_textView_next -> if (!data.mActivate.isEmpty()) {
                    val last = data.mActivate.removeLast()
                    data.mActivate.addFirst(last)
                    refreshShowing(true)
                }
            }
            true
        }//输入错误后，下次输入则要清空除位置1以后的内容//用于不重复

    //输入框输入监听器
    private val watcher: TextWatcher
        get() = object : TextWatcher {
            var start_ = 0
            var state1 = 1 //用于不重复
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                start_ = start
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) { //输入错误后，下次输入则要清空除位置1以后的内容
                if (clearInput == 2) {
                    clearInput = 0
                    etInput.text.delete(1, etInput.length())
                }
            }

            override fun afterTextChanged(s: Editable) {
                etInput.setTextColor(Color.BLACK)
                val text = etInput.text.toString()
                val str = lastText.text.toString()
                if (text == "") {
                    if (state1 == 2) return
                    state1 = 2
                    val section = "上个单词: "
                    SpanUtil.create()
                            .addAbsSizeSection(section, 28)
                            .setForeColor(section, Color.GRAY)
                            .addForeColorSection(str, Color.BLACK)
                            .showIn(lastText)
                } else {
                    lastText.text = text
                    lastText.setTextColor(Color.BLACK)
                    state1 = 1
                }
            }
        }

    private val onEditorActionListener: OnEditorActionListener
        get() = OnEditorActionListener { v, actionId, event ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_UNSPECIFIED -> matchInput()
            }
            true
        }

    //初始化变量-------------------------------------------------------------------------------------
    internal fun initVariable() {
        Speech.initVoice(this)
        Thread(Runnable { Speech.initVoice_Baidu(this@MainActivity) }).start()
    }

    override fun onDestroy() {
        super.onDestroy()
        //        unbindService(this);
        Speech.release_Baidu()
    }

    internal var isShowedScreen = false
    override fun onStart() {
        super.onStart()
        mReviewedNum = 0
        isShowedScreen = true
        refreshProgress()
        //如果打开过编辑窗口，则要刷新显示界面数据
        if (ListActivity.currentClickedRs != null) {
            ListActivity.currentClickedRs = null
            refreshShowing(true)
        } else refreshShowing(false)
        //延迟刷新界面，不延迟则界面刷新不了
        if (keyboard != null) {
            Handler(Callback { message: Message? ->
                keyboard!!.refresh()
                false
            }).sendEmptyMessageDelayed(0, 20)
        }
        val notifyManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifyManager.cancel(1)
    }

    override fun onPause() {
        super.onPause()
        isShowedScreen = false
        data.save()
        if (keyboard != null) keyboard!!.stop()
    }

    inner class PathBoth(var nexus: String, var library: String, var prefix: String)

    //取提示
    private fun getTips(rs: ReviewStruct): String {
        var sb = StringBuilder()
        if (keyboard is KeyboardType2) {
            val kt = keyboard as KeyboardType2
            for (we in kt.frameRight!!) {
                val explains = we.toString()
                sb.append(explains + "\n")
            }
        } else sb.append(rs.match.text)
        val index = sb.length - 1
        val c = sb[index]
        if (c == '\n') sb = sb.replace(index, index + 1, "")
        return sb.toString()
    }

    //选择要复习的库
    private fun pickDataDialog() {
        pathBoth = getPathBoth()
        //没有文件则不执行后续代码
        if (pathBoth == null) return
        val names: MutableList<String> = LinkedList()
        for (pathBoth in pathBoth!!) names.add(pathBoth.prefix)
        val strings = arrayOfNulls<String>(names.size)
        for (i in strings.indices) strings[i] = names[i]
        Builder(this)
                .setTitle("选择：")
                .setItems(strings) { dialogInterface: DialogInterface?, i: Int ->
                    libIndex = i
                    val pathBoth = pathBoth!![i]
                    pathNexus = File(pathApp, pathBoth.nexus)
                    pathLibrary = File(pathApp, pathBoth.library)
                    Setting.edit?.putString("libName", this@MainActivity.pathBoth!![libIndex].prefix)?.commit()
                    tvTitle.text = pathBoth.prefix
                    textViewArrival.text = "00:00"
                    try {
                        service.initData()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    refreshShowing(true)
                }.show()
    }

    private fun getPathBoth(): List<PathBoth> {
        val list = pathApp.list { file: File?, str: String ->
            str.endsWith("nexus") ||
            str.endsWith("library")
        } ?: return ArrayList()
        val paths: MutableList<PathBoth> = LinkedList()
        val strs: MutableList<String> = LinkedList(listOf(*list))

        var index = 0
        while (index < strs.size) {
            val str = strs[index]
            val name = str.split(Regex("\\."))

            //不符合格式的把它去除掉，再重新开始
            if (name.size != 2) {
                strs.removeAt(index--)
                continue
            }
            strs.removeAt(index--)

            var newName: String
            when (name[1]) {
                "nexus" -> {
                    newName = name[0] + ".library"
                    if (strs.contains(newName)) {
                        strs.remove(newName)
                        paths.add(PathBoth(str, newName, name[0]))
                    }
                }
                "library" -> {
                    newName = name[0] + ".nexus"
                    if (strs.contains(newName)) {
                        strs.remove(newName)
                        paths.add(PathBoth(newName, str, name[0]))
                    }
                }
            }
            index++
        }
        return paths
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == 1) {
            val paths = data!!.getStringArrayListExtra("paths")
            if (paths != null) {
                var prefix = ""
                var postfix: String
                for (path in paths) {
                    var split = path.split("/").toTypedArray()
                    split = split[split.size - 1].split("\\.").toTypedArray()
                    if (split.size == 2) {
                        prefix = split[0]
                        postfix = split[1]
                        if (postfix == "nexus") pathNexus = File(pathApp, String.format("%s.%s", prefix, postfix)) else pathLibrary = File(pathApp, String.format("%s.%s", prefix, postfix))
                    }
                }
                tvTitle.text = prefix
                textViewArrival.text = "00:00"
                try {
                    service.initData()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                refreshShowing(true)
            }
        }
    }

    /* new设想，2019年7月18日
     *
     *      word: compare,you,@0123(引用该单词的ID，4字节)
     *   explain: vi.比较;vt.比拟、誉为；
     *   picture: storage/emulate/0/Image/what the fuck?.jpg
     *     sound: storage/emulate/0/sound/what the.mp3
     *     frameInput: @meat,@ant
     * candidate: @_auto_,@meat(交集、差集),you,me,him,单击,大家好,shit
     *  previous: @118(引用该单词的ID，4字节),@112(previous引用的暂时都记住了，才考虑复习这条单词)
     *        id: 666(该id由系统自动分配，4字节)
     *      type: 单词、解释、填空
     *      hello world!
     *  todo above is detail ↑↑↑
     * */
//输入匹配---------------------------------------------------------------------------------------
    internal fun matchInput() {
        val inputText = etInput.text.toString()
        //避开下标越界
        if (data.mActivate.isEmpty()) { //            tvShow.setHint("暂时没有复习的");
            return
        }
        //        etInput.setText(inputText);//清楚文字残留颜色
        val rs = data.mActivate.first
        val cl = CountList()
        val type = rs.match.type
        when (type) {
            1 -> {
                correct = rs.matching(inputText)
                rs.viewCount++
                correctProc(inputText, rs)
            }
            2 -> {
                val keyboardType2 = keyboard as KeyboardType2?
                correct = rs.matching(keyboardType2!!.frameInput!!, cl)
                if (correct) correctAction(rs) else errorAction(cl)
            }
            3 -> {
                val keyboardType3 = keyboard as KeyboardType3?
                correct = rs.matchingType3(keyboardType3!!.mCandidateType, keyboardType3.mCandidate!!, cl)
                if (correct) correctAction(rs) else errorAction(cl)
            }
            4 -> {
            }
        }
        addLog(rs)
    }

    private fun correctAction(rs: ReviewStruct) {
        canJoinLog = 0
        data.updateInavalable_AddLevel(rs)
        mReviewedNum++
        //下面监听器，等颜色动画播放完毕，然后显示下一条数据在textShow中
        keyboard!!.setLightAnimation(false, duration)
        toNextItem(duration)
    }

    private fun errorAction(cl: CountList) {
        canJoinLog++
        tips.callOnClick()
        if (keyboard is KeyboardType2) {
            val keyboardType2 = keyboard as KeyboardType2
            keyboardType2.handleInterface!!.showDifferent(true)
        }
        //显示错误提示
        SpanUtil.create()
                .addForeColorSection("完成", Color.GRAY)
                .addForeColorSection(cl.corrCount.toString() + "/" + cl.totalNum, Color.BLACK)
                .addForeColorSection("个(" + cl.needCorrNum + "/" + cl.totalNum + ")  错误", Color.GRAY)
                .addForeColorSection(cl.errCount.toString() + "", Color.BLACK)
                .addForeColorSection("个", Color.GRAY)
                .showIn(tips)
    }

    internal val duration = 200
    internal fun toNextItem(milliDelay: Int) { //延时后，再进入下一条复习计划
        Timer().schedule(object : TimerTask() {
            override fun run() {
                handler.sendEmptyMessage(HANDLER_UPDATE_SHOWING)
            }
        }, milliDelay.toLong())
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun correctProc(inputText: String, rs: ReviewStruct) { //***正确************************************************************************************
        if (correct) {
            data.updateInavalable_AddLevel(rs)
            canJoinLog = 0
            //下面监听器，等颜色动画播放完毕，然后显示下一条数据在textShow中
            val keyboardType1 = keyboard as KeyboardType1?
            keyboardType1!!.handleInterfaceType1!!.setLightAnimation(false, duration)
            //渐变显示绿色动画，表示输入正确
            TextColorAnimator.ofArgb(etInput, Color.BLACK, Color.GREEN, Color.TRANSPARENT).setDuration(duration.toLong()).start()
            toNextItem(duration)
            mReviewedNum++
        } else { //***错误********************************************************************************
            tips.callOnClick()
            val ct = ColorfulText()
            val ecs = ct.categoryString(inputText, rs.match.text) //todo
            tips.text = Html.fromHtml(ct.txt, 1) //显示缺少的字符

            //输入错误后，指出错误类型
            val spanBuilder = SpanUtil.create()
            for (ec in ecs) {
                when (ec.category) {
                    Category.correct -> spanBuilder.addForeColorSection(ec.txt, Color.BLACK)
                    Category.malposition -> spanBuilder.addUnderlineSection(ec.txt)
                    Category.unnecesary -> spanBuilder.addStrickoutSection(ec.txt).setForeColor(ec.txt!!, -0x3c3c3d)
                    Category.missing -> {
                    }
                }
            }
            spanBuilder.showIn(etInput)
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val ch = event.unicodeChar.toChar()
        val action = event.action
        val keyCode = event.keyCode
        //键盘按键被按下
        if (action == KeyEvent.ACTION_DOWN) { //左右键赋予功能：上一条、下一条
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    if (keyboard is KeyboardType2) {
                        tvNext.performLongClick()
                        return true
                    }
                    clearInput = 0 //左右箭头点击后，则下次输入不清空内容
                    if (keyboard is KeyboardType2) {
                        tvNext.performClick()
                        return true
                    }
                    clearInput = 0 //左右箭头点击后，则下次输入不清空内容
                    if (event.isShiftPressed) {
                        if (keyboard is KeyboardType1) etInput.setText("") else if (keyboard is KeyboardType2) return (keyboard as KeyboardType2).keyDown(KeyEvent.KEYCODE_FORWARD_DEL, '\n', -1)
                    }
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    if (keyboard is KeyboardType2) {
                        tvNext.performClick()
                        return true
                    }
                    clearInput = 0
                    if (event.isShiftPressed) {
                        if (keyboard is KeyboardType1) etInput.setText("") else if (keyboard is KeyboardType2) return (keyboard as KeyboardType2).keyDown(KeyEvent.KEYCODE_FORWARD_DEL, '\n', -1)
                    }
                }
                KeyEvent.KEYCODE_DEL -> if (event.isShiftPressed) {
                    if (keyboard is KeyboardType1) etInput.setText("") else if (keyboard is KeyboardType2) return (keyboard as KeyboardType2).keyDown(KeyEvent.KEYCODE_FORWARD_DEL, '\n', -1)
                }
            }
            //显示代替字符
            if (keyCode == KeyEvent.KEYCODE_S && event.isAltPressed) {
                MyAdapter.isShowNum = !MyAdapter.isShowNum
                keyboard!!.adapter?.notifyDataSetChanged()
                return super.dispatchKeyEvent(event)
            }
            //MyAdapter.isShowNum为真，则显示键盘索引字符
            if (MyAdapter.isShowNum && keyboard!!.keyDown(keyCode, ch, -1)) return true
            //按任意键，显示索引字母
            val isInsOfType2 = keyboard is KeyboardType2
            val isVisibleChar = ch.toInt() > 32 && ch.toInt() < 128 //限制为可见的ASCII码
            if (!MyAdapter.isShowNum && isInsOfType2 && isVisibleChar) {
                MyAdapter.isShowNum = true
                keyboard!!.adapter?.notifyDataSetChanged()
                return super.dispatchKeyEvent(event)
            }
        } else if (action == KeyEvent.ACTION_UP) {
            if (clearInput == 1) clearInput = 2
        }
        //解决软键盘按回车键后，输入框失去焦点问题
        return if (action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) false else super.dispatchKeyEvent(event)
    }

    private fun addLog(rs: ReviewStruct) { //去除多次同样的重复记录
        if (canJoinLog > 1) return
        //添加log记录
        val dateTime: DateTime = DateTime.getCurrentTime()
        if (!correct) {
            val second = dateTime.second
            dateTime.second = -second
        }
        rs.logs.add(dateTime.toBytes())
    }

    companion object {
        var sorts: SortLib? = null
        private const val TAG = "msg_mine"
        var data = ReviewData()
        var externalRoot = Environment.getExternalStorageDirectory() //外部存储夹根目录
        var pathApp = File(externalRoot, "Review") //软件根目录
        var pathNexus = File(pathApp, "nexus.lib") //数据所在目录
        var pathLibrary = File(pathApp, "library.lib") //数据所在目录
        var pathInit = File(pathApp, "Total Word.ini") //数据所在目录
        internal var canJoinLog = 0
    }
}