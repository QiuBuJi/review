package com.example.review.Activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.example.review.R
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class EditSortsActivity : AppCompatActivity() {
    private lateinit var btSave: Button
    private lateinit var etContent: EditText
    private lateinit var ivBackButton: ImageView

    private val pathSorts = File(MainActivity.pathApp, "sorts.txt")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_sorts)

        initView()
        try {
            val fis = FileInputStream(pathSorts)
            val readBytes = fis.readBytes()
            fis.close()
            etContent.setText(String(readBytes))
        } catch (e: Exception) {
            Toast.makeText(this, "数据不存在！", Toast.LENGTH_SHORT).show()
        }

        btSave.setOnClickListener {
            val fos = FileOutputStream(pathSorts)
            fos.write(etContent.text.toString().toByteArray())
            fos.close()
            finish()
        }

        ivBackButton.setOnClickListener { finish() }
    }

    private fun initView() {
        btSave = findViewById(R.id.editSorts_button_save)
        etContent = findViewById(R.id.editSorts_editText_content)
        ivBackButton = findViewById(R.id.editSorts_imageView_backButton)
    }
}
