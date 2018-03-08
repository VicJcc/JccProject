package jcc.example.com.browseimg;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by jincancan on 2018/2/5.
 * Description:
 */

public class JBehavior extends CoordinatorLayout.Behavior<View> {

    private static final String TAG = "JBehavior";

    private Context mContext;

    public JBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        if(params != null && params.height == CoordinatorLayout.LayoutParams.MATCH_PARENT){
            child.layout(0,0, parent.getWidth(), parent.getHeight());
            child.setTranslationY(getHeaderHeight());
            return true;
        }
        return super.onLayoutChild(parent, child, layoutDirection);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        return (axes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
        Log.d(TAG, "onNestedPreScroll "
                + dx + "  "
                + dy + "  "
                + consumed[0] + "  "
                + consumed[1] + "  "
                + type);
        if(dy < 0){
            return;
        }

        float diffY = child.getTranslationY() - dy;
        if(diffY> 0){
            child.setTranslationY(diffY);
            consumed[1] = dy;
        }

    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
        Log.d(TAG, "onNestedScroll "
                + dxConsumed + "  "
                + dyConsumed + "  "
                + dxUnconsumed + "  "
                + dyUnconsumed + "  "
                + type);
        if(dyConsumed > 0){
            return;
        }

        float diffY = child.getTranslationY() - dyUnconsumed;

        if(getHeaderHeight() > diffY && diffY > 0) {
            child.setTranslationY(diffY);
        }
    }

    /**
     * 获取Header 高度
     * @return
     */
    public int getHeaderHeight(){
        return mContext.getResources().getDimensionPixelOffset(R.dimen.test);
    }
}
