package com.example.review

import android.app.ActivityManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.widget.Toast
import com.example.review.Activity.MainActivity
import com.example.review.DataStructureFile.DateTime
import com.example.review.DataStructureFile.ReviewData
import com.example.review.DataStructureFile.ReviewData.AvailableComplete
import java.io.File
import java.io.IOException

class ReviewService : Service() {
    var data = ReviewData()
    internal var TAG = "msg"
    internal var notify = false
    private var notifyRegion = false
    var sortLib = SortLib()

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        notify = Setting.getBoolean("通知提醒")
        notifyRegion = Setting.getBoolean("夜间不通知")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder = LocalBinder()

    override fun onCreate() {
        super.onCreate()
        try {
            initData()
        } catch (e: IOException) {
            data.clear()
            val msg = e.toString()
            Toast.makeText(this, "出现异常！ 错误信息：$msg", Toast.LENGTH_LONG).show()
        }
    }

    @Throws(IOException::class)
    fun initData() {
        data.stopTimer()
        data.setDefaultPath(MainActivity.pathLibrary, MainActivity.pathNexus)
//      long millis = System.currentTimeMillis();
        data.read()
//      millis = System.currentTimeMillis() - millis;
//      Toast.makeText(this, "read data cost millis:" + millis, Toast.LENGTH_SHORT).show();
        try {
            sortLib.read(File(MainActivity.pathApp, "sorts.txt"))
        } catch (e: Exception) {
//            Toast.makeText(this, "", Toast.LENGTH_LONG).show()
        }
        data.retrieveInvaluable()
        data.mInactivate
        data.mActivate
        data.updateToAvailableAuto(1000)
        data.setOnAvailableComplete(availableComplete())
    }
    // notifyHaveReveiw();//下一条的时间差，未满8分钟，但现有数据已经达到12条，则通知复习//首条的时间-当前时间，得到差值
    //下一条距离现在的时间差，如果大于8分钟，则通知该复习了

    //取首条数据的时间
    private fun availableComplete() = object : AvailableComplete {
        override fun onAvailableComplete() {
            var isNotify = false
            if (notifyRegion) {
                val currentTime: DateTime = DateTime.getCurrentTime()
                val hour = currentTime.hour
                if (hour < 8 || hour > 23) {
                    isNotify = true
                }
            }
            if (data.mInactivate.isEmpty() || !notify || isNotify) return

            val dateTime = DateTime(data.mInactivate[0].time) //取首条数据的时间
            val timeGap = dateTime.subtract(DateTime.getCurrentTime()) //首条的时间-当前时间，得到差值

            //下一条距离现在的时间差，如果大于8分钟，则通知该复习了
            if (timeGap.biggerThan(DateTime("8分"))) {
                notifyHaveReview()
            } else { //下一条的时间差，未满8分钟，但现有数据已经达到12条，则通知复习
                if (data.mActivate.size > 12) {
                    //notifyHaveReveiw();
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        data.stopTimer()
        Toast.makeText(this, "复习服务已经关闭！", Toast.LENGTH_SHORT).show()
    }

    //设置通知信息
    fun notifyHaveReview() { //界面没有关闭之前，不能启动通知
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val topActivity = activityManager.getRunningTasks(2)[0].topActivity

        if (topActivity != null) {
            val className = topActivity.className
            val name = MainActivity::class.java.name
            if (name == className) return  //前台界面如果跟软件的首页界面类名相等，则不执行
        }

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        val pending = PendingIntent.getActivity(this, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = NotificationCompat.Builder(this)

        builder.setSmallIcon(R.mipmap.review_logo)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.review_logo))
                .setContentTitle("该复习咯")
                .setContentText("您有" + data.mActivate.size + "单词需要复习")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .setLights(Color.RED, 200, 100)
                .setVibrate(longArrayOf(100, 100, 200, 300))
                .setContentIntent(pending)

        val notifiManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //        notifiManager.cancel(1);
        notifiManager.notify(1, builder.build())

        //播放音频
        val pathname = "/system/media/audio/notifications/19_Chocolate.ogg"
        val mp = MediaPlayer()
        try {
            mp.setVolume(0.5f, 0.5f)
            mp.setDataSource(pathname)
            mp.prepare()
            mp.start()
            mp.setOnCompletionListener { mp -> mp.release() }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    inner class LocalBinder : Binder() {
        val service
            get() = this@ReviewService
    }
}