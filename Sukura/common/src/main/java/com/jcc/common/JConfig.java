package com.jcc.common;

import android.content.Context;

/**
 * Created by jincancan on 2018/2/5.
 * Description:
 */

public class JConfig {

    private static JConfig ins;
    private Context mContext;

    private JConfig(){

    }

    public static JConfig getInstence(){
        if(ins == null){
            synchronized (JConfig.class){
                if(ins == null){
                    ins = new JConfig();
                }
            }
        }
        return ins;
    }

    public Context getContext() {
        if(mContext == null){
            throw new NullPointerException("Please set context first");
        }
        return mContext;
    }

    public void setContext(Context context) {
        mContext = context;
    }
}
