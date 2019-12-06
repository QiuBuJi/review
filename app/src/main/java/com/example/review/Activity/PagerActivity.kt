package com.example.review.Activity

import android.content.Context
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.PagerTabStrip
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.text.SpannableString
import android.text.Spanned
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.review.Fragment.MyFragment
import com.example.review.R

class PagerActivity : AppCompatActivity() {
    lateinit var pager: ViewPager
    private val pagerTitle: PagerTabStrip? = null
    private var titles: ArrayList<String>? = null
    lateinit var tabLayout: TabLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pager)
        pager = findViewById(R.id.pager_viewPager)
        tabLayout = findViewById(R.id.pager_tabLayout)
        tabLayout.addTab(tabLayout.newTab().setText("you shit"))
        val ss = SpannableString("you")
        ss.setSpan(0, 0, 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        tabLayout.addTab(tabLayout.newTab().setText(ss))
        titles = ArrayList()
        val data = ArrayList<String>()
        for (i in 0..3) data.add(i.toString() + "个数据")
        val fragments = ArrayList<Fragment>()
        for (i in 0..0) {
            fragments.add(MyFragment())
            titles!!.add("标题$i")
        }
        pager.setAdapter(object : FragmentStatePagerAdapter(supportFragmentManager) {
            override fun getItem(i: Int): Fragment {
                return fragments[i]
            }

            override fun getCount(): Int {
                return fragments.size
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return titles!![position]
            }
        })
    }

    internal inner class Adapter(var context: Context, var data: ArrayList<String>) : PagerAdapter() {
        var titles = arrayOf("one", "two", "three")
        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val inflate = View.inflate(context, R.layout.activity_pager_item, null)
            val item = inflate.findViewById<TextView>(R.id.item_pager_textView)
            val str = data[position]
            item.text = str
            container.addView(inflate)
            return inflate
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        override fun getCount(): Int {
            return data.size
        }

        override fun isViewFromObject(view: View, o: Any): Boolean {
            return view === o
        }

    }
}