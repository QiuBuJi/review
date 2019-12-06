package com.example.review.Activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import com.example.review.R
import com.example.review.Setting

class SettingActivity : AppCompatActivity(), OnClickListener {
    private var list: RecyclerView? = null
    private var imageViewBack: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        list = findViewById<RecyclerView?>(R.id.setting_recyclerView_list)
        imageViewBack = findViewById<ImageView?>(R.id.setting_imageView_back_button)
        imageViewBack!!.setOnClickListener(this)
        val stringArray = resources.getStringArray(R.array.strings_settings)
        list!!.adapter = getAdapter(stringArray)
        list!!.layoutManager = LinearLayoutManager(this)
        list!!.itemAnimator = DefaultItemAnimator()
        list!!.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    private fun getAdapter(stringArray: Array<String?>): RecyclerView.Adapter<Holder> {
        return object : RecyclerView.Adapter<Holder>() {
            override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): Holder {
                val inflate = LayoutInflater.from(this@SettingActivity).inflate(R.layout.activity_setting_item, viewGroup, false)
                return Holder(inflate!!)
            }

            override fun onBindViewHolder(viewHolder: Holder, i: Int) {
                val view = viewHolder.view
                val textView = view.findViewById<TextView?>(R.id.settingItem_itextView_text)
                val switch_ = view.findViewById<Switch?>(R.id.settingItem_switch)
                val text = stringArray[i]
                textView!!.text = text
                val state = Setting.getBoolean(text)
                switch_!!.isChecked = state
                switch_.setOnCheckedChangeListener { compoundButton, b -> Setting.set(text, b) }
            }

            override fun getItemCount(): Int = stringArray.size
        }
    }

    override fun onClick(view: View?) {
        finish()
    }

    internal inner class Holder(itemView: View) : ViewHolder(itemView) {
        var view: View = itemView

    }
}