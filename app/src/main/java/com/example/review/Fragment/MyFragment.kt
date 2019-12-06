package com.example.review.Fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.example.review.Activity.MainActivity
import com.example.review.New.LibraryList
import com.example.review.New.ReviewList
import com.example.review.R
import java.io.File
import java.io.IOException

class MyFragment : Fragment() {
    lateinit var mainView: View
    private var librarySet: LibraryList? = null
    private var reviewSet: ReviewList? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { //        View mainView = inflater.mainView(R.layout.activity_fragment_backward, container, false);
        mainView = inflater.inflate(R.layout.activity_list_temp, container, false)
        val textView = mainView.findViewById<TextView>(R.id.text)
        librarySet = LibraryList()
        reviewSet = ReviewList()
        var millis = System.currentTimeMillis()
        try {
            librarySet!!.read(File(MainActivity.Companion.pathApp, "library.lib"))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        var millis1 = System.currentTimeMillis()
        millis = millis1 - millis
        try {
            reviewSet!!.read(File(MainActivity.Companion.pathApp, "nexus.lib"))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        var millis2 = System.currentTimeMillis()
        millis1 = millis2 - millis1
        reviewSet!!.connectOf(librarySet!!)
        val millis3 = System.currentTimeMillis()
        millis2 = millis3 - millis2
        textView.append("\nmillis :$millis")
        textView.append("\nmillis1:$millis1")
        textView.append("\nmillis2:$millis2")
        Toast.makeText(context, "millis3-millis:" + (millis3 - millis), Toast.LENGTH_SHORT).show()
        //        recyclerView.setAdapter(new MyAdapter(context, reviewSet));
//        recyclerView.setLayoutManager(new GridLayoutManager(context, 5));
//        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(5, StaggeredGridLayoutManager.VERTICAL));
//        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
//        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL));
        return mainView
    }
}