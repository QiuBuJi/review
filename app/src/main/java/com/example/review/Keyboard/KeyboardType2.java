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

    public KeyboardType2(Context context, RecyclerView keyboardView, ConstraintLayout container, EditText input, ReviewStruct reviewStruct) {
        super(context, keyboardView, container, input, reviewStruct);
    }

    @Override
    void init() {
        frameInput = rs.getFrame();
        frameRight = rs.getMatchWordExplains();

        makeRandom(frameInput);
        span = 6;

        input.setEnabled(false);
        input.setText("");
        input.setHint("↑↑↑在上面操作↑↑↑");
        input.setShowSoftInputOnFocus(false);
        input.setInputType(InputType.TYPE_NULL);

        Speech.play_Baidu(rs.getShow());
        handleInterface = new HandleInterfaceType2(context, container, frameInput, frameRight);
        boolean camPlay = Setting.getBoolean("开启朗读");


        //单词提示；没开启朗读，则显示单词
        SpanUtil.SpanBuilder spanBuilder = SpanUtil.create();
        if (rs.getLevel() <= 3 || !camPlay) {
            showWord();
        } else {
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

        ArrayList<WordExplain> wes = rs.getMatchWordExplains();

        List<KeyText> kts = Arrays.asList(
                new KeyText(COM_DONE, true, KeyEvent.KEYCODE_ENTER),
                new KeyText(COM_UP, true, KeyEvent.KEYCODE_DPAD_UP, 'u'),
                new KeyText(COM_DOWN, true, KeyEvent.KEYCODE_DPAD_DOWN, 'd'),
                new KeyText(COM_EMPTY, true, KeyEvent.KEYCODE_FORWARD_DEL),
                new KeyText(COM_DELETE, true, KeyEvent.KEYCODE_DEL),
                new KeyText(COM_DONE, true, KeyEvent.KEYCODE_ENTER));

        ArrayList<KeyText> data = new ArrayList<>(kts);

        LibraryList libs    = MainActivity.data.getLibraries();
        LibraryList tempLib = new LibraryList();

        //在库内取出类型为2的数据
        for (LibraryStruct lib : libs) {
            int type = lib.getType();
            if (type == 2) tempLib.add(lib);
        }

        int                    size    = tempLib.size();
        Random                 random  = new Random(System.currentTimeMillis());
        ArrayList<WordExplain> wesTemp = new ArrayList<>();

        //把数据解释取出来
        for (int i = 0; i < 3; i++) {
            int           ramdomNum = random.nextInt(size);
            LibraryStruct ls        = tempLib.get(ramdomNum);

            ArrayList<WordExplain> mwe = ReviewStruct.getMatchWordExplains(ls.getText());
            wesTemp.addAll(mwe);
        }

        //收集为单列数据
        LinkedList<String> link    = new LinkedList<>();
        LinkedList<String> ownList = new LinkedList<>();
        for (WordExplain we : wesTemp) link.addAll(we.explains);
        for (WordExplain we : wes) ownList.addAll(we.explains);

        //让数量保持在24个以内
        int len = link.size() + ownList.size();
        len -= 23;
        if (len > 0) {
            for (int i = 0; i < len; i++) link.removeFirst();
        }
        link.addAll(ownList);

        //去重复
        removeRedundancy(link);
        makeRandom(link);
        sortByCharLen(link);

        //键盘索引字符
        char key = 'a';
        for (int i = 0; i < link.size(); i++) {
            String str = link.get(i);
            if (key == 'u' || key == 'd') key++;
            data.add(new KeyText(str, false, 0, key));
            key++;
        }

        data.add(new KeyText("播放", true, KeyEvent.KEYCODE_DPAD_LEFT));

        return data;
    }

    @Override
    void adapterComplete() {
        super.adapterComplete();
        adapter.textSize = 14;
    }

    public boolean keyDown(int keyCode, char key, int posi) {

        KeyText keyText = null;
        if (posi >= 0) {
            keyText = strData.get(posi);
        } else {

            for (KeyText kt : strData) {
                if (kt.keyCode == keyCode || kt.key == key) {
                    keyText = kt;
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
