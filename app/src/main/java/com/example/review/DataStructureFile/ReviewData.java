package com.example.review.DataStructureFile;

import android.os.Handler;
import android.os.Message;

import com.example.review.MainActivity;
import com.example.review.New.LibrarySet;
import com.example.review.New.LibraryStruct;
import com.example.review.New.ReviewSet;
import com.example.review.New.ReviewStruct;
import com.example.review.Util.Speech;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

public class ReviewData extends ReviewSet {
    private File fileLibrary;
    private File fileNexus;

    public LinkedList<ReviewStruct> mInactivate = new LinkedList<>();
    public LinkedList<ReviewStruct> mActivate   = new LinkedList<>();

    static public DateTime[] reviewRegions = new DateTime[]{
            new DateTime("0秒"),  // 0级
            new DateTime("2秒"),  // 1级，插入mActivate第三个位置-----------
            new DateTime("10秒"), // 2级
            new DateTime("5分"),  // 3级
            new DateTime("15分"), // 4级
            new DateTime("3时"),  // 5级
            new DateTime("12时"), // 6级
            new DateTime("1日"),  // 7级
            new DateTime("3日"),  // 8级
            new DateTime("5日"),  // 9级
            new DateTime("10日"), //10级
            new DateTime("1月"),  //11级
            new DateTime("3月"),  //12级
            new DateTime("7月"),  //13级
            new DateTime("1年"),  //14级
            new DateTime("3年")}; //15级

    private Timer timer = null;

    final private int        UPDATE_TO_AVALABLE = 0;
    final private int        DATA_SAVE          = 1;
    final private int        DATA_SAVE_COMPLETE = 2;
    private       LibrarySet library            = new LibrarySet();

    public ReviewData() {
    }

    public ReviewData(File path) {
        loadDataOf(path, library, this);
    }


