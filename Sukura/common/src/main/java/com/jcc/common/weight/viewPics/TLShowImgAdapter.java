package com.jcc.common.weight.viewPics;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by jincancan on 2017/9/29.
 * Description:查看图片适配器
 */

public class TLShowImgAdapter extends PagerAdapter {

    private ArrayList<View> mViewList;

    public TLShowImgAdapter(ArrayList<View> viewList) {
        mViewList = viewList;
    }

    @Override
    public int getCount() {
        return mViewList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(mViewList.get(position));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(mViewList.get(position), 0);
        return mViewList.get(position);
    }
}
