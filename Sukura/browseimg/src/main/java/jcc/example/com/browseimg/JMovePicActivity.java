package jcc.example.com.browseimg;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.jcc.common.util.JStringUtils;
import com.jcc.common.util.JWindowUtil;
import com.jcc.common.weight.viewPics.GFViewPager;
import com.jcc.common.weight.viewPics.PhotoInfo;

import java.util.ArrayList;

import jcc.example.com.browseimg.beans.JPhotosInfos;


/**
 * Created by jincancan on 2018/2/6.
 * Description:
 */

public class JMovePicActivity extends Activity implements
        PhotoPreviewAdapter.PhotoCallback,
        ViewPager.OnPageChangeListener {

    private static final String TAG = "JMovePicActivity";

    public static final String PARAMS_IMGS = "imgs";
    public static final String PARAMS_INDEX = "index";
    public static final String PARAMS_LOCAL = "local";
    public static final String PARAMS_IMGS_INFO = "imgs_info";
    public static void start(Context context, ArrayList imgs, int position, ArrayList<JPhotosInfos> infos){
        Intent intent = new Intent(context, JMovePicActivity.class);
        intent.putExtra(PARAMS_IMGS, imgs);
        intent.putExtra(PARAMS_INDEX, position);
        intent.putExtra(PARAMS_LOCAL, false);
        intent.putExtra(PARAMS_IMGS_INFO, infos);
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(0, 0);
    }

    public static void start(Context context, ArrayList<String> imgs, int position, boolean local){
        Intent intent = new Intent(context, JMovePicActivity.class);
        intent.putExtra(PARAMS_IMGS, imgs);
        intent.putExtra(PARAMS_INDEX, position);
        intent.putExtra(PARAMS_LOCAL, local);
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(0, 0);
    }

    private RelativeLayout mRlRoot;
    private GFViewPager mGFViewPager;

    private boolean bFirstResume = true;

    private ArrayList<JPhotosInfos> mInfos; // 各个图片位置
    private PhotoPreviewAdapter mAdapter;
    private ArrayList mImgs;
    private int mCurrentIndex; //
    private float mWindowScale;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move_pic);

        initData();
        initView();
    }

    private void initData(){
        mInfos = (ArrayList<JPhotosInfos>) getIntent().getSerializableExtra(PARAMS_IMGS_INFO);
        mImgs = (ArrayList) getIntent().getSerializableExtra(PARAMS_IMGS);
        mCurrentIndex = getIntent().getIntExtra(PARAMS_INDEX, 0);

        ArrayList<PhotoInfo> mPhotoList = new ArrayList<>();
//        mAdapter = new TLShowImgAdapter(mViews);
        if(mImgs != null){
            for (int i = 0; i < mImgs.size(); i++) {
//                TLShowImageView view = new TLShowImageView(this);
//                view.setImg(mImgs.get(i));
//                mViews.add(view);
                PhotoInfo info = new PhotoInfo();
                info.setPhotoPath(mImgs.get(i)+"");
                mPhotoList.add(info);
            }
        }
        mAdapter = new PhotoPreviewAdapter(this, mPhotoList, this);
        mWindowScale = JWindowUtil.getWindowScale(this);
    }

    private float mScale = 1.0f;
    private void initView(){
        mGFViewPager = findViewById(R.id.vp_pager);
        mRlRoot = findViewById(R.id.rl_root);

        mGFViewPager.setAdapter(mAdapter);
        mGFViewPager.setCurrentItem(mCurrentIndex);
        mGFViewPager.addOnPageChangeListener(this);

//        findViewById(R.id.tv_move).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mGFViewPager.setPivotX(JWindowUtil.getWindowWidth(JMovePicActivity.this) / 2);
//                mGFViewPager.setPivotY(JWindowUtil.getWindowHeight(JMovePicActivity.this) / 2);
//                mGFViewPager.setScaleX(mScale * 0.99f);
//                mGFViewPager.setScaleY(mScale * 0.99f);
//                mScale = mScale*0.99f;
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(bFirstResume){
            startAnim();
            bFirstResume = false;
        }
        hideBar();
    }

    private void startAnim(){
        startImgAnim();
    }

    private void hideBar(){
        //隐藏状态栏
//        WindowManager.LayoutParams lp = getWindow().getAttributes();
//        lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
//        getWindow().setAttributes(lp);

        mRlRoot.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private void showBar(){
        //显示状态栏
        WindowManager.LayoutParams attr = getWindow().getAttributes();
        attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setAttributes(attr);
    }

    @Override
    public void onBackPressed() {
//        mPicView.startEndAnim();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mCurrentIndex = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPhotoClick() {
        startExitAnim();
    }

    @Override
    public void onDrag(float x, float y) {
        Log.i(TAG, "onDrag: ");

        startDrag(x, y);

    }

    @Override
    public void onDragFinish() {
        if(mGFViewPager.getScaleX() > 0.7f) {
            mGFViewPager.setTranslationX(0);
            mGFViewPager.setTranslationY(0);
            mGFViewPager.setScaleX(1);
            mGFViewPager.setScaleY(1);
            mRlRoot.setBackgroundColor(Color.parseColor(JStringUtils.getBlackAlphaBg(1)));
        }else{
            startExitAnim();
        }
    }

    public void startExitAnim(){
        mRlRoot.setBackgroundColor(Color.parseColor(JStringUtils.getBlackAlphaBg(0)));

        ObjectAnimator animatorX = ObjectAnimator.ofFloat(mGFViewPager, View.SCALE_X,
                mGFViewPager.getScaleX(),
                getCurrentPicOriginalScale());
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(mGFViewPager, View.SCALE_Y,
                mGFViewPager.getScaleY(),
                getCurrentPicOriginalScale());
        getCurrentPicOriginalScale();
        ObjectAnimator animatorTransX = ObjectAnimator.ofFloat(mGFViewPager, View.TRANSLATION_X,
                mGFViewPager.getTranslationX() +
                        (JWindowUtil.getWindowWidth(this) / 2 * (1 - mGFViewPager.getScaleX()) -
                                mGFViewPager.getPivotX() * (1 - mGFViewPager.getScaleX())),
                0);
        ObjectAnimator animatorTransY = ObjectAnimator.ofFloat(mGFViewPager, View.TRANSLATION_Y,
                mGFViewPager.getTranslationY() +
                        (JWindowUtil.getWindowHeight(this) / 2 * (1 - mGFViewPager.getScaleY()) -
                                mGFViewPager.getPivotY() * (1 - mGFViewPager.getScaleY())),
                0);

        Log.i("TTTT", mGFViewPager.getTranslationX() + " finish  " + mGFViewPager.getTranslationY());

        AnimatorSet set = new AnimatorSet();
        set.playTogether(animatorX, animatorY, animatorTransX, animatorTransY);
        set.setDuration(5000);
        set.start();

        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
//                if(mCallback != null){
//                    mCallback.needDismiss();
//                }
                finish();
                overridePendingTransition(0, 0);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private float getCurrentPicOriginalScale(){

        float mScale;

        float pivotX;
        float pivotY;
        float animImgStartHeight;
        float animImgStartWidth;
        int width = mInfos.get(mCurrentIndex).getWidth();
        int height = mInfos.get(mCurrentIndex).getHeight();
        int localX = mInfos.get(mCurrentIndex).getLeft();
        int localY = mInfos.get(mCurrentIndex).getTop();

        float imgScale = getImgScale(width, height);

        if(imgScale >= mWindowScale){
            mScale = width * 1.0f / JWindowUtil.getWindowWidth(this);
            animImgStartHeight = JWindowUtil.getWindowHeight(this) * mScale;
            pivotX = localX / (1 - mScale);
            pivotY = (localY - (animImgStartHeight - height) / 2) / (1 - mScale);
        }else{
            mScale = height * 1.0f / JWindowUtil.getWindowHeight(this);
            animImgStartWidth = JWindowUtil.getWindowWidth(this) * mScale;
            pivotX = (localX - (animImgStartWidth - width) / 2) / (1 - mScale);
            pivotY = localY / (1 - mScale);
        }
        mGFViewPager.setPivotX(pivotX);
        mGFViewPager.setPivotY(pivotY);
        return mScale;
    }

    private float getImgScale(float width, float height){
        return width * 1.0f / height;
    }

    public void startImgAnim(){

        float mScale;

        float pivotX;
        float pivotY;
        float animImgStartHeight;
        float animImgStartWidth;
        int width = mInfos.get(mCurrentIndex).getWidth();
        int height = mInfos.get(mCurrentIndex).getHeight();
        int localX = mInfos.get(mCurrentIndex).getLeft();
        int localY = mInfos.get(mCurrentIndex).getTop();

        float windowScale = JWindowUtil.getWindowScale(this);
        float imgScale = getImgScale(width, height);

        if(imgScale >= windowScale){
            mScale = width * 1.0f / JWindowUtil.getWindowWidth(this);
            animImgStartHeight = JWindowUtil.getWindowHeight(this) * mScale;
            pivotX = localX / (1 - mScale);
            pivotY = (localY - (animImgStartHeight - height) / 2) / (1 - mScale);
        }else{
            mScale = height * 1.0f / JWindowUtil.getWindowHeight(this);
            animImgStartWidth = JWindowUtil.getWindowWidth(this) * mScale;
            pivotX = (localX - (animImgStartWidth - width) / 2) / (1 - mScale);
            pivotY = localY / (1 - mScale);
        }

        mGFViewPager.setPivotX(pivotX);
        mGFViewPager.setPivotY(pivotY);

        ObjectAnimator animatorX = ObjectAnimator.ofFloat(mGFViewPager, View.SCALE_X,
                mScale,
                1.0f);
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(mGFViewPager, View.SCALE_Y,
                mScale,
                1.0f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animatorX, animatorY);
        set.setDuration(500);
        set.start();

//        AlphaAnimation alphaAnimation = new AlphaAnimation(mScale, 1f);
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setDuration(500);
        valueAnimator.setFloatValues(mScale, 1f);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Log.i("ValueAnimator", (float)animation.getAnimatedValue() + "");
                mRlRoot.setBackgroundColor(Color.parseColor(JStringUtils.getBlackAlphaBg((float)animation.getAnimatedValue())));
            }
        });
        valueAnimator.start();
//        alphaAnimation.setDuration(300);
//        mRlRoot.startAnimation(alphaAnimation);

    }


    private void startDrag(float x, float y){
        mGFViewPager.setTranslationX(x);
        mGFViewPager.setTranslationY(y);
        if(y > 0){
            mGFViewPager.setPivotX(JWindowUtil.getWindowWidth(JMovePicActivity.this) / 2);
            mGFViewPager.setPivotY(JWindowUtil.getWindowHeight(JMovePicActivity.this) / 2);
            float scale = Math.abs(y) / JWindowUtil.getWindowHeight(this);
            if(scale < 1 && scale > 0) {
                mGFViewPager.setScaleX(1-scale);
                mGFViewPager.setScaleY(1-scale);
                mRlRoot.setBackgroundColor(Color.parseColor(JStringUtils.getBlackAlphaBg(1-scale)));
            }
        }
        Log.i("TTTT", x + "   " + y);
    }

}
