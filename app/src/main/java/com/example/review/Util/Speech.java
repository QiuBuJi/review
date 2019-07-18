package com.example.review.Util;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.media.MediaPlayer;
import android.view.View;

import com.baidu.tts.client.TtsMode;
import com.example.review.MainActivity;
import com.example.review.Setting;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.regex.Pattern;


public abstract class Speech {

    static private SpeechSynthesizer mVoice;

    static public  String                                 voice_小燕   = "xiaoyan";
    static public  String                                 voice_许久   = "aisjiuxu";
    static public  String                                 voice_小萍   = "aisxping";
    static public  String                                 voice_小婧   = "aisjinger";
    static public  String                                 voice_许小宝 = "aisbabyxu";
    static public  String                                 voice_Steve  = "x_steve";
    static public  String                                 voice_凯瑟琳  = "x_catherine";
    private static com.baidu.tts.client.SpeechSynthesizer instance;

    static public void play(String txt) {
        if (mVoice != null)
            mVoice.startSpeaking(txt, null);
    }

    static public void play(String txt, View animatorView) {
        play(txt);
        playAnimator(animatorView);
    }

    public static void playAnimator(View animatorView) {

        if (animatorView == null) return;

        //播放声音图标，放大缩小动画
        ObjectAnimator objX = ObjectAnimator.ofFloat(animatorView, "scaleX", 1, 1.3f, 1, 1.3f, 1, 1.3f, 1);
        ObjectAnimator objY = ObjectAnimator.ofFloat(animatorView, "scaleY", 1, 1.3f, 1, 1.3f, 1, 1.3f, 1);
        objX.setDuration(800);
        objY.setDuration(800);
        objX.start();
        objY.start();
    }

    public static void setVoice(String voice) {
        if (mVoice != null)
            mVoice.setParameter(SpeechConstant.VOICE_NAME, voice);
    }

    public static void initVoice(Context context) {

        SpeechUtility speechUtility = SpeechUtility.createUtility(context, SpeechConstant.APPID + "=5cb48789");

        mVoice = SpeechSynthesizer.createSynthesizer(context, null);
        if (mVoice == null) return;

        mVoice.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        mVoice.setParameter(SpeechConstant.ENGINE_MODE, SpeechConstant.ENGINE_MODE);
        mVoice.setParameter(SpeechConstant.VOICE_NAME , voice_凯瑟琳);
    }


    static String apiKey    = "ac50ZaUXVNYqcu05zshdGLBI";
    static String secretKey = "HSeXdXPhdaWBpqGTVfDGtxjgCgEtkWD0";
    static String appId     = "16028297";

    public static void initVoice_Baidu(Context context) {
        instance = com.baidu.tts.client.SpeechSynthesizer.getInstance();
        instance.setContext(context);

        instance.setApiKey(apiKey, secretKey);
        instance.setAppId(appId);

        instance.auth(TtsMode.ONLINE);

        instance.setParam(com.baidu.tts.client.SpeechSynthesizer.PARAM_SPEAKER, "2");
        instance.initTts(TtsMode.ONLINE);
    }

    public static int play_Baidu(String txt, View animatorView) {
        int i = play_Baidu(txt);
        playAnimator(animatorView);
        return i;
    }

    public static int play_Baidu(String txt) {
        int     speak  = 0;
        boolean speech = Setting.getBoolean("开启朗读");
        if (!speech) return 0;

        if (!play_word(txt)) {
//            Matcher matcher = Pattern.compile(".+").matcher(txt);
            instance.speak(txt);

//            if (matcher.matches()) {
//                play(txt);
//            } else {
//                instance.speak(txt);
//            }
        }
        return speak;
    }

    public static void release_Baidu() {
        instance.release();
    }

    public static boolean play_word(String word, View animatorView) {
        boolean b = play_word(word);
        playAnimator(animatorView);
        return b;
    }

    public static boolean play_word(String word) {
        char   c     = word.charAt(0);
        File   file1 = new File(MainActivity.pathApp, "语音库/" + c + "/" + word + ".mp3");
        String path  = file1.getPath();

        String[] words   = Pattern.compile("\\s+").split(word);
        boolean  isExist = true;
        if (file1.exists()) isExist = false;
        LinkedList<String> strLink = new LinkedList<>();

        for (String strWord : words) {
            if (strWord.equals("")) continue;
            strLink.add(strWord);

            c = strWord.charAt(0);
            File file = new File(MainActivity.pathApp, "语音库/" + c + "/" + strWord + ".mp3");
            if (!file.exists()) isExist = false;
        }

        if (isExist) {
            String strWord = strLink.getFirst();
            strLink.removeFirst();
            c = strWord.charAt(0);
            path = new File(MainActivity.pathApp, "语音库/" + c + "/" + strWord + ".mp3").getPath();

            return !playSound(path, strLink);
        } else {
            return !playSound(path, null);
        }

    }

    private static boolean playSound(String path, final LinkedList<String> list) {
        MediaPlayer mp = new MediaPlayer();
        try {
            mp.setDataSource(path);
            mp.prepare();

            mp.start();
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (list == null || list.isEmpty()) {
                        mp.release();
                        return;
                    }

                    String strWord = list.getFirst();
                    char   c       = strWord.charAt(0);
                    list.removeFirst();
                    String path = new File(MainActivity.pathApp, "语音库/" + c + "/" + strWord + ".mp3").getPath();
                    playSound(path, list);
//                    mp.release();
                }
            });
        } catch (IOException e) {
            return true;
        }
        return false;
    }
}
