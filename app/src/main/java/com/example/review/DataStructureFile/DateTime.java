package com.example.review.DataStructureFile;

import android.support.annotation.NonNull;

import com.example.review.New.StoreData;

import java.io.*;
import java.util.Calendar;
import java.util.Objects;

public class DateTime extends StoreData {
    public static int YEAR   = 0;
    public static int MONTH  = 1;
    public static int DAY    = 2;
    public static int HOUR   = 3;
    public static int MINUTE = 4;
    public static int SECOND = 5;

    private int year;
    private int month;
    private int day;

    private int hour;
    private int minute;
    private int second;

    public static DateTime getCurrentTime() {
        return new DateTime(Calendar.getInstance());
    }

    public DateTime() {
    }

    public DateTime(byte[] rawBytes) {
        loadWith(rawBytes);
    }

    public DateTime(int years, int months, int days, int hours, int minuts, int seconds) {
        this.year = years;
        this.month = months;
        this.day = days;
        this.hour = hours;
        this.minute = minuts;
        this.second = seconds;
    }

    public DateTime(int years, int months, int days) {
        this.year = years;
        this.month = months;
        this.day = days;
    }

    public DateTime(DateTime dateTime) {
        this.year = dateTime.year;
        this.month = dateTime.month;
        this.day = dateTime.day;
        this.hour = dateTime.hour;
        this.minute = dateTime.minute;
        this.second = dateTime.second;
    }

    public DateTime(Calendar calendar) {
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
        second = calendar.get(Calendar.SECOND);
    }

    private int getInt(String src, String objStr) {
        int index1, index2 = src.indexOf(objStr);
        if (index2 == -1) return 0;

        index1 = index2;
        char c;
        do {
            if (index1 == 0) {
                index1 = -1;
                break;
            }
            c = src.charAt(--index1);
        } while (c == 0x2d || c >= 0x30 && c <= 0x39);
        String substring = src.substring(++index1, index2);
        return Integer.valueOf(substring);
    }

    public DateTime(String str) {
        year = getInt(str, "年");
        month = getInt(str, "月");
        day = getInt(str, "日");
        hour = getInt(str, "时");
        minute = getInt(str, "分");
        second = getInt(str, "秒");
    }


    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    //设置该时间域及以下域为0
    public void setZeroSegment(TimeFieldEnum timeFieldEnum) {
        switch (timeFieldEnum) {
            case YEAR:
                year = 0;
            case MONTH:
                month = 0;
            case DAY:
                day = 0;
            case HOUR:
                hour = 0;
            case MINUTE:
                minute = 0;
            case SECOND:
                second = 0;
        }
    }

    //设置该时间域及以下域为0
    public void setZeroSegment(int field) {
        if (field > 5 || field < 0) throw new IllegalArgumentException();
        switch (field) {
            case 0:
                year = 0;
            case 1:
                month = 0;
            case 2:
                day = 0;
            case 3:
                hour = 0;
            case 4:
                minute = 0;
            case 5:
                second = 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DateTime dateTime = (DateTime) o;
        return year == dateTime.year &&
               month == dateTime.month &&
               day == dateTime.day &&
               hour == dateTime.hour &&
               minute == dateTime.minute &&
               second == dateTime.second;
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, month, day, hour, minute, second);
    }

    /**
     * 判断本时间，是否比参数的时间大
     *
     * @param dateTime 参数时间
     * @return true 比参数的时间要大；false 比参数的时间小或者相等
     */
    public boolean biggerThan(DateTime dateTime) {
        int value = compareTo(dateTime);
        return value > 0;
    }

