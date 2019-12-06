package com.example.review.Fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.review.Activity.MainActivity
import com.example.review.DataStructureFile.DateTime
import com.example.review.R
import com.example.review.Setting
import java.util.*

class OutlineFragment @SuppressLint("ValidFragment") constructor() : SortFragment() {
    var data1 = LinkedList<String>()
    lateinit var adapter1:AdapterSort1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_sort_fragment, container, false)
        recyclerView = view.findViewById(R.id.sort_fragment_recyclerView)
        tip = view.findViewById(R.id.sort_fragment_textView_noData)
        tip.visibility = View.INVISIBLE
        displayField = Setting.getInt("displayField")
        selectPartToShow(displayField)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.isVerticalScrollBarEnabled = true
        adapter1 = AdapterSort1(data1)
        recyclerView.adapter = adapter1
        return view
    }

    override fun selectPartToShow(part: Int) {
        data1.clear()
        val strings = getStrings(part)
        data1.addAll(strings)
    }

    private fun getStrings(part: Int): LinkedList<String> {
        val sb = LinkedList<String>()
        mData = MainActivity.Companion.data.mInactivate
        super.selectPartToShow(part)
        var count = 1
        for (rs in mData) {
            if (rs.showed) {
                if (!sb.isEmpty()) {
                    val last = sb.last
                    sb.removeLast()
                    sb.add(last + " (" + count + "条)")
                }
                val time = DateTime(rs.time)
                time.setZeroSegment(part + 1)
                sb.add(time.toStringTime())
                count = 1
            } else {
                count++
            }
        }
        if (!sb.isEmpty()) {
            val last = sb.last
            sb.removeLast()
            sb.add(last + " (" + count + "条)")
        }
        return sb
    }

    inner class AdapterSort1(data: LinkedList<String>) : RecyclerView.Adapter<AdapterSort1.MyHolder>() {
        var data: LinkedList<String> = data

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MyHolder {
            val inflate = LayoutInflater.from(context).inflate(R.layout.activity_sort_item1, viewGroup, false)
            return MyHolder(inflate)
        }

        override fun onBindViewHolder(holder: MyHolder, posi: Int) {
            holder.region.setText(data.get(posi))
            holder.index.text = String.format("%d.", posi + 1)
        }

        override fun getItemCount(): Int = data.size

        inner class MyHolder(var view: View) : ViewHolder(view) {
            val index: TextView = view.findViewById(R.id.item_sort_textView_index)
            val region: TextView = view.findViewById(R.id.item_sort_textView_region)
        }

    }
}