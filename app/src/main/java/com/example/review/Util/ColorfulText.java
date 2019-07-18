package com.example.review.Util;

import com.example.review.DataStructureFile.ElementCategory;
import com.example.review.DataStructureFile.ElementCategory.Category;

import java.util.ArrayList;

public class ColorfulText {
    public String txt = "";

    /**
     * 字符分类
     *///-------------------------------------------------------------------------------------------
    public ArrayList<ElementCategory> categoryString(String inputText, String src) {

        ArrayList<ElementCategory> ecs          = new ArrayList<>();
        ArrayList<CharDataSimple>  matchedSeris = getMatchedSeris(src, inputText);

        int    endIndex = 0;
        String str;

        for (int i = 0; i < inputText.length(); i++) {
            char c = inputText.charAt(i);

            //matchedSeris遍历完后，inputText仍有数据。
            if (matchedSeris.size() == 0) {

                //添加inputText中剩余的字符串
                if (endIndex < inputText.length()) {

                    str = inputText.substring(endIndex);
                    ecs.add(new ElementCategory(str, Category.unnecesary));
                }
                break;
            }

            boolean un_found = true;
            //遍历，是否有匹配的字符在matchedSeris中
            for (int j = 0; j < matchedSeris.size(); j++) {
                CharDataSimple cds = matchedSeris.get(j);

                //匹配到该字符&位置下标一致
                if (c == cds.c && cds.indexMatch == i) {

                    //添加匹配了的字符串
                    endIndex = cds.indexMatch + Math.abs(cds.seris);
                    str = inputText.substring(cds.indexMatch, endIndex);
                    i += str.length() - 1;

                    if (cds.seris > 0)
                        ecs.add(new ElementCategory(str, Category.correct));
                    else {
                        if (Math.abs(cds.seris) > 1)
                            ecs.add(new ElementCategory(str, Category.correct));
                        else
                            ecs.add(new ElementCategory(str, Category.malposition));
                    }

                    matchedSeris.remove(j);
                    un_found = false;
                    break;
                }
            }

            //添加多余字符串
            if (un_found)
                ecs.add(new ElementCategory(String.valueOf(c), Category.unnecesary));
        }

        //matchedSeris中，有数据未被匹配。
        if (matchedSeris.size() > 0) {

            StringBuilder sb = new StringBuilder();
            sb.append(" ");
            int count = 0;
            for (CharDataSimple cds : matchedSeris) {
                if (count == 0)
                    sb.append(cds.c).append(" ");
                else
                    sb.append(", ").append(cds.c).append(" ");
                count++;
            }

            str = "缺失: {" + sb.toString() + "} " + "<font color='#C3C3C3'>" + matchedSeris.size() + "个</font>";
            txt = str;
        }

        //去冗余
        for (int i = 0; i < ecs.size(); i++) {
            ElementCategory ec1 = ecs.get(i);

            for (int k = i + 1; k < ecs.size(); k++) {
                ElementCategory ec2 = ecs.get(k);

                if (ec1.category == ec2.category) {
                    ec1.txt = ec1.txt.concat(ec2.txt);
                    ecs.remove(k);
                    k--;
                } else {
                    break;
                }
            }
        }

        return ecs;
    }


    //----------------------------------------------------------------------------------------------
    class CharDataSimple {
        public CharDataSimple(char c, int indexCorrect, int indexMatch) {
            this.c = c;
            this.indexCorrect = indexCorrect;
            this.indexMatch = indexMatch;
        }

        char c;

        int indexCorrect;
        int indexMatch;
        int seris = 0;
    }

