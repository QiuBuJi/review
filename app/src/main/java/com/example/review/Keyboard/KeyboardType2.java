package com.example.review.Keyboard;

import android.content.Context;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import com.example.review.DataStructureFile.WordExplain;
import com.example.review.Activity.MainActivity;
import com.example.review.New.KeyText;
import com.example.review.New.LibraryList;
import com.example.review.New.LibraryStruct;
import com.example.review.New.ReviewStruct;
import com.example.review.Setting;
import com.example.review.Util.SpanUtil;
import com.example.review.Util.Speech;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class KeyboardType2 extends Keyboard {

    public ArrayList<WordExplain> frameInput;
    public ArrayList<WordExplain> frameRight;
    public HandleInterfaceType2   handleInterface;

    public static LibraryList libTemp;

    public KeyboardType2(Context context, RecyclerView keyboardView, ConstraintLayout container, EditText input, ReviewStruct reviewStruct) {
        super(context, keyboardView, container, input, reviewStruct);
    }

    @Override
    void init() {
        input.setEnabled(false);
        input.setText("");
        input.setHint("");
        input.setShowSoftInputOnFocus(false);
        input.setInputType(InputType.TYPE_NULL);

        frameInput = rs.getFrame();
        frameRight = rs.getMatchWordExplains();

        makeRandom(frameInput);
        span = 6;

        Speech.play_Baidu(rs.getShow());
        handleInterface = new HandleInterfaceType2(context, container, frameInput, frameRight);
        boolean camPlay = Setting.getBoolean("开启朗读");


        //单词提示；没开启朗读，则显示单词
        SpanUtil.SpanBuilder spanBuilder = SpanUtil.create();
        if (rs.getLevel() <= 3 || !camPlay) showWord();
        else {
            spanBuilder.addForeColorSection("--- ", Color.LTGRAY)
                       .addForeColorSection("?", Color.RED)
                       .addForeColorSection(" ---", Color.LTGRAY)
                       .showIn(handleInterface.windowExplainHolder.explainTitle);

            handleInterface.windowExplainHolder.explainTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showWord();
                }
            });
        }
        handleInterface.setLightAnimation(true, 200);
    }

    @Override
    public void setLightAnimation(boolean lightUp, int duration) {
        handleInterface.setLightAnimation(lightUp, duration);
    }

    void showWord() {
        SpanUtil.SpanBuilder spanBuilder = SpanUtil.create();
        spanBuilder.addForeColorSection("--- ", Color.LTGRAY)
                   .addForeColorSection(rs.getShow(), Color.BLACK)
                   .addForeColorSection(" ---", Color.LTGRAY)
                   .showIn(handleInterface.windowExplainHolder.explainTitle);
    }

    @Override
    public void refresh() {
        handleInterface.refresh();
    }

    @Override
    ArrayList<KeyText> getLayout() {
        return getWideLayout();
    }

    private ArrayList<KeyText> getWideLayout() {

        ArrayList<WordExplain> wesNative = rs.getMatchWordExplains();

        List<KeyText> kts = Arrays.asList(
                new KeyText(COM_DONE, true, KeyEvent.KEYCODE_ENTER),
                new KeyText(COM_UP, true, KeyEvent.KEYCODE_DPAD_UP, 'u'),
                new KeyText(COM_DOWN, true, KeyEvent.KEYCODE_DPAD_DOWN, 'd'),
                new KeyText(COM_EMPTY, true, KeyEvent.KEYCODE_FORWARD_DEL),
                new KeyText(COM_DELETE, true, KeyEvent.KEYCODE_DEL),
                new KeyText(COM_DONE, true, KeyEvent.KEYCODE_ENTER));

        ArrayList<KeyText> data = new ArrayList<>(kts);

        //libTemp没数据就要弄点数据了
        if (libTemp == null) {
            libTemp = new LibraryList();
            LibraryList library = MainActivity.data.getLibrary();

            //在库内取出类型为2且不是引用的数据
            for (LibraryStruct lib : library)
                if (lib.getType() == 2 && lib.refer == 0) libTemp.add(lib);
        }

        int                    size     = libTemp.size();
        Random                 random   = new Random(System.currentTimeMillis());
        ArrayList<WordExplain> wesAdded = new ArrayList<>();

        //把数据解释取出来
        for (int i = 0; i < 5; i++) {
            int           ramdomNum = random.nextInt(size);
            LibraryStruct ls        = libTemp.get(ramdomNum);

            ArrayList<WordExplain> mwe = ReviewStruct.getMatchWordExplains(ls.getText());
            wesAdded.addAll(mwe);
        }

        //收集为单列数据
        LinkedList<String> wordsNative = new LinkedList<>();
        LinkedList<String> wordsAdded  = new LinkedList<>();
        for (WordExplain we : wesNative) wordsNative.addAll(we.explains);
        for (WordExplain we : wesAdded) wordsAdded.addAll(we.explains);

        //去重复
        removeRedundancy(wordsNative);
        removeRedundancy(wordsAdded);
        wordsAdded.removeAll(wordsNative);

        int nativeSize = wordsNative.size();
        int num        = nativeSize / 6;
        int mode       = nativeSize % 6;

        if (mode > 0) num++;
        float nativeRate = wordsNative.size() / (num * 6f - 1);
        if (nativeRate >= 0.5) num++;//本地词语数超过这个阈值，则增加一行的混淆词语
        size = wordsNative.size() + wordsAdded.size() + 1;
        size -= num * 6;

        //去掉多余的数据
        for (int i = 0; i < size; i++) {
            if (!wordsAdded.isEmpty()) wordsAdded.removeFirst();
        }
        wordsAdded.addAll(wordsNative);
        makeRandom(wordsAdded);
        sortByCharLen(wordsAdded);

        //键盘索引字符
        char key = 'a';
        for (int i = 0; i < wordsAdded.size(); i++) {
            String str = wordsAdded.get(i);

            if (key == 'u' || key == 'd') key++;
            data.add(new KeyText(str, false, 0, key));
            key++;
        }

        data.add(new KeyText("播放", true, KeyEvent.KEYCODE_SPACE));
        return data;
    }

    @Override
    void adapterComplete() {
        super.adapterComplete();
        adapter.textSize = 14;
    }

    public boolean keyDown(int keyCode, char key, int posi) {
        KeyText keyText = null;

        if (posi >= 0) keyText = strData.get(posi);
        else {
            for (KeyText kt : strData) {
                if (kt.keyCode == keyCode || kt.key == key) {
                    keyText = kt;

                    //模拟单击
                    if (kt.view != null) {
                        kt.view.performClick();
                        kt.view.setPressed(true);
                        kt.view.setPressed(false);
                        return true;
                    }
                    break;
                }
            }
        }

        if (keyText == null) return false;

        if (keyText.isCom) {
            switch (keyText.text) {
                case COM_DONE:
//                    if (onKeyDownListener != null) onKeyDownListener.onKeyDown(keyText);
                    break;
                case COM_UP:
                    handleInterface.moveUp();
                    break;
                case COM_DOWN:
                    handleInterface.moveDown();
                    break;
                case COM_EMPTY:
                    handleInterface.emptying();
                    //设置给按键，显示字符
                    for (KeyText kt : strData) kt.isPressed = false;
                    adapter.notifyDataSetChanged();
                    break;
                case COM_DELETE:
                    String deletedStr = handleInterface.delete();

                    //设置给按键，显示字符
                    for (int i = 0; i < strData.size(); i++) {
                        KeyText kt = strData.get(i);

                        if (kt.text.equals(deletedStr)) {
                            kt.isPressed = false;
                            adapter.notifyItemChanged(i);
                            break;
                        }
                    }
                    break;
                case "播放":
                    Speech.play_Baidu(rs.getShow());
                    handleInterface.windowExplainHolder.explainTitle.performClick();
                    break;
            }
        } else {
            boolean isSuccess = handleInterface.addSegment(keyText.text);

            //设置给按键，不显示字符
            if (isSuccess) {
                int wordCountRight = 0, wordCountInput = 0;

                //累计keyText.text在frameRight中重复数量
                for (WordExplain we : frameRight) {
                    for (String word : we.explains) if (word.equals(keyText.text)) wordCountRight++;
                }

                //累计keyText.text在frameInput中重复数量
                for (WordExplain we : frameInput) {
                    for (String word : we.explains) if (word.equals(keyText.text)) wordCountInput++;
                }

                //wordCountRight为0，则keyText.text它不是正确的，就只能单击1次
                if (wordCountRight == wordCountInput || wordCountRight == 0)
                    keyText.isPressed = true;
                else keyText.isPressed = false;

                adapter.notifyItemChanged(posi);
            }
        }
        return true;
    }
}
