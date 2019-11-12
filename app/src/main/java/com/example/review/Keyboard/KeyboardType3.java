package com.example.review.Keyboard;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.review.Activity.MainActivity;
import com.example.review.New.KeyText;
import com.example.review.New.LibraryList;
import com.example.review.New.LibraryStruct;
import com.example.review.New.ReviewStruct;
import com.example.review.R;
import com.example.review.Util.SpanUtil;
import com.example.review.Util.Speech;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeyboardType3 extends Keyboard {

    public  ArrayList<String>  mCandidate;
    private ArrayList<String>  candidate;
    public  ArrayList<TextCom> mCandidateType;
    private Pattern            patterBitPar;
    private Pattern            patternPar;
    private TextView           show;
    private View               inflate;

    public KeyboardType3(Context context, RecyclerView keyboardView, ConstraintLayout show, EditText input, ReviewStruct reviewStruct) {
        super(context, keyboardView, show, input, reviewStruct);
    }

    public void setLightAnimation(boolean lightUp, int duration) {
        ValueAnimator valueAnim;
        if (lightUp)
            valueAnim = ValueAnimator.ofFloat(0f, 1f);
        else valueAnim = ValueAnimator.ofFloat(1f, 0f);

        valueAnim.setDuration(duration);
        valueAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (Float) valueAnimator.getAnimatedValue();
                inflate.setAlpha(value);
            }
        });
        valueAnim.start();
    }

    @Override
    void init() {
        input.setHint("");
        input.setText("");

        inflate = LayoutInflater.from(context).inflate(R.layout.activity_text_view, container, false);
        show = inflate.findViewById(R.id.tv_text);
        container.addView(inflate);

        String strMatch = rs.getMatch();
        patternPar = Pattern.compile("\\(.*?\\)");
        String[] split   = patternPar.split(strMatch);
        Matcher  matcher = patternPar.matcher(strMatch);

        candidate = new ArrayList<>();
        mCandidate = new ArrayList<>();
        mCandidateType = new ArrayList<>();

        //找出备选词语
        while (matcher.find()) {
            int    start = matcher.start();
            int    end   = matcher.end();
            String word  = strMatch.substring(start + 1, end - 1);
            mCandidate.add(word);
        }
        if (split.length == 0) {
            split = new String[mCandidate.size()];
            for (int i = 0; i < split.length; i++) split[i] = " ";
        }


        LibraryList       libs  = MainActivity.data.getLibraries();
        ArrayList<String> list  = new ArrayList<>();
        int               index = 0;

        //在库内取一些数据
        for (LibraryStruct lib : libs) {
            int type = lib.getType();
            if (type == 1) {
                list.add(lib.getText());
            }
            if (index++ == 100) break;
        }

        //随机取出数据
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < 10; i++) {
            String str = list.get(random.nextInt(list.size()));
            candidate.add(str);
        }

        //小括号，备选词
        String  show = rs.getShow();
        Matcher mat  = patternPar.matcher(show);
        while (mat.find()) {
            int    start = mat.start();
            int    end   = mat.end();
            String word  = show.substring(start + 1, end - 1);
            candidate.add(word);
        }

        //大括号，备选词
        patterBitPar = Pattern.compile("\\{.*\\}");
        Matcher matcherBigPar = patterBitPar.matcher(show);

        while (matcherBigPar.find()) {
            int      start    = matcherBigPar.start();
            int      end      = matcherBigPar.end();
            String   strArray = show.substring(start + 1, end - 1);
            String[] words    = Pattern.compile("[,，]").split(strArray);

            Collections.addAll(candidate, words);
        }

        candidate.addAll(mCandidate);
        removeRedundancy(candidate);
        makeRandom(candidate);

        int length = mCandidate.size();

        for (String s : split) {
            mCandidateType.add(new TextCom(s));
            if (length-- == 0) break;
            mCandidateType.add(new TextCom("  ", true));
        }

        input.setText("");
        input.setHint("↑↑↑在上面操作↑↑↑");
        input.setShowSoftInputOnFocus(false);
        input.setInputType(InputType.TYPE_NULL);
    }


    @Override
    void adapterComplete() {
        super.adapterComplete();
        adapter.textSize = 14;
    }

    @Override
    public void refresh() {
        int                  posi        = 0;
        SpanUtil.SpanBuilder spanBuilder = SpanUtil.create();
        String               show        = rs.getShow();

        //不显示，备选内容
        show = patternPar.matcher(show).replaceAll("");
        show = patterBitPar.matcher(show).replaceAll("");

        spanBuilder.addSection(show + "\n");

        for (TextCom tc : mCandidateType) {
            if (tc.isCandidate) {

                if (posi == index)
                    spanBuilder.addBackColorSection(tc.text, Color.LTGRAY).setStyle(tc.text, Typeface.BOLD);
                else
                    spanBuilder.addForeColorSection(tc.text, Color.BLACK).setStyle(tc.text, Typeface.BOLD);

                if (tc.isStrike) spanBuilder.setStrikethrough(tc.text);
                spanBuilder.setUnderline(tc.text);
                posi++;
            } else spanBuilder.addForeColorSection(tc.text, Color.GRAY);
        }
        spanBuilder.showIn(this.show);

        this.show.setHint("格式不对！");
    }

    @Override
    ArrayList<KeyText> getLayout() {
        span = 6;

        KeyText[] kts = new KeyText[]{
                new KeyText(COM_DONE, true, KeyEvent.KEYCODE_ENTER),
                new KeyText(COM_LEFT, true, KeyEvent.KEYCODE_DPAD_UP, 'l'),
                new KeyText(COM_RIGHT, true, KeyEvent.KEYCODE_DPAD_DOWN, 'r'),
                new KeyText(COM_EMPTY, true, KeyEvent.KEYCODE_FORWARD_DEL),
                new KeyText(COM_DELETE, true, KeyEvent.KEYCODE_DEL),
                new KeyText(COM_DONE, true, KeyEvent.KEYCODE_ENTER)};

        ArrayList<KeyText> data = new ArrayList<>(Arrays.asList(kts));

        sortByCharLen(candidate);

        char key = 'a';
        for (String str : candidate) {
            if (key == 'l' || key == 'r') key++;

            data.add(new KeyText(str, false, 0, key));
            key++;
        }

        return data;
    }

    public class TextCom {
        public TextCom(String text) {
            this.text = text;
        }

        public TextCom(String text, boolean isCandidate) {
            this.text = text;
            this.isCandidate = isCandidate;
        }

        public String  text;
        public boolean isCandidate;
        public boolean isStrike;
    }

    @Override
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
        ArrayList<TextCom> tcs = new ArrayList<>();
        for (TextCom textCom : mCandidateType) {
            if (textCom.isCandidate) tcs.add(textCom);
        }

        TextCom textCom;

        if (keyText.isCom) {
            switch (keyText.text) {
                case COM_DONE:
                    break;
                case COM_LEFT:
                    if (--index < 0) index = tcs.size() - 1;
                    break;
                case COM_RIGHT:
                    if (++index == tcs.size()) index = 0;
                    break;
                case COM_EMPTY:
                    if (!tcs.isEmpty()) {
                        for (TextCom com : tcs) {
                            if (com.isCandidate) {
                                com.text = "    ";
                                com.isStrike = false;
                            }
                        }
                        index = 0;
                    }
                    break;
                case COM_DELETE:
                    if (!tcs.isEmpty()) {
                        TextCom textCom1 = tcs.get(index);
                        textCom1.text = "    ";
                        textCom1.isStrike = false;
                    }
                    break;
                default:
            }

        } else {
            if (!tcs.isEmpty()) {
                textCom = tcs.get(index);
                textCom.text = keyText.text;
                textCom.isStrike = false;

                Speech.play_Baidu(keyText.text);

                if (++index == tcs.size()) index = 0;
            }
        }

        refresh();
        return true;
    }
}
