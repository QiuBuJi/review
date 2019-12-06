package com.example.review.New

import android.view.View

interface ItemClickListener {
    fun onItemClick(view: View?, data: ArrayList<KeyText>, posi: Int)
}