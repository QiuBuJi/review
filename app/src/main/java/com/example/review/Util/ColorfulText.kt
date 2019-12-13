package com.example.review.Util

import com.example.review.DataStructureFile.ElementCategory
import com.example.review.DataStructureFile.ElementCategory.Category

class ColorfulText {
    var txt = ""

    /**
     * 字符分类
     */
//-------------------------------------------------------------------------------------------
    fun categoryString(inputText: String, src: String): ArrayList<ElementCategory> {
        val ecs = ArrayList<ElementCategory>()
        val matchedSeris = getMatchedSerial(src, inputText)
        var endIndex = 0
        var str: String
        run {
            var i = 0
            while (i < inputText.length) {
                val c = inputText[i]

                //matchedSeris遍历完后，inputText仍有数据。
                if (matchedSeris.size == 0) { //添加inputText中剩余的字符串
                    if (endIndex < inputText.length) {
                        str = inputText.substring(endIndex)
                        ecs.add(ElementCategory(str, Category.Unnecessary))
                    }
                    break
                }
                var unFound = true

                //遍历，是否有匹配的字符在matchedSerial中
                for (j in matchedSeris.indices) {
                    val cds = matchedSeris[j]

                    //匹配到该字符&位置下标一致
                    if (c == cds.c && cds.indexMatch == i) { //添加匹配了的字符串
                        endIndex = cds.indexMatch + Math.abs(cds.serial)
                        str = inputText.substring(cds.indexMatch, endIndex)
                        i += str.length - 1
                        if (cds.serial > 0) ecs.add(ElementCategory(str, Category.Correct)) else {
                            if (Math.abs(cds.serial) > 1) ecs.add(ElementCategory(str, Category.Correct)) else ecs.add(ElementCategory(str, Category.Malposition))
                        }
                        matchedSeris.removeAt(j)
                        unFound = false
                        break
                    }
                }

                //添加多余字符串
                if (unFound) ecs.add(ElementCategory(c.toString(), Category.Unnecessary))
                i++
            }
        }
        //matchedSerial中，有数据未被匹配。
        if (matchedSeris.size > 0) {
            val sb = StringBuilder()
            for (cds in matchedSeris) sb.append(cds.c)
            str = "缺失: {" + sb.toString() + "} <font color='#C3C3C3'>" + matchedSeris.size + "个</font>"
            txt = str
        }

        //去冗余
        var i = -1
        while (++i < ecs.size) {
            val ec1 = ecs[i]
            var k = i + 1
            while (k < ecs.size) {
                val ec2 = ecs[k]
                if (ec1.category == ec2.category) {
                    ec1.txt = ec1.txt + ec2.txt
                    ecs.removeAt(k)
                    k--
                } else {
                    break
                }
                k++
            }
        }
        return ecs
    }

    //----------------------------------------------------------------------------------------------
    inner class CharDataSimple(var c: Char, var indexCorrect: Int, var indexMatch: Int) {
        var serial = 0
    }

    /**
     * 取匹配了的序列
     *
     * @param src   待匹配字符串
     * @param match 被匹配字符串
     * @return 返回匹配好的字符组
     */
    //-------------------------------------------------------------------------------------------
    fun getMatchedSerial(src: String, match: String): ArrayList<CharDataSimple> {
        val temp = uniteSameChars(src, match)
        var i = -1

        //序列"seris"长度记录
        for (cds in temp) {
            var increment = 0
            while (true) {
                val indexSrc = cds.indexCorrect + increment
                val indexMatch = cds.indexMatch + increment

                //排除下标越界
                if (indexSrc >= src.length || indexMatch >= match.length || indexMatch < 0) break
                val cSrc = src[indexSrc]
                val cMatch = match[indexMatch]

                //字符匹配，记录匹配数量
                if (cSrc == cMatch) {
                    cds.serial++ //增加长度
                    increment++ //转下一个
                } else break //字符不匹配，结束循环
            }

            //开始位置不匹配，设置为负值
            if (cds.indexCorrect != cds.indexMatch) cds.serial = -cds.serial
        }

        //去除权小的冗余元素
        i = -1
        while (++i < temp.size) {
            val cdsPre = temp[i]
            var k = i + 1
            while (k < temp.size) {
                val cdsPost = temp[k]

                //下标匹配
                if (cdsPre.indexCorrect == cdsPost.indexCorrect) { //序列长度相等
                    if (Math.abs(cdsPre.serial) == Math.abs(cdsPost.serial)) {
                        if (cdsPre.serial > 0 && cdsPost.serial < 0) {
                            temp.remove(cdsPost) //移除权小的元素
                            if (k > 0) k-- //保持位置
                        }
                        if (cdsPre.serial < 0 && cdsPost.serial > 0) {
                            temp.remove(cdsPre) //移除权小的元素
                        }
                    } else if (Math.abs(cdsPre.serial) > Math.abs(cdsPost.serial)) {
                        temp.remove(cdsPost) //移除权小的元素
                        if (k > 0) k-- //保持位置
                    } else temp.remove(cdsPre) //移除权小的元素
                }
                k++
            }
        }

        //去除冗余元素
        while (++i < temp.size) {
            val cdsMatchRedundancy = temp[i]
            var k = i + 1
            while (k < temp.size) {
                val cdsPost = temp[k]
                if (cdsMatchRedundancy.indexCorrect == cdsPost.indexCorrect) break
                val region = Math.abs(cdsMatchRedundancy.serial) + cdsMatchRedundancy.indexCorrect
                //去除region区间的元素
                if (region > cdsPost.indexCorrect) {
                    temp.remove(cdsPost)
                    if (k > 0) k--
                }
                k++
            }
        }

        //去除
        i = -1
        while (++i < temp.size) {
            val cdsPre = temp[i]
            var k = i + 1

            while (k < temp.size) {
                val cdsPost = temp[k]

                //去除与"cdsPre"冗余的数据
                if (cdsPost.indexCorrect != cdsPre.indexCorrect) {
                    var j = k
                    while (j < temp.size) {
                        val cdsTemp = temp[j]
                        if (cdsPre.indexMatch == cdsTemp.indexMatch && Math.abs(cdsPre.serial) == Math.abs(cdsTemp.serial)) {
                            temp.removeAt(j)
                            if (j > 0) j--
                        }
                        j++
                    }
                    break
                }

                //删除最小的元素
                if (cdsPre.indexMatch < cdsPost.indexMatch) {
                    temp.removeAt(k)
                    if (k > 0) k--
                } else temp.removeAt(i)
                k++
            }
        }
        return temp
    }

    /**
     * 聚集相同字符
     *
     * @param src   待匹配字符串
     * @param match 被匹配字符串
     * @return 返回匹配好的字符组
     */
    //-------------------------------------------------------------------------------------------
    fun uniteSameChars(src: String, match: String): ArrayList<CharDataSimple> {
        val cdss = ArrayList<CharDataSimple>()
        var indexChar = 0
        var indexMissing = 0
        for (c in src.toCharArray()) {
            var index = 0
            var count = 0
            //从match中匹配src的字符并提取
            while (match.indexOf(c, index).also { index = it } != -1) {
                cdss.add(CharDataSimple(c, indexChar, index))
                index++
                count++
            }
            //该字符无匹配
            if (count == 0) cdss.add(CharDataSimple(c, indexChar, --indexMissing))
            indexChar++
        }
        return cdss
    }
}