    /**
     * 比较两个数据，谁大一些
     *
     * @param dateTime 用于比较的另一个参数
     * @return 返回值：0一样大，1本数据大，-1本数据小
     **/
    public int compareTo(DateTime dateTime) {
        int value;
        if (dateTime.year == year) {
            if (dateTime.month == month) {
                if (dateTime.day == day) {
                    if (dateTime.hour == hour) {
                        if (dateTime.minute == minute) {
                            if (dateTime.second == second) return 0;
                            else value = second - dateTime.second;
                        } else value = minute - dateTime.minute;
                    } else value = hour - dateTime.hour;
                } else value = day - dateTime.day;
            } else value = month - dateTime.month;
        } else value = year - dateTime.year;
        return value;
    }

    public void addTo(Calendar calendar) {
        calendar.add(Calendar.YEAR, year);
        calendar.add(Calendar.MONTH, month);
        calendar.add(Calendar.DAY_OF_MONTH, day);

        calendar.add(Calendar.HOUR_OF_DAY, hour);
        calendar.add(Calendar.MINUTE, minute);
        calendar.add(Calendar.SECOND, second);
    }

    /**
     * 取两个数据之差
     *
     * @param dateTime 用于减去的时间
     * @return 2个时间的差值
     */
    public DateTime subtract(DateTime dateTime) {
        DateTime temp = new DateTime(this);
        temp.subtractOf(dateTime);
        return temp;
    }

    /**
     * 取两个数据之差，保存于本数据中
     *
     * @param dateTime 用于减去的时间
     */
    public void subtractOf(DateTime dateTime) {
        BorrowStru borrow = subtractOf(new BorrowStru(second, minute), dateTime.second, 60);
        second = borrow.lowOrder;
        minute = borrow.highOrder;

        borrow = subtractOf(new BorrowStru(minute, hour), dateTime.minute, 60);
        minute = borrow.lowOrder;
        hour = borrow.highOrder;

        borrow = subtractOf(new BorrowStru(hour, day), dateTime.hour, 24);
        hour = borrow.lowOrder;
        day = borrow.highOrder;

        int multiple = 30;
        if (month == 1) {
            //闰年判断
            if (year % 4 == 0 && year % 100 > 0) multiple = 29;
            else multiple = 28;

        } else if (month == 0 || month == 2 || month == 4 || month == 6 || month == 7 || month == 9 || month == 11)
            multiple = 31;

        borrow = subtractOf(new BorrowStru(day, month), dateTime.day, multiple);
        day = borrow.lowOrder;
        month = borrow.highOrder;

        borrow = subtractOf(new BorrowStru(month, year), dateTime.month, 12);
        month = borrow.lowOrder;
        year = borrow.highOrder;

        year -= dateTime.year;
    }

    /**
     * 把两个时间相加，保存于本时间中
     *
     * @param dateTime 用于相加的时间，可以为负值
     */
    public void add(DateTime dateTime) {
        DateTime dt = new DateTime(dateTime);
        dt.year = -dt.year;
        dt.month = -dt.month;
        dt.day = -dt.day;
        dt.hour = -dt.hour;
        dt.minute = -dt.minute;
        dt.second = -dt.second;
        subtractOf(dt);
    }

    class BorrowStru {
        BorrowStru(int lowOrder, int highOrder) {
            this.lowOrder = lowOrder;
            this.highOrder = highOrder;
        }

        int lowOrder;
        int highOrder;
    }

    /**
     * 两个数据相加和进位
     *
     * @param val        保存高位&低位的数据结构
     * @param subtractor 减数，用于减去的数值
     * @param multiple   进位标志数，如满10进1的10、满60进1的60
     * @return 返回已经被相加&进位了的数据
     */
    private BorrowStru subtractOf(BorrowStru val, int subtractor, int multiple) {
        val.lowOrder -= subtractor;

        if (val.lowOrder < 0) {
            //得出要借的数量
            int times = Math.abs(val.lowOrder / multiple);

            //val.lowOrder还有余数的话，要多借1
            val.lowOrder %= multiple;
            times += (val.lowOrder == 0 ? 0 : 1);

            //向高位借数
            val.highOrder -= times;

            //todo ??
            val.lowOrder = (val.lowOrder == 0 ? 0 : multiple) + val.lowOrder;

        } else if (val.lowOrder >= multiple) {
            //低位数大于进制数，则向高位进位
            int times = val.lowOrder / multiple;
            val.lowOrder %= multiple;
            val.highOrder += times;
        }
        return val;
    }


