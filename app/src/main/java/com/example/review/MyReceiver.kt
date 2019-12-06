package com.example.review

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            Log.d("msg", "ACTION_BOOT_COMPLETED: ")
            val mIntent = Intent(context, ReviewService::class.java)
            context.startService(mIntent)
        }
    }
}