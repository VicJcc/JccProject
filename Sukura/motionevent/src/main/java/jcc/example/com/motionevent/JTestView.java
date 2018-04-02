package jcc.example.com.motionevent;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by jincancan on 2018/3/22.
 * Description:
 */

public class JTestView extends View {
    private static final String TAG = "JTestView";
    public JTestView(Context context) {
        super(context);
    }

    public JTestView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public JTestView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean b;
        getParent().requestDisallowInterceptTouchEvent(true);
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
                b = true;
                break;
        }
        Log.i(TAG, "onTouchEvent: " + event.getAction() + " " + b);
        return b;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

}
