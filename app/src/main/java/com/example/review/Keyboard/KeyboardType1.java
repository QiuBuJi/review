package com.example.review.Keyboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.media.MediaPlayer;
import android.support.v7.widget.DrawableUtils;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.review.DataStructureFile.WordExplain;
import com.example.review.MainActivity;
import com.example.review.New.KeyText;
import com.example.review.New.ReviewStruct;
import com.example.review.R;
import com.example.review.Util.SpanUtil;
import com.example.review.Util.Speech;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class KeyboardType1 extends Keyboard {

    private MediaPlayer mp;

    public KeyboardType1(Context context, RecyclerView keyboardView, TextView show, EditText input, ReviewStruct reviewStruct) {
        super(context, keyboardView, show, input, reviewStruct);
    }

    @Override
    void init() {
        input.setShowSoftInputOnFocus(false);
        input.requestFocus();
//        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setInputType(InputType.TYPE_NULL);
    }

    @Override
    public void refresh() {
        String text = rs.getShow();
        int    type = rs.show.getType();

        switch (type) {
//            case TYPE_WORD:
//            case TYPE_EXPLAIN:
//            case TYPE_CHOOSE:
            case TYPE_PICTURE:
                View parent = ((View) show.getParent());
                Drawable drawable = Drawable.createFromPath(text);

                if (drawable == null) {
                    show.setHint("路径内容不是图片！");
                } else {
                    show.setHint("");
                    parent.setBackground(drawable);
                }
                break;
            case TYPE_SOUND:

                //播放音频
                mp = new MediaPlayer();
                try {
                    mp.setDataSource(text);
                    mp.prepare();
                    mp.start();
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mp.release();
                        }
                    });
                } catch (IOException e) {
                    Speech.play(text);
                }
                show.setHint("听声音...");

                break;
            default:
                ArrayList<WordExplain> mwe = ReviewStruct.getMatchWordExplains(text);
                if (!mwe.isEmpty()) {
                    int max = 0;

                    //取最长的一条
                    for (WordExplain we : mwe) {
                        int length = we.category.length();
                        if (length > max) max = length;
                    }

                    for (WordExplain we : mwe) {
                        if (!we.ediable) continue;
                        we.ediable = false;

                        StringBuffer buf    = new StringBuffer();
                        int          length = we.category.length();
                        length = max - length;

                        //添加空格，达到对齐目的
                        for (int i = 0; i < length; i++) buf.append(" ");
                        we.category = buf.toString() + we.category;
                    }


                    SpanUtil.SpanBuilder span = SpanUtil.create();

                    for (WordExplain wordExplain : mwe) {

                        int    color    = getColor(wordExplain.category);
                        span.addForeColorSection(wordExplain.category, color);

                        for (String explain : wordExplain.explains) {
                            span.addSection(explain + "；");
                        }
                        span.addSection("\n");
                    }

                    span.showIn(show);
                } else {
                    show.setText(text);
                }

                break;
        }

    }

    @Override
    public void stop() {
        super.stop();
        if (mp != null)
            mp.stop();
    }

    @Override
    void adapterComplete() {
        adapter.isShowNum = false;
    }

    @Override
    ArrayList<KeyText> getLayout() {
        ArrayList<KeyText> data;
        span = 10;
        data = getRegularLayout();


//        if (rs.getLevel() < UNDER_LEVEL) {
//            data = getRandomLayout();
//
////            if (rs.getLevel() < UNDER_LEVEL - 1)
////                data = getRandomLayout();
////            else data = getLessLayout();
//        } else {
//            data = getRegularLayout();
//        }
        return data;
    }

    private static final int UNDER_LEVEL = 1;

    ArrayList<KeyText> getRandomLayout() {
        ArrayList<KeyText> data = new ArrayList<>();

        String[] strs = new String[]{
                COM_DONE, "", COM_LEFT, COM_RIGHT, COM_EMPTY, COM_DELETE, COM_DONE};
        span = 7;

        StringBuffer someChars  = new StringBuffer();
        String       matchStr   = redundancyGone(rs.getMatch());
        char[]       matchChars = matchStr.toCharArray();


        //去重复
        for (int i = 'a'; i < ('z' + 1); i++) {
            char    ch       = (char) i;
            boolean contains = matchStr.contains(ch + "");
            if (!contains) someChars.append(ch);
        }

        for (String str : strs) data.add(new KeyText(str, true));
        String unionStrs = someChars + matchStr;
        char[] Chars = getSomeRandomChars(new StringBuffer(unionStrs))
                .toCharArray();
        for (char ch : Chars) data.add(new KeyText(ch + ""));
        data.add(new KeyText(""));
        data.add(new KeyText(COM_DONE, true));

        return data;
    }

    ArrayList<KeyText> getRegularLayout() {
        ArrayList<KeyText> data = new ArrayList<>();

        String[] com = new String[]{
                COM_DONE, "", COM_LEFT, COM_RIGHT, "", COM_DELETE, "", COM_EMPTY, "", COM_DONE};
        String[] strs = new String[]{
                "q", "w", "e", "r", "t", "y", "u", "i", "o", "p",
                "a", "s", "d", "f", "g", "", "h", "j", "k", "l",
                "z", "x", "c", "v", "b", "", "n", "m"};
        span = 10;

        for (String str : com) data.add(new KeyText(str, true));
        for (String str : strs) data.add(new KeyText(str));

        data.add(new KeyText(COM_SPACE, true));
//        data.add(new KeyText("", false));

        char[] matchChars = rs.getMatch().toCharArray();

        //去除重复的字符
        for (char chAdd : matchChars) {
            boolean conti = false;

            for (String strExist : strs) {
                String strCh = chAdd + "";

                if (strExist.equals(strCh) ||//重复的不要
                    strCh.equals(" ")) {//空格不要，已经有了
                    conti = true;
                    break;
                }
            }
            if (conti) continue;
            data.add(new KeyText(chAdd + ""));
        }


        return data;
    }

    ArrayList<KeyText> getLessLayout() {
        ArrayList<KeyText> data = new ArrayList<>();

        StringBuffer someChars  = new StringBuffer();
        String       matchStr   = redundancyGone(rs.getMatch());
        char[]       matchChars = matchStr.toCharArray();


        int length = rs.getMatch().length();
        int more   = (char) (length * 1.8);
        if (more < 40) {
            int column = more / 4;
            if (column < 4) {

                column = more / 3;
                if (column < 3) {

                    column = more / 2;
                    if (column < 2) span = more;
                    else span = column;
                } else span = column;
            } else span = column;
        }


        //去重复
        for (int i = 'a'; i < ('z' + 1); i++) {
            boolean conti = false;
            for (char ch : matchChars) {
                if (i == ch) {
                    conti = true;
                    break;
                }
            }
            if (conti) continue;
            someChars.append((char) i);
        }
        for (char matchChar : matchChars) {
            someChars.append(matchStr);
        }

        //填补空洞
        more += span - (more % span);

        String extraStr  = getSomeRandomChars(someChars, more - matchChars.length);
        String unionStrs = extraStr + matchStr;

        StringBuffer keys1  = new StringBuffer("qwertyuiop");
        StringBuffer keys2  = new StringBuffer("asdfghjkl");
        StringBuffer keys3  = new StringBuffer("zxcvbnm");
        StringBuffer keys[] = new StringBuffer[]{keys1, keys2, keys3};

        for (int i = 0; i < keys.length; i++) {
            StringBuffer key = keys[i];

            for (int k = 0; k < key.length(); k++) {

                char    ch       = key.charAt(k);
                boolean contains = unionStrs.contains(ch + "");
                if (!contains) {
                    key.deleteCharAt(k);
                    k--;
                }
            }
        }

        for (StringBuffer key : keys) {
            char[] chars = key.toString().toCharArray();
            for (char aChar : chars) data.add(new KeyText(aChar + ""));
        }

        data.add(new KeyText(COM_DONE, true));
        data.add(new KeyText(""));
        data.add(new KeyText(COM_EMPTY, true));
        data.add(new KeyText(COM_DONE, true));

        return data;
    }

    String redundancyGone(String tobeRemoved) {
        StringBuffer sb = new StringBuffer(tobeRemoved);

        for (int i = 0; i < sb.length(); i++) {
            char ch = sb.charAt(i);

            int k;
            while ((k = sb.indexOf(ch + "", i + 1)) != -1) {
                sb.deleteCharAt(k);
            }
        }
        return sb.toString();
    }

    String getSomeRandomChars(StringBuffer text, int number) {
        StringBuffer sb   = new StringBuffer(text);
        StringBuffer temp = new StringBuffer();
        if (number > sb.length()) number = sb.length();

        for (int i = 0; i < number; i++) {
            int random = (int) (Math.random() * 10000) % sb.length();
            temp.append(sb.charAt(random));
            sb.deleteCharAt(random);
        }
        return temp.toString();
    }

    String getSomeRandomChars(StringBuffer text) {
        return getSomeRandomChars(text, text.length());
    }

    @Override
    public void onItemClick(View view, ArrayList<KeyText> data, int posi) {
        super.onItemClick(view, data, posi);

        TextView textViewnum = view.findViewById(R.id.item_textView_number);
        String   inputText   = input.getText().toString();
        KeyText  keyText     = data.get(posi);

        if (keyText.isCom) {
            int selection;
            selection = input.getSelectionStart();
            switch (keyText.text) {
                case COM_SPACE:
                    input.append(" ");
                    break;
                case COM_DONE:
                    break;
                case COM_DELETE:
                    Editable editable = input.getText();
                    if (0 > (selection - 1)) break;
                    editable.delete(selection - 1, selection);
                    break;
                case COM_LEFT:
                    --selection;
                    if (0 > selection) break;
                    input.setSelection(selection);
                    break;
                case COM_RIGHT:
                    ++selection;
                    if (inputText.length() < selection) break;
                    input.setSelection(selection);
                    break;
                case COM_EMPTY:
                    selection = -1;
                    if (!inputText.equals("")) input.setText("");
                    if (rs.getLevel() < UNDER_LEVEL) adapter.notifyDataSetChanged();
                    break;
            }
        } else {
            Editable editableText = input.getEditableText();
            int      selection    = input.getSelectionStart();
            editableText.insert(selection, keyText.text);
//
            int type = rs.match.getType();
            if (type == TYPE_WORD && rs.getLevel() < 1) {
//                Speech.play_Baidu(keyText.text);
            }

            if (rs.getLevel() < UNDER_LEVEL) {
                String num   = (String) textViewnum.getText();
                int    value = Integer.valueOf(num);
                textViewnum.setText(++value + "");
                textViewnum.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean keyDown(int keyCode, char key, int posi) {
        return false;
    }
}
