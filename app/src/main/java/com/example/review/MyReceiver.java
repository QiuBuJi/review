package com.example.review;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("msg", "ACTION_BOOT_COMPLETED: ");

            Intent mIntent = new Intent(context, ReviewService.class);
            context.startService(mIntent);
        }
    }
}
