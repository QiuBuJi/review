package com.example.review.Activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.Options
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.review.R
import java.io.File
import java.io.FilenameFilter
import java.util.*
import kotlin.collections.ArrayList

class FilePickerActivity : AppCompatActivity() {
    internal var suffixs = ArrayList(listOf("jpg", "png"))
    internal var data = LinkedList<String>()
    internal var files = LinkedList<File>()
    private lateinit var filePicker: RecyclerView
    private lateinit var adapter: MyAdapter
    internal var filter: FilenameFilter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_picker)
        filePicker = findViewById(R.id.filePicker_recyclerView_picker)

        filter = MyFilenameFilter()
        val file: File = MainActivity.externalRoot
        val list = file.list(filter)
        files.add(file)
        data.addAll(list)
        adapter = MyAdapter()
        filePicker.adapter = adapter
        filePicker.layoutManager = GridLayoutManager(this, 6)
    }

    override fun onBackPressed() {
        try {
            files.removeLast()
            val list = files.last.list(filter)
            data.clear()
            data.addAll(listOf(*list))
            adapter.notifyDataSetChanged()
        } catch (e: Exception) {
            super.onBackPressed()
        }
    }

    private inner class MyAdapter : RecyclerView.Adapter<MyHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MyHolder {
            val view = LayoutInflater.from(this@FilePickerActivity).inflate(R.layout.activity_file_picker_item, viewGroup, false)
            return MyHolder(view)
        }

        override fun onBindViewHolder(holder: MyHolder, posi: Int) {
            val last = files.last
            val text = data[posi]
            val file = File(last, text)

            if (file.isDirectory) {
                holder.image.setImageResource(R.mipmap.folder_icon)
            } else {
                val options = Options()
//                options.inJustDecodeBounds = true;
//                BitmapFactory.decodeFile(file.getPath(), options);
                options.inJustDecodeBounds = false
                options.inSampleSize = 46
//                options.inSampleSize = options.outWidth / 200;
                options.inScaled = true

                val bitmap = BitmapFactory.decodeFile(file.path, options)
                holder.image.setImageBitmap(bitmap)
            }

            holder.item.text = text
            holder.view.setOnClickListener {
                if (!file.isDirectory) {
                    val intent = Intent()
                    intent.putExtra("directory", file.path)
                    setResult(1, intent)
                    finish()
                } else {
                    files.add(file)
                    val list = file.list(filter)
                    data.clear()
                    data.addAll(listOf(*list))
                    adapter.notifyDataSetChanged()
                }
            }
        }

        override fun getItemCount() = data.size
    }

    internal inner class MyFilenameFilter : FilenameFilter {
        override fun accept(file: File, str: String): Boolean {
            val path = File(file, str)
            if (path.isDirectory && !path.isHidden) return true
            var index = str.lastIndexOf('.')

            if (index != -1) {
                val suffix = str.substring(++index)
                return suffixs.contains(suffix)
            }
            return false
        }
    }

    internal inner class MyHolder(var view: View) : ViewHolder(view) {
        var image: ImageView = view.findViewById(R.id.filePicker_imageView_image)
        var item: TextView = view.findViewById(R.id.filePicker_textView_item)
    }
}