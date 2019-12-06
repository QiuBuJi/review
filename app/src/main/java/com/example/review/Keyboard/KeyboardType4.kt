package com.example.review.Keyboard

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.view.View
import android.widget.EditText
import com.example.review.New.KeyText
import com.example.review.New.ReviewStruct

class KeyboardType4(context: Context, keyboardView: RecyclerView, show: ConstraintLayout, input: EditText, reviewStruct: ReviewStruct) : Keyboard(context, keyboardView, show, input, reviewStruct) {
    override fun init() {
        span = 3
        input.inputType = InputType.TYPE_NULL
    }

    override fun setLightAnimation(lightUp: Boolean, duration: Int) {}
    override fun refresh() {}
    override fun getLayout(): ArrayList<KeyText> {
        val data = ArrayList<KeyText>()
        data.add(KeyText("a"))
        data.add(KeyText("b"))
        data.add(KeyText("c"))
        return data
    }

    override fun onItemClick(view: View?, data: ArrayList<KeyText>, posi: Int) {
        super.onItemClick(view, data, posi)
    }

    override fun keyDown(keyCode: Int, key: Char, posi: Int): Boolean {
        return false
    }
}