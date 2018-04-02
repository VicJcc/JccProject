package jcc.example.com.motionevent;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by jincancan on 2018/3/22.
 * Description:
 */

public class JTestViewGroup extends ViewGroup {

    private static final String TAG = "JTestViewGroup";
    private int mHorizonalSpace = 6;//dp
    private int mVerticalSpace = 6;//dp
    private int mChildSize = 70;//dp


    private int testCount = 2;

    public JTestViewGroup(Context context) {
        this(context, null);
    }

    public JTestViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JTestViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        for (int i = 0; i < testCount; i++) {
            ImageView imageView = new ImageView(getContext());
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(PixelsUtils.dip2px(getContext(), mChildSize), PixelsUtils.dip2px(getContext(), mChildSize));
            imageView.setLayoutParams(params);
            imageView.setBackgroundColor(getContext().getResources().getColor(R.color.colorPrimary));
            addView(imageView);
        }

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int usedWidth = 0;
        int usedHeight = 0;

        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if(i % 3 == 0 && i != 0){
                usedHeight += (view.getMeasuredHeight() + PixelsUtils.dip2px(getContext(),mVerticalSpace));
                usedWidth = 0;
            }
            view.layout(usedWidth,
                    usedHeight,
                    usedWidth + view.getMeasuredWidth() ,
                    usedHeight + view.getMeasuredHeight());

            usedWidth += view.getMeasuredWidth() + PixelsUtils.dip2px(getContext(),mHorizonalSpace);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        for (int i = 0; i < getChildCount(); i++) {
            measureChild(getChildAt(i), widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
//        Log.i(TAG, "dispatchTouchEvent: " + ev.getAction());
//        switch (ev.getAction()){
//            case MotionEvent.ACTION_DOWN:
//                return false;
//            case MotionEvent.ACTION_MOVE:
//                return true;
//            case MotionEvent.ACTION_UP:
//                return true;
//        }
        super.dispatchTouchEvent(ev);
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean b;
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                b = false;
                break;
            case MotionEvent.ACTION_MOVE:
                b = true;
                break;
            case MotionEvent.ACTION_UP:
                b = true;
                break;
            default:
                b = super.onInterceptTouchEvent(ev);

        }
        Log.i(TAG, "onInterceptTouchEvent: " + ev.getAction() + " " +b);
        return b;
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean b;
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                b = true;
                break;
            case MotionEvent.ACTION_MOVE:
                b = true;
                break;
            case MotionEvent.ACTION_UP:
                b = true;
                break;
            default:
                b = super.onTouchEvent(event);
                break;
        }
        Log.i(TAG, "onTouchEvent: " + event.getAction() + " " +b);
        return b;
    }
}
