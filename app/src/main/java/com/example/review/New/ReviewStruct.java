package com.example.review.New;

import android.support.annotation.NonNull;

import com.example.review.DataStructureFile.DateTime;
import com.example.review.DataStructureFile.WordExplain;
import com.example.review.Keyboard.KeyboardType3;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReviewStruct extends StoreData {
    public LibraryStruct show;
    public LibraryStruct match;

    int previousID = 0;
    int classType  = 0;
    int level;

    public int      posi;
    public int      viewCount;
    public boolean  joined;
    public boolean  selected;
    public boolean  showed;
    public DateTime time = new DateTime(0, 0, 0, 0, 0, 0);

    public LinkedList<byte[]> logs = new LinkedList<>();

    public ReviewStruct() {
    }

    public ReviewStruct(ReviewStruct rs) {
        this.show       = rs.show;
        this.showed     = rs.showed;
        this.time       = rs.time;
        this.match      = rs.match;
        this.joined     = rs.joined;
        this.logs       = rs.logs;
        this.posi       = rs.posi;
        this.level      = rs.level;
        this.classType  = rs.classType;
        this.previousID = rs.previousID;
    }

    public ReviewStruct(int level) {
        this.level = level;
    }

    public ReviewStruct(byte[] rawBytes) {
        loadWith(rawBytes);
    }

    public String getShow() {
        return show.getText();
    }

    public void setShow(LibraryStruct show) {
        this.show = show;
    }

    public void setShow(String text) {
        this.show.setText(text);
    }

    public String getMatch() {
        return match.getText();
    }

    //完成率，到这个阈值后判断为正确
    float corRate = 0.6f;

    public boolean matching(ArrayList<WordExplain> wesInput, CountList countList) {
        ArrayList<WordExplain> wesRight  = getMatchWordExplains();
        int                    corrCount = 0, errCount = 0, total = 0;

        for (int i = 0; i < wesRight.size(); i++) {
            WordExplain weRight = wesRight.get(i);
            WordExplain weInput = null;

            //挑出和matchWE.category相同的条目到we中
            for (WordExplain weTemp : wesInput) {
                String trim = weTemp.category.trim();

                if (weRight.category.equals(trim)) {
                    weInput = new WordExplain(weTemp);
                    break;
                }
            }

            //统计总数
            total += weRight.explains.size();

            assert weInput != null;
            ArrayList<String> wrongInput = new ArrayList<>(weInput.explains);

            for (int k = 0; k < wrongInput.size(); k++) {
                String  word     = wrongInput.get(k);
                boolean contains = weRight.explains.contains(word);

                //移除正确词语，留下不正确的
                if (contains) {
                    wrongInput.remove(k--);
                    corrCount++;
                }
            }

            //累计错误数
            errCount += wrongInput.size();
        }

        int needCorrectNum = (int) (total * corRate);
        if (total <= 3) needCorrectNum = total;

        countList.corrCount   = corrCount;
        countList.errCount    = errCount;
        countList.totalNum    = total;
        countList.corrRate    = corRate;
        countList.needCorrNum = needCorrectNum;

        return errCount == 0 && corrCount >= needCorrectNum;
    }

    public boolean matchingType3(ArrayList<KeyboardType3.TextCom> tc, ArrayList<String> candidate, CountList countList) {
        int corrCount = 0, errCount = 0, total = 0;


        ArrayList<KeyboardType3.TextCom> tcs = new ArrayList<>();
        for (KeyboardType3.TextCom textCom : tc) {
            if (textCom.isCandidate) tcs.add(textCom);
        }

        for (int i = 0; i < tcs.size(); i++) {
            String                txt1    = candidate.get(i);
            KeyboardType3.TextCom textCom = tcs.get(i);
            boolean               equals1 = txt1.equals(textCom.text);

            if (equals1) {
                corrCount++;
            } else {
                errCount++;
                textCom.isStrike = true;
            }
            total++;
        }

        int needCorrectNum = total;

        countList.corrCount   = corrCount;
        countList.errCount    = errCount;
        countList.totalNum    = total;
        countList.corrRate    = 1;
        countList.needCorrNum = needCorrectNum;

        return errCount == 0 && corrCount >= needCorrectNum;
    }

    public boolean matching(String text) {
        return getMatch().equals(text);
    }

    public ArrayList<WordExplain> getFrame() {
        ArrayList<WordExplain> wordExplains = getMatchWordExplains();

        for (WordExplain we : wordExplains) we.explains.clear();
        return wordExplains;
    }

    public ArrayList<WordExplain> getMatchWordExplains() {
        String match = getMatch();
        return getMatchWordExplains(match);
    }

    /**
     * 取字符串text中匹配regex的所有字符
     *
     * @param text  要从中检索的字符串
     * @param regex 要匹配的正则表达式
     * @return 返回所有匹配的字符串
     */
    static public LinkedList<String> toMatchList(String text, String regex) {
        Matcher            mat         = Pattern.compile(regex).matcher(text);
        LinkedList<String> matchedList = new LinkedList<>();

        //寻找匹配regex的字符串
        while (mat.find()) {
            int    start    = mat.start();
            int    end      = mat.end();
            String strMatch = text.substring(start, end);
            matchedList.add(strMatch);
        }
        return matchedList;
    }

    public static ArrayList<WordExplain> getMatchWordExplains(String text) {
        ArrayList<WordExplain> item  = new ArrayList<>();
        String[]               lines = text.split("\n");

        //把字符串以\n拆分
        for (String line : lines) {
            //去掉空字符串
            if (!line.equals("")) {
                WordExplain we    = new WordExplain();
                int         index = line.indexOf('.') + 1;

                //找不到‘.’，分类设置为默认的“*.”
                if (index == -1) we.category = "*.";
                else {
                    we.category = line.substring(0, index);
                    if (we.category.equals("")) we.category = "*.";
                    line        = line.substring(index);
                }

                //把词语分离出来
                String[] words = line.split("[;；，,]");
                for (String word : words) if (!word.equals("")) we.explains.add(word);
                item.add(we);
            }
        }
        return item;
    }

    public void setMatch(String text) {
        match.setText(text);
    }

    public final LibraryStruct getLibraryShow() {
        return show;
    }

    public LibraryStruct getLibraryMatch() {
        return match;
    }

    public void setMatch(LibraryStruct match) {
        this.match = match;
    }

    public void addData(LibraryStruct show, LibraryStruct match) {
        this.show  = show;
        this.match = match;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void resetLevel() {
        setLevel(-1);
    }

    @Override
    public void toBytes(DataOutputStream dos) throws IOException {
        dos.writeInt(previousID);
        dos.writeByte(classType);
        dos.writeByte(level);
//        dos.writeByte(viewCount);

        dos.writeBoolean(joined);
        dos.writeBoolean(selected);
        dos.writeBoolean(showed);

        dos.write(time.toBytes());

        int size = logs.size();
        dos.writeInt(size);
        for (byte[] log : logs) {
            dos.write(log);
        }

        //压缩存储
//        byte[] oldBt = logs.get(0).getBytes();
//        int    posi  = -1;
//        dos.write(oldBt);
//
//        for (int i = 1; i < size; i++) {
//            DateTime log   = logs.get(i);
//            byte[]   bytes = log.getBytes();
//
//            for (int k = 0; k < bytes.length; k++) {
//
//                if (bytes[k] != oldBt[k]) {
//                    posi = k;
//                    break;
//                }
//            }
//
//            int len = bytes.length - posi;
//            dos.writeByte(len);
//            dos.write(bytes, posi, len);
//            oldBt = bytes;
//        }
    }

    @Override
    public void loadWith(DataInputStream dis) throws IOException {
        previousID = dis.readInt();
        classType  = dis.readByte();
        level      = dis.readByte();
//        viewCount = dis.readByte();

        joined   = dis.readBoolean();
        selected = dis.readBoolean();
        showed   = dis.readBoolean();


        byte[] bytes = new byte[7];
        dis.read(bytes);
        time = new DateTime(bytes);


        int size = dis.readInt();
        for (int i = 0; i < size; i++) {
            bytes = new byte[7];
            dis.read(bytes);
            logs.add(bytes);
        }

        //压缩存储
//        byte[] oldBt = null;
//        dis.read(bytes);
//        oldBt = bytes;
//        logs.add(new DateTime(bytes));
//
//        for (int i = 0; i < size; i++) {
//            byte len = dis.readByte();
//
//            byte[] clone = oldBt.clone();
//            dis.read(clone, 7 - len, len);
//            logs.add(new DateTime(clone));
//            oldBt = clone;
//        }

    }

    @NonNull
    @Override
    public String toString() {
        return time.toString();
//        return "单词：" + match.getText() + "  解释：" + show.getText();
    }
}
