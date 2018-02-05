package com.jcc.common.util.img;

import java.util.Comparator;

/**
 * Created by jincancan on 17/8/7.
 * Description:
 */

public class TLPicSortByTime implements Comparator {

    public static final int SORT_TYPE_ASC = 1;
    public static final int SORT_TYPE_DESC = 2;

    private int mType;

    public TLPicSortByTime(int type) {
        mType = type;
    }

    public int compare(Object o1, Object o2) {
        TLPickPic program1 = (TLPickPic) o1;
        TLPickPic program2 = (TLPickPic) o2;
        if(mType == SORT_TYPE_DESC){
            return (program1.getTime() < program2.getTime()) ? ((program1.getTime() == program2.getTime()) ? 0 : 1):-1;
        }
        return Long.valueOf(program1.getTime()).compareTo(program2.getTime());
    }
}
