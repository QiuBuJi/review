package com.example.review.Animator

import android.animation.ValueAnimator
import android.text.Editable
import android.text.Spanned
import android.text.style.ForegroundColorSpan

object TextPartColorAnimator {
    fun ofArgb(editable: Editable, start: Int, end: Int, vararg color: Int): ValueAnimator {
        val valueAnimator = ValueAnimator.ofArgb(*color)
        valueAnimator.addUpdateListener { animation ->
            val what: ForegroundColorSpan
            val textColor = animation.animatedValue as Int
            what = ForegroundColorSpan(textColor)
            editable.setSpan(what, start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        }
        return valueAnimator
    }
}