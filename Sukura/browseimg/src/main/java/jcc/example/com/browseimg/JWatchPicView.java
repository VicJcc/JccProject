package jcc.example.com.browseimg;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.jcc.common.util.JWindowUtil;

/**
 * Created by jincancan on 2018/2/6.
 * Description:
 */

public class JWatchPicView extends RelativeLayout {

    private static final String TAG = "JWatchPicView";

    private Context mContext;
    private float mScale; // 图片缩放比例

    private ImageView mImageView;
    private RelativeLayout mRlRoot;

    public JWatchPicView(Context context) {
        this(context, null);
    }

    public JWatchPicView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JWatchPicView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init(){
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_move, null, false);
        addView(view);

        mImageView = view.findViewById(R.id.iv_test);
        mRlRoot = view.findViewById(R.id.rl_root);

        RelativeLayout.LayoutParams params = (LayoutParams) mImageView.getLayoutParams();
        params.height = JWindowUtil.getWindowHeight(getContext());
        params.width = JWindowUtil.getWindowWidth(getContext());
        mImageView.setLayoutParams(params);

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i(TAG, "onTouch");
                return false;
            }
        });
    }


    private float mPressX;
    private float mPressY;

    private boolean bHasMoveMore;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i(TAG, "onTouchEvent");
        int action = event.getAction();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                mPressX = event.getRawX();
                mPressY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                float diffX = event.getRawX() - mPressX;
                float diffY = event.getRawY() - mPressY;
                Log.i(TAG, diffX + "  " + diffY);

                if(Math.abs(diffY) > 10 && !bHasMoveMore){ // Y轴移动大于10 当做移动事件处理
                    bHasMoveMore = true;
                }

                if(!bHasMoveMore){
                    return true;
                }

                mImageView.setTranslationX(diffX);
                mImageView.setTranslationY(diffY);
                mRlRoot.setBackgroundColor(Color.parseColor(getAlphaBg(1 - diffY / JWindowUtil.getWindowHeight(mContext))));
//                mImageView.setScaleX(1 - Math.abs(diffX) / JWindowUtil.getWindowWidth(this));
                float scale = Math.abs(diffY) / JWindowUtil.getWindowHeight(mContext);
                if(scale < 1 && scale > 0) {
                    mImageView.setScaleY(1 - scale);
                    mImageView.setScaleX(1 - scale);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                Log.v(TAG,  "ACTION_UP  "
                        + mImageView.getScaleX() + "  "
                        + mImageView.getTranslationX() + "  "
                        + mImageView.getTranslationY() + "  "
                );

                float diffYfinal = event.getRawY() - mPressY;

                if(Math.abs(diffYfinal) < 10 && !bHasMoveMore){
                    startEndAnim();
                    bHasMoveMore = false;
                    return true;
                }

                if(diffYfinal < 600) {
                    mImageView.setTranslationX(0);
                    mImageView.setTranslationY(0);
                    mRlRoot.setBackgroundColor(Color.parseColor(getAlphaBg(1)));
                    mImageView.setScaleX(1.0f);
                    mImageView.setScaleY(1.0f);
                }else {
                    startEndAnim();
                }
                bHasMoveMore = false;
                break;
        }
        return super.onTouchEvent(event); // notice 暂未完成，手势冲突未解决
    }

    public void startImgAnim(int height, int width, int localX, int localY){

        float pivotX;
        float pivotY;
        float animImgStartHeight;
        float animImgStartWidth;

        float windowScale = JWindowUtil.getWindowScale(mContext);
        float imgScale = getImgScale(width, height);

        if(imgScale >= windowScale){
            mScale = width * 1.0f / JWindowUtil.getWindowWidth(mContext);
            animImgStartHeight = JWindowUtil.getWindowHeight(mContext) * mScale;
            pivotX = localX / (1 - mScale);
            pivotY = (localY - (animImgStartHeight - height) / 2) / (1 - mScale);
        }else{
            mScale = height * 1.0f / JWindowUtil.getWindowHeight(mContext);
            animImgStartWidth = JWindowUtil.getWindowWidth(mContext) * mScale;
            pivotX = (localX - (animImgStartWidth - width) / 2) / (1 - mScale);
            pivotY = localY / (1 - mScale);
        }

        mImageView.setPivotX(pivotX);
        mImageView.setPivotY(pivotY);

        Log.d(TAG, mImageView.getPivotX() + "  " + mImageView.getPivotY());
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(mImageView, SCALE_X,
                mScale,
                1.0f);
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(mImageView, SCALE_Y,
                mScale,
                1.0f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animatorX, animatorY);
        set.setDuration(3000);
        set.start();

        AlphaAnimation alphaAnimation = new AlphaAnimation(mScale, 1f);
        alphaAnimation.setDuration(3000);
        mRlRoot.startAnimation(alphaAnimation);

    }

    public void startEndAnim(){
        mRlRoot.setBackgroundColor(Color.parseColor(getAlphaBg(0)));

        ObjectAnimator animatorX = ObjectAnimator.ofFloat(mImageView, SCALE_X,
                mImageView.getScaleX(),
                mScale);
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(mImageView, SCALE_Y,
                mImageView.getScaleY(),
                mScale);

        ObjectAnimator animatorTransX = ObjectAnimator.ofFloat(mImageView, TRANSLATION_X,
                mImageView.getTranslationX(),
                0f);
        ObjectAnimator animatorTransY = ObjectAnimator.ofFloat(mImageView, TRANSLATION_Y,
                mImageView.getTranslationY(),
                0f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(animatorX, animatorY, animatorTransX, animatorTransY);
        set.setDuration(3000);
        set.start();

        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(mCallback != null){
                    mCallback.needDismiss();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private float getImgScale(float width, float height){
        return width * 1.0f / height;
    }

    private String getAlphaBg(float alpha){
        if(alpha >= 1){
            alpha = 1f;
        }
        if(alpha <= 0){
            alpha = 0.0f;
        }
        String colorAlpha = Integer.toHexString((int) (255 * alpha));
        if(colorAlpha.length() == 1){
            colorAlpha = "0" + colorAlpha;
        }
        if(colorAlpha.length() == 0){
            colorAlpha = "00";
        }
        return "#" + colorAlpha + "000000";
    }

    public void setCallback(WatchCallback callback) {
        mCallback = callback;
    }

    private WatchCallback mCallback;
    public interface WatchCallback{
        void needDismiss();
    }
}
