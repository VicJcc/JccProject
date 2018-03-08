package jcc.example.com.browseimg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.jcc.common.weight.viewPics.GFViewPager;
import com.jcc.common.weight.viewPics.PhotoInfo;

import java.util.ArrayList;


/**
 * Created by jincancan on 2018/2/6.
 * Description:
 */

public class JMovePicActivity extends Activity implements
        PhotoPreviewAdapter.PhotoCallback,
        ViewPager.OnPageChangeListener {

    private static final String TAG = "JMovePicActivity";

    public static final String PARAMS_PRESS_X = "press_x";
    public static final String PARAMS_PRESS_Y = "press_y";

    public static final String PARAMS_HEIGHT = "height";
    public static final String PARAMS_WIDTH = "width";
    public static final String PARAMS_LOCATION_X = "location_x";
    public static final String PARAMS_LOCATION_Y = "location_y";

    public static void start(Context context, float x, float y){
        Intent intent = new Intent(context, JMovePicActivity.class);
        intent.putExtra(PARAMS_PRESS_X, x);
        intent.putExtra(PARAMS_PRESS_Y, y);
        context.startActivity(intent);
    }

    public static void start(Context context, int width, int height, int locationX, int locationY){
        Intent intent = new Intent(context, JMovePicActivity.class);
        intent.putExtra(PARAMS_HEIGHT, height);
        intent.putExtra(PARAMS_WIDTH, width);
        intent.putExtra(PARAMS_LOCATION_X, locationX);
        intent.putExtra(PARAMS_LOCATION_Y, locationY);
        context.startActivity(intent);
    }

    private RelativeLayout mRlRoot;
    private JWatchPicView mPicView;
    private GFViewPager mGFViewPager;

    private float mStartX;
    private float mStartY;

    private int mImgHeight;
    private int mImgWidth;
    private int mLocationX;
    private int mLocationY;

    private boolean bFirstResume = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move_pic);

        mStartX = getIntent().getFloatExtra(PARAMS_PRESS_X, 0f);
        mStartY = getIntent().getFloatExtra(PARAMS_PRESS_Y, 0f);

        mImgHeight = getIntent().getIntExtra(PARAMS_HEIGHT, 0);
        mImgWidth = getIntent().getIntExtra(PARAMS_WIDTH, 0);
        mLocationX = getIntent().getIntExtra(PARAMS_LOCATION_X, 0);
        mLocationY = getIntent().getIntExtra(PARAMS_LOCATION_Y, 0);

        mPicView = findViewById(R.id.iv_test);
        mGFViewPager = findViewById(R.id.vp_data);
        mRlRoot = findViewById(R.id.rl_root);

        mPicView.setCallback(new JWatchPicView.WatchCallback() {
            @Override
            public void needDismiss() {
                finish();
                overridePendingTransition(0, 0);
            }
        });


        ArrayList<PhotoInfo> mPhotoList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            PhotoInfo info = new PhotoInfo();
            mPhotoList.add(info);
        }
        PhotoPreviewAdapter mAdapter = new PhotoPreviewAdapter(this, mPhotoList, this);
        mGFViewPager.setAdapter(mAdapter);
        mGFViewPager.setCurrentItem(0);
        mGFViewPager.addOnPageChangeListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(bFirstResume){
            startAnim();
            bFirstResume = false;
        }


//        mRlRoot.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
//                | View.SYSTEM_UI_FLAG_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        hideBar();

    }

    private void startAnim(){
//        mPicView.setVisibility(View.VISIBLE);
        mPicView.startImgAnim(mImgHeight, mImgWidth, mLocationX, mLocationY);
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
        mPicView.startEndAnim();
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
