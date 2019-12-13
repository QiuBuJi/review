package com.example.review.Activity

import android.app.Activity
import android.app.AlertDialog.Builder
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.*
import android.widget.CompoundButton.OnCheckedChangeListener
import android.widget.NumberPicker.OnValueChangeListener
import com.example.review.DataStructureFile.DateTime
import com.example.review.DataStructureFile.ReviewData
import com.example.review.New.LibraryList
import com.example.review.New.LibraryStruct
import com.example.review.New.ReviewStruct
import com.example.review.R
import com.example.review.Util.SpanUtil
import java.util.*

class EditActivity : Activity(), OnClickListener, OnCheckedChangeListener, OnValueChangeListener {
    private lateinit var etExplain: EditText
    private lateinit var etWord: EditText
    private lateinit var tvNumber: TextView
    private lateinit var tvSave: TextView
    private lateinit var tvTypeWord: TextView
    private lateinit var tvTypeExplain: TextView
    private lateinit var tvTimeLogs: TextView
    private lateinit var tvPeriod: TextView
    private lateinit var ivBackButton: ImageView
    private lateinit var imgAlter: ImageView
    private lateinit var switchJoin: Switch
    private lateinit var swGenerate: Switch
    private lateinit var npPicker: NumberPicker
    private lateinit var btUp: Button
    private lateinit var btDown: Button
    internal var rs: ReviewStruct? = null
    lateinit var data: ReviewData
    private var level = 0
    private var checked = false
    private var libraries: LibraryList? = null
    private var scrollList: ScrollView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        //初始化视图&监听器
        initViewAndListener()
        data = MainActivity.data
        npPicker.maxValue = ReviewData.reviewRegions.size - 1
        npPicker.minValue = 0
        switchJoin.isChecked = false
        libraries = data.library

