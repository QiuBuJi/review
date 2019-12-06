package com.example.review.Activity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.support.v4.view.ViewPager.OnPageChangeListener
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnScrollChangeListener
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
    lateinit var imageViewBackButton: ImageView
    lateinit var spinnerSort: Spinner
    lateinit var pager: ViewPager
    lateinit var indicate: TextView
    private val fragments = ArrayList<SortFragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sort)

        spinnerSort = findViewById(R.id.sort_spinner_sort)
        imageViewBackButton = findViewById(R.id.sort_edit_imageView_back_button)
        pager = findViewById(R.id.sort_viewPager_pager)
        indicate = findViewById(R.id.sort_textView_indicate)

        imageViewBackButton.setOnClickListener(this)
        val intent = intent
        val posi = intent.getIntExtra("posi", 0)

        fragments.add(SortFragment(true))
        fragments.add(SortFragment())
        fragments.add(OutlineFragment())

        pager.setAdapter(adapter)
        pager.addOnPageChangeListener(listener)
        if (posi > 0) {
            pager.arrowScroll(posi)
        }
        pager.setOnScrollChangeListener(OnScrollChangeListener { view, i, i1, i2, i3 -> xPosi = i })
    }

    private val adapter: FragmentStatePagerAdapter
        private get() = object : FragmentStatePagerAdapter(supportFragmentManager) {
            override fun getItem(i: Int): Fragment {
                return fragments[i]
            }

            override fun getCount(): Int {
                return fragments.size
            }
        }

    //                fragment.displayField = Setting.getInt("displayField");
//                fragment.selectPartToShow(fragment.displayField);
//                fragment.adapter.notifyDataSetChanged();
    private val listener: OnPageChangeListener
        private get() = object : OnPageChangeListener {
            override fun onPageScrolled(i: Int, v: Float, i1: Int) {}
            override fun onPageSelected(i: Int) {
                fragment = fragments[i]
                when (i) {
                    0 -> indicate!!.text = "复习中"
                    1 -> indicate!!.text = "待复习"
                    2 -> indicate!!.text = "大纲"
                }
                //                fragment.displayField = Setting.getInt("displayField");
//                fragment.selectPartToShow(fragment.displayField);
//                fragment.adapter.notifyDataSetChanged();
            }

            override fun onPageScrollStateChanged(i: Int) {}
        }

    override fun onClick(view: View) {
        if (view.id == R.id.sort_edit_imageView_back_button) {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (fragment != null) {
            Setting.set("displayField", fragment!!.displayField)
            fragment = null
        }
    }

    override fun onStart() {
        super.onStart()
        if (fragment == null) return
        fragment!!.displayField = Setting.getInt("displayField")
        spinnerSort!!.setSelection(fragment!!.displayField, true)
        spinnerSort!!.onItemSelectedListener = object : OnItemSelectedListener {
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