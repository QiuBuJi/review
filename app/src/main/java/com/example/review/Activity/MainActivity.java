package com.example.review.Activity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.review.Adapter.MyAdapter;
import com.example.review.Animator.TextColorAnimator;
import com.example.review.DataStructureFile.DateTime;
import com.example.review.DataStructureFile.ElementCategory;
import com.example.review.DataStructureFile.ReviewData;
import com.example.review.DataStructureFile.WordExplain;
import com.example.review.Keyboard.Keyboard;
import com.example.review.Keyboard.KeyboardType1;
import com.example.review.Keyboard.KeyboardType2;
import com.example.review.Keyboard.KeyboardType3;
import com.example.review.New.CountList;
import com.example.review.New.KeyText;
import com.example.review.New.ReviewStruct;
import com.example.review.R;
import com.example.review.ReviewService;
import com.example.review.Setting;
import com.example.review.Util.ColorfulText;
import com.example.review.Util.SpanUtil;
import com.example.review.Util.Speech;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
                                                               TextWatcher,
                                                               ServiceConnection,
                                                               Handler.Callback {

    private static final String TAG = "msg_mine";

    static public ReviewData data = new ReviewData();

    public static File externalRoot = Environment.getExternalStorageDirectory();//外部存储夹根目录
    public static File pathApp      = new File(externalRoot, "Review");//软件根目录
    public static File pathNexus    = new File(pathApp, "nexus.lib");//数据所在目录
    public static File pathLibrary  = new File(pathApp, "library.lib");//数据所在目录
    public static File pathInit     = new File(pathApp, "Total Word.ini");//数据所在目录

    //**************************************** Views ************************************************
    TextView textViewTime;
    TextView textViewAbout;
    TextView textViewPercent;
    TextView tips;
    TextView lastText;

    ImageView imageViewDetail;
    ImageView imageViewSort;
    ImageView imageViewPlaySound;

    ProgressBar  progressBarProgress;
    EditText     etInput;
    ImageButton  imageButtonSetting;
    ImageButton  imageButtonClear;
    TextView     tvLevel;
    RecyclerView recyclerViewKeyboard;
    TextView     tvTitle;
    TextView     tvNext;
    TextView     tvReviewedNum;
    //***********************************************************************************************

    private TextView textViewArrival;

    final  int HANDLER_UPDATE_SHOWING = 4;
    final  int HANDLER_START_TIMER    = 3;
    static int canJoinLog             = 0;

    boolean       correct;
    Handler       handler = new Handler(this);
    ReviewService service;
    Keyboard      keyboard;
    private ConstraintLayout entireBackground;

    private List<PathBoth>   pathBoth;
    private int              libIndex     = 0;
    private int              mReviewedNum = 0;
    private ConstraintLayout mainContainer;
    private TextView         tvLastDuration;

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                break;
            case HANDLER_START_TIMER: {//读取数据完毕
            }
            break;
            case HANDLER_UPDATE_SHOWING:
                refreshShowing(false);
                break;
            case 5:
                break;
        }

        return false;
    }

    //刷新即将到来的时间
    private void refreshArrivalTime() {

        //显示下一个待复习数据到现在的剩余时间
        if (!data.mInactivate.isEmpty()) {
            DateTime dateTime = new DateTime(data.mInactivate.get(0).time);
            dateTime.subtractOf(DateTime.getCurrentTime());

            textViewArrival.setText(dateTime.toNoneZero0String());
        }
    }

    int          state = 2;
    ReviewStruct lastRs;

    //刷新显示界面文字
    void refreshShowing(boolean isChange) {
        if (keyboard != null) keyboard.stop();

        //屏幕没有显示，则不启动
        if (!isShowedScreen) return;

        //初始化显示界面
        mainContainer.setBackgroundResource(R.drawable.bg_text_show);

        //避开下标越界，有复习数据*********************************************************************
        if (!data.mActivate.isEmpty()) {
            final ReviewStruct rs = data.mActivate.getFirst();
            tvLevel.setText(String.format(Locale.CHINA, "%d", rs.getLevel()));
            tips.setText("");

            //显示距离上次复习间隔了多久
            DateTime dateTime;
            String   text;
            try {
                dateTime = new DateTime(rs.logs.getLast());
                DateTime subtract = DateTime.getCurrentTime().subtract(dateTime);
                text = subtract.toAboutValue();
            } catch (Exception e) {
                text = "编辑";
            }

            SpanUtil.create()
                    .addUnderlineSection(text)
                    .showIn(tvLastDuration);

            tvLastDuration.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //跳转页面，到编辑窗口
                    ListActivity.currentClickedRs = rs;
                    MainActivity.this.startActivity(new Intent(MainActivity.this, EditActivity.class));
                }
            });

            //不让重复刷新
            if (isChange || lastRs != rs) {
                lastRs = rs;
                int type = rs.match.getType();

                switch (type) {
                    case Keyboard.TYPE_WORD:
                        keyboard = new KeyboardType1(this, recyclerViewKeyboard, mainContainer, etInput, rs);
                        keyboard.buildKeyboard();
                        break;
                    case Keyboard.TYPE_EXPLAIN:
                        keyboard = new KeyboardType2(this, recyclerViewKeyboard, mainContainer, etInput, rs);
                        keyboard.buildKeyboard();
                        break;
                    case Keyboard.TYPE_CHOOSE:
                        keyboard = new KeyboardType3(this, recyclerViewKeyboard, mainContainer, etInput, rs);
                        keyboard.buildKeyboard();
                        break;
                    case Keyboard.TYPE_PICTURE:
                        break;
                    case Keyboard.TYPE_SOUND:
                        break;
                }
            }
            if (keyboard != null) {
                keyboard.setOnKeyDownListener(getOnKeyDownListener());
                keyboard.refresh();
            }

            state = 1;
        } else {
            //当没有复习数据时，要配置的参数************************************************************
            lastRs = null;
            state = 2;
            etInput.setShowSoftInputOnFocus(true);
            tvLevel.setText("☺");
            tvLastDuration.setText("---");
            tvLastDuration.setOnClickListener(null);
            textViewArrival.setText("**:**");

            etInput.setHint("");
            etInput.setEnabled(true);
            etInput.setInputType(InputType.TYPE_CLASS_TEXT);

            //显示界面，显示文字“当前没有复习计划”
            mainContainer.removeAllViews();
            TextView textView = new TextView(this);
            textView.setHint("当前没有复习计划");
            textView.setGravity(Gravity.CENTER);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            textView.setLayoutParams(lp);
            mainContainer.addView(textView);

            if (state == 1 && !data.isEmpty()) data.save();
            if (keyboard != null) keyboard.clearKeyboard();
        }
    }

    private Keyboard.OnKeyDownListener getOnKeyDownListener() {
        return new Keyboard.OnKeyDownListener() {
            @Override
            public void onKeyDown(KeyText keyText) {
                if (keyText.isCom &&
                    keyText.text.equals(Keyboard.COM_DONE)) {
                    matchInput();
                }
            }
        };
    }

    //刷新进度信息
    private void refreshProgress() {
        int activateSize   = data.mActivate.size();
        int InactivateSize = data.mInactivate.size();
        progressBarProgress.setProgress(activateSize);
        progressBarProgress.setMax(activateSize + InactivateSize);

        textViewPercent.setText(String.format(Locale.CHINA, "%d : %d", activateSize, InactivateSize));
        tvReviewedNum.setText(String.valueOf(mReviewedNum));

    }


    /**
     * Feature Log
     * <p>
     * 2019年4月12日 MainActivity主界面更改、增加新功能<p>
     * 2019年4月13日 ListActivity创建与完善、EditActive&activity_edit内容创建与完善、activity_list、activity_item完善、保存数据<p>
     * 2019年4月14日 EditActive&activity_edit内容创建与完善、activity_list、activity_item完善、保存数据<p>
     * 2019年4月15日 调用讯飞语音.jar来生成语音实现发音功能、完善EditActive&activity_edit内容、创建AboutActivity&Activity_about和其他<p>
     * 2019年4月16日 百度语音.jar来生成语音实现发音功能（未能发音成功）、完善Speech类、其它小更改<p>
     * 2019年4月17日 懒得写记录...<p>
     * 2019年4月18日 懒得写记录...<p>
     * 2019年4月19日 懒得写记录...<p>
     * 2019年4月20日 找了一天关于Android Studio问题的解决办法，晚上才终于可以正常开发<p>
     * 2019年4月21日 懒得写记录...<p>
     * 2019年4月22日 懒得写记录...<p>
     * 2019年4月23日 懒得写记录...<p>
     * 2019年4月24日 增加notification、一些小修改、<p>
     * 2019年4月25日 懒得写记录...<p>
     * 2019年4月26日 懒得写记录...<p>
     * 2019年4月27日 懒得写记录...<p>
     * 2019年4月28日 半天<p>
     * 2019年4月29日 半天<p>
     * 2019年4月30日 半天<p>
     * 2019年5月01日 半天<p>
     * 2019年5月02日 半天<p>
     * 2019年5月03日 半天<p>
     * 2019年5月04日 半天，自己的键盘布局，和其他<p>
     * 2019年5月05日 半天，在库中挑选内容到，编辑或者添加复习内容的界面中，自己的键盘布局的一些修改<p>
     * 2019年5月06日 半天，增加了复习中&待复习中的复习中列表界面，可以用蓝牙键盘输入<p>
     * 2019年5月07日 半天<p>
     * 2019年5月08日 半天，单词解释的复习功能、其他<p>
     * 2019年5月09日 半天，一些界面的小更改、其他...<p>
     * 2019年5月10日 半天<p>
     * 2019年5月11日 半天，自己的键盘，的一些修改，提取类，方便以后开发<p>
     * 2019年5月12日 图片选择对话框、键盘优化、显示图片<p>
     * 2019年5月13日 播放显示内容的声音、<p>
     * 2019年5月16日 增加播放语音库功能、百度语音tts<p>
     * 2019年5月17日 半天<p>
     * 2019年5月18日 半天<p>
     * 2019年5月25日 半天，一些小修改、修复bug<p>
     * 2019年6月11日 半天，简单的大纲视图、修复一些bug<p>
     * 2019年6月1*日 半天，选择库内容，比如单词库
     * 2019年6月23日 2hour 单词未发音，点击文字显示单词文字。
     */


    //主窗口被创建-----------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //界面数据保存 SharedPreferences
        Setting.init(this);

        String prefix = "";
        pathBoth = getPathBoth();

        if (pathBoth != null && !pathBoth.isEmpty()) {
            String libName = Setting.sp.getString("libName", "");

            for (int i = 0; i < pathBoth.size(); i++) {
                PathBoth pathBoth = this.pathBoth.get(i);
                if (pathBoth.prefix.equals(libName)) libIndex = i;
            }

            PathBoth pathBoth = this.pathBoth.get(libIndex);
            pathNexus = new File(pathApp, pathBoth.nexus);
            pathLibrary = new File(pathApp, pathBoth.library);
            prefix = pathBoth.prefix;
        }

        //启动&绑定服务
        //***********************************************************************************************
        Intent intentService = new Intent(this, ReviewService.class);
        startService(intentService);
        bindService(intentService, this, BIND_AUTO_CREATE);


        //各种初始化
        initViews();
        initListener();
        initVariable();

        tvTitle.setText(prefix);

    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        ReviewService.LocalBinder binder = (ReviewService.LocalBinder) iBinder;
        service = binder.getService();
        data = service.data;

        dataPrepared();
    }

    void dataPrepared() {
        data.setOnAvalableUpdate(new ReviewData.AvalableUpdate() {
            @Override
            public void onUpdateToAvailableComplete(int count_) {
                refreshShowing(false);
            }

            @Override
            public void onUpdatingToAvailable(ReviewStruct dtUnion) {
                if (SortActivity.fragment != null) {
                    SortActivity.fragment.adapter.notifyItemRemoved(0);
                    if (!SortActivity.fragment.mData.isEmpty()) {
                        ReviewStruct rs = SortActivity.fragment.mData.get(0);
                        rs.showed = true;
                    }
                    SortActivity.fragment.adapter.notifyItemChanged(0);
                }
            }

            @Override
            public void onUpdatedNoChange() {
                if (SortActivity.fragment != null &&
                    SortActivity.fragment.recyclerView != null) {

                    int scrollState = SortActivity.fragment.recyclerView.getScrollState();
                    int xPosi       = SortActivity.xPosi;
                    //不上下、左右滚动后，可以通知数据改变
                    if (scrollState == 0 && xPosi == 0)
                        SortActivity.fragment.adapter.notifyDataSetChanged();
                }
                refreshArrivalTime();
                refreshProgress();
            }
        });

        data.setOnSave(new ReviewData.StateSave() {
            @Override
            public void onSaveCalled() {
                tips.setText("保存数据中...");
            }

            @Override
            public void onSaveComplete() {
                tips.setText("");
            }
        });

        refreshArrivalTime();
        refreshProgress();
        refreshShowing(false);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    //初始化Views
    private void initViews() {
        textViewTime = findViewById(R.id.fragment_textView_time);
        textViewAbout = findViewById(R.id.main_textView_about);
        textViewPercent = findViewById(R.id.fragment_textView_persent);
        textViewArrival = findViewById(R.id.main_textView_time_arrival);

        imageViewDetail = findViewById(R.id.main_imageView_Detail);
        imageViewSort = findViewById(R.id.main_imageView_sort);
        imageViewPlaySound = findViewById(R.id.main_imageView_play_sound);

        progressBarProgress = findViewById(R.id.fragment_progressBar_progress);
        etInput = findViewById(R.id.fragment_editText_input);
        tips = findViewById(R.id.fragment_textView_tips);
        lastText = findViewById(R.id.fragment_textView_lastText);
        imageButtonSetting = findViewById(R.id.main_imageButton_setting);
        imageButtonClear = findViewById(R.id.main_imageButton_clear);
        recyclerViewKeyboard = findViewById(R.id.main_recycllerView_keyboard);
        tvLevel = findViewById(R.id.main_textView_level);
        entireBackground = findViewById(R.id.entire_background);
        tvTitle = findViewById(R.id.main_about_textView_title);
        tvNext = findViewById(R.id.main_textView_next);
        tvReviewedNum = findViewById(R.id.main_textView_reviewedNum);

        mainContainer = findViewById(R.id.cl_main_container);
        tvLastDuration = findViewById(R.id.tvLastDuration);
    }

    //初始化监听器-----------------------------------------------------------------------------------
    private void initListener() {
        //显示框背景
        textViewArrival.setOnClickListener(this);
        //输入框
        etInput.addTextChangedListener(this);
        //分类列表按钮
        imageViewSort.setOnClickListener(this);
        //关于按钮
        textViewAbout.setOnClickListener(this);
        //播放音频按钮
        imageViewPlaySound.setOnClickListener(this);
        //跳转页面到ActivityList
        imageViewDetail.setOnClickListener(this);
        //待复习进度条被单击
        progressBarProgress.setOnClickListener(this);

        imageButtonSetting.setOnClickListener(this);

        etInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
        etInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                //回车键按下以及IME_ACTION_DONE被按下的，都可以执行matchInput()
                switch (actionId) {
                    case EditorInfo.IME_ACTION_UNSPECIFIED:
                        //回车键被按下和放开，都会触发此事件。现在要排除回车键被放开的事件
                        if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP)
                            return false;
                    case EditorInfo.IME_ACTION_DONE:
                        matchInput();
                        break;
                }
                return true;
            }
        });

        imageButtonClear.setOnClickListener(this);
        tvNext.setOnClickListener(this);
        tips.setOnClickListener(this);
        tips.setOnLongClickListener(showTipsWindowListener());