        //添加编辑内容&显示详细内容，的分支************************************************************
        if (ListActivity.currentClickedRs != null) { //显示************************************************************
            rs = ListActivity.currentClickedRs

            //如果是引用，则不能编辑
            if (rs!!.match.refer > 0) {
                etWord.isEnabled = false
                btUp.isEnabled = false
            }
            if (rs!!.show.refer > 0) {
                etExplain.isEnabled = false
                btDown.isEnabled = false
            }

            //设置要显示的数据
            etWord.setText(rs!!.match.text)
            etExplain.setText(rs!!.show.text)
            tvNumber.text = rs!!.level.toString()
            tvTypeWord.text = rs!!.match.type.toString()
            tvTypeExplain.text = rs!!.show.type.toString()
            swGenerate.isEnabled = false
            level = rs!!.level
            npPicker.value = level

            tvTimeLogs.text = generateFormattedTimeLogs()
            switchJoin.isChecked = rs!!.joined
        }
    }

    private fun generateFormattedTimeLogs(): String {
        val value = StringBuffer()
        var oldLog: DateTime? = null

        for ((count, log) in rs!!.logs.withIndex()) {
            val log = DateTime(log)
            val strIndex: String = DateTime.fillChar(count + 1, 3, ' ')
            val sb = StringBuilder(log.toString())

            if (oldLog == null) {
                oldLog = DateTime(log)
                value.append("$strIndex. $log\n")
            } else {
                if (log.year == oldLog.year) {
                    if (log.month == oldLog.month) {
                        if (log.day == oldLog.day) {
                            if (log.hour == oldLog.hour) {
                                if (log.minute == oldLog.minute) {
                                    if (log.second == oldLog.second) {
                                        sb.replace(0, 21, "    ┊  ┊  ┊   ┊  ┊  ┊")
                                    } else {
                                        oldLog.second = log.second
                                        sb.replace(0, 18, "    ┊  ┊  ┊   ┊  ┊")
                                    }
                                } else {
                                    oldLog.minute = log.minute
                                    sb.replace(0, 15, "    ┊  ┊  ┊   ┊")
                                }
                            } else {
                                oldLog.hour = log.hour
                                sb.replace(0, 11, "    ┊  ┊  ┊")
                            }
                        } else {
                            oldLog.day = log.day
                            sb.replace(0, 8, "    ┊  ┊")
                        }
                    } else {
                        oldLog.month = log.month
                        sb.replace(0, 5, "    ┊")
                    }
                } else oldLog = DateTime(log)

                value.append("$strIndex. $sb\n")
            }
        }
        return value.toString()
    }

    private fun initViewAndListener() {
        tvNumber = findViewById(R.id.textView_number)
        etExplain = findViewById(R.id.editText_explain)
        etWord = findViewById(R.id.editText_word)
        ivBackButton = findViewById(R.id.edit_imageView_back_button)
        tvSave = findViewById(R.id.edit_button_save)
        npPicker = findViewById(R.id.edit_numberPiker_piker)
        switchJoin = findViewById(R.id.edit_switch_join)
        tvTypeWord = findViewById(R.id.edit_textView_type_word)
        tvTypeExplain = findViewById(R.id.edit_textView_type_explain)
        scrollList = findViewById(R.id.edit_scrollView_list)
        tvTimeLogs = findViewById(R.id.edit_scrollView_textView_detail)
        btUp = findViewById(R.id.edit_button_up)
        btDown = findViewById(R.id.edit_button_down)
        swGenerate = findViewById(R.id.edit_switch_generate_reverse)
        tvPeriod = findViewById(R.id.tvPeriod)
        imgAlter = findViewById(R.id.edit_img_alter)

        //设置监听器
        switchJoin.setOnCheckedChangeListener(this)
        npPicker.setOnValueChangedListener(this)
        ivBackButton.setOnClickListener(this)
        tvSave.setOnClickListener(this)
        tvTypeWord.setOnClickListener(this)
        tvTypeExplain.setOnClickListener(this)
        tvNumber.setOnClickListener(this)
        //        editTextWord.setOnClickListener(this);
//        editTextExplain.setOnClickListener(this);
        btUp.setOnClickListener(this)
        btDown.setOnClickListener { view: View -> onClick(view) }
        imgAlter.setOnClickListener { view: View -> onClick(view) }
    }

    private var items = arrayOf("1 纯单词", "2 单词解释", "3 填空式", "4 图片", "5 声音")

    override fun onClick(view: View) {
        when (view.id) {
            R.id.edit_imageView_back_button -> finish()
            R.id.edit_button_save -> if (ListActivity.currentClickedRs == null) addData() else editData()
            R.id.edit_textView_type_word ->
                Builder(this)
                        .setItems(items, getListener(tvTypeWord))
                        .setTitle("选择类型")
                        .show()
            R.id.edit_textView_type_explain ->
                Builder(this)
                        .setItems(items, getListener(tvTypeExplain))
                        .setTitle("选择类型")
                        .show()
            R.id.editText_word -> {
            }
            R.id.editText_explain -> {
            }
            R.id.edit_button_up -> {
                requestCode = REQUEST_CODE_WORD
                dialogShow()
            }
            R.id.edit_button_down -> {
                requestCode = REQUEST_CODE_EXPLAIN
                dialogShow()
            }
            R.id.edit_img_alter -> {
                val text = tvTypeWord.text
                tvTypeWord.text = tvTypeExplain.text
                tvTypeExplain.text = text
            }
        }
    }

    private fun dialogShow() {
        Builder(this)
                .setItems(itemsChoose) { dialogInterface, i ->
                    when (i) {
                        0 -> startActivityForResult(Intent(this@EditActivity, LibraryActivity::class.java), requestCode)
                        1 -> startActivityForResult(Intent(this@EditActivity, FilePickerActivity::class.java), requestCode)
                    }
                }.show()
    }

    private fun getListener(textViewType: TextView?): DialogInterface.OnClickListener {
        return DialogInterface.OnClickListener { dialogInterface, i ->
            if (i >= 0) {
                val item = items[i]
                val chtype = item[0]
                textViewType!!.text = chtype.toString()
            }
        }
    }

    //返回界面数据
    private val savingData: ReviewStruct
        get() {
            val rsSave = ReviewStruct()
            val word = etWord.text.toString()
            val explain = etExplain.text.toString()
            val lsWord = LibraryStruct(word, 1)
            val lsExplain = LibraryStruct(explain, 2)
            val textWord = tvTypeWord.text
            val typeWord = Integer.valueOf(textWord as String)
            lsWord.type = typeWord
            val textExplain = tvTypeExplain.text
            val typeExplain = Integer.valueOf(textExplain as String)
            lsExplain.type = typeExplain
            rsSave.addData(lsExplain, lsWord)
            rsSave.joined = checked
            rsSave.level = level
            return rsSave
        }

    //添加数据
    private fun addData() {
        val rsTemp = savingData
        //自动生成ID
        rsTemp.match.setIdAuto(0)
        rsTemp.show.setIdAuto(1)
        data.addLibrary(0, rsTemp.match, rsTemp.show)
        data.add(0, rsTemp) //数据添加到顶部
        //添加单词、解释，相反的内容：解释、单词
        if (swGenerate.isChecked) {
            val rs = savingData
            //交换显示字符串
            val show = rs.show
            rs.show = rs.match
            rs.match = show
            //设置相反的类型
            rs.show.type = 1
            rs.match.type = 2
            //绑定引用
            rs.show.refer = rsTemp.match.id
            rs.match.refer = rsTemp.show.id
            //添加数据
            data.addLibrary(0, rs.match, rs.show)
            data.add(0, rs) //数据添加到顶部
        }
        data.setLibrarySaveMark()
        //加入复习
        if (rsTemp.joined) data.sortAddToInactivate(rsTemp)
        finish()
    }

    //编辑数据
    private fun editData() {
        try {
            var isChange = false
            val reviewStruct = savingData
            val level = reviewStruct.level
            if (level != rs!!.level) isChange = true
            rs!!.level = level
            rs!!.joined = reviewStruct.joined
            rs!!.show.copyOf(reviewStruct.show)
            rs!!.match.copyOf(reviewStruct.match)
            //加入复习
            if (rs!!.joined) {
                if (isChange) {
                    rs!!.level = rs!!.level - 1
                    data.updateInavailable_AddLevel(rs!!)
                } else data.sortAddToInactivate(rs!!)
            } else data.removeFromInavailable_Available(rs!!)
            data.setLibrarySaveMark()
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this@EditActivity, "不能保存大于12的值", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //        ListActivity.currentClickedRs = null;
    }

    private var itemsChoose = arrayOf("在库内选择", "选择图片")
    private var requestCode = 0
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        //没有返回数据
        if (resultCode == 0) return
        //接收图片路径
        val directory = data.getStringExtra("directory")
        var ls: LibraryStruct? = null
        if (directory == null) {
            val index = data.getIntExtra("indexOfItem", -1)
            ls = libraries!![index]
        }
        when (requestCode) {
            REQUEST_CODE_WORD -> if (directory != null) {
                etWord.setText(directory)
                tvTypeWord.text = 4.toString()
            } else {
                etWord.setText(ls!!.text)
                tvTypeWord.text = ls.type.toString()
            }
            REQUEST_CODE_EXPLAIN -> if (directory != null) {
                etExplain.setText(directory)
                tvTypeExplain.text = 4.toString()
            } else {
                etExplain.setText(ls!!.text)
                tvTypeExplain.text = ls.type.toString()
            }
        }
    }

    override fun onCheckedChanged(compoundButton: CompoundButton, isChecked: Boolean) {
        checked = isChecked
    }

    override fun onValueChange(numberPicker: NumberPicker, i: Int, value: Int) {
        level = value
        tvNumber.text = level.toString()
        val reviewRegion: DateTime = ReviewData.reviewRegions[level]
        val text = reviewRegion.toAboutValueNoDot()
        val textB = "后复习"
        SpanUtil.create()
                .addForeColorSection(text, Color.BLACK)
                .addForeColorSection(textB, Color.LTGRAY)
                .setAbsSize(textB, 24)
                .showIn(tvPeriod)
    }

    companion object {
        private const val REQUEST_CODE_WORD = 1
        private const val REQUEST_CODE_EXPLAIN = 2
    }
}