    private android.os.Handler handler = new android.os.Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TO_AVALABLE:
                    updateToAvalable(DateTime.getCurrentTime());
                    break;
                case DATA_SAVE:
                    if (stateSave != null) stateSave.onSaveCalled();
                    break;
                case DATA_SAVE_COMPLETE:
                    if (stateSave != null) stateSave.onSaveComplete();
                    break;
            }
            return false;
        }
    });

    public void setDefaultPath(File fileLibrary, File fileNexus) {
        this.fileLibrary = fileLibrary;
        this.fileNexus = fileNexus;
    }

    /**
     * 开启一条线程，保存数据
     */
    public void save() {
        if (dataChangeCount == 0) return;
        dataChangeCount = 0;

        new Thread(new Runnable() {
            @Override
            public void run() {
                saveDataTo(fileNexus);
            }
        }).start();
    }

    public void setOnSave(StateSave stateSave) {
        this.stateSave = stateSave;
    }

    private StateSave stateSave;

    public interface StateSave {
        void onSaveCalled();

        void onSaveComplete();
    }

    public boolean read() {
        if (fileNexus == null || fileLibrary == null) return false;
        readDataFrom(fileNexus);
        library.readOf(fileLibrary);

        int        sizeNexus     = size() * 2;
        int        sizeLibrary   = library.size();
        ReviewData reviewStructs = this;

        if (sizeNexus != sizeLibrary) {
            throw new IllegalArgumentException("数据不一致 sizeNexus*2 = " + sizeNexus + " sizeLibrary = " + sizeLibrary);
        }

        connectOf(library);
        return true;
    }

    /**
     * 保存数据，到外部存储器上。
     *
     * @param path 文件路径
     */
    public void saveDataTo(File path) {
        handler.sendEmptyMessage(DATA_SAVE);
        super.save(path);
        handler.sendEmptyMessage(DATA_SAVE_COMPLETE);
    }

    /**
     * 从外部存储器上，读取数据到内部。
     *
     * @param path 文件路径
     */
    public void readDataFrom(File path) {
        clear();//清空原有数据
        super.readOf(path);
    }

    public void loadDataOf(File path) {
        library.clear();
        loadDataOf(path, library, this);

        int a = 0;
        for (int i = 0; i < this.size(); i++) {

            int           index = i * 2;
            LibraryStruct match = new LibraryStruct(library.get(index));
            LibraryStruct show  = new LibraryStruct(library.get(++index));
            ReviewStruct  rs    = new ReviewStruct();

            library.add(++index, match);
            library.add(index, show);

            rs.match = show;
            rs.show = match;
            add(++i, rs);
        }

        connectOf(library);
        library.save(fileLibrary);
        save();
    }

    public void setProgress(ProgressListener progressListener) {
        this.progressListener = progressListener;

    }

    public interface ProgressListener {
        void onProgress(int total, int posi);
    }

    ProgressListener progressListener;

    /**
     * 转换到本土数据
     *
     * @param path 需要转换为本土数据的文件路径
     * @return LinkedList 转换好的本土数据
     */
    public boolean loadDataOf(File path, LibrarySet librarySet, ReviewSet reviewSet) {
        clear();//清空原有数据

        try {
            FileInputStream fis   = new FileInputStream(path);
            byte[]          bytes = new byte[(int) path.length()];

            fis.read(bytes);//读入全部内容
            fis.close();
            String strRaw = new String(bytes, "GBK");
            strRaw = Pattern.compile("\r").matcher(strRaw).replaceAll("");

            StringBuilder sb1 = new StringBuilder(strRaw);
            sb1.delete(0, sb1.indexOf("@") + 1);

            String[] split = Pattern.compile("@").split(sb1);//先以'@'作为分格符号，分组内容
            int      posi  = 0;

            for (String str : split) {

                String[] part       = Pattern.compile("#").split(str);//后以'#'作为分格符号，分组内容
                String   strWord    = part[0].trim();
                String   strExplain = part[1].trim();

                strExplain = Pattern.compile("[;,，]").matcher(strExplain).replaceAll("；");

                librarySet.add(new LibraryStruct(strWord, 1));
                librarySet.add(new LibraryStruct(strExplain, 2));

                Integer      level        = Integer.valueOf(part[5].trim());
                ReviewStruct reviewStruct = new ReviewStruct(level);


                String[] strLog = Pattern.compile("\\r\\n|\\n").split(part[2]);
                for (String txt : strLog) {
                    txt = txt.trim();
                    if (txt.length() < 25) continue;

                    DateTime dt = toDateTime(txt);
                    reviewStruct.logs.add(dt.getBytes());
                }

                reviewSet.add(reviewStruct);

                if (progressListener != null)
                    progressListener.onProgress(split.length, ++posi);
            }

        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public DateTime toDateTime(String strLog) {
        String[] split = Pattern.compile("\\s").split(strLog);


        String text = split[0];
        int    i    = text.indexOf(".");
        text = text.substring(i + 1);

        String[] split1 = Pattern.compile("/").split(text);


        String[] split2 = null;
        try {
            split2 = Pattern.compile(":").split(split[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }


        DateTime time = null;
        try {
            time = new DateTime(
                    Integer.valueOf(split1[0]),
                    Integer.valueOf(split1[1]),
                    Integer.valueOf(split1[2]),
                    Integer.valueOf(split2[0]),
                    Integer.valueOf(split2[1]),
                    Integer.valueOf(split2[2]));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        boolean state  = !split[2].contains("F");
        int     second = time.getSecond();
        if (!state) time.setSecond(-second);

        return time;
    }

    /**
     * 从总数据中取要复习的数据
     */
    public void retrieveInavalable() {
        mInactivate.clear();
        mActivate.clear();
        DateTime dateTime = new DateTime();

        for (ReviewStruct rs : this) {
            //分离出不符合条件的数据
            if (!rs.joined) continue;

            //导入以前的复习时间
            boolean isBigger = rs.time.biggerThan(dateTime);
            if (!isBigger) {
                try {
                    rs.time = new DateTime(rs.logs.getLast());
                } catch (Exception e) {
                    e.printStackTrace();
                    //没有log数据
                }
            }
            //从小到大的时间排序
            sortAddToInactivate(rs);
        }
    }

    boolean sortToAvalable(ReviewStruct reviewStruct) {
        if (!mActivate.isEmpty()) {
            int matchType = reviewStruct.match.getType();

            for (int i = mActivate.size() - 1; i >= 0; i--) {
                int type = mActivate.get(i).match.getType();
                if (matchType == type) {
                    mActivate.add(i + 1, reviewStruct);
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * mInactivate更新到mAvalable中
     */
    public void updateToAvalable(DateTime currTime) {
        int count = 0;

        while (!mInactivate.isEmpty()) {
            ReviewStruct rs = mInactivate.getFirst();

            if (currTime.biggerThan(rs.time)) {
                if (!sortToAvalable(rs))
                    mActivate.add(rs);
                mInactivate.removeFirst();
                if (avalableUpdate != null)
                    avalableUpdate.onUpdatingToAvalable(rs);

                count++;
            } else break;
        }
        if (count > 0) {
            if (avalableUpdate != null) avalableUpdate.onUpdateToAvalableComplete(count);
            if (avalableComplete != null) avalableComplete.onAvalablecomplete();

        } else {
            if (avalableUpdate != null)
                avalableUpdate.onUpdatedNoChange();
        }

    }

    AvalableComplete avalableComplete;

    public void setOnAvalableComplete(AvalableComplete avalablecomplete) {
        this.avalableComplete = avalablecomplete;
    }

    public interface AvalableComplete {
        void onAvalablecomplete();
    }


    public void setOnAvalableUpdate(AvalableUpdate avalableUpdate) {
        this.avalableUpdate = avalableUpdate;
    }

    AvalableUpdate avalableUpdate;

    public interface AvalableUpdate {

        void onUpdateToAvalableComplete(int count);

        void onUpdatingToAvalable(ReviewStruct reviewStruct);

        void onUpdatedNoChange();
    }

    /**
     * 自动动更新到mAvalable中
     */
    public void updateToAvalableAuto(int period) {
        if (timer != null) timer.cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(UPDATE_TO_AVALABLE);
            }
        }, 0, period);
    }

    public void stopTimer() {
        if (timer != null) timer.cancel();
    }

    int dataChangeCount = 0;

    /**
     * mAvalable更新到mInactivate中
     */
    public void updateInavalable_AddLevel(ReviewStruct rs) {
        dataChangeCount++;

        mActivate.remove(rs);
        int level = rs.getLevel();
        if (level < 0) level = 0;
        int tempLevel = level;


        rs.time = DateTime.getCurrentTime();//取当前时间
        level++;//增加水平
        rs.time.add(reviewRegions[level]);
        rs.setLevel(level);


        //水平为0时，加入到第三个复习位置。
        // 因为，新单词不太熟悉，就多加一步复习步骤，来巩固
        if (tempLevel == 0) {
            try {
                mActivate.add(3, rs);
            } catch (Exception e) {
                mActivate.addLast(rs);
            }
        } else {
            //正常复习
            try {
                sortAddToInactivate(rs);
            } catch (IllegalArgumentException e) {//todo 如果水平为13，则复习完成，取消复习计划。
                removeFromInavalable_Avalable(rs);
            }
        }

    }


    /**
     * 排序加入到mInactivate中
     */
    public void sortAddToInactivate(ReviewStruct rsNew) {
        if (mActivate.contains(rsNew)) return;
        mInactivate.remove(rsNew);//删除旧数据

        for (int i = 0; i < mInactivate.size(); i++) {
            ReviewStruct rs = mInactivate.get(i);

            if (rs.time.biggerThan(rsNew.time)) {
                mInactivate.add(i, rsNew);
                return;
            }
        }

        //添加至末尾
        mInactivate.add(rsNew);
    }

    public void removeFromInavalable_Avalable(ReviewStruct rs) {
        rs.joined = false;
        mInactivate.remove(rs);
        mActivate.remove(rs);
    }

    public void saveLibrary() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                library.save(fileLibrary);
            }
        }).start();
    }

    public void addLibrary(int posi, LibraryStruct match, LibraryStruct show) {
        library.add(posi, show);
        library.add(posi, match);
        dataChangeCount++;
    }

    public void removeLibraryItem(int posi) {
        library.remove(posi);
        library.remove(posi);
//        remove(posi);
        dataChangeCount++;
    }

    public void removeLibraryItem(ReviewStruct rs) {
        int count = 0;
        for (int i = 0; i < library.size(); i++) {
            LibraryStruct ls = library.get(i);

            if (ls == rs.match || ls == rs.show) {
                library.remove(i);
                i--;
                count++;
            }
        }
        if (count != 2) {
            throw new IllegalStateException();
        }

//        library.remove(rs.match);
//        library.remove(rs.show);
        dataChangeCount++;
    }


    public LibrarySet getLibraries() {
        return library;
    }
}
