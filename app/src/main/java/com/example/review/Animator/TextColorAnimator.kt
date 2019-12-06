package com.example.review.Animator

import android.animation.ValueAnimator
import android.widget.TextView

object TextColorAnimator {
    fun ofArgb(view: TextView, vararg values: Int): ValueAnimator {
        val valueAnimator = ValueAnimator.ofArgb(*values)
        valueAnimator.addUpdateListener { animation -> view.setTextColor(animation.animatedValue as Int) }
        return valueAnimator
    }
}