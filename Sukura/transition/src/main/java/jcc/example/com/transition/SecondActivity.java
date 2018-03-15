package jcc.example.com.transition;

import android.animation.Animator;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.jcc.common.weight.viewPics.GFViewPager;
import com.jcc.common.weight.viewPics.PhotoInfo;

import java.util.ArrayList;

/**
 * Created by jincancan on 2018/3/13.
 * Description:
 */

public class SecondActivity extends Activity implements ViewPager.OnPageChangeListener, PhotoPreviewAdapter.PhotoCallback {


    private ImageView mIvSF;
    private RelativeLayout mRlRoot;
    private GFViewPager mGFViewPager;
    private PhotoPreviewAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move_pic);
        mIvSF = findViewById(R.id.iv_sf);

        initData();
        initView();



        getWindow().setEnterTransition(initContentEnterTransition());
        getWindow().setSharedElementEnterTransition(initSharedElementEnterTransition());
//        getWindow().setReturnTransition(TransitionInflater.from(this).inflateTransition(R.transition.return_slide));
    }

    private Transition initContentEnterTransition() {
        Transition transition= TransitionInflater.from(this).inflateTransition(R.transition.slide_and_fade);
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {

            }

            @Override
            public void onTransitionEnd(Transition transition) {
                transition.removeListener(this);
                mIvSF.setVisibility(View.GONE);
                mGFViewPager.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });
        return transition;
    }


    private Transition initSharedElementEnterTransition() {
        final Transition sharedTransition= TransitionInflater.from(this).inflateTransition(R.transition.changebounds_with_arcmotion);
        sharedTransition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                Animator circularReveal = ViewAnimationUtils.createCircularReveal(
                        mRlRoot, mRlRoot.getWidth() / 2, mRlRoot.getHeight() / 2,
                        mIvSF.getWidth()/2, Math.max(mRlRoot.getWidth(), mRlRoot.getHeight())
                );
//                mRlRoot.setBackgroundColor(Color.BLACK);
                circularReveal.setDuration(6000);
                circularReveal.start();
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                sharedTransition.removeListener(this);
                mIvSF.setVisibility(View.GONE);
                mGFViewPager.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });
        return sharedTransition;
    }

    private void initData(){
        ArrayList<PhotoInfo> mPhotoList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            PhotoInfo info = new PhotoInfo();
            info.setPhotoPath(R.drawable.sf+"");
            mPhotoList.add(info);
        }
        mAdapter = new PhotoPreviewAdapter(this, mPhotoList, this);

    }

    private void initView(){
        mRlRoot = findViewById(R.id.rl_root);
        mGFViewPager = findViewById(R.id.vp_pager);
        mGFViewPager.setAdapter(mAdapter);
        mGFViewPager.setCurrentItem(0);
        mGFViewPager.addOnPageChangeListener(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPhotoClick() {

    }
}
