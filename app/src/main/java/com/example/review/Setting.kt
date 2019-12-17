package com.example.review

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor

object Setting {
    var sp: SharedPreferences? = null
    var edit: Editor? = null

    fun init(context: Context) {
        sp = context.getSharedPreferences("ActivitysData", Context.MODE_PRIVATE)
        edit = sp!!.edit()
    }

    fun getBoolean(extra: String?): Boolean {
        return sp!!.getBoolean(extra, false)
    }

    fun getInt(extra: String?): Int {
        return sp!!.getInt(extra, 0)
    }

    operator fun get(extra: String?): Boolean {
        return sp!!.getBoolean(extra, false)
    }

    operator fun set(extra: String?, value: Boolean) {
        edit!!.putBoolean(extra, value).commit()
    }

    operator fun set(extra: String?, value: Int) {
        edit!!.putInt(extra, value).commit()
    }
}