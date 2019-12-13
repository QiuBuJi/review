package com.example.review.Animator

import android.animation.ValueAnimator
import android.graphics.Color
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.EditText
import com.example.review.DataStructureFile.ElementCategory
import com.example.review.DataStructureFile.ElementCategory.Category

object TextColorGroupAnimator {
    internal fun ofEc(view: EditText, ecs: ArrayList<ElementCategory>): ValueAnimator {
        val valueAnimator = ValueAnimator.ofInt(0, 255)
        valueAnimator.addUpdateListener { animation ->
            var value = animation.animatedValue as Int
            var begin = 0
            var end: Int
            val et = view.editableText
            for (ec in ecs) {
                end = begin + ec.txt!!.length
                var abc = 0xff
                when (ec.category) {
                    Category.Correct -> {
                        abc = Color.BLACK
                        et.setSpan(ForegroundColorSpan(abc), begin, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
                    }
                    Category.Malposition -> {
                        abc = abc shl 8
                        abc = abc shl 8
                        abc = abc shl 8
                        value = value shl 8
                        value = value shl 8
                        abc = abc or value
                        et.setSpan(ForegroundColorSpan(abc), begin, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
                    }
                    Category.Unnecessary -> {
                        if (value > 0xc3) value = 0xc3
                        var i = 0
                        while (i < 3) {
                            abc = abc shl 8
                            abc = abc or value
                            i++
                        }
                        et.setSpan(ForegroundColorSpan(abc), begin, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
                    }
                }
                begin = end
            }
        }
        return valueAnimator
    }

    private fun toHexText(rgb: Int): StringBuffer {
        var rgb = rgb
        val baseNum = 16
        val hex = StringBuffer()
        val hexList = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
        if (rgb != 0) {
            while (rgb != 0) {
                val mode = rgb % baseNum
                rgb /= baseNum
                val hex1 = hexList[mode]
                hex.insert(0, hex1)
            }
        } else {
            hex.append("0")
        }
        val len = 2 - hex.length
        for (i in 0 until len) hex.insert(0, "0")
        return hex
    }
}