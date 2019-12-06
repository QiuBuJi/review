package com.example.review.Activity

import android.animation.ObjectAnimator
import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.RecyclerView.LayoutParams
import android.view.View.OnClickListener
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import com.example.review.R

class AboutActivity : Activity() {
    lateinit var textViewTitle: TextView
    lateinit var imageViewBackButton: ImageView
    lateinit var imageView: ImageView
    private var count = 0
    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        textViewTitle = findViewById(R.id.main_about_textView_title)
        imageViewBackButton = findViewById(R.id.about_imageView_back_button)
        imageView = findViewById(R.id.imageView)

        imageViewBackButton.setOnClickListener(OnClickListener { finish() })
        imageView.setOnClickListener(OnClickListener {
            //单击动画
            val scaleX = ObjectAnimator.ofFloat(imageView, "scaleX", 1f, 0.5f, 1f, 0.5f, 1f)
            val scaleY = ObjectAnimator.ofFloat(imageView, "scaleY", 1f, 0.5f, 1f, 0.5f, 1f)
            val value: Interpolator = LinearInterpolator()
            scaleX.interpolator = value
            scaleY.interpolator = value
            scaleX.duration = 600
            scaleY.duration = 600
            scaleX.start()
            scaleY.start()
            count++
            if (count == 12) {
                val layoutParams: LayoutParams = imageView.getLayoutParams() as LayoutParams
                layoutParams.width = (imageView.getWidth() * 1.6f).toInt()
                layoutParams.height = (imageView.getHeight() * 1.6f).toInt()
                imageView.requestLayout()
            } else if (count > 6) {
                imageView.setImageResource(R.mipmap.master)
            }
        })
    }
}