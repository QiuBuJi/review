package com.example.review;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyReceiver extends BroadcastReceiver {

    private Intent mIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case Intent.ACTION_BOOT_COMPLETED:
                Log.d("msg", "ACTION_BOOT_COMPLETED: ");

                mIntent = new Intent(context, ReviewService.class);
                context.startService(mIntent);
                break;
        }
    }
}
