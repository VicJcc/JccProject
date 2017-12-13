package com.jcc.common;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by jincancan on 2017/12/13.
 * Description:Fragment 基础类
 */

public abstract class JBaseFragment extends Fragment {

    public Activity mActivity;
    public View mRootView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(layoutID(),container, false);
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = getActivity();
        initData();
        initView();
    }

    public abstract int layoutID();
    public abstract void initData();
    public abstract void initView();
}
