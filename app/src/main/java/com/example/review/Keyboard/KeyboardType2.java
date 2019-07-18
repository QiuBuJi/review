package com.example.review.Keyboard;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.review.DataStructureFile.WordExplain;
import com.example.review.MainActivity;
import com.example.review.New.KeyText;
import com.example.review.New.LibrarySet;
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

    public  ArrayList<WordExplain> frame;
    private ArrayList<WordExplain> frameTemp;

    public KeyboardType2(Context context, RecyclerView keyboardView, TextView show, EditText input, ReviewStruct reviewStruct) {
        super(context, keyboardView, show, input, reviewStruct);
    }

    @Override
    void init() {
        frame = rs.getFrame();
        frameTemp = rs.getMatchWordExplains();

        makeRandom(frame);
        span = 6;

        input.setText("");
        input.setHint("↑↑↑在上面操作↑↑↑");
        input.setShowSoftInputOnFocus(false);
        input.setInputType(InputType.TYPE_NULL);

        Speech.play_Baidu(rs.getShow());
    }

    @Override
    public void refresh() {
        int max = 0;
        if (frame.isEmpty()) return;

        //取最长的一条
        for (WordExplain we : frame) {
            int length = we.category.length();
            if (length > max) max = length;
        }

        for (WordExplain we : frame) {
            if (!we.ediable) continue;
            we.ediable = false;

            StringBuilder buf    = new StringBuilder();
            int           length = we.category.length();
            length = max - length;

            for (int i = 0; i < length; i++) buf.append(" ");
            we.category = buf.toString().concat(we.category);
        }


        SpanUtil.SpanBuilder span    = SpanUtil.create();
        boolean              camPlay = Setting.getBoolean("开启朗读");

        //单词提示；没开启朗读，则显示单词
        if (rs.getLevel() < 3 || !camPlay) {
            String strWord = "--- " + rs.getShow() + " ---\n\n";
            span.addForeColorSection(strWord, Color.BLUE);
            span.setTypeface(strWord, "sans-serif");
        } else {
            //点击提示
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {
                    Toast.makeText(context, rs.getShow(), Toast.LENGTH_SHORT).show();
                }
            };
            String section = "提示?\n\n";
            span.addAbsSizeSection(section, 38);
            span.getSpanStrBuilder().setSpan(clickableSpan, 0, section.length() - 2, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        int i = 0;
        for (WordExplain wordExplain : frame) {

            int prefixColor = getColor(wordExplain.category);
            span.addForeColorSection(wordExplain.category, prefixColor);

            for (String explain : wordExplain.explains) {
                boolean contains = "×".equals(explain.charAt(0) + "");
                if (contains) {
                    String section = explain.substring(1) + "；";
                    span.addForeColorSection(section, Color.LTGRAY);
                    span.setStrikethrough(section);
                } else span.addSection(explain + "；");

            }

            if (i++ == index) span.addForeColorSection("«", Color.BLUE);
            span.addSection("\n");
        }

        show.setMovementMethod(LinkMovementMethod.getInstance());
        span.showIn(show);
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

        LibrarySet libs    = MainActivity.data.getLibraries();
        LibrarySet tempLib = new LibrarySet();

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

        WordExplain wordExplain;

        if (keyText.isCom) {
            switch (keyText.text) {
                case COM_DONE:
                    if (onKeyDownListener != null) onKeyDownListener.onKeyDown(keyText);
                    break;
                case COM_UP:
                    index--;
                    int size = frame.size();
                    if (index < 0) index = size - 1;
                    break;
                case COM_DOWN:
                    index++;
                    size = frame.size();
                    if (index == size) index = 0;
                    break;
                case COM_EMPTY:
                    for (WordExplain explain : frame) {
                        explain.explains.clear();
                        index = 0;
                    }
                    break;
                case COM_DELETE:
                    wordExplain = frame.get(index);
                    int position = wordExplain.explains.size();
                    if (position > 0) {
                        wordExplain.explains.remove(--position);
                    }
                    break;
                case "播放":
                    Speech.play_Baidu(rs.getShow());
                    break;
            }
        } else {
            wordExplain = frame.get(index);
            WordExplain we = frameTemp.get(index);

            for (WordExplain explain : frameTemp) {
                String category1 = explain.category.trim();
                String category2 = wordExplain.category.trim();

                if (category1.equals(category2)) {
                    we = explain;
                    break;
                }
            }

            //主动跳转下一行
            int size = wordExplain.explains.size();
            if (size < we.explains.size()) {
                if (wordExplain.explains.contains(keyText.text)) {
                    Toast.makeText(context, "不能重复哦！", Toast.LENGTH_SHORT).show();
                } else wordExplain.explains.add(keyText.text);

            }

            while (true) {
                size = wordExplain.explains.size();

                if (size == we.explains.size()) {
                    index++;
                    size = frame.size();
                    if (index == size) {
                        index = 0;
                        break;
                    }
                } else {
                    break;
                }
                wordExplain = frame.get(index);
            }
        }

        refresh();
        return true;
    }
}