//        etInput.setOnClickListener(this);

        //长按显示上一个复习条目
        tvNext.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                if (!data.mActivate.isEmpty()) {
                    ReviewStruct last = data.mActivate.removeLast();
                    data.mActivate.addFirst(last);

                    refreshShowing(false);
                }

                return true;
            }
        });
    }

    private View.OnLongClickListener showTipsWindowListener() {
        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!data.mActivate.isEmpty()) {
                    ReviewStruct rs       = data.mActivate.getFirst();
                    Point        size     = new Point();
                    View         inflate  = View.inflate(MainActivity.this, R.layout.activity_popup_window, null);
                    TextView     showText = inflate.findViewById(R.id.popupWindow_textView_txt);

                    getWindowManager().getDefaultDisplay().getSize(size);
                    PopupWindow popupWindow = new PopupWindow(inflate, -2, -2);

                    showText.setText(getTips(rs));
                    popupWindow.setOutsideTouchable(true);
                    popupWindow.showAtLocation(MainActivity.this.entireBackground, Gravity.CENTER, 0, 0);

                    rs.resetLevel();
                    if (rs.match.getType() == 1) Speech.play_Baidu(rs.getMatch());

                    correct = false;
                    canJoinLog++;
                    addLog(rs);
                    refreshShowing(false);
                }
                return true;
            }
        };
    }

    //初始化变量-------------------------------------------------------------------------------------
    void initVariable() {
        Speech.initVoice(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Speech.initVoice_Baidu(MainActivity.this);
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unbindService(this);
        Speech.release_Baidu();
    }

    boolean isShowedScreen = false;

    @Override
    protected void onStart() {
        super.onStart();
        mReviewedNum = 0;
        isShowedScreen = true;
        refreshProgress();

        //如果打开过编辑窗口，则要刷新显示界面数据
        if (ListActivity.currentClickedRs != null) {
            ListActivity.currentClickedRs = null;
            refreshShowing(true);
        } else refreshShowing(false);


        //延迟刷新界面，不延迟则界面刷新不了
        if (keyboard != null) {
            new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(@NonNull Message message) {
                    keyboard.refresh();
                    return false;
                }
            }).sendMessageDelayed(new Message(), 20);
        }

        NotificationManager notifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notifyManager.cancel(1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isShowedScreen = false;
        data.save();
        if (keyboard != null)
            keyboard.stop();
    }

    class PathBoth {
        private PathBoth(String nexus, String library, String prefix) {
            this.nexus = nexus;
            this.library = library;
            this.prefix = prefix;
        }

        String nexus;
        String library;
        String prefix;
    }

    //textShow被单击后，调用的函数--------------------------------------------------------------------
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.main_imageView_sort:
                pickDataDialog();
                break;
            case R.id.main_textView_next:
                if (!data.mActivate.isEmpty()) {
                    ReviewStruct first = data.mActivate.removeFirst();
                    data.mActivate.addLast(first);

                    refreshShowing(false);
                }
                break;
            case R.id.main_textView_about:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                break;
            case R.id.main_imageView_play_sound:
                Speech.play_Baidu(etInput.getText().toString(), imageViewPlaySound);
                break;
            case R.id.main_imageView_Detail:
                startActivity(new Intent(MainActivity.this, ListActivity.class));
                break;
            case R.id.fragment_progressBar_progress:
                startActivity(new Intent(this, SortActivity.class));
                break;
            case R.id.main_imageButton_setting:
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
                break;
            case R.id.main_imageButton_clear:
                etInput.setText("");
                break;
            case R.id.fragment_textView_tips:
                if (!data.mActivate.isEmpty()) {
                    ReviewStruct rs = data.mActivate.getFirst();

                    String sb = getTips(rs);
                    Toast.makeText(this, sb, Toast.LENGTH_LONG).show();

                    rs.resetLevel();
                    if (rs.match.getType() == 1) {
                        Speech.play_Baidu(rs.getMatch());
                    }

                    correct = false;
                    canJoinLog++;
                    addLog(rs);
                    refreshShowing(false);
                }
                break;
            case R.id.main_textView_time_arrival:
                Intent intent = new Intent(this, SortActivity.class);
                intent.putExtra("posi", 2);
                startActivity(intent);
                break;
            case R.id.fragment_textView_textShow:
                break;
            case R.id.fragment_editText_input:
                break;
        }

    }

    private String getTips(ReviewStruct rs) {
        StringBuilder sb = new StringBuilder();

        if (keyboard instanceof KeyboardType2) {
            KeyboardType2 kt = (KeyboardType2) keyboard;

            for (WordExplain we : kt.frameRight) {
                String explains = we.toString();
                sb.append(explains + "\n");
            }
        } else sb.append(rs.getMatch());

        int  index = sb.length() - 1;
        char c     = sb.charAt(index);
        if (c == '\n') sb = sb.replace(index, index + 1, "");

        return sb.toString();
    }

    private void pickDataDialog() {
        pathBoth = getPathBoth();
        final List<String> names = new LinkedList<>();
        for (PathBoth pathBoth : pathBoth) names.add(pathBoth.prefix);

        String[] strings = new String[names.size()];
        for (int i = 0; i < strings.length; i++) strings[i] = names.get(i);

        new AlertDialog.Builder(this)
                .setTitle("选择：")
                .setItems(strings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        libIndex = i;

                        PathBoth pathBoth = MainActivity.this.pathBoth.get(i);
                        pathNexus = new File(pathApp, pathBoth.nexus);
                        pathLibrary = new File(pathApp, pathBoth.library);
                        Setting.edit.putString("libName", MainActivity.this.pathBoth.get(libIndex).prefix).commit();
                        tvTitle.setText(pathBoth.prefix);

                        service.initData();
                        refreshShowing(true);
                    }
                })
                .show();
    }

    List<PathBoth> getPathBoth() {
        String[] list = pathApp.list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String str) {
                return str.endsWith("nexus") || str.endsWith("library");
            }
        });
        if (list == null) return null;

        final List<PathBoth> paths = new LinkedList<>();
        List<String>         strs  = new LinkedList<>(Arrays.asList(list));

        for (int i = 0; i < strs.size(); i++) {
            String   str  = strs.get(i);
            String[] name = str.split("\\.");
            if (name.length != 2) break;

            strs.remove(i);
            i--;

            if (str.endsWith("nexus")) {
                String s = name[0] + ".library";
                if (strs.contains(s)) {
                    strs.remove(s);
                    paths.add(new PathBoth(str, s, name[0]));
                } else {
                    break;
                }

            } else if (str.endsWith("library")) {

                String s = name[0] + ".nexus";
                if (strs.contains(s)) {
                    strs.remove(s);
                    paths.add(new PathBoth(s, str, name[0]));
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        return paths;
    }

    //todo below is detail ↓↓↓

    /* new设想，2019年7月18日
     *
     *      word: compare,you,@0123(引用该单词的ID，4字节)
     *   explain: vi.比较;vt.比拟、誉为；
     *   picture: storage/emulate/0/Image/what the fuck?.jpg
     *     sound: storage/emulate/0/sound/what the.mp3
     *     frameInput: @meat,@ant
     * candidate: @_auto_,@meat(交集、差集),you,me,him,单击,大家好,shit
     *  previous: @118(引用该单词的ID，4字节),@112(previous引用的暂时都记住了，才考虑复习这条单词)
     *        id: 666(该id由系统自动分配，4字节)
     *      type: 单词、解释、填空
     *      hello world!
     *
     * */

    //输入匹配---------------------------------------------------------------------------------------
    void matchInput() {
        String inputText = etInput.getText().toString();

        //避开下标越界
        if (data.mActivate.isEmpty()) {
//            tvShow.setHint("暂时没有复习的");
            return;
        }

        etInput.setText(inputText);//清楚文字残留颜色
        ReviewStruct rs   = data.mActivate.getFirst();
        CountList    cl   = new CountList();
        int          type = rs.match.getType();

        switch (type) {
            case 1: {
                //如果输入为空，就显示listen...
                if (inputText.isEmpty()) {
                    tips.setText("");
                    etInput.setHint("听发音...");
//                    Speech.play_Baidu(rs.getMatch());
                    tips.callOnClick();
                    return;
                }

                correct = rs.matching(inputText);
                rs.viewCount++;
                correctProc(inputText, rs);
                break;
            }
            case 2: {
                KeyboardType2 keyboardType2 = (KeyboardType2) keyboard;
                correct = rs.matching(keyboardType2.frameInput, cl);
                if (correct) {
                    matchCorrect(rs);
                    mReviewedNum++;

                    //下面监听器，等颜色动画播放完毕，然后显示下一条数据在textShow中
                    final int duration = 200;
                    keyboardType2.handleInterface.setLightAnimation(false, duration);

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            handler.sendEmptyMessage(HANDLER_UPDATE_SHOWING);
                        }
                    }, duration);
                } else {
                    matchError(cl);
                    tips.callOnClick();
                }
                break;
            }
            case 3: {
                KeyboardType3 keyboardType3 = (KeyboardType3) keyboard;
                correct = rs.matchingType3(keyboardType3.mCandidateType, keyboardType3.mCandidate, cl);
                if (correct) {
                    matchCorrect(rs);
                    mReviewedNum++;


                    //下面监听器，等颜色动画播放完毕，然后显示下一条数据在textShow中
                    final int duration = 200;
                    keyboardType3.setLightAnimation(false, duration);

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            handler.sendEmptyMessage(HANDLER_UPDATE_SHOWING);
                        }
                    }, duration);
                } else {
                    matchError(cl);
                    tips.callOnClick();
                }
                break;
            }
            case 4: {
                break;
            }
        }


        addLog(rs);
    }

    private void matchError(CountList cl) {
        canJoinLog++;

//        rs.resetLevel();//重置水平
//        refreshShowing(false);
        KeyboardType2 keyboardType2 = (KeyboardType2) keyboard;
        keyboardType2.handleInterface.showDifferent(true);

        //显示错误提示
        SpanUtil.create()
                .addForeColorSection("完成", Color.GRAY)
                .addForeColorSection(cl.corrCount + "/" + cl.totalNum, Color.BLACK)
                .addForeColorSection("个(" + cl.needCorrNum + "/" + cl.totalNum + ")  错误", Color.GRAY)
                .addForeColorSection(cl.errCount + "", Color.BLACK)
                .addForeColorSection("个", Color.GRAY)
                .showIn(tips);
    }

    private void matchCorrect(ReviewStruct rs) {
        canJoinLog = 0;
        data.updateInavalable_AddLevel(rs);
    }


    @TargetApi(Build.VERSION_CODES.N)
    private void correctProc(String inputText, ReviewStruct rs) {
        //***正确************************************************************************************
        if (correct) {
            data.updateInavalable_AddLevel(rs);
            canJoinLog = 0;

            //下面监听器，等颜色动画播放完毕，然后显示下一条数据在textShow中
            KeyboardType1 keyboardType1 = (KeyboardType1) keyboard;
            int           duration      = 200;
            keyboardType1.handleInterfaceType1.setLightAnimation(false, duration);

            //渐变显示绿色动画，表示输入正确
            TextColorAnimator.ofArgb(etInput, Color.BLACK, Color.GREEN, Color.TRANSPARENT).setDuration(duration).start();

            //延时后，再进入下一条复习计划
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.sendEmptyMessage(HANDLER_UPDATE_SHOWING);
                }
            }, duration);

            mReviewedNum++;

        } else {
            //***错误********************************************************************************
            canJoinLog++;
            rs.resetLevel();//重置水平

            ColorfulText               ct  = new ColorfulText();
            ArrayList<ElementCategory> ecs = ct.categoryString(inputText, rs.getMatch());//todo

            tips.setText(Html.fromHtml(ct.txt, 1));//显示缺少的字符
            Speech.play_Baidu(rs.getMatch());//播放单词发音

            //输入错误后，指出错误类型
            SpanUtil.SpanBuilder spanBuilder = SpanUtil.create();
            for (ElementCategory ec : ecs) {

                switch (ec.category) {
                    case correct:
                        spanBuilder.addForeColorSection(ec.txt, Color.BLACK);
                        break;
                    case malposition:
                        spanBuilder.addUnderlineSection(ec.txt);
                        break;
                    case unnecesary:
                        spanBuilder.addStrickoutSection(ec.txt).setForeColor(ec.txt, 0xffc3c3c3);
                        break;
                    case missing:
//                        spanBuilder.addForeColorSection(ec.txt, Color.YELLOW);
                        break;
                }
            }
            spanBuilder.showIn(etInput);
//            etInput.setSelection(etInput.length());//全选输入框
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        char ch     = (char) event.getUnicodeChar();
        int  action = event.getAction();

        //键盘按键被按下
        if (action == KeyEvent.ACTION_DOWN) {
            int keyCode = event.getKeyCode();

            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    break;
                case KeyEvent.KEYCODE_FORWARD_DEL:
                    etInput.setText("");
                    break;
                case KeyEvent.KEYCODE_DEL:

                    break;
                case KeyEvent.KEYCODE_S:
                    //显示代替字符
                    if (event.isAltPressed()) {
                        MyAdapter.isShowNum = !MyAdapter.isShowNum;
                        keyboard.adapter.notifyDataSetChanged();
                        return super.dispatchKeyEvent(event);
                    }
            }

            if (MyAdapter.isShowNum) {
                boolean b = keyboard.keyDown(keyCode, ch, -1);
                if (b) return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void addLog(ReviewStruct rs) {
        //去除多次同样的重复记录
        if (canJoinLog > 1) return;

        //添加log记录
        DateTime dateTime = DateTime.getCurrentTime();
        if (!correct) {
            int second = dateTime.getSecond();
            dateTime.setSecond(-second);
        }
        rs.logs.add(dateTime.toBytes());
    }

    int start_ = 0;

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        start_ = start;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    //用于不重复
    int state1 = 1;

    @Override
    public void afterTextChanged(Editable s) {
        etInput.setTextColor(Color.BLACK);
        String text = etInput.getText().toString();

        if (text.equals("")) {
            if (state1 == 2) return;
            state1 = 2;
            imageButtonClear.setVisibility(View.INVISIBLE);
            SpannableStringBuilder ssb = new SpannableStringBuilder("上个单词: " + lastText.getText().toString());

            ssb.setSpan(new AbsoluteSizeSpan(28),
                        0, 6,
                        Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            ssb.setSpan(new ForegroundColorSpan(Color.GRAY),
                        0, 6,
                        Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            lastText.setText(ssb);

        } else {
            lastText.setText(text);
            lastText.setTextColor(Color.BLACK);
            imageButtonClear.setVisibility(View.VISIBLE);
            state1 = 1;
        }
    }
}