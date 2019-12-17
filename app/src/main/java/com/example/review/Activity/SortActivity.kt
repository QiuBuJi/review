package com.example.review.Activity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.support.v4.view.ViewPager.OnPageChangeListener
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import com.example.review.Fragment.OutlineFragment
import com.example.review.Fragment.SortFragment
import com.example.review.R
import com.example.review.Setting

class SortActivity : AppCompatActivity(), OnClickListener {
    private lateinit var ivBackButton: ImageView
    private lateinit var spinSort: Spinner
    private lateinit var vpPager: ViewPager
    lateinit var tvIndicate: TextView
    private val fragments = ArrayList<SortFragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sort)

        //*******************************找到view*******************************
        spinSort = findViewById(R.id.sort_spinner_sort)
        ivBackButton = findViewById(R.id.sort_edit_imageView_back_button)
        vpPager = findViewById(R.id.sort_viewPager_pager)
        tvIndicate = findViewById(R.id.sort_textView_indicate)


        //*******************************监听器*******************************
        ivBackButton.setOnClickListener(this::onClick)


        val posi = intent.getIntExtra("posi", 0)

        fragments.add(SortFragment(true))
        fragments.add(SortFragment())
        fragments.add(OutlineFragment())

        vpPager.adapter = adapter
        vpPager.addOnPageChangeListener(listener)
        if (posi > 0) vpPager.arrowScroll(posi)
        vpPager.setOnScrollChangeListener { view, i, i1, i2, i3 -> xPosi = i }
    }

    private val adapter: FragmentStatePagerAdapter
        get() = object : FragmentStatePagerAdapter(supportFragmentManager) {
            override fun getItem(i: Int): Fragment = fragments[i]
            override fun getCount(): Int = fragments.size
        }

    private val listener: OnPageChangeListener
        get() = object : OnPageChangeListener {
            override fun onPageScrolled(i: Int, v: Float, i1: Int) {}
            override fun onPageSelected(i: Int) {
                fragment = fragments[i]
                tvIndicate.text = when (i) {
                    0 -> "复习中"
                    1 -> "待复习"
                    2 -> "大纲"
                    else -> "out of rang!"
                }
//                fragment.displayField = Setting.getInt("displayField");
//                fragment.selectPartToShow(fragment.displayField);
//                fragment.adapter.notifyDataSetChanged();
            }

            override fun onPageScrollStateChanged(i: Int) {}
        }

    override fun onClick(view: View) {
        if (view.id == R.id.sort_edit_imageView_back_button) finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (fragment == null) return
        Setting["displayField"] = fragment!!.displayField
        fragment = null
    }

    override fun onStart() {
        super.onStart()
        if (fragment == null) return

        fragment!!.displayField = Setting.getInt("displayField")
        spinSort.setSelection(fragment!!.displayField, true)
        spinSort.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                fragment!!.selectPartToShow(i)
                fragment!!.adapter.notifyDataSetChanged()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
    }

    companion object {
        var fragment: SortFragment? = null
        var xPosi = 0
    }
}