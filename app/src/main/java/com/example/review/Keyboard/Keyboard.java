package com.example.review.Keyboard;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.review.Adapter.MyAdapter;
import com.example.review.New.ItemClickListener;
import com.example.review.New.KeyText;
import com.example.review.New.ReviewStruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public abstract class Keyboard implements ItemClickListener {
    final public static String COM_SPACE  = "空格";
    final public static String COM_DONE   = "完成";
    final public static String COM_DELETE = "←";
    final public static String COM_EMPTY  = "清空";
    final public static String COM_LEFT   = "<";
    final public static String COM_RIGHT  = ">";
    final public static String COM_UP     = "↑";
    final public static String COM_DOWN   = "↓";

    final public static int TYPE_WORD    = 1;
    final public static int TYPE_EXPLAIN = 2;
    final public static int TYPE_CHOOSE  = 3;
    final public static int TYPE_PICTURE = 4;
    final public static int TYPE_SOUND   = 5;

    String[]     mPriorites    = new String[]{"n.", "vt.", "adj.", "pron.", "conj.", "adv.", "intj.", "adv.", "art.", "vi."};
    List<String> strPriorities = Arrays.asList(mPriorites);

    int          span;
    int          index;
    Context      context;
    RecyclerView keyboardView;
    TextView     show;
    EditText     input;

    public MyAdapter adapter;
    ArrayList<KeyText> strData;

    ReviewStruct rs;

    public Keyboard(Context context, RecyclerView keyboardView, TextView show, EditText input, ReviewStruct reviewStruct) {
        this.context = context;
        this.keyboardView = keyboardView;
        this.show = show;
        this.input = input;
        this.rs = reviewStruct;

        init();
    }

    abstract void init();

    public abstract void refresh();

    abstract ArrayList<KeyText> getLayout();

    void adapterComplete() {

    }

    public int getColor(String src) {
        src = src.replaceAll("\\s", "");//去除空格
        int prefixColor;
        int i           = strPriorities.indexOf(src);

        switch (i) {
            case 0:
                prefixColor = Color.RED;
                break;
            case 1:
                prefixColor = Color.GREEN;
                break;
            case 2:
                prefixColor = Color.DKGRAY;
                break;
            case 3:
                prefixColor = Color.BLUE;
                break;
            case 4:
                prefixColor = Color.MAGENTA;
                break;
            case 5:
                prefixColor = Color.CYAN;
                break;
            default:
                prefixColor = Color.GRAY;
        }

        return prefixColor;
    }

    public static <E> void makeRandom(List<E> list) {
        Random random = new Random(System.currentTimeMillis());
        int    size   = list.size();

        for (int i = 0; i < list.size(); i++) {
            E str = list.get(i);
            list.remove(str);
            int index = random.nextInt(size);
            list.add(index, str);
        }
    }

    public static <E> void removeRedundancy(List<E> link) {
        for (int i = 0; i < link.size(); i++) {
            E match = link.get(i);

            for (int k = i + 1; k < link.size(); k++) {
                E temp = link.get(k);

                if (match.equals(temp)) {
                    link.remove(k);
                    k--;
                }
            }
        }
    }

    //按字符长度排序
    public static void sortByCharLen(List<String> link) {
        LinkedList<String> linkTemp = new LinkedList<>(), linkc = new LinkedList<>();
        int                len      = 0;

        while (!link.isEmpty()) {
            len++;

            for (int i = 0; i < link.size(); i++) {
                String str = link.get(i);

                if (str.length() == len) {
                    linkTemp.add(str);
                    link.remove(i);
                    i--;
                }
            }
            makeRandom(linkTemp);
            linkc.addAll(linkTemp);
            linkTemp.clear();
        }
        link.addAll(linkc);
    }

    public void buildKeyboard(int millis) {
        final Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                buildKeyboard();
                return false;
            }
        });

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
            }
        }, millis);
    }

    public void buildKeyboard() {
        refreshLayout();
    }

    void refreshLayout() {
        strData = getLayout();
        adapter = new MyAdapter(context, strData, this);
        adapterComplete();
        keyboardView.setAdapter(adapter);
        keyboardView.setLayoutManager(new GridLayoutManager(context, span));
//        main.recyclerViewKeyboard.setLayoutManager(new StaggeredGridLayoutManager(span, StaggeredGridLayoutManager.VERTICAL));
    }

    public interface OnKeyDownListener {
        void onKeyDown(KeyText keyText);
    }

    OnKeyDownListener onKeyDownListener;

    public void setOnKeyDownListener(OnKeyDownListener onKeyDownListener) {
        this.onKeyDownListener = onKeyDownListener;
    }

    @Override
    public void onItemClick(View view, ArrayList<KeyText> data, int posi) {
        if (posi < 0) return;
        KeyText keyText = data.get(posi);
        if (onKeyDownListener != null) onKeyDownListener.onKeyDown(keyText);

        keyDown(0, (char) 0, posi);
    }

    public abstract boolean keyDown(int keyCode, char key, int posi);

    public void clearKeyboard() {
        if (adapter != null) {
            strData.clear();
            adapter.notifyDataSetChanged();
        }
    }

    public void stop() {
    }
}
