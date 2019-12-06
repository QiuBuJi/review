package com.example.review.Util

import android.animation.ObjectAnimator
import android.content.Context
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.view.View
import com.baidu.tts.client.TtsMode
import com.example.review.Activity.MainActivity
import com.example.review.Setting
import com.iflytek.cloud.SpeechConstant
import com.iflytek.cloud.SpeechSynthesizer
import com.iflytek.cloud.SpeechUtility
import java.io.File
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

object Speech {
    private var mVoice: SpeechSynthesizer? = null
    var voice_小燕 = "xiaoyan"
    var voice_许久 = "aisjiuxu"
    var voice_小萍 = "aisxping"
    var voice_小婧 = "aisjinger"
    var voice_许小宝 = "aisbabyxu"
    var voice_Steve = "x_steve"
    var voice_凯瑟琳 = "x_catherine"

    private lateinit var insOfBaidu: com.baidu.tts.client.SpeechSynthesizer
    private lateinit var insOfIfly: SpeechSynthesizer

    fun play(txt: String?) {
        if (mVoice != null) mVoice!!.startSpeaking(txt, null)
    }

    fun play(txt: String?, animatorView: View?) {
        play(txt)
        playAnimator(animatorView)
    }

    fun playAnimator(animatorView: View?) {
        if (animatorView == null) return
        //播放声音图标，放大缩小动画
        val objX = ObjectAnimator.ofFloat(animatorView, "scaleX", 1f, 1.3f, 1f, 1.3f, 1f, 1.3f, 1f)
        val objY = ObjectAnimator.ofFloat(animatorView, "scaleY", 1f, 1.3f, 1f, 1.3f, 1f, 1.3f, 1f)
        objX.duration = 800
        objY.duration = 800
        objX.start()
        objY.start()
    }

    fun setVoice(voice: String?) {
        if (mVoice != null) mVoice!!.setParameter(SpeechConstant.VOICE_NAME, voice)
    }

    fun initVoice(context: Context?) {
        val speechUtility = SpeechUtility.createUtility(context, SpeechConstant.APPID + "=5cb48789")
        mVoice = SpeechSynthesizer.createSynthesizer(context, null)
        if (mVoice == null) return
        mVoice!!.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD)
        mVoice!!.setParameter(SpeechConstant.ENGINE_MODE, SpeechConstant.ENGINE_MODE)
        mVoice!!.setParameter(SpeechConstant.VOICE_NAME, voice_凯瑟琳)
    }

    internal var apiKey = "ac50ZaUXVNYqcu05zshdGLBI"
    internal var secretKey = "HSeXdXPhdaWBpqGTVfDGtxjgCgEtkWD0"
    internal var appId = "16028297"

    fun initVoice_Baidu(context: Context?) {
        insOfBaidu = com.baidu.tts.client.SpeechSynthesizer.getInstance()
        insOfBaidu.setContext(context)
        insOfBaidu.setApiKey(apiKey, secretKey)
        insOfBaidu.setAppId(appId)
        insOfBaidu.auth(TtsMode.ONLINE)
        insOfBaidu.setParam(com.baidu.tts.client.SpeechSynthesizer.PARAM_SPEAKER, "2")
        insOfBaidu.initTts(TtsMode.ONLINE)
    }

    fun play_Baidu(txt: String, animatorView: View?): Int {
        val i = play_Baidu(txt)
        playAnimator(animatorView)
        return i
    }

    fun play_Baidu(txt: String): Int {
        val speak = 0
        val speech = Setting.getBoolean("开启朗读")
        if (!speech) return 0
        if (!play_word(txt)) { //            Matcher matcher = Pattern.compile(".+").matcher(txt);
            insOfBaidu.speak(txt)
            //            if (matcher.matches()) {
//                play(txt);
//            } else {
//                instance.speak(txt);
//            }
        }
        return speak
    }

    fun release_Baidu() {
        insOfBaidu.release()
    }

    fun play_word(word: String, animatorView: View?): Boolean {
        val b = play_word(word)
        playAnimator(animatorView)
        return b
    }

    fun play_word(word: String): Boolean {
        if (word.isEmpty()) return false
        var c = word[0]
        val file1 = File(MainActivity.Companion.pathApp, "语音库/$c/$word.mp3")
        var path: String = file1.path
        val words = Pattern.compile("\\s+").split(word)
        var isExist = true
        if (file1.exists()) isExist = false
        val strLink = LinkedList<String>()
        for (strWord in words) {
            if (strWord == "") continue
            strLink.add(strWord)
            c = strWord[0]
            val file = File(MainActivity.Companion.pathApp, "语音库/$c/$strWord.mp3")
            if (!file.exists()) isExist = false
        }
        return if (isExist) {
            val strWord = strLink.first
            strLink.removeFirst()
            c = strWord[0]
            path = File(MainActivity.Companion.pathApp, "语音库/$c/$strWord.mp3").getPath()
            !playSound(path, strLink)
        } else {
            !playSound(path, null)
        }
    }

    private fun playSound(path: String, list: LinkedList<String>?): Boolean {
        val mp = MediaPlayer()
        try {
            mp.setDataSource(path)
            mp.prepare()
            mp.start()
            mp.setOnCompletionListener(OnCompletionListener { mp ->
                if (list == null || list.isEmpty()) {
                    mp.release()
                    return@OnCompletionListener
                }
                val strWord = list.first
                val c = strWord[0]
                list.removeFirst()
                val path: String = File(MainActivity.Companion.pathApp, "语音库/$c/$strWord.mp3").getPath()
                playSound(path, list)
                //                    mp.release();
            })
        } catch (e: IOException) {
            return true
        }
        return false
    }
}