    /**
     * 取匹配了的序列
     *
     * @param src   待匹配字符串
     * @param match 被匹配字符串
     * @return 返回匹配好的字符组
     *///-------------------------------------------------------------------------------------------
    public ArrayList<CharDataSimple> getMatchedSeris(String src, String match) {
        ArrayList<CharDataSimple> temp = uniteSameChars(src, match);


        /*序列"seris"长度记录*/
        for (CharDataSimple cds : temp) {
            int increment = 0;

            while (true) {
                int indexSrc   = cds.indexCorrect + increment;
                int indexMatch = cds.indexMatch + increment;

                //排除下标越界
                if (indexSrc >= src.length() || indexMatch >= match.length() || indexMatch < 0)
                    break;

                char cSrc   = src.charAt(indexSrc);
                char cMatch = match.charAt(indexMatch);

                //字符匹配，记录匹配数量
                if (cSrc == cMatch) {

                    cds.seris++;//增加长度
                    increment++;//转下一个

                } else break;//字符不匹配，结束循环
            }

//            开始位置不匹配，设置为负值
            if (cds.indexCorrect != cds.indexMatch) cds.seris = -cds.seris;
        }


        /*去除权小的冗余元素*/
        for (int i = 0; i < temp.size(); i++) {
            CharDataSimple cdsPre = temp.get(i);

            for (int k = i + 1; k < temp.size(); k++) {
                CharDataSimple cdsPost = temp.get(k);

                //下标匹配
                if (cdsPre.indexCorrect == cdsPost.indexCorrect) {

                    //序列长度相等
                    if (Math.abs(cdsPre.seris) == Math.abs(cdsPost.seris)) {

                        if (cdsPre.seris > 0 && cdsPost.seris < 0) {
                            temp.remove(cdsPost);//移除权小的元素
                            if (k > 0) k--;//保持位置
                        }
                        if (cdsPre.seris < 0 && cdsPost.seris > 0) {
                            temp.remove(cdsPre);//移除权小的元素
                        }

                    }
                    //"cdsPre"长度 > "cdsPost"长度都取"cdsPre"元素
                    else if (Math.abs(cdsPre.seris) > Math.abs(cdsPost.seris)) {
                        temp.remove(cdsPost);//移除权小的元素
                        if (k > 0) k--;//保持位置
                    }
                    //"cdsPre"长度 < "cdsPost"长度都取"cdsPost"元素
                    else temp.remove(cdsPre);//移除权小的元素
                }
            }
        }

        /*去除冗余元素*/
        for (int i = 0; i < temp.size(); i++) {
            CharDataSimple cdsMatchRedundancy = temp.get(i);

            for (int k = i + 1; k < temp.size(); k++) {
                CharDataSimple cdsPost = temp.get(k);

                if (cdsMatchRedundancy.indexCorrect == cdsPost.indexCorrect) break;

                int region = Math.abs(cdsMatchRedundancy.seris) + cdsMatchRedundancy.indexCorrect;

                //去除region区间的元素
                if (region > cdsPost.indexCorrect) {
                    temp.remove(cdsPost);
                    if (k > 0) k--;
                }
            }
        }

        //去除
        for (int i = 0; i < temp.size(); i++) {
            CharDataSimple cdsPre = temp.get(i);

            for (int k = i + 1; k < temp.size(); k++) {
                CharDataSimple cdsPost = temp.get(k);

                //去除与"cdsPre"冗余的数据
                if (cdsPost.indexCorrect != cdsPre.indexCorrect) {

                    for (int j = k; j < temp.size(); j++) {
                        CharDataSimple cdsTemp = temp.get(j);

                        if (cdsPre.indexMatch == cdsTemp.indexMatch && Math.abs(cdsPre.seris) == Math.abs(cdsTemp.seris)) {
                            temp.remove(j);
                            if (j > 0) j--;
                        }
                    }
                    break;
                }

                //删除最小的元素
                if (cdsPre.indexMatch < cdsPost.indexMatch) {
                    temp.remove(k);
                    if (k > 0) k--;
                } else temp.remove(i);
            }

        }
        return temp;
    }

    /**
     * 聚集相同字符
     *
     * @param src   待匹配字符串
     * @param match 被匹配字符串
     * @return 返回匹配好的字符组
     *///-------------------------------------------------------------------------------------------
    public ArrayList<CharDataSimple> uniteSameChars(String src, String match) {
        ArrayList<CharDataSimple> cdss      = new ArrayList<>();
        int                       indexChar = 0, indexMissing = 0;

        for (char c : src.toCharArray()) {

            int index = 0, count = 0;

            //从match中匹配src的字符并提取
            while ((index = match.indexOf(c, index)) != -1) {
                cdss.add(new CharDataSimple(c, indexChar, index));
                index++;
                count++;
            }

            //该字符无匹配
            if (count == 0) cdss.add(new CharDataSimple(c, indexChar, --indexMissing));

            indexChar++;
        }
        return cdss;
    }


}
