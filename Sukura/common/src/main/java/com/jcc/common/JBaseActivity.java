package com.jcc.common;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by jincancan on 2017/12/13.
 * Description:Activity 基础类
 */

public abstract class JBaseActivity extends Activity {

    public Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutID());
        mContext = this;
        initData();
        initView();
    }

    public abstract int layoutID();
    public abstract void initData();
    public abstract void initView();
}
