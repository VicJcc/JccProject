package com.taole.myapplication.MediaTest;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

/**
 * Resize surface view
 * Created by taole on 15/5/13.
 */
public class TLResizeSurfaceView extends GLSurfaceView {

    
    private int mWidth = -1;
    private int mHeight = -1;

    public TLResizeSurfaceView(Context context) {
        super(context);
    }

    public TLResizeSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (-1 == mWidth || -1 == mHeight) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        else {
            setMeasuredDimension(mWidth, mHeight);
        }
    }


    public void resize(int width, int height)
    {
        mWidth = width;
        mHeight = height;

        // not sure whether it is useful or not but safe to do so
        getHolder().setFixedSize(width, height);

        requestLayout();
        invalidate();     // very important, so that onMeasure will be triggered
    }

}

