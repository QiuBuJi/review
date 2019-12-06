package com.example.review.Activity

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.OnFlingListener
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import com.example.review.New.LibraryList
import com.example.review.R

class LibraryActivity : AppCompatActivity() {
    lateinit var libraries: LibraryList
    private lateinit var adapter: MyAdapter
    lateinit var imageViewBack: ImageView
    lateinit var search: EditText
    lateinit var list: RecyclerView
    lateinit var floating: FloatingActionButton
    private var mUpDown = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        list = findViewById(R.id.library_recyclerView_list)
        search = findViewById(R.id.library_editText_search)
        imageViewBack = findViewById(R.id.library_imageView_back_button)
        floating = findViewById(R.id.library_floatingActionButton)

        libraries = MainActivity.data.library
        adapter = MyAdapter(libraries)
        list.setAdapter(adapter)
        list.setLayoutManager(LinearLayoutManager(this))
        list.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        imageViewBack.setOnClickListener(OnClickListener { finish() })
        list.setOnFlingListener(object : OnFlingListener() {
            override fun onFling(i: Int, i1: Int): Boolean {
                mUpDown = i1
                return false
            }
        })
        floating.setOnClickListener(OnClickListener {
            val scrollState = list.getScrollState()
            val lm = list.getLayoutManager()!!
            val itemCount = lm.itemCount - 1
            if (scrollState > 0) {
                if (mUpDown < 0) list.scrollToPosition(0) else if (mUpDown > 0) list.scrollToPosition(itemCount)
            }
        })
        floating.setOnLongClickListener(OnLongClickListener {
            val lm = list.getLayoutManager()!!
            val itemCount = lm.itemCount - 1
            list.scrollToPosition(itemCount / 2)
            false
        })
        search.setImeOptions(EditorInfo.IME_ACTION_SEARCH)
        search.setOnEditorActionListener(OnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                acc()
            }
            true
        })
    }

    private fun acc() {
        val libs = LibraryList()
        val text = search!!.text.toString()

        for (library in libraries!!) {
            val libText = library.text
            if (libText.contains(text)) {
                libs.add(library)
            }
        }
        val size = libs.size
        adapter = if (size == 0) MyAdapter(libraries) else MyAdapter(libs)
        list!!.setAdapter(adapter)
        Toast.makeText(this, "一共找到" + size + "项", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    internal inner class MyAdapter(private val libraries: LibraryList) : RecyclerView.Adapter<MyHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MyHolder {
            val inflate = LayoutInflater.from(this@LibraryActivity).inflate(R.layout.activity_library_item, viewGroup, false)
            return MyHolder(inflate)
        }

        override fun onBindViewHolder(holder: MyHolder, posi: Int) {
            val libraryStruct = libraries!![posi]
            holder.index.text = String.format("%d", posi)
            holder.textView.text = libraryStruct.text
            holder.view.setOnClickListener(getclickListener(posi))
        }

        override fun getItemCount() = libraries.size

    }

    private fun getclickListener(posi: Int): OnClickListener {
        return OnClickListener {
            val intent = Intent()
            intent.putExtra("indexOfItem", posi)
            setResult(2, intent)
            finish()
        }
    }

    internal inner class MyHolder(var view: View) : ViewHolder(view) {
        val textView: TextView
        val index: TextView

        init {
            textView = view.findViewById(R.id.library_item_textView_text)
            index = view.findViewById(R.id.library_item_textView_index)
        }
    }
}