    @NonNull
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();

        buffer.append(fillZero(year, 4)).append("年")
              .append(fillZero(month + 1, 2)).append("月")
              .append(fillZero(day, 2)).append("日 ")
              .append(fillZero(hour, 2)).append("时")
              .append(fillZero(minute, 2)).append("分")
              .append(fillZero(Math.abs(second), 2)).append('秒')
              .append(second < 0 ? " ✘" : " ");//✔
        return buffer.toString();
    }

    public String toStringTime() {
        StringBuilder time = new StringBuilder();

        if (year > 0) time.append(String.format("%d年", year));
        if (month >= 0) time.append(String.format("%d月", month + 1));
        if (day > 0) time.append(String.format("%d日", day));
        if (hour > 0) time.append(String.format("%d时", hour));
        if (minute > 0) time.append(String.format("%d分", minute));
        if (second > 0) time.append(String.format("%d秒", second));
        return time.toString();
    }

    public String toAboutValue() {
        StringBuilder strTime = new StringBuilder();

        if (year > 0) strTime.append(String.format("%.1f年", year + (month / 12f)));
        else if (month > 0) strTime.append(String.format("%.1f月", month + (day / 31f)));
        else if (day > 0) strTime.append(String.format("%.1f日", day + (hour / 24f)));
        else if (hour > 0) strTime.append(String.format("%.1f时", hour + (minute / 60f)));
        else if (minute > 0) strTime.append(String.format("%.1f分", minute + (second / 60f)));
        else if (second > 0) strTime.append(String.format("%.1f秒", second + 0.0f));

        return strTime.toString();
    }

    public StringBuffer toNoneZero0String() {
        StringBuffer time = new StringBuffer();

        if (year > 0) time.append(String.format("%d年", year));
        if (month > 0) time.append(String.format("%d月", month + 1));
        if (day > 0) time.append(String.format("%d日 ", day));
        if (hour > 0) time.append(String.format("%s:", fillZero(hour, 2)));

        time.append(String.format("%s:%s", fillZero(minute, 2), fillZero(second + 1, 2)));
        return time;
    }

    /**
     * 数字不够位数，则在前面填充0
     *
     * @param value 要填充的数字
     * @param width 填充好的字符，总的位数
     * @param ch    要填充的字符
     * @return 填充好的字符串
     */
    public static String fillChar(int value, int width, char ch) {
        int           temp  = Math.abs(value);
        StringBuilder sb    = new StringBuilder();
        int           count = 0;

        do {
            temp /= 10;
            count++;
        } while (temp > 0);

        if (count > width) width = count;
        width -= count;
        if (value < 0) sb.append('-');
        for (int i = 0; i < width; i++) sb.append(ch);
        sb.append(Math.abs(value));
        return sb.toString();
    }

    /**
     * 数字不够位数，则在前面填充0
     *
     * @param value 要填充的数字
     * @param width 填充好的字符，总的位数
     * @return 填充好的字符串
     */
    private static String fillZero(int value, int width) {
        return fillChar(value, width, '0');
    }


    static public int MAX_BYTES = 7;

    @Override
    public void toBytes(DataOutputStream dos) throws IOException {
        dos.writeShort(year);//2
        dos.writeByte(month);//1
        dos.writeByte(day);//1
        dos.writeByte(hour);//1
        dos.writeByte(minute);//1
        dos.writeByte(second);//1
    }

    @Override
    public void loadWith(DataInputStream dis) throws IOException {
        year = dis.readShort();
        month = dis.readByte();
        day = dis.readByte();
        hour = dis.readByte();
        minute = dis.readByte();
        second = dis.readByte();
    }

}
