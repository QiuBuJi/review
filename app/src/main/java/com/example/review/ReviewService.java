package com.example.review;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.example.review.Activity.MainActivity;
import com.example.review.DataStructureFile.DateTime;
import com.example.review.DataStructureFile.ReviewData;

import java.io.IOException;

public class ReviewService extends Service {
    public ReviewData data = new ReviewData();
    String  TAG    = "msg";
    boolean notify = false;
    private boolean notify_region;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notify = Setting.getBoolean("通知提醒");
        notify_region = Setting.getBoolean("夜间不通知");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initData();
    }

    public void initData() {
        data.stopTimer();
        data.setDefaultPath(MainActivity.pathLibrary, MainActivity.pathNexus);
//        long millis = System.currentTimeMillis();
        data.read();
//        millis = System.currentTimeMillis() - millis;
//        Toast.makeText(this, "read data cost millis:" + millis, Toast.LENGTH_SHORT).show();

        data.retrieveInavalable();
        data.updateToAvalableAuto(1000);
        data.setOnAvalableComplete(getAvalablecomplete());
    }

    private ReviewData.AvalableComplete getAvalablecomplete() {
        return new ReviewData.AvalableComplete() {
            @Override
            public void onAvalablecomplete() {
                boolean isNotify = false;
                if (notify_region) {
                    DateTime currentTime = DateTime.getCurrentTime();
                    int      hour        = currentTime.getHour();
                    if (hour < 8 || hour > 23) {
                        isNotify = true;
                    }
                }

                if (data.mInactivate.isEmpty() || !notify || isNotify) return;

                DateTime dateTime = new DateTime(data.mInactivate.get(0).time);//取首条数据的时间
                DateTime timeGap  = dateTime.subtract(DateTime.getCurrentTime());//首条的时间-当前时间，得到差值

                //下一条距离现在的时间差，如果大于8分钟，则通知该复习了
                if (timeGap.biggerThan(new DateTime("8分"))) {
                    notifyHaveReveiw();
                } else {
                    //下一条的时间差，未满8分钟，但现有数据已经达到12条，则通知复习
                    if (data.mActivate.size() > 12) {
//                                notifyHaveReveiw();
                    }
                }
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        data.stopTimer();
        Toast.makeText(this, "复习服务已经关闭！", Toast.LENGTH_SHORT).show();
    }

    //设置通知信息
    public void notifyHaveReveiw() {

        //界面没有关闭之前，不能启动通知
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ComponentName   topActivity     = activityManager.getRunningTasks(2).get(0).topActivity;

        if (topActivity != null) {
            String className = topActivity.getClassName();
            String name      = MainActivity.class.getName();
            if (name.equals(className)) return;//前台界面如果跟软件的首页界面类名相等，则不执行
        }


        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pending = PendingIntent.getActivity(this, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.review_logo)
               .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.review_logo))
               .setContentTitle("该复习咯")
               .setContentText("您有" + data.mActivate.size() + "单词需要复习")
               .setAutoCancel(true)
               .setPriority(NotificationCompat.PRIORITY_HIGH)
               .setOngoing(true)
               .setLights(Color.RED, 200, 100)
               .setVibrate(new long[]{100, 100, 200, 300})
               .setContentIntent(pending);


        NotificationManager notifiManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        notifiManager.cancel(1);
        notifiManager.notify(1, builder.build());

        //播放音频
        String      pathname = "/system/media/audio/notifications/19_Chocolate.ogg";
        MediaPlayer mp       = new MediaPlayer();
        try {
            mp.setVolume(0.5f, 0.5f);
            mp.setDataSource(pathname);
            mp.prepare();
            mp.start();
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class LocalBinder extends Binder {
        public ReviewService getService() {
            return ReviewService.this;
        }
    